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

package org.fourthline.lemma.reader.javadoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import org.seamless.xhtml.XHTML;
import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.AnchorAddress;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.anchor.Scheme;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.reader.AbstractReader;

import java.util.logging.Logger;

/**
 * Looks up the Javadoc <code>RootDoc</code> from the context.
 *
 * @author Christian Bauer
 */
public abstract class AbstractJavadocReader extends AbstractReader {

    final private Logger log = Logger.getLogger(AbstractJavadocReader.class.getName());

    final public static String CONTEXT_ROOT_DOC = "JavadocReader.rootDoc";

    public XHTML read(CitationAnchor citation, Context context) {
        RootDoc rootDoc = (RootDoc) context.get(CONTEXT_ROOT_DOC);
        if (rootDoc == null) {
            throw new IllegalStateException("Missing root Javadoc in context, can't read Javadoc");
        }
        return read(citation, context, rootDoc);
    }

    protected abstract XHTML read(CitationAnchor citation, Context context, RootDoc rootDoc);

    protected Doc findTargetDoc(CitationAnchor citation, RootDoc rootDoc) {

        if (!(citation.getAddress().getScheme().equals(Scheme.JAVADOC) ||
                citation.getAddress().getScheme().equals(Scheme.JAVACODE))) {
            throw new RuntimeException("TODO: NO SUPPORT FOR file://some/JavaClass.java ADDRESSES!");
        }

        // Try package name first
        Doc targetDoc = rootDoc.packageNamed(citation.getAddress().getPath());
        if (targetDoc == null) {

            // Now try class name
            targetDoc = rootDoc.classNamed(citation.getAddress().getPath());

            // Get method doc for signature (both qualified and flat are attempted)
            // TODO: This might not guarantee a hit because we only check qualified names syntactically, not semantically
            String fragment = citation.getAddress().getFragment();
            if (targetDoc != null && fragment != null) {

                MethodDoc[] methodDocs = ((ClassDoc) targetDoc).methods();
                targetDoc = null;

                log.finest("Trying to find matching signature for citation target fragment: " + fragment);
                for (MethodDoc methodDoc : methodDocs) {
                    String qualifiedSignature = methodDoc.name() + methodDoc.signature();
                    String unqualifiedSignature = methodDoc.name() + methodDoc.flatSignature();
                    if (qualifiedSignature.equals(fragment) || unqualifiedSignature.equals(fragment)) {
                        log.finest("Found method with matching signature: " + methodDoc.position());
                        targetDoc = methodDoc;
                        break;
                    }
                }

            }
        }

        if (targetDoc == null) {
            throw new IllegalArgumentException("Target not found in Javadoc unit: " + citation);
        }
        return targetDoc;
    }

    protected XHTML resolveThisReferences(Context context, Doc targetDoc, XHTML input) {
        CitationAnchor[] anchors = CitationAnchor.findCitationAnchors(getXPath(), input, Constants.TYPE_CITATION);
        for (CitationAnchor citation : anchors) {
            if (citation.getAddress().getPath().equals(AnchorAddress.PATH_THIS)) {

                AnchorAddress resolvedAddress;

                if (targetDoc instanceof ClassDoc) {
                    resolvedAddress = AnchorAddress.valueOf(
                            citation.getAddress().getScheme(),
                            (ClassDoc)targetDoc,
                            citation.getAddress().getFragment()
                    );
                } else if (targetDoc instanceof PackageDoc) {
                    resolvedAddress = AnchorAddress.valueOf(
                            citation.getAddress().getScheme(),
                            (PackageDoc)targetDoc
                    );
                } else if (targetDoc instanceof MethodDoc) {
                    resolvedAddress = AnchorAddress.valueOf(
                            citation.getAddress().getScheme(),
                            (MethodDoc)targetDoc
                    );
                } else {
                    throw new IllegalArgumentException(
                        "Unknown doc type/reference, unable to resolve 'this' reference: " + citation
                    );
                }

                log.fine("Replacing 'this' reference with anchor address: " + resolvedAddress);
                citation.setAttribute(XHTML.ATTR.href, resolvedAddress.toString());
            }
        }

        return input;
    }

}
