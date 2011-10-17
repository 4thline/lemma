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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excludes and includes lines, depending on the given fragment rules of the citation anchor.
 *
 * @author Christian Bauer
 */
public class FragmentFilter implements ContentFilter {

    final private Logger log = Logger.getLogger(FragmentFilter.class.getName());

    final private Pattern fragmentLabelPattern;

    public FragmentFilter(Pattern fragmentLabelPattern) {
        this.fragmentLabelPattern = fragmentLabelPattern;
    }

    public Pattern getFragmentLabelPattern() {
        return fragmentLabelPattern;
    }

    public String[] filter(String[] source, CitationAnchor citation) {

        if (source == null || source.length == 0) return source;

        Option includeOption = citation.getOption(CitationAnchor.OptionKey.INCLUDE);
        Option excludeOption = citation.getOption(CitationAnchor.OptionKey.EXCLUDE);
        Option dotsOption = citation.getOption(CitationAnchor.OptionKey.DOTS);
        String[] includeFragments = includeOption != null ? includeOption.getValues() : new String[0];
        String[] excludeFragments = excludeOption != null ? excludeOption.getValues() : new String[0];
        boolean printDotsForExcluded = dotsOption != null ? Boolean.valueOf(dotsOption.getFirstValue()) : false;

        log.fine("Filtering " + source.length + " source lines, included/excluded fragments: "
                + includeFragments.length + "/" + excludeFragments.length);

        List<Integer> includedLines = new ArrayList();

        if (includeFragments.length == 0) {
            // Include ALL
            for (int i = 0; i < source.length; i++) {
                includedLines.add(i);
            }
        } else {
            // Include fragment blocks
            includedLines.addAll(getFragmentLines(source, includeFragments));
        }

        // Exclude fragment blocks
        List<Integer> excludedLines = getFragmentLines(source, excludeFragments);

        List<String> filtered = new ArrayList();

        boolean dotsAlreadyPrinted = false;
        for (int i = 0; i < source.length; i++) {
            String s = source[i];
            if (includedLines.contains(i)) {
                if (!excludedLines.contains(i)) {
                    filtered.add(s);
                    // If content (not just whitespace) has been added, print dots again
                    if (!s.matches("\\s*")) {
                        dotsAlreadyPrinted = false;
                    }
                } else if (excludedLines.contains(i) && printDotsForExcluded && !dotsAlreadyPrinted) {
                    // Print some ... instead of the excluded content, indentation needs to be observed
                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x< s.toCharArray().length; x++) {
                        char c = s.toCharArray()[x];
                        if (!Character.isWhitespace(c))
                            break;
                        sb.append(c);
                    }
                    sb.append("...");
                    filtered.add(sb.toString());
                    dotsAlreadyPrinted = true;
                }
            }
        }
        return filtered.toArray(new String[filtered.size()]);
    }

    protected List<Integer> getFragmentLines(String[] source, String[] fragments) {
        List<Integer> lines = new ArrayList();

        // Include only lines that are inside a block that BEGINs/ENDs with a fragment label; as
        // a special case, non-closed blocks are also supported for the most common case when
        // only a single line is actually marked.

        for (String fragment : fragments) {

            List<Integer> fragmentLines = new ArrayList();
            boolean inBlock = false;
            int lastBegin = 0;

            for (int line = 0; line < source.length; line++) {

                String s = source[line];
                if (isLineMatchingFragment(s, fragment)) {
                    if (!inBlock) {
                        // BEGIN the block demarcated by the fragment label
                        inBlock = true;
                        lastBegin = line;
                    } else {
                        // END the block demarcated by the fragment label
                        inBlock = false;
                        // Add last line of block
                        fragmentLines.add(line);
                    }
                }

                // If we are inside a BEGIN/END fragment block, add the line
                if (inBlock) {
                    fragmentLines.add(line);
                }

                // If the last block was not ENDed and we have reached the end, roll back to the
                // last BEGIN but include that line. In other words: Enable single-line blocks
                if (line == source.length - 1 && inBlock) {
                    Iterator<Integer> it = fragmentLines.iterator();
                    while (it.hasNext()) {
                        if (it.next() > lastBegin) it.remove();
                    }
                }
            }
            lines.addAll(fragmentLines);
        }
        return lines;
    }

    protected boolean isLineMatchingFragment(String line, String fragment) {
        Matcher m = getFragmentLabelPattern().matcher(line);
        return m.matches() && m.group(2).equals(fragment);
    }

}
