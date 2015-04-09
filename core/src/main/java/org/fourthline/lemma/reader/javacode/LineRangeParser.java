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

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.fourthline.lemma.reader.content.LineRange;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Finds the "line range" of declarations in a Java source file.
 * <p>
 * We need this to demarcate raw source code for citation, where a method/nested class begins and where it ends.
 * The challenge is matching the Javadoc metadata of a method to this parser's metadata of a method.
 * Javadoc has knowledge of the type system (e.g. qualified vs. unqualified parameters of a method
 * are detectable) while this parser only has the declared string as-is from the source file and there is
 * no way we can qualify/unqualify a method parameter type. So calculating a method signature here
 * might have a different result than calculating a method signature in the Javadoc reader.
 * </p>
 * <p>
 * In other words, don't be surprised when the source lines of a method can't be found if you use
 * qualified type names on method parameters in your source.
 * </p>
 * TODO: Revisit this and see if it helps: http://code.google.com/p/javaparser/issues/detail?id=9
 *
 * @author Christian Bauer
 */
public class LineRangeParser {

    private Logger log = Logger.getLogger(LineRangeParser.class.getName());

    protected final Map<String, LineRange> methodsLineRange = new HashMap();
    protected final Map<String, LineRange> typesLineRange = new HashMap();

    public LineRangeParser(final File file) throws IOException, ParseException {
        log.fine("Parsing Java source of file: " + file);

        CompilationUnit cu = JavaParser.parse(file);

        new VoidVisitorAdapter() {

            @Override
            public void visit(ClassOrInterfaceDeclaration typeDeclaration, Object o) {

                String signature = typeDeclaration.getName();
                LineRange range = new LineRange(typeDeclaration.getBeginLine(), typeDeclaration.getEndLine());

                log.fine("Parsed source of type '" + signature + "', lines: " + range);
                LineRangeParser.this.typesLineRange.put(signature, range);

                for (BodyDeclaration bodyDeclaration : typeDeclaration.getMembers()) {
                    bodyDeclaration.accept(this, null);
                }
            }

            @Override
            public void visit(MethodDeclaration methodDeclaration, Object arg) {

                String signature = getSignature(methodDeclaration);
                LineRange range = new LineRange(methodDeclaration.getBeginLine(), methodDeclaration.getEndLine());

                log.fine("Parsed source of method '" + signature + "', lines: " + range);

                LineRangeParser.this.methodsLineRange.put(signature, range);
            }
        }.visit(cu, null);
    }

    public Map<String, LineRange> getMethodsLineRange() {
        return methodsLineRange;
    }

    public Map<String, LineRange> getTypesLineRange() {
        return typesLineRange;
    }

    protected String getSignature(MethodDeclaration methodDeclaration) {
        final StringBuilder signature = new StringBuilder();
        signature.append(methodDeclaration.getName());
        signature.append("(");
        if (methodDeclaration.getParameters() != null) {
            for (Iterator<Parameter> i = methodDeclaration.getParameters().iterator(); i.hasNext(); ) {
                Parameter p = i.next();
                signature.append(p.getType().toString());
                if (i.hasNext()) {
                    signature.append(",");
                }
            }
        }
        signature.append(")");
        return signature.toString();
    }
}
