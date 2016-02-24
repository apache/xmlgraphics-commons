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
 * Represents the %%Pages DSC comment.
 */
public class DSCCommentPages extends AbstractDSCComment {

    private int pageCount = -1;

    /**
     * Creates a new instance.
     */
    public DSCCommentPages() {
    }

    /**
     * Creates a new instance.
     * @param pageCount the number of pages
     */
    public DSCCommentPages(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Returns the page count.
     * @return the page count
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Sets the page count.
     * @param count the new page count
     */
    public void setPageCount(int count) {
        this.pageCount = count;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    public String getName() {
        return DSCConstants.PAGES;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#hasValues()
     */
    public boolean hasValues() {
        return true;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#parseValue(java.lang.String)
     */
    public void parseValue(String value) {
        this.pageCount = Integer.parseInt(value);
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        if (getPageCount() > 0) {
            gen.writeDSCComment(getName(), getPageCount());
        } else {
            gen.writeDSCComment(getName(), DSCConstants.ATEND);
        }
    }

}
