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
 * Represents a line of PostScript code.
 */
public class PostScriptLine extends AbstractEvent {

    private String line;

    /**
     * Creates a new instance.
     * @param line the code line
     */
    public PostScriptLine(String line) {
        this.line = line;
    }

    /**
     * Returns the code line.
     * @return the code line
     */
    public String getLine() {
        return this.line;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        gen.writeln(getLine());
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#getEventType()
     */
    public int getEventType() {
        return LINE;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#asLine()
     */
    public PostScriptLine asLine() {
        return this;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#isLine()
     */
    public boolean isLine() {
        return true;
    }

}
