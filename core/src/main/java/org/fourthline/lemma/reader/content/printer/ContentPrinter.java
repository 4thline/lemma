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

import java.util.logging.Logger;

/**
 * Prints citation source lines into a single string.
 *
 * @author Christian Bauer
 */
public class ContentPrinter {

    final private Logger log = Logger.getLogger(ContentPrinter.class.getName());

    public String print(String[] source) {
        if (source == null || source.length == 0) return null;
        log.fine("Printing content lines to XHTML: " + source.length);
        StringBuilder sb = new StringBuilder();

        for (String s : source) {
            sb.append(s).append(getEndOfLine());
        }

        return sb.toString();
    }

    protected String getEndOfLine() {
        return "\n";
    }

}
