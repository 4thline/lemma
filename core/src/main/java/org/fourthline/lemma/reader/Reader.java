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

package org.fourthline.lemma.reader;

import org.seamless.xhtml.XHTML;
import org.fourthline.lemma.pipeline.Context;
import org.fourthline.lemma.anchor.CitationAnchor;

/**
 * Reads content of a citation source and returns it as an XHTML model.
 *
 * @author Christian Bauer
 */
public interface Reader {

    public XHTML read(CitationAnchor citation, Context context);

}
