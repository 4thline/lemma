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

package org.fourthline.lemma.reader.text;

import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.reader.AbstractReader;
import org.fourthline.lemma.reader.content.filter.CleanupFilter;
import org.fourthline.lemma.reader.content.filter.ContentFilter;
import org.fourthline.lemma.reader.content.filter.FragmentFilter;
import org.fourthline.lemma.reader.content.handler.ContentFileHandler;
import org.fourthline.lemma.reader.content.printer.ContentPrinter;
import org.fourthline.lemma.reader.content.printer.PlainContentPrinter;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;

import java.io.File;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Reads any text file, uses "#" as a comment marker.
 * <p>
 * This reader can process any text file, its only job is to detect
 * fragment labels in plain text comments - which are all characters
 * following a hash "#" character.
 * </p>
 *
 * @author Christian Bauer
 */
public class PlaintextReader extends AbstractReader {

    final public static String CONTEXT_SOURCE_DIRECTORIES = "PlaintextReader.sourceDirectories";

    final private Logger log = Logger.getLogger(PlaintextReader.class.getName());

    final public static Pattern PATTERN_FRAGMENT_LABEL =
            Pattern.compile("(.*?)[\\t ]*#[\\t ]*" + Constants.PATTERN_FRAGMENT_LABEL + "[\\t ]*$");

    final protected ContentFileHandler handler;
    final protected ContentPrinter printer;
    final protected ContentFilter[] filters;

    public PlaintextReader() {
        handler = new ContentFileHandler();
        printer = new PlainContentPrinter();
        filters = new ContentFilter[]{
                new FragmentFilter(PATTERN_FRAGMENT_LABEL),
                new CleanupFilter(PATTERN_FRAGMENT_LABEL)
        };
    }

    public XHTML read(CitationAnchor citation, Context context) {

        File[] sourceDirectories = (File[])context.get(CONTEXT_SOURCE_DIRECTORIES);
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

        for (ContentFilter filter : filters) {
            content = filter.filter(content, citation);
        }

        printer.print(content, parent);
    }

}
