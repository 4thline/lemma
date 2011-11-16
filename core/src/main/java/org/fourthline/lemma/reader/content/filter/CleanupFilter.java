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

package org.fourthline.lemma.reader.content.filter;

import org.seamless.xhtml.Option;
import org.fourthline.lemma.anchor.CitationAnchor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes fragment labels, whitespaces, etc. from citation source text.
 *
 * @author Christian Bauer
 */
public class CleanupFilter implements ContentFilter {

    final private Logger log = Logger.getLogger(CleanupFilter.class.getName());

    final private Pattern fragmentLabelPattern;

    public CleanupFilter(Pattern fragmentLabelPattern) {
        this.fragmentLabelPattern = fragmentLabelPattern;
    }

    public Pattern getFragmentLabelPattern() {
        return fragmentLabelPattern;
    }

    public String[] filter(String[] source, CitationAnchor citation) {
        if (source == null || source.length == 0) return source;

        log.fine("Cleaning (removing labels, whitespace, escaping) source lines: " + source.length);
        List<String> cleanLines = new ArrayList();

        // Count whitespaces of first line
        String firstline = source[0];
        int spaces = 0;
        while (firstline.matches("^\\s+.*")) {
            firstline = firstline.substring(1);
            spaces++;
        }

        for (int i = 0; i < source.length; i++) {
            String line = source[i];

            // Remove fragment label from line, or remove the whole line
            Option cleanLabelsOption = citation.getOption(CitationAnchor.OptionKey.CLEAN_LABELS);
            if (cleanLabelsOption == null || cleanLabelsOption.isTrue()) {
                line = removeFragmentComment(line);
                if (line == null) continue;
            }

            // Remove fragment label from line, or remove whole line, if it's the first or last line (boundary of fragment)
            if (cleanLabelsOption != null && cleanLabelsOption.getFirstValue() != null
                    && cleanLabelsOption.getFirstValue().toLowerCase().equals("boundary")
                    && (i == 0 || i == source.length-1)) {
                line = removeFragmentComment(line);
                if (line == null) continue;
            }

            Option ltrimOption = citation.getOption(CitationAnchor.OptionKey.LTRIM);
            if (ltrimOption == null || ltrimOption.isTrue()) {
                // Remove white spaces from beginning of line (if there are that many spaces at the beginning of the line)
                line = line.length() > spaces && line.matches("^\\s{"+spaces+",}.*") ? line.substring(spaces) : line;
            }

            // Escape XHTML reserved characters
            // TODO line = XHTMLParser.escape(line);

            cleanLines.add(line);
        }

        List<String> strippedLines = new ArrayList();

        Option lineTrimOption = citation.getOption(CitationAnchor.OptionKey.LINE_TRIM);
        if (lineTrimOption == null || lineTrimOption.isTrue()) {
            for (int i = 0; i < cleanLines.size(); i++) {
                String line = cleanLines.get(i);
                // If this line is only whitespace and the next line is only whitespace, drop this line
                if (line.matches("\\s*") && cleanLines.size() > i+1 && cleanLines.get(i+1).matches("\\s*")) {
                    continue;
                }
                strippedLines.add(line);
            }
        } else {
            strippedLines = cleanLines;
        }

        return strippedLines.toArray(new String[strippedLines.size()]);
    }

    protected String removeFragmentComment(String line) {
        Matcher m = getFragmentLabelPattern().matcher(line);
        if (m.matches()) {
            String cleanLine = m.group(1);

            // Remove trailing whitespace then return remaining before-comment text
            while(cleanLine.matches(".*( |\\t)$")) {
                cleanLine = cleanLine.substring(0, cleanLine.length()-1);
            }

            // Well if nothing is left, we remove the whole line (returning null does that)
            return cleanLine.length() == 0 ? null : cleanLine;
        }
        return line;
    }

}