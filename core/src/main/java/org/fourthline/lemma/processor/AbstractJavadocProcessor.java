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

import com.sun.javadoc.RootDoc;

/**
 * Resolves citation sources with the help of a Javadoc <code>RootDoc</code> instance.
 *
 * @author Christian Bauer
 */
public abstract class AbstractJavadocProcessor<IN, OUT> extends AbstractProcessor<IN, OUT> {

    final private RootDoc rootDoc;

    protected AbstractJavadocProcessor(RootDoc rootDoc) {
        this.rootDoc = rootDoc;
    }

    public RootDoc getRootDoc() {
        return rootDoc;
    }

}
