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
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Represents a %BeginDocument DSC comment.
 */
public class DSCCommentBeginDocument extends AbstractDSCComment {

    private PSResource resource;
    private Float version;
    private String type;

    /**
     * Creates a new instance
     */
    public DSCCommentBeginDocument() {
        super();
    }

    /**
     * Creates a new instance for a given PSResource instance
     * @param resource the resource
     */
    public DSCCommentBeginDocument(PSResource resource) {
        this.resource = resource;
        if (resource != null && !PSResource.TYPE_FILE.equals(resource.getType())) {
            throw new IllegalArgumentException("Resource must be of type 'file'");
        }
    }

    /**
     * Creates a new instance for a given PSResource instance
     * @param resource the resource
     * @param version the version of the resource (or null)
     * @param type the type of resource (or null)
     */
    public DSCCommentBeginDocument(PSResource resource, Float version, String type) {
        this(resource);
        this.version = version;
        this.type = type;
    }

    /**
     * Returns the resource version.
     * @return the resource version (or null if not applicable)
     */
    public Float getVersion() {
        return this.version;
    }

    /**
     * Returns the resource type
     * @return the resource type (or null if not applicable)
     */
    public String getType() {
        return this.type;
    }

    /** {@inheritDoc} */
    public String getName() {
        return DSCConstants.BEGIN_DOCUMENT;
    }

    /**
     * Returns the associated PSResource.
     * @return the resource
     */
    public PSResource getResource() {
        return this.resource;
    }

    /** {@inheritDoc} */
    public boolean hasValues() {
        return true;
    }

    /** {@inheritDoc} */
    public void parseValue(String value) {
        List params = splitParams(value);
        Iterator iter = params.iterator();
        String name = (String)iter.next();
        this.resource = new PSResource(PSResource.TYPE_FILE, name);
        if (iter.hasNext()) {
            this.version = Float.valueOf(iter.next().toString());
            this.type = null;
            if (iter.hasNext()) {
                this.type = (String)iter.next();
            }
        }
    }

    /** {@inheritDoc} */
    public void generate(PSGenerator gen) throws IOException {
        List params = new java.util.ArrayList();
        params.add(getResource().getName());
        if (getVersion() != null) {
            params.add(getVersion());
            if (getType() != null) {
                params.add(getType());
            }
        }
        gen.writeDSCComment(getName(), params.toArray());
    }

}
