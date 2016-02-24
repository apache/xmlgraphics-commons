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

/**
 * Represents a DSC header comment (beginning with "%!).
 */
public class DSCHeaderComment extends AbstractEvent {

    private String comment;

    /**
     * Creates a new instance.
     * @param comment the comment
     */
    public DSCHeaderComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the comment.
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Indicates whether the file started by this comments is DSC 3.0 compliant.
     * @return true if the file is DSC 3.0 compliant.
     */
    public boolean isPSAdobe30() {
        return getComment().startsWith(DSCConstants.PS_ADOBE_30.substring(2));
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(
     *              org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        gen.writeln("%!" + getComment());
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#getEventType()
     */
    public int getEventType() {
        return HEADER_COMMENT;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#isHeaderComment()
     */
    public boolean isHeaderComment() {
        return true;
    }

}
