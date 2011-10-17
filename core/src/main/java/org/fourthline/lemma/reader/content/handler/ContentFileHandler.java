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

package org.fourthline.lemma.reader.content.handler;

import org.fourthline.lemma.reader.content.LineRange;
import org.seamless.util.io.IO;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.logging.Logger;
import java.io.File;

/**
 * Reads contents of a file, returns the given line range as a string array.
 *
 * @author Christian Bauer
 */
public class ContentFileHandler {

    final private Logger log = Logger.getLogger(ContentFileHandler.class.getName());

    final private Map<File, String[]> cache = new HashMap();

    public String[] getContent(File file, LineRange range) {
        try {

            String[] content;

            synchronized (cache) {
                if (cache.containsKey(file)) {
                    log.fine("Using cached content lines of file: " + file.getName());
                    content = cache.get(file);
                } else {
                    log.fine("Reading content lines from file on disk: " + file);
                    content = IO.readLines(file, false);
                    cache.put(file, content);
                }
            }

            if (range != null) {
                log.fine("Returning content line range " + range + " of file: " + file.getName());
                return Arrays.copyOfRange(content, range.getBegin()- 1, range.getEnd());
            } else {
                return content;
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

}
