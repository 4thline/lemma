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

package org.fourthline.lemma.reader.xml;

import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.reader.AbstractReader;
import org.fourthline.lemma.reader.content.filter.CleanupFilter;
import org.fourthline.lemma.reader.content.filter.ContentFilter;
import org.fourthline.lemma.reader.content.filter.FragmentFilter;
import org.fourthline.lemma.reader.content.handler.ContentFileHandler;
import org.fourthline.lemma.reader.content.printer.ContentPrinter;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;

import java.io.File;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Reads XML files - without parsing them.
 * <p>
 * This reader can process citations from any XML, XHTML, or HTML file - it does
 * not parse the content. Its only job is to understand fragment labels embedded in
 * XML comments.
 * </p>
 * <p>
 * It wraps the read content into a {@code <pre class="prettyprint"/>} tag, which
 * helps you to detect XML code blocks in your output document, e.g. for a
 * Javascript syntax highlighter.
 * </p>
 *
 * @author Christian Bauer
 */
public class XMLReader extends AbstractReader {

    final private Logger log = Logger.getLogger(XMLReader.class.getName());

    final public static String CONTEXT_SOURCE_DIRECTORIES = "XMLReader.sourceDirectories";

    final public static Pattern PATTERN_FRAGMENT_LABEL =
            Pattern.compile("(.*?)[\\t ]*<!--[\\t ]*" + Constants.PATTERN_FRAGMENT_LABEL + "[\\t ]*-->$");

    final protected ContentFileHandler handler;
    final protected ContentPrinter printer;
    final protected ContentFilter[] filters;

    public XMLReader() {
        handler = new ContentFileHandler();
        printer = new ContentPrinter();
        filters = new ContentFilter[]{
                new FragmentFilter(PATTERN_FRAGMENT_LABEL),
                new CleanupFilter(PATTERN_FRAGMENT_LABEL)
        };
    }

    public XHTML read(CitationAnchor citation, Context context) {

        File[] sourceDirectories = (File[]) context.get(CONTEXT_SOURCE_DIRECTORIES);
        File addressedFile = resolveFile(citation.getAddress().getPath(), sourceDirectories);
        log.fine("Including and parsing XHTML file: " + addressedFile);

        XHTML xhtml = getParser().createDocument();

        XHTMLElement root =
                xhtml.createRoot(getXPath(), Constants.WRAPPER_ELEMENT)
                        .setAttribute(XHTML.ATTR.CLASS, citation.getOutputClasses());

        if (isGenerateId(context))
            root.setAttribute(XHTML.ATTR.id, citation.getOutputIdentifier());

        appendTitle(root, citation.getTitle());
        addFilePath(root, citation, addressedFile);

        appendContent(root, addressedFile, citation);

        return xhtml;
    }

    protected void appendContent(XHTMLElement parent, File file, CitationAnchor citation) {

        String[] content = handler.getContent(file, null);

        // Filtering of source
        for (ContentFilter filter : filters) {
            content = filter.filter(content, citation);
        }

        // Just print it into a <pre>
        String contentOutput = printer.print(content);
        if (contentOutput != null) {
            parent.createChild(Constants.WRAPPER_ELEMENT)
                    .setAttribute(XHTML.ATTR.CLASS, Constants.TYPE_CONTENT)
                            // Wrap it in a <pre class="prettyprint"> for syntax highlighting on websites!
                    .createChild(XHTML.ELEMENT.pre)
                    .setAttribute(XHTML.ATTR.CLASS, "prettyprint")
                    .setContent(contentOutput);
        }
    }

}
