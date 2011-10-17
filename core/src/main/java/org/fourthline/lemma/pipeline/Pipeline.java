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

package org.fourthline.lemma.pipeline;

import org.fourthline.lemma.processor.Processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Encapsulates an array of <code>Processor</code>s.
 * <p>
 * A pipeline implementation processes an input object, with all its
 * processors in the configured order, and returns an output object.
 * </p>
 *
 * @author Christian Bauer
 */
public abstract class Pipeline<IN, OUT> {

    final private Logger log = Logger.getLogger(Pipeline.class.getName());

    final private Context context = new Context();

    public Context getContext() {
        return context;
    }

    protected void resetContext() {
        getContext().clear();
    }

    public OUT execute(IN input) {
        log.info("Executing: " + getClass().getSimpleName());

        resetContext();
        OUT output = null;
        for (Processor<IN, OUT> processor : getProcessors()) {
            log.info("Processing with: " + processor.getClass().getSimpleName());
            output = processor.process(input, getContext());
        }
        return output;
    }

    public void prepareOutputFile(File file, boolean overwrite) throws Exception {
        if (file.exists() && !overwrite) {
            String input = "";
            while (!input.toLowerCase().equals("y")) {
                System.out.print("Overwrite output file '" + file.getAbsolutePath() + "'? (Y/n): ");
                input = (new BufferedReader(new InputStreamReader(System.in))).readLine();
                if (input.length() == 0) {
                    input = "y";
                }
                if (input.equals("n")) {
                    System.out.println("Aborting...");
                    return;
                }
            }
        } else {
            if (!file.getParentFile().exists()) {
                log.fine("Creating output directory: " + file.getParentFile());
                file.getParentFile().mkdirs();
            }
        }
        file.createNewFile();
    }

    public abstract Processor<IN, OUT>[] getProcessors();
}
