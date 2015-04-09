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

package org.fourthline.lemma.anchor;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.SeeTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Encapsulates parsing of citation anchor URIs.
 * <p>
 * Two addresses are <em>equal</em> if they have the same scheme and path, and (if not null) fragment.
 * </p>
 *
 * @author Christian Bauer
 */
public class AnchorAddress {

    final private static Logger log = Logger.getLogger(AnchorAddress.class.getName());

    protected static final String PATTERN_SCHEME =  "([a-zA-Z]+?)";

    protected static final String PATTERN_PATH = "([ \\p{Alnum}\\./_-]+?)";

    protected static final String PATTERN_FRAGMENT = "([\\p{Alnum}]+?(?:\\([\\p{Alnum},\\.\\[\\]<>\\s]*?\\))??)";

    public static final Pattern PATTERN =
            Pattern.compile("^"+PATTERN_SCHEME+Scheme.SEPARATOR+PATTERN_PATH+"(?:#"+PATTERN_FRAGMENT+")??$");

    public static final String PATH_THIS = "this";

    final private Scheme scheme;
    final private String path;
    final private String fragment;

    public AnchorAddress(Scheme scheme, String path, String fragment) {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("Reference path can not be empty");
        }
        this.scheme = scheme;
        this.path = path;
        if (fragment != null && !fragment.endsWith(")")) fragment = fragment + "()"; // normalize
        this.fragment = fragment;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }

    public String getFragment() {
        return fragment;
    }

    public static AnchorAddress valueOf(String string) {
        if (string == null || string.length() == 0) return null;

        Matcher m = PATTERN.matcher(string.trim());
        if (!m.matches()) {
            // If it doesn't then convert it to file:// address and try again
            String fileString = toFileAddress(string);
            log.finest("Address string does not contain a schema, using file schema: " + fileString);
            m.reset(fileString);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid reference, no pattern match of anchor: " + string);
            }
        }
        return new AnchorAddress(Scheme.valueOf(m.group(1).toUpperCase()), m.group(2), m.group(3));
    }

    public static AnchorAddress valueOf(Scheme scheme, SeeTag tag) {
        String reference = null;
        if (tag.referencedMember() != null && tag.referencedMember().isMethod()) {
            reference = tag.referencedClassName() + "#" +
                    tag.referencedMember().name() +
                    ((MethodDoc) tag.referencedMember()).flatSignature();
        } else if (tag.referencedClass() != null) {
            reference = tag.referencedClassName();
        } else if (tag.referencedPackage() != null) {
            reference = tag.referencedPackage().name();
        }

        return reference != null
                ? valueOf(scheme.name().toLowerCase() + Scheme.SEPARATOR + reference)
                : null;
    }

    public static AnchorAddress valueOf(Scheme scheme, PackageDoc packageDoc) {
        return new AnchorAddress(
                scheme,
                packageDoc.name(),
                null
        );
    }

    public static AnchorAddress valueOf(Scheme scheme, ClassDoc classDoc, String fragment) {
        return new AnchorAddress(
                scheme,
                classDoc.qualifiedTypeName(),
                fragment
        );
    }

    public static AnchorAddress valueOf(Scheme scheme, MethodDoc methodDoc) {
        return new AnchorAddress(
                scheme,
                methodDoc.containingClass().qualifiedTypeName(),
                methodDoc.name() + methodDoc.flatSignature()
        );
    }

    /**
     * Converts a simple file path into a <code>file://</code> URI string.
     *
     * @param string A file path, e.g. "/my/file.txt".
     * @return A URI string, e.g. "file://my/file.txt";
     */
    public static String toFileAddress(String string) {
        if (string.startsWith(".")) throw new IllegalArgumentException("Reference can't start with a period '.'");

        // Always remove leading and trailing slashes
        if (string.startsWith("/")) string = string.substring(1);
        if (string.endsWith("/")) string = string.substring(0, string.length() - 1);

        return Scheme.FILE + Scheme.SEPARATOR + string;
    }

    /**
     * Generates an XSD:id typed String that can be used in XML documents as identifier attribute value.
     *
     * @return An XSD:id compatible string representation of this URI.
     */
    public String toIdentifierString() {

        String schemeString = getScheme().name().toLowerCase();
        String pathString = getPath();
        String fragmentString = (getFragment() != null ? "#" + getFragment() : "");

        return (schemeString + "." + pathString + fragmentString)
                .replaceAll("\\s", "")
                .replaceAll("#|,", ".")
                .replaceAll("\\[|\\]", "-")
                .replaceAll("\\(|\\)", ".")
                .replaceAll("[^a-zA-Z0-9-._]", "_")
                .replaceAll("__", "_");
    }

/*
    public boolean isMatching(SeeTag tag) {
        // Is the referenced class/member/package of this tag the same as the target of this citation?
        if (tag.referencedPackage() != null) {
            if (!getPath().equals(tag.referencedPackage().name()) || getFragment() != null) {
                return false;
            }
        }
        if (tag.referencedClass() != null) {
            if (!getPath().equals(tag.referencedClassName())) {
                return false;
            }
        }
        if (tag.referencedMember() != null) {
            if (getFragment() == null || !getFragment().equals(tag.referencedMemberName())) {
                return false;
            }
        }
        return true;
    }
*/

    /**
     * @return The complement of the <code>valueOf(String)</code> operation.
     */
    @Override
    public String toString() {
        return getScheme().name().toLowerCase() + Scheme.SEPARATOR + getPath() + (getFragment() != null ? "#" + getFragment() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnchorAddress that = (AnchorAddress) o;

        if (fragment != null ? !fragment.equals(that.fragment) : that.fragment != null) return false;
        if (!path.equals(that.path)) return false;
        if (scheme != that.scheme) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = scheme.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        return result;
    }
}
