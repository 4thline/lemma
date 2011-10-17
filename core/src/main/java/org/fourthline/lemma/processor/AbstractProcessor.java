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

package org.fourthline.lemma.processor;

import org.seamless.xhtml.Option;
import org.seamless.xhtml.XHTML;
import org.seamless.xhtml.XHTMLElement;
import org.seamless.xhtml.XHTMLParser;
import org.fourthline.lemma.anchor.CitationAnchor;
import org.fourthline.lemma.anchor.Scheme;
import org.fourthline.lemma.reader.javacode.JavacodeRawReader;
import org.fourthline.lemma.reader.javacode.JavacodeReader;
import org.fourthline.lemma.reader.text.PlaintextReader;
import org.fourthline.lemma.reader.Reader;
import org.fourthline.lemma.reader.javadoc.JavadocReader;
import org.fourthline.lemma.reader.xml.XMLReader;

import javax.xml.xpath.XPath;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates common processor functionality.
 * <p>
 * Also provides a mapping from citation anchor to {@link org.fourthline.lemma.reader.Reader}
 * instance. For example, a citation anchor which addresses a text file is mapped to the
 * text reader. You can not extend this built-in mapping, however, you can name your custom
 * {@link org.fourthline.lemma.reader.Reader} class in a citation anchor with the <code>reader</code>
 * style key.
 * </p>
 *
 * @author Christian Bauer
 */
public abstract class AbstractProcessor<IN, OUT> implements Processor<IN, OUT> {

    final private XHTMLParser parser = new XHTMLParser();
    final private XPath xpath;

    protected AbstractProcessor() {
        this.xpath = parser.createXPath();
    }

    public XHTMLParser getParser() {
        return parser;
    }

    public XPath getXPath() {
        return xpath;
    }

    final protected Map<String, Class<? extends Reader>> READER_SUFFIX_MAP =
            new HashMap<String, Class<? extends Reader>>() {{
                put(".java", JavacodeRawReader.class);
                put(".html", XMLReader.class);
                put(".xhtml", XMLReader.class);
                put(".xml", XMLReader.class);
                put(".txt", PlaintextReader.class);
                put(".csv", PlaintextReader.class);
            }};

    final private Map<Class<? extends Reader>, Reader> readerCache = new HashMap();

    public Reader getReader(CitationAnchor citation) {

        Class<? extends Reader> readerType = null;
        Option readerOption = citation.getOption(CitationAnchor.OptionKey.READER);

        if (readerOption == null && !citation.getAddress().getScheme().equals(Scheme.FILE)) {

            switch(citation.getAddress().getScheme()) {
                case JAVADOC:
                    return new JavadocReader();
                case JAVACODE:
                    return new JavacodeReader();
                default:
                    throw new IllegalStateException("No reader availablef or address scheme of: " + citation);
            }

        } else if (readerOption != null) {

            try {
                readerType = (Class<? extends Reader>) Thread.currentThread()
                        .getContextClassLoader()
                        .loadClass(readerOption.getFirstValue());
            } catch (Exception ex) {
                throw new RuntimeException("Unknown reader type: " + readerOption.getFirstValue(), ex);
            }

        } else {

            // Try a suffix match
            for (Map.Entry<String, Class<? extends Reader>> entry : READER_SUFFIX_MAP.entrySet()) {
                if (citation.getAddress().getPath().endsWith(entry.getKey())) {
                    readerType = entry.getValue();
                }
            }

        }
        
        if (readerType == null)
            throw new IllegalStateException("Unconfigured and/or unknown reader type for: " + citation);

        if (!readerCache.containsKey(readerType)) {
            try {
                Reader reader = readerType.newInstance();
                readerCache.put(readerType, reader);
            } catch (Exception ex) {
                throw new RuntimeException("Can't instantiate reader type: " + readerType, ex);
            }
        }

        return readerCache.get(readerType);
    }

}
