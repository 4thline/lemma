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

package org.fourthline.lemma;

import org.seamless.xhtml.XHTML;

/**
 * @author Christian Bauer
 */
public interface Constants {

    public static final String PATTERN_FRAGMENT_LABEL = "DOC:\\s*([A-Z_-]+[0-9]*)\\s*";

    public static final XHTML.ELEMENT WRAPPER_ELEMENT = XHTML.ELEMENT.div;

    public static final String TYPE_CITATION = "citation";
    public static final String TYPE_XREF = "xref";
    public static final String TYPE_TITLE = "title";
    public static final String TYPE_FILEPATH = "filepath";
    public static final String TYPE_CONTENT = "content";
    public static final String TYPE_UNRESOLVED = "unresolved";

}
