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

package org.fourthline.lemma.maven;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.seamless.util.io.IO;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.fourthline.lemma.pipeline.javadoc.XHTMLTemplateJavadocPipeline;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Christian Bauer
 * @goal site-manual
 * @phase site
 * @requiresDependencyResolution test
 */
public class LemmaReport extends LemmaMojo implements MavenReport {

    /* ##################################################################################################### */

    protected final AbstractMavenReport delegate;

    public LemmaReport() {
        this.delegate = new AbstractMavenReport() {


            @Override
            protected Renderer getSiteRenderer() {
                return null;
            }

            @Override
            protected String getOutputDirectory() {
                return null;
            }

            @Override
            protected MavenProject getProject() {
                return LemmaReport.this.getProject();
            }

            @Override
            protected void executeReport(Locale locale) throws MavenReportException {
                LemmaReport.this.executeReport(locale);
            }

            @Override
            protected void closeReport() {
                LemmaReport.this.closeReport();
                super.closeReport();
            }

            public String getOutputName() {
                return LemmaReport.this.getOutputName();
            }

            public String getName(Locale locale) {
                return LemmaReport.this.getName(locale);
            }

            public String getDescription(Locale locale) {
                return LemmaReport.this.getDescription(locale);
            }

        };
    }

    public void generate(org.codehaus.doxia.sink.Sink sink, Locale locale) throws MavenReportException {
        delegate.generate(sink, locale);
    }

    public String getCategoryName() {
        return delegate.getCategoryName();
    }

    public void setReportOutputDirectory(File file) {
        delegate.setReportOutputDirectory(file);
    }

    public File getReportOutputDirectory() {
        return delegate.getReportOutputDirectory();
    }

    public boolean isExternalReport() {
        return delegate.isExternalReport();
    }

    public boolean canGenerateReport() {
        return delegate.canGenerateReport();
    }

    /* ##################################################################################################### */

    // Abused by Maven site plugin, a / denotes a directory path (they even convert from / to \\!)
    // Thanks guys... it's so much easier now!
    public String getOutputName() {

        String path = IO.makeRelativePath(outputPath, project.getReporting().getOutputDirectory());

        // Yes, the damn stupid code that attaches ".html" to everything (good luck overriding that) does
        // so by chopping off everything after the FIRST DOT and not the LAST DOT (which is how sane
        // people usually detect a file suffix). So we make dashes out of dots...
        // The offending code is hidden here - don't even try to replace it, you'll suffer:
        //
        // org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext
        //
        //outputFilename = outputFilename.replaceAll("\\.", "-");

        return path + "/" + outputFilename;
    }

    public String getName(Locale locale) {
        return "Lemma Manual";
    }

    public String getDescription(Locale locale) {
        return "A user manual generated from unit test source code.";
    }

    protected void executeReport(Locale locale) throws MavenReportException {

        try {
            // We might want to load stuff from the test classpath
            extendPluginClasspath((List<String>)project.getTestClasspathElements());

            File templateFile = new File(manualSourceDirectory, templateFilename);
            if (!templateFile.exists()) {
                throw new Exception("Configured 'templateFile' not found: " + templateFile);
            }

            // Default to test source directory if no source directories are configured
            if (sourceDirectories.isEmpty()) {
                File testSourceDirectory = new File(project.getBuild().getTestSourceDirectory());
                sourceDirectories.add(testSourceDirectory);
                getLog().info(">>> Generating documentation using test source directory: " + testSourceDirectory);
            } else {
                getLog().info(">>> Generating documentation using multiple source directories:");
                for (File sourceDirectory : sourceDirectories) {
                    getLog().info(sourceDirectory.toString());
                }
            }

            XHTMLTemplateJavadocPipeline pipeline = createPipeline(sourceDirectories, packageNames, project);
            XHTML result = pipeline.execute(templateFile);

            Sink sink = delegate.getSink();
            sink.head();
            sink.title();
            sink.text(result.getRoot(pipeline.getXPath()).getHead().getHeadTitle().getContent());
            sink.title_();
            sink.head_();
            sink.body();

            XHTML bodyDom = pipeline.getParser().createDocument();
            bodyDom.createRoot(pipeline.getXPath(), XHTML.ELEMENT.div);
            for (XHTMLElement child : result.getRoot(pipeline.getXPath()).getBody().getChildren()) {
                bodyDom.getRoot(pipeline.getXPath()).appendChild(child, true);
            }
            sink.rawText(pipeline.getParser().print(bodyDom, 4, false));

            sink.body_();
            sink.flush();
            sink.close();

        } catch (Exception ex) {
            throw new MavenReportException("Error occurred: " + ex.getMessage(), ex);
        }

    }

    protected void closeReport() {
        try {
            String path = IO.makeRelativePath(outputPath, project.getReporting().getOutputDirectory());
            copyManualResources(new File(project.getReporting().getOutputDirectory(), path));
            copyDocFiles(new File(project.getReporting().getOutputDirectory(), path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    
}
