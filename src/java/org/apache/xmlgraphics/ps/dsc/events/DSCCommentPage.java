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
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Represents a %%Page DSC comment.
 */
public class DSCCommentPage extends AbstractDSCComment {

    private String pageName;
    private int pagePosition = -1;
    
    /**
     * Creates a new instance.
     */
    public DSCCommentPage() {
    }

    /**
     * Creates a new instance.
     * @param pageName the name of the page
     * @param pagePosition the position of the page within the file (1-based)
     */
    public DSCCommentPage(String pageName, int pagePosition) {
        setPageName(pageName);
        setPagePosition(pagePosition);
    }
    
    /**
     * Creates a new instance. The page name will be set to the same value as the page position.
     * @param pagePosition the position of the page within the file (1-based)
     */
    public DSCCommentPage(int pagePosition) {
        this(Integer.toString(pagePosition), pagePosition);
    }

    /**
     * Resturns the name of the page.
     * @return the page name
     */
    public String getPageName() {
        return this.pageName;
    }
    
    /**
     * Sets the page name.
     * @param name the page name
     */
    public void setPageName(String name) {
        this.pageName = name;
    }

    /**
     * Returns the page position.
     * @return the page position (1-based)
     */
    public int getPagePosition() {
        return this.pagePosition;
    }
    
    /**
     * Sets the page position.
     * @param position the page position (1-based)
     */
    public void setPagePosition(int position) {
        if (position <= 0) {
            throw new IllegalArgumentException("position must be 1 or above");
        }
        this.pagePosition = position;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    public String getName() {
        return DSCConstants.PAGE;
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
        List params = splitParams(value);
        Iterator iter = params.iterator();
        this.pageName = (String)iter.next();
        this.pagePosition = Integer.parseInt((String)iter.next());
    }
    
    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(
     *          org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        gen.writeDSCComment(getName(), 
                new Object[] {getPageName(), new Integer(getPagePosition())});
    }

}
