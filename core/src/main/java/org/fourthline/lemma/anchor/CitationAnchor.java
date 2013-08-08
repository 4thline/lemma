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

package org.fourthline.lemma.anchor;

import org.seamless.xhtml.Anchor;
import org.seamless.xhtml.Option;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;

/**
 * Adapter which extends the regular DOM anchor, provides access to common options.
 *
 * @author Christian Bauer
 */
public class CitationAnchor extends Anchor {

    /**
     * The most common option (keys) of citation anchors.
     */
    public enum OptionKey {

        READ_TITLE("read-title"),
        INCLUDE("include"),
        EXCLUDE("exclude"),
        CLEAN_LABELS("clean-labels"),
        LINE_TRIM("line-trim"),
        LTRIM("ltrim"),
        DOTS("dots"),
        FILEPATH("filepath"),
        READER("reader"),
        PRETTY("pretty");


        private String key;

        OptionKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public CitationAnchor(XPath xpath, org.seamless.xhtml.Anchor anchor) {
        this(xpath, anchor.getW3CElement());
    }

    public CitationAnchor(XPath xpath, Element element) {
        super(xpath, element);
    }

    public AnchorAddress getAddress() {
        // Don't use the Href class here, we need our own parsing routine for the href attribute
        return AnchorAddress.valueOf(getAttribute(XHTML.ATTR.href));
    }

    public Option getOption(OptionKey key) {
        for (Option option : getOptions()) {
            if (option.getKey().equals(key.key)) return option;
        }
        return null;
    }

    /**
     * @return The value of the <code>id</code> attribute of the anchor, or if not present the id of the given address.
     */
    public String getOutputIdentifier() {
        return getId() != null ? getId() : getAddress().toIdentifierString();
    }

    /**
     * @return Any given <code>class</code> class attribute value (if present), in addition the name of the address scheme.
     */
    public String getOutputClasses() {
        return getAttribute(XHTML.ATTR.CLASS) + " " + getAddress().getScheme().name().toLowerCase();
    }

    public static CitationAnchor[] findCitationAnchors(XPath xpath, XHTML input, String type) {
        return findCitationAnchors(xpath, input.getRoot(xpath), type);
    }

    public static  CitationAnchor[] findCitationAnchors(XPath xpath, XHTMLElement start, String type) {
        org.seamless.xhtml.Anchor[] anchors = start.findAllAnchors(null, type);
        CitationAnchor[] a = new CitationAnchor[anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            a[i] = new CitationAnchor(xpath, anchors[i]);
        }
        return a;
    }

}
