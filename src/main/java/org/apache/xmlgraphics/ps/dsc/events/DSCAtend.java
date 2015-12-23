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

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.DSCCommentFactory;

/**
 * This class represents a DSC comment with an "(ATEND)" value.
 */
public class DSCAtend extends AbstractDSCComment {

    private String name;

    /**
     * Creates a new instance
     * @param name the name of the DSC comment (without the "%%" prefix)
     */
    public DSCAtend(String name) {
        this.name = name;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#hasValues()
     */
    public boolean hasValues() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractDSCComment#isAtend()
     */
    public boolean isAtend() {
        return true;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#parseValue(java.lang.String)
     */
    public void parseValue(String value) {
        //nop
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(
     *              org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        gen.writeDSCComment(getName(), DSCConstants.ATEND);
    }

    /**
     * Creates a new instance of a DSC comment with the same name as this instance but does not
     * have an "Atend" value.
     * @return the new DSC comment
     */
    public DSCComment createDSCCommentFromAtend() {
        return DSCCommentFactory.createDSCCommentFor(getName());
    }

}
