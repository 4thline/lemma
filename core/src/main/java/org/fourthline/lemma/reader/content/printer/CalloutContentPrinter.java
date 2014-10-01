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

import org.fourthline.lemma.anchor.CitationAnchor;
import org.seamless.xhtml.Option;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.seamless.xhtml.XHTMLParser;
import org.seamless.xml.ParserException;

import javax.xml.xpath.XPath;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christian Bauer
 */
abstract public class CalloutContentPrinter extends ContentPrinter {

    final public static Pattern PATTERN_CALLOUT = Pattern.compile("^\\s*DOC:\\s*CALLOUT.*");

    @Override
    protected void append(String[] source, CitationAnchor citation, XHTMLElement contentElement, String... preFormattedClasses) {

        // TODO: This option should be tested!
        Option calloutsOption = citation.getOption(CitationAnchor.OptionKey.CALLOUTS);
        boolean calloutsEnabled = calloutsOption == null || Boolean.valueOf(calloutsOption.getFirstValue());

        List<Integer> skippedLines = new ArrayList<Integer>();
        Map<Integer, String> callouts = new LinkedHashMap<Integer, String>();

        int currentLine = 0;
        while (currentLine < source.length) {
            String line = source[currentLine];

            List<Integer> commentLines = new ArrayList<Integer>();
            if (getCommentBeginPattern().matcher(line).matches()) {
                StringBuilder blockComment = new StringBuilder();
                commentLines.clear();

                Matcher blockCommentEndMatcher = getCommentEndPattern().matcher(line);
                boolean foundBlockCommentEnd = blockCommentEndMatcher.matches();

                line = line.trim();
                line = replaceCommentBegin(line);
                if (foundBlockCommentEnd)
                    line = replaceCommentEnd(line);
                blockComment.append(line);
                commentLines.add(currentLine);

                while (!foundBlockCommentEnd && currentLine + 1 < source.length) {
                    currentLine++;
                    line = source[currentLine];

                    blockCommentEndMatcher = getCommentEndPattern().matcher(line);
                    foundBlockCommentEnd = blockCommentEndMatcher.matches();

                    line = line.trim();
                    if (foundBlockCommentEnd)
                        line = replaceCommentEnd(line);
                    line = replaceLine(line);
                    if (!line.startsWith(" "))
                        line = " " + line;
                    blockComment.append(line);
                    commentLines.add(currentLine);
                }

                String blockCommentString = blockComment.toString();
                currentLine++;

                if (PATTERN_CALLOUT.matcher(blockCommentString).matches()) {
                    String calloutText = blockCommentString.replaceAll("DOC:\\s*CALLOUT", "").trim();
                    callouts.put(currentLine, calloutText);
                    skippedLines.addAll(commentLines);
                }

            } else {
                currentLine++;
            }
        }

        StringBuilder preFormattedString = new StringBuilder();
        currentLine = 0;
        int currentCallout = 1;
        boolean searchNextLine = false;
        while (currentLine < source.length) {
            if (skippedLines.contains(currentLine)) {
                currentLine++;
                continue;
            }

            String line = source[currentLine];

            if (searchNextLine || callouts.containsKey(currentLine)) {
                if (calloutsEnabled && isCalloutMarkerLine(line)) {
                    preFormattedString.append(line)
                        .append(wrapCalloutMarker(currentCallout++))
                        .append(getEndOfLine());
                    searchNextLine = false;
                } else {
                    preFormattedString.append(line).append(getEndOfLine());
                    searchNextLine = true;
                }
            } else {
                preFormattedString.append(line).append(getEndOfLine());
            }

            currentLine++;
        }

        XHTMLParser parser = new XHTMLParser();
        XPath xpath = parser.createXPath();

        XHTMLElement preFormatted =
            createPreFormattedElement(contentElement, preFormattedClasses);

        if (preFormattedString.length() > 0) {
            preFormatted.setContent(preFormattedString.toString());
        } else {
            preFormatted.getParent().removeChild(preFormatted);
        }

        if (calloutsEnabled) {
            XHTMLElement calloutList =
                contentElement.createChild(XHTML.ELEMENT.ol)
                    .setClasses("callouts");

            // Wrap the callout comment in an XHTML <li> and append to the <ol>
            for (String calloutString : callouts.values()) {
                if (calloutString.length() > 0) {
                    try {
                        XHTML calloutContent = parser.parse(
                            XHTMLParser.wrap(
                                XHTML.ELEMENT.li.name(),
                                XHTML.NAMESPACE_URI,
                                calloutString
                            )
                        );
                        XHTMLElement calloutItem = calloutContent.getRoot(xpath);
                        calloutItem.setClasses("callout");
                        calloutList.appendChild(calloutItem, true);
                    } catch (ParserException ex) {
                        throw new RuntimeException(
                            "Error parsing callout comment as XHTML: " + calloutString, ex
                        );
                    }
                }
            }

            if (calloutList.getChildren().length == 0)
                calloutList.getParent().removeChild(calloutList);
        }
    }

    protected boolean isCalloutMarkerLine(String line) {
        return true;
    }

    abstract protected String wrapCalloutMarker(int callout);

    abstract protected Pattern getCommentBeginPattern();

    abstract protected Pattern getCommentEndPattern();

    abstract protected String replaceCommentBegin(String line);

    abstract protected String replaceCommentEnd(String line);

    abstract protected String replaceLine(String line);

}
