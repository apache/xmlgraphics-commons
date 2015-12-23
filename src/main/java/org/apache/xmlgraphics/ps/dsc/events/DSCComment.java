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

/**
 * Base interface for all DSC comments.
 */
public interface DSCComment extends DSCEvent {

    /**
     * Returns the name of the DSC comment.
     * @return the name of the DSC comment (without the "%%" prefix)
     */
    String getName();

    /**
     * Parses the value of the DSC comment.
     * @param value the value
     */
    void parseValue(String value);

    /**
     * Indicates whether this DSC comment has values.
     * @return true if the DSC comment has values
     */
    boolean hasValues();

    /**
     * Indicates whether the DSC comment's value is "Atend".
     * @return true if the value is "Atend"
     */
    boolean isAtend();

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(
     *      org.apache.xmlgraphics.ps.PSGenerator)
     */
    void generate(PSGenerator gen) throws IOException;

}
