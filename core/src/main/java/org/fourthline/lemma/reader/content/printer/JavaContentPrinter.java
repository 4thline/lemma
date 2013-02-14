/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
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

import java.util.regex.Pattern;

/**
 * @author Christian Bauer
 */
public class JavaContentPrinter extends CalloutContentPrinter {

    final public static Pattern PATTERN_BLOCK_COMMENT_BEGIN = Pattern.compile(".*?/\\*.*");
    final public static Pattern PATTERN_BLOCK_COMMENT_END = Pattern.compile(".*?\\*/.*");

    @Override
    protected String wrapCalloutMarker(int callout) {
        return " // (" + callout + ")";
    }

    @Override
    protected Pattern getCommentBeginPattern() {
        return PATTERN_BLOCK_COMMENT_BEGIN;
    }

    @Override
    protected Pattern getCommentEndPattern() {
        return PATTERN_BLOCK_COMMENT_END;
    }

    @Override
    protected String replaceCommentBegin(String line) {
        return line.replaceAll("/\\*+", "");
    }

    @Override
    protected String replaceCommentEnd(String line) {
        return line.replaceAll("\\*+/", "");
    }

    @Override
    protected String replaceLine(String line) {
        return line.startsWith("*") ? line.substring(1) : line;
    }
}
