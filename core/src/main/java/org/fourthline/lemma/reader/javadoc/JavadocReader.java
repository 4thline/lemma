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

import com.sun.javadoc.Doc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import org.seamless.util.Text;
import org.seamless.xhtml.Option;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.seamless.xhtml.XHTMLParser;
import org.seamless.xml.DOM;
import org.seamless.xml.ParserException;
import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.AnchorAddress;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.anchor.Scheme;
import org.fourthline.lemma.pipeline.Context;

import java.util.logging.Logger;

/**
 * Reads title and content from a Javadoc <code>Doc</code>, handles <code>javadoc://</code> scheme.
 * <p>
 * This reader understands the individual Javadoc <em>"tags"</em>, it wraps
 * these individual parts of each Javadoc comment and validates the whole.
 * </p>
 *
 * @author Christian Bauer
 */
public class JavadocReader extends AbstractJavadocReader {

    final private Logger log = Logger.getLogger(JavadocReader.class.getName());

    protected XHTML read(CitationAnchor citation, Context context, RootDoc rootDoc) {
        Doc targetDoc = findTargetDoc(citation, rootDoc);
        XHTML result = read(targetDoc, citation, isGenerateId(context));
        resolveThisReferences(context, targetDoc, result);
        return result;
    }

    protected XHTML read(Doc doc, CitationAnchor citation, boolean uniqueId) {

        log.fine("Reading Javadoc: " + doc.position());

        XHTML xhtml = getParser().createDocument();

        XHTMLElement root =
                xhtml.createRoot(getXPath(), Constants.WRAPPER_ELEMENT)
                        .setAttribute(XHTML.ATTR.CLASS, citation.getOutputClasses());

        if (uniqueId)
            root.setAttribute(XHTML.ATTR.id, citation.getOutputIdentifier());

        String titleString = readTitle(doc, citation);

        appendTitle(root, titleString);
        addFilePath(root, citation, doc.position().file());

        appendContent(root, doc, citation, titleString);

        return xhtml;
    }

    protected String readTitle(Doc doc, CitationAnchor citation) {
        String text = citation.getTitle();
        Option readTitleOption = citation.getOption(CitationAnchor.OptionKey.READ_TITLE);
        if ((readTitleOption == null || readTitleOption.isTrue()) && text == null) {
            if (doc.firstSentenceTags().length > 0) {
                text = readTags(doc.firstSentenceTags());
            }
        }
        return text;
    }

    protected void appendContent(XHTMLElement parent, Doc doc, CitationAnchor citation, String titleString) {

        String content = readTags(doc.inlineTags());

        // Cut off the title if we already have it
        if (titleString != null && content.startsWith(titleString)) {
            content = content.substring(titleString.length());
        }

        if (content.length() > 0) {

            // Make it look pretty
            String text = Text.ltrim(Text.rtrim(content));

            try {
                String wrapped = XHTMLParser.wrap(Constants.WRAPPER_ELEMENT.name(), XHTML.NAMESPACE_URI, text);
                XHTML textDom = getParser().parse(wrapped, false);

                // Let's validate here!
                XHTML validationDOM = getParser().createDocument();
                XHTMLElement validationRoot = validationDOM.createRoot(getXPath(), XHTML.ELEMENT.html);
                validationRoot.createChild(XHTML.ELEMENT.head).createChild(XHTML.ELEMENT.title); // Mandatory
                validationRoot.createChild(XHTML.ELEMENT.body).appendChild(textDom.getRoot(getXPath()), true);
                getParser().validate(validationDOM);

                textDom.getRoot(getXPath()).setAttribute(XHTML.ATTR.CLASS, Constants.TYPE_CONTENT);
                parent.appendChild(textDom.getRoot(getXPath()), false);

            } catch (ParserException ex) {
                throw new RuntimeException("Couldn't wrap and parse XHTML content referenced by: " + citation, ex);
            }


        } else {
            log.fine("Citation does not have content: " + citation);
        }
    }

    protected String readTags(Tag[] tags) {
        StringBuilder content = new StringBuilder();
        for (Tag tag : tags) {

            if (tag.kind().equals("Text")) {

                content.append(readTagText(tag));

            } else if (tag.kind().equals("@see")) {

                content.append(readTagSee((SeeTag) tag));

            } else if (tag.kind().equals("@code") || tag.kind().equals("@literal")) {

                content.append(readTagCode(tag));

            } else {
                log.warning("Skipping unknown tag of kind: " + tag.kind());
            }
        }
        return content.toString();
    }

    protected String readTagText(Tag tag) {
        log.finest("Reading inline text tag");
        return tag.text();
    }

    protected String readTagSee(SeeTag seeTag) {
        log.finest("Reading inline link tag");

        AnchorAddress xref;
        if ((xref = getLinkReferenceAddress(seeTag)) != null) {
            return getLinkReference(seeTag, xref);
        } else {
            return getLinkLabel(seeTag);
        }
    }

    protected String readTagCode(Tag tag) {
        log.finest("Reading code/literal tag");
        StringBuilder content = new StringBuilder();
        // If it's an inline {@code} tag with no newlines in its text, we wrap the text in a <tt> element
        boolean inlineCode = !tag.text().contains("\n");
        if (inlineCode) content.append("<code>");
        content.append(DOM.CDATA_BEGIN);
        content.append(tag.text());
        content.append(DOM.CDATA_END);
        if (inlineCode) content.append("</code>");
        return content.toString();
    }

    protected AnchorAddress getLinkReferenceAddress(SeeTag tag) {
        // TODO: Always javadoc:// scheme?
        return AnchorAddress.valueOf(Scheme.JAVADOC, tag);
    }

    protected String getLinkReference(SeeTag tag, AnchorAddress address) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a class=\"").append(Constants.TYPE_XREF).append("\" href=\"").append(address.toString()).append("\">");
        sb.append(tag.label());
        sb.append("</a>");
        return sb.toString();
    }

    protected String getLinkLabel(SeeTag tag) {
        String referencedLabel;
        if (tag.referencedMember() != null) {
            referencedLabel = tag.referencedClassName() + "#" + tag.referencedMemberName();
        } else if (tag.referencedClass() != null) {
            referencedLabel = tag.referencedClassName();
        } else if (tag.referencedPackage() != null) {
            referencedLabel = tag.referencedPackage().name();
        } else {
            throw new IllegalStateException("Reference not found: " + tag + " at " + tag.position());
        }

        if (tag.label() != null && tag.label().length() > 0) {
            return tag.label() + " (" + referencedLabel + ")";
        } else {
            return referencedLabel;
        }
    }

}
