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

package org.fourthline.lemma.reader.content.printer;

import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;

import java.util.logging.Logger;

/**
 * Prints citation source lines, appends them to an XHTML element.
 *
 * @author Christian Bauer
 */
public abstract class ContentPrinter {

    final private Logger log = Logger.getLogger(ContentPrinter.class.getName());

    public void print(String[] source, CitationAnchor citation, XHTMLElement parentElement, String... preFormattedClasses) {
        if (source == null || source.length == 0)
            return;

        log.fine("Printing content lines: " + source.length);

        XHTMLElement content =
            parentElement.createChild(Constants.WRAPPER_ELEMENT)
            .setAttribute(XHTML.ATTR.CLASS, Constants.TYPE_CONTENT);

        append(source, citation, content, preFormattedClasses);
    }


    protected String getEndOfLine() {
        return "\n";
    }

    protected XHTMLElement createPreFormattedElement(XHTMLElement parent, String... preFormattedClasses) {
        XHTMLElement element = parent.createChild(XHTML.ELEMENT.pre);
        if (preFormattedClasses != null && preFormattedClasses.length > 0)
            element.setClasses(preFormattedClasses);
        return element;
    }

    abstract protected void append(String[] source, CitationAnchor citation, XHTMLElement contentElement, String... preFormattedClasses);

}
