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

package org.fourthline.lemma.reader;

import org.seamless.xhtml.Option;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.seamless.xhtml.XHTMLParser;
import org.seamless.xml.ParserException;
import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;

import javax.xml.xpath.XPath;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides shared operations for reading and wrapping citation content.
 *
 * @author Christian Bauer
 */
public abstract class AbstractReader implements Reader {

    final private XHTMLParser parser = new XHTMLParser();
    final private XPath xpath;

    protected AbstractReader() {
        this.xpath = parser.createXPath();
    }

    public XHTMLParser getParser() {
        return parser;
    }

    public XPath getXPath() {
        return xpath;
    }

    /**
     * Resolves a file with the given path.
     * <p>
     * First, the given path is resolved against the given source directories. If no file
     * can be found in any source directory matching the path, a classpath lookup is attempted.
     * </p>
     *
     * @param path The path of the file to be resolved.
     * @param sourceDirectories The source directories of any file.
     * @return The found file.
     */
    protected File resolveFile(String path, File[] sourceDirectories) {
        File file = null;
        for (File sourceDirectory : sourceDirectories) {
            file = new File(sourceDirectory, path);
            if (file.canRead())
                break;
        }
        if (file == null || !file.canRead()) {
            // Try the classpath
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            try {
                if (url != null)
                    file = new File(url.toURI());
            } catch (URISyntaxException e) {
                // Ignore
            }
        }
        if (file == null || !file.canRead()) {
            throw new RuntimeException("Referenced file not found in source directories or classpath: " + path);
        }
        return file;
    }

    /**
     * Appends a new child element to the given element, wrapping the title string.
     *
     * @param parent The parent element to which the title child element is appended.
     * @param titleString The title string, any XHTML elements within will be parsed.
     */
    protected void appendTitle(XHTMLElement parent, String titleString) {
        if (titleString == null) return;
        try {
            String wrappedTitle = XHTMLParser.wrap(Constants.WRAPPER_ELEMENT.name(), XHTML.NAMESPACE_URI, titleString);
            XHTML titleDom = getParser().parse(wrappedTitle, false);

            titleDom.getRoot(getXPath()).setAttribute(XHTML.ATTR.CLASS, Constants.TYPE_TITLE);
            parent.appendChild(titleDom.getRoot(getXPath()), false);

        } catch (ParserException ex) {
            throw new RuntimeException("Can't parse title: " + titleString, ex);
        }
    }

    /**
     * Appends a new child element containing the file path of the citation.
     *
     * @param parent The parent element to which the file path child element is appended.
     * @param citation The anchor is checked for the option if a file path should be added.
     * @param file The actual file path.
     */
    protected void addFilePath(XHTMLElement parent, CitationAnchor citation, File file) {
        Option filepathOption = citation.getOption(CitationAnchor.OptionKey.FILEPATH);
        boolean addFilepath = filepathOption != null ? Boolean.valueOf(filepathOption.getFirstValue()) : false;
        if (!addFilepath) return;

        try {
            String path = file.getCanonicalPath();
            String cwd = System.getProperty("user.dir");
            path = path.substring(cwd.length(), path.length());

            parent.createChild(Constants.WRAPPER_ELEMENT)
                .setClasses(Constants.TYPE_FILEPATH)
                .setContent(path);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get canonical path of file: " + file, ex);
        }
    }

}
