/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.xmlgraphics.ps.dsc.events;

import java.io.IOException;

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.DSCParserConstants;

/**
 * Interface representing a DSC event. A DSC event can be a DSC comment, a PostScript comment
 * or a line of PostScript code.
 */
public interface DSCEvent extends DSCParserConstants {

    /**
     * Returns the event type.
     * @return the event type (see {@link DSCParserConstants})
     */
    int getEventType();

    /**
     * Casts this instance to a DSCComment if possible.
     * @return this event as a DSCComment
     * @throws ClassCastException if the event is no DSCComment
     */
    DSCComment asDSCComment();

    /**
     * Casts this instance to a PostScriptLine if possible.
     * @return this event as a PostScriptLine
     * @throws ClassCastException if the event is no PostScriptLine
     */
    PostScriptLine asLine();

    /**
     * Indicates whether the instance is a DSC comment.
     * @return true if the instance is a DSC comment
     */
    boolean isDSCComment();

    /**
     * Indicates whether the instance is a PostScript comment.
     * @return true if the instance is a PostScript comment
     */
    boolean isComment();

    /**
     * Indicates whether the instance is a header comment.
     * @return true if the instance is a header comment
     */
    boolean isHeaderComment();

    /**
     * Indicates whether the instance is a PostScript line.
     * @return true if the instance is a PostScript line
     */
    boolean isLine();

    /**
     * Writes the event to the given PSGenerator.
     * @param gen the PSGenerator to write to
     * @throws IOException In case of an I/O error
     */
    void generate(PSGenerator gen) throws IOException;

}
