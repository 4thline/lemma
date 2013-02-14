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

package example.advanced;

import example.util.DocletTest;
import org.seamless.xhtml.XHTML;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class Callouts extends DocletTest {

    @Test
    public void processCallouts() throws Exception {
        XHTML output = getTemplatePipeline().execute(
            parseDocument("example/advanced/example03_input.xhtml")
        );

        assertEquals(
            getParser().print(output),
            getContent("example/advanced/example03_output.xhtml")
        );
    }
}
