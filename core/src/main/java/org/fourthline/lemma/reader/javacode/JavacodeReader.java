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

package org.fourthline.lemma.reader.javacode;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import org.fourthline.lemma.Constants;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.reader.content.LineRange;
import org.fourthline.lemma.reader.content.filter.CleanupFilter;
import org.fourthline.lemma.reader.content.filter.ContentFilter;
import org.fourthline.lemma.reader.content.filter.FragmentFilter;
import org.fourthline.lemma.reader.content.handler.ContentFileHandler;
import org.fourthline.lemma.reader.content.printer.ContentPrinter;
import org.fourthline.lemma.reader.javadoc.AbstractJavadocReader;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Reads raw lines of Java code, handles <code>javacode://</code> scheme.
 * <p>
 * This class will discover the source of the citation reference by using the
 * Javadoc index and {@link org.fourthline.lemma.reader.javacode.LineRangeParser}s.
 * </p>
 *
 * @author Christian Bauer
 */
public class JavacodeReader extends AbstractJavadocReader {

    final private Logger log = Logger.getLogger(JavacodeRawReader.class.getName());

    final public static Pattern PATTERN_FRAGMENT_LABEL =
            Pattern.compile("(.*)//\\s*" + Constants.PATTERN_FRAGMENT_LABEL + "\\s*$");

    final protected ContentFileHandler handler;
    final protected ContentPrinter printer;
    final protected ContentFilter[] filters;

    final private Map<File, LineRangeParser> lineRangeParsers = new HashMap();

    public JavacodeReader() {
        handler = new ContentFileHandler();
        printer = new ContentPrinter();
        filters = new ContentFilter[]{
                new FragmentFilter(PATTERN_FRAGMENT_LABEL),
                new CleanupFilter(PATTERN_FRAGMENT_LABEL)
        };
    }

    protected XHTML read(CitationAnchor citation, Context context, RootDoc rootDoc) {
        return read(
                findTargetDoc(citation, rootDoc),
                citation,
                isGenerateId(context)
        );
    }

    protected XHTML read(Doc doc, CitationAnchor citation, boolean uniqueId) {
        if (doc == null) return null;

        log.fine("Reading Javacode: " + doc.position());

        XHTML xhtml = getParser().createDocument();

        XHTMLElement root =
                xhtml.createRoot(getXPath(), Constants.WRAPPER_ELEMENT)
                        .setAttribute(XHTML.ATTR.CLASS, citation.getOutputClasses());
        if (uniqueId)
            root.setAttribute(XHTML.ATTR.id, citation.getOutputIdentifier());

        appendTitle(root, citation.getTitle());
        addFilePath(root, citation, doc.position().file());

        appendContent(root, doc, citation);

        return xhtml;

    }

    protected void appendContent(XHTMLElement parent, Doc doc, CitationAnchor citation) {

        String[] source = readSource(doc);

        // Filtering of source
        for (ContentFilter filter : filters) {
            source = filter.filter(source, citation);
        }

        // Transform the source into an XML document
        String sourceLines = printer.print(source);
        if (sourceLines != null) {
            parent.createChild(Constants.WRAPPER_ELEMENT)
                    .setAttribute(XHTML.ATTR.CLASS, Constants.TYPE_CONTENT)
                            // Wrap it in a <pre class="prettyprint"> for syntax highlighting on websites!
                    .createChild(XHTML.ELEMENT.pre)
                    .setAttribute(XHTML.ATTR.CLASS, "prettyprint")
                    .setContent(sourceLines);
        }
    }

    public String[] readSource(Doc doc) {

        File file = doc.position().file();

        // The type of doc decides if the whole file is returned or just a few lines of the file

        if (doc instanceof ClassDoc) {
            ClassDoc classDoc = (ClassDoc) doc;

            // If it's a nested class, read only the lines of that nested class source
            if (classDoc.containingClass() == null) {
                log.finest("Doc is referencing a root type declaration: " + doc.name());
                return handler.getContent(file, null);
            } else {
                String nestedClassName = classDoc.simpleTypeName();
                log.finest("Doc is referencing a nested type declaration: " + nestedClassName);
                return handler.getContent(file, getLineRangeParser(file).getTypesLineRange().get(nestedClassName));
            }

        } else if (doc instanceof PackageDoc) {

            // For a package we return everything
            log.finest("Doc is referencing a package: " + doc.name());
            return handler.getContent(file, null);

        } else if (doc instanceof ExecutableMemberDoc) {

            // For methods we return the lines of the method source (signature matching is complex though)
            log.finest("Doc is referencing method declaration: " + doc.name());
            return handler.getContent(file, getMethodLineRange(file, (MethodDoc) doc));

        } else {
            log.warning("Unknown doc type/reference, not reading any source: " + doc);
        }

        return new String[0];
    }


    public LineRange getMethodLineRange(File file, MethodDoc methodDoc) {
        LineRangeParser parser = getLineRangeParser(file);

        String signature = getSignature(methodDoc);
        log.fine("Looking up source line range of method using signature: " + signature);
        LineRange range = parser.getMethodsLineRange().get(signature);

        // Out of options
        if (range == null) {
            throw new RuntimeException(
                    "Can't find method line range (begin/end) with signature in source file: " + signature
            );
        }

        log.fine("Method line range is: " + range);
        return range;

    }

    protected LineRangeParser getLineRangeParser(File file) {
        synchronized (lineRangeParsers) {
            if (lineRangeParsers.containsKey(file)) return lineRangeParsers.get(file);
            try {
                LineRangeParser parser = instantiateLineRangeParser(file);
                lineRangeParsers.put(file, parser);
                return parser;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected LineRangeParser instantiateLineRangeParser(File file) throws Exception {
        return new LineRangeParser(file);
    }

    protected String getSignature(MethodDoc methodDoc) {
        StringBuilder signature = new StringBuilder();
        signature.append(methodDoc.name());
        signature.append("(");
        for (Parameter parameter : methodDoc.parameters()) {
            signature.append(toString(parameter.type(), false)); // TODO: Always use unqualified name?!
            signature.append(",");
        }
        // Cut last comma
        if (methodDoc.parameters().length > 0) signature.deleteCharAt(signature.length() - 1);
        signature.append(")");
        return signature.toString();
    }

    protected String toString(Type type, boolean qualified) {
        return (qualified ? type.qualifiedTypeName() : type.simpleTypeName()) + type.dimension();
    }
}
