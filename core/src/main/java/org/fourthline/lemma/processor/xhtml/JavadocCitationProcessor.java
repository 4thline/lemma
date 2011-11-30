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

package org.fourthline.lemma.processor.xhtml;

import com.sun.javadoc.RootDoc;
import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.processor.AbstractJavadocProcessor;
import org.fourthline.lemma.processor.ProcessorOptions;
import org.fourthline.lemma.reader.Reader;
import org.seamless.xhtml.XHTML;

import java.util.Stack;
import java.util.logging.Logger;

/**
 * Recursively processes citation anchors in XHTML documents and fragments.
 * <p>
 * Starts processing with an XHTML document object model as input, each citation
 * anchor in this DOM will be processed and the result (another DOM) will be
 * recursively processed until no more citaton anchors are found. Finally, all
 * citation anchors will be replaced with their respective result DOM.
 * </p>
 *
 * @author Christian Bauer
 */
public class JavadocCitationProcessor extends AbstractJavadocProcessor<XHTML, XHTML> {

    private Logger log = Logger.getLogger(JavadocCitationProcessor.class.getName());

    public JavadocCitationProcessor(RootDoc rootDoc) {
        super(rootDoc);
    }

    public XHTML process(XHTML input, Context context) {
        log.fine("Processing input...");

        XHTML output = processCitations(context, input, new Stack<CitationAnchor>());

/*
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Completed processing input, generated output: ");
            log.finest("--------------------------------------------------------------------------------");
            log.finest(XML.toString(output, false));
            log.finest("--------------------------------------------------------------------------------");
        }
*/
        ProcessorOptions processorOptions = (ProcessorOptions)context.get(ProcessorOptions.CONTEXT_PROCESSOR_OPTIONS);
        if (processorOptions.processXRefs)
            getParser().checkDuplicateIdentifiers(output);

        return output;
    }

    protected XHTML processCitations(Context context, XHTML input, Stack<CitationAnchor> stack) {

        CitationAnchor[] anchors = CitationAnchor.findCitationAnchors(getXPath(), input, Constants.TYPE_CITATION);
        log.fine("Found citation anchors in input: " + anchors.length);
        for (CitationAnchor citation : anchors) {

            if (citation.getAddress() == null) continue;

            // Use a stack to detect circular references
            if (stack.contains(citation)) {
                throw new IllegalStateException("Circular citations, remove: " + citation);
            }

            log.fine("Start processing: " + citation);
            stack.push(citation);

            Reader reader = getReader(citation);
            XHTML result = reader.read(citation, context);

            if (result == null) {
                log.warning("Reader '" + reader.getClass() + "' did not produce a result for: " + citation);
                continue;
            }

            // Parse it again recursively!
            result = processCitations(context, result, stack);

            // Now swap the citation element with the result root element
            citation.getParent().replaceChild(citation, result.getRoot(getXPath()), false);

            log.fine("Completed processing: " + citation);
            stack.pop();
        }

        return input;
    }


}