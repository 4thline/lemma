/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.lemma.pipeline.javadoc;

import com.sun.javadoc.RootDoc;
import org.fourthline.lemma.processor.ProcessorOptions;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.seamless.util.io.IO;
import org.seamless.javadoc.EasyDoclet;
import org.seamless.util.logging.LoggingUtil;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLParser;
import org.seamless.xml.ParserException;
import org.fourthline.lemma.pipeline.Pipeline;
import org.fourthline.lemma.processor.Processor;
import org.fourthline.lemma.processor.xhtml.JavadocCitationProcessor;
import org.fourthline.lemma.processor.xhtml.TocProcessor;
import org.fourthline.lemma.processor.xhtml.XRefProcessor;
import org.fourthline.lemma.reader.javacode.JavacodeRawReader;
import org.fourthline.lemma.reader.javadoc.AbstractJavadocReader;
import org.fourthline.lemma.reader.text.PlaintextReader;
import org.fourthline.lemma.reader.xml.XMLReader;

import javax.xml.xpath.XPath;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reads an input XHTML document, processes it and returns an XHTML output document.
 * <p>
 * Call the {@link #main(String[])} method with your
 * {@link org.fourthline.lemma.pipeline.javadoc.XHTMLTemplateJavadocPipeline.Options} to start
 * Lemma.
 * </p>
 * <p>
 * The processors of this pipeline, in order, are:
 * </p>
 * <ol>
 * <li>{@link org.fourthline.lemma.processor.xhtml.JavadocCitationProcessor}</li>
 * <li>{@link org.fourthline.lemma.processor.xhtml.XRefProcessor}</li>
 * <li>{@link org.fourthline.lemma.processor.xhtml.TocProcessor}</li>
 * </ol>
 *
 * @author Christian Bauer
 */
public class XHTMLTemplateJavadocPipeline extends Pipeline<XHTML, XHTML> {

    static final private Logger log = Logger.getLogger(XHTMLTemplateJavadocPipeline.class.getName());

    final private XHTMLParser parser = new XHTMLParser();
    final private XPath xpath;

    final private RootDoc rootDoc;
    final private File[] sourceDirectories;
    final private boolean normalizeOutput;
    final private ProcessorOptions processorOptions;

    public XHTMLTemplateJavadocPipeline(SharedOptions options) {
        this(options.sourceDirectories, options.packageNames, true, options.processXRefs);
    }

    public XHTMLTemplateJavadocPipeline(List<File> sourceDirectories,
                                        List<String> packageNames,
                                        boolean normalizeOutput,
                                        boolean processXRefs) {
        log.info("Configuring pipeline...");

        this.sourceDirectories = sourceDirectories.toArray(new File[sourceDirectories.size()]);

        // First sentence detection routine depends on locale in Javadoc
        // tool, so enforce it! Ridiculous!
        rootDoc = new EasyDoclet(
                "en_US",
                this.sourceDirectories,
                packageNames.toArray(new String[packageNames.size()]),
                new File[0]
        ).getRootDoc();

        this.normalizeOutput = normalizeOutput;
        this.xpath = getParser().createXPath();

        this.processorOptions = new ProcessorOptions();
        processorOptions.processXRefs = processXRefs;
    }

    public XHTMLParser getParser() {
        return parser;
    }

    public XPath getXPath() {
        return xpath;
    }

    public File[] getSourceDirectories() {
        return sourceDirectories;
    }

    public RootDoc getRootDoc() {
        return rootDoc;
    }

    public boolean isNormalizeOutput() {
        return normalizeOutput;
    }

    public XHTML execute(File xhtmlTemplateFile) {
        XHTML template;
        try {
            log.info("Parsing initial XHTML template file: " + xhtmlTemplateFile);
            template = parser.parse(xhtmlTemplateFile);
        } catch (ParserException ex) {
            throw new RuntimeException(ex);
        }
        return execute(template);
    }

    @Override
    protected void resetContext() {
        super.resetContext();
        getContext().put(AbstractJavadocReader.CONTEXT_ROOT_DOC, getRootDoc());
        getContext().put(JavacodeRawReader.CONTEXT_SOURCE_DIRECTORIES, getSourceDirectories());
        getContext().put(XMLReader.CONTEXT_SOURCE_DIRECTORIES, getSourceDirectories());
        getContext().put(PlaintextReader.CONTEXT_SOURCE_DIRECTORIES, getSourceDirectories());
    }

    @Override
    public XHTML execute(XHTML input) {
        XHTML output = super.execute(input);

        if (isNormalizeOutput())
            output.getW3CDocument().normalizeDocument();

        return output;
    }

    @Override
    public Processor<XHTML, XHTML>[] getProcessors() {
        return new Processor[]{
                new JavadocCitationProcessor(getRootDoc()),
                new XRefProcessor(),
                new TocProcessor(),
        };
    }

    @Override
    public ProcessorOptions getProcessorOptions() {
        return processorOptions;
    }

    public static void main(String[] args) throws Exception {

        LoggingUtil.loadDefaultConfiguration();

        Options options = new Options(args);

        XHTMLTemplateJavadocPipeline pipeline =
                new XHTMLTemplateJavadocPipeline(options);

        XHTML result = pipeline.execute(options.xhtmlTemplateFile);

        pipeline.prepareOutputFile(options.xhtmlOutputFile, options.overwriteOutputFile);

        System.out.println("Writing output file: " + options.xhtmlOutputFile.getAbsolutePath());

        IO.writeUTF8(
                options.xhtmlOutputFile,
                pipeline.getParser().print(result, 4, true) // TODO: Make configurable?
        );
    }

    /**
     * Options which are shared between this bootstrap class and the Maven plugin.
     */
    public static class SharedOptions {

        @Option(required = true, name = "-d", metaVar = "<path>", multiValued = true,
                usage = "The base path(s) of all source and resource files.")
        public List<File> sourceDirectories = new ArrayList();

        @Option(required = false, name = "-p", metaVar = "<package.name>", multiValued = true,
                usage = "Included package, repeat option for multiple packages.")
        public List<String> packageNames = new ArrayList();

        // TODO: Make this optional
        @Option(required = true, name = "-i", metaVar = "<template.xhtml>",
                usage = "XHTML template file.")
        public File xhtmlTemplateFile;

        @Option(name = "-xref", metaVar = "true|false", usage = "Process Javadoc {@link} tags with stable identifiers.")
        public boolean processXRefs = true;

        public SharedOptions() {
        }

        public SharedOptions(String[] args) {
            CmdLineParser cmdLineParser = new CmdLineParser(this);
            try {
                cmdLineParser.parseArgument(args);
            } catch (CmdLineException e) {
                System.err.println(e.getMessage());
                System.err.println("USAGE: java -jar <JARFILE> [options]");
                cmdLineParser.printUsage(System.err);
                System.exit(1);
            }

            if (!prepare()) {
                System.exit(1);
            }
        }

        /**
         * Called by the constructor to convert and validate the given option values.
         *
         * @return true if validation was successful.
         */
        public boolean prepare() {

            for (File sourceDirectory : sourceDirectories) {
                if (!sourceDirectory.canRead()) {
                    System.err.println("Source directory not found or not readable: " + sourceDirectory);
                    return false;
                }
                if (!sourceDirectory.isDirectory()) {
                    System.err.println("Source directory is not a directory: " + sourceDirectory);
                    return false;
                }
            }

            if (!xhtmlTemplateFile.exists()) {
                System.err.println("XHTML template file not found: " + xhtmlTemplateFile);
                return false;
            }

            if (packageNames.size() == 0) {
                for (File sourceDirectory : sourceDirectories) {
                    // Default to all sub-directories in source directory
                    File[] subdirs = sourceDirectory.listFiles(new FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() && file.getName().matches("[a-zA-Z_]+");
                        }
                    });
                    for (File subdir : subdirs) {
                        packageNames.add(subdir.getName());
                    }
                }
                // Filter duplicates
                packageNames = new ArrayList(new LinkedHashSet(packageNames));
            }
            return true;
        }
    }

    /**
     * Options which are specific to this bootstrap class.
     */
    public static class Options extends SharedOptions {

        @Option(required = true, name = "-o", metaVar = "<result.xhtml>", usage = "XHTML output file.")
        public File xhtmlOutputFile;

        @Option(name = "-overwrite", metaVar = "true|false", usage = "Overwrite existing output file quietly.")
        public boolean overwriteOutputFile = false;

        public Options() {
        }

        public Options(String[] args) {
            super(args);
        }
    }

}
