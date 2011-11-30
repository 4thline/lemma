package example.util;

import org.seamless.util.io.IO;
import org.seamless.util.logging.LoggingUtil;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLParser;
import org.fourthline.lemma.pipeline.javadoc.XHTMLTemplateJavadocPipeline;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DocletTest {

    protected XHTMLParser xhtmlParser = new XHTMLParser();
    protected File sourceDirectory;
    protected XHTMLTemplateJavadocPipeline xhtmlTemplatePipeline;

    @BeforeTest
    public void init() throws Exception {
        LoggingUtil.loadDefaultConfiguration();
    }

    @BeforeClass
    @Parameters("sourceDirectory")
    public void init(@Optional String sourceDirectoryString) throws Exception {

        sourceDirectory = new File(sourceDirectoryString);
        List<File> dirs = new ArrayList();
        dirs.add(sourceDirectory);

        xhtmlTemplatePipeline = new XHTMLTemplateJavadocPipeline(
                dirs, getDefaultPackageNames(sourceDirectory), true, isProcessXRefs()
        );
    }

    public List<String> getDefaultPackageNames(File sourceDirectory) {
        List<String> names = new ArrayList();
        File[] subdirs = sourceDirectory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().matches("[a-zA-Z_]+");
            }
        });
        if (subdirs != null) {
            for (File subdir : subdirs) {
                names.add(subdir.getName());
            }
            return names;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public XHTMLParser getParser() {
        return xhtmlParser;
    }

    public XHTMLTemplateJavadocPipeline getTemplatePipeline() {
        return xhtmlTemplatePipeline;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public XHTML parseDocument(String file) throws Exception {
        return getParser().parse(getResource(file));
    }

    public String getContent(String file) throws Exception {
        return IO.readLines(new File(getResource(file).toURI()));
    }

    protected URL getResource(String file) throws Exception {
        if (getTemplatePipeline() == null)
            throw new IllegalStateException("Call init() before accessing resources");
        File resourceFile = new File(getSourceDirectory(), file);
        if (!resourceFile.canRead()) {
            throw new IllegalArgumentException("Can't read or find file: " + resourceFile);
        }
        return resourceFile.toURI().toURL();
    }

    protected boolean isProcessXRefs() {
        return true;
    }
}
