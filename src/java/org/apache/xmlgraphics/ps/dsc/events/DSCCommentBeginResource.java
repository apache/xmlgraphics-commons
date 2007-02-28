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
import org.apache.xmlgraphics.ps.PSProcSet;
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Represents a %BeginResource DSC comment.
 */
public class DSCCommentBeginResource extends AbstractDSCComment {

    private PSResource resource;
    private Integer min;
    private Integer max;
    
    /**
     * Creates a new instance
     */
    public DSCCommentBeginResource() {
    }

    /**
     * Creates a new instance for a given PSResource instance
     * @param resource the resource
     */
    public DSCCommentBeginResource(PSResource resource) {
        this.resource = resource;
    }
    
    /**
     * Creates a new instance for a given PSResource instance
     * @param resource the resource
     * @param min Minimum VM used by the resource
     * @param max Maximum VM used by the resource
     */
    public DSCCommentBeginResource(PSResource resource, int min, int max) {
        this.resource = resource;
        this.min = new Integer(min);
        this.max = new Integer(max);
    }
    
    /**
     * Returns the associated PSResource.
     * @return the resource
     */
    public PSResource getResource() {
        return this.resource;
    }
    
    /**
     * Returns the minimum VM used by the resource.
     * @return the minimum VM used by the resource
     */
    public Integer getMin() {
        return this.min;
    }
    
    /**
     * Returns the maximum VM used by the resource.
     * @return the maximum VM used by the resource
     */
    public Integer getMax() {
        return this.max;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#getName()
     */
    public String getName() {
        return DSCConstants.BEGIN_RESOURCE;
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
        String name = (String)iter.next();
        if (PSResource.TYPE_FONT.equals(name)) {
            String fontname = (String)iter.next();
            this.resource = new PSResource(name, fontname);
        } else if (PSResource.TYPE_PROCSET.equals(name)) {
            String procname = (String)iter.next();
            String version = (String)iter.next();
            String revision = (String)iter.next();
            this.resource = new PSProcSet(procname, 
                    Float.parseFloat(version), Integer.parseInt(revision));
        } else if (PSResource.TYPE_FILE.equals(name)) {
            String filename = (String)iter.next();
            this.resource = new PSResource(name, filename);
        } else {
            throw new IllegalArgumentException("Invalid resource type: " + name);
        }
    }
    
    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#generate(
     *          org.apache.xmlgraphics.ps.PSGenerator)
     */
    public void generate(PSGenerator gen) throws IOException {
        if (getMin() != null) {
            Object[] params = new Object[] {getResource(), getMin(), getMax()};
            gen.writeDSCComment(getName(), params);
        } else {
            gen.writeDSCComment(getName(), getResource());
        }
    }

}
