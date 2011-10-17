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

import java.util.HashMap;

/**
 * Execution context of a all participants in a pipeline.
 * <p>
 * Each participant (processors and readers) used in a pipeline might require certain
 * properties when it executes, it is the job of the pipeline to set these properties
 * before execution.
 * </p>
 * <p>
 * Participants can use this context to pass values between each other during execution,
 * or to cache data.
 * </p>
 *
 * @author Christian Bauer
 */
public class Context extends HashMap<String, Object> {
}
