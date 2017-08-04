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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSProcSet;
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Abstract base class for Resource DSC comments (DocumentNeededResources,
 * DocumentSuppliedResources and PageResources).
 */
public abstract class AbstractResourcesDSCComment extends AbstractDSCComment {

    private Set resources;

    /**
     * Creates a new instance.
     */
    public AbstractResourcesDSCComment() {
        super();
    }

    /**
     * Creates a new instance.
     * @param resources a Collection of PSResource instances
     */
    public AbstractResourcesDSCComment(Collection resources) {
        addResources(resources);
    }

    /** {@inheritDoc} */
    public boolean hasValues() {
        return true;
    }

    private void prepareResourceSet() {
        if (this.resources == null) {
            this.resources = new java.util.TreeSet();
        }
    }

    /**
     * Adds a new resource.
     * @param res the resource
     */
    public void addResource(PSResource res) {
        prepareResourceSet();
        this.resources.add(res);
    }

    /**
     * Adds a collection of resources.
     * @param resources a Collection of PSResource instances.
     */
    public void addResources(Collection resources) {
        if (resources != null) {
            prepareResourceSet();
            this.resources.addAll(resources);
        }
    }

    /**
     * Returns the set of resources associated with this DSC comment.
     * @return the set of resources
     */
    public Set getResources() {
        return Collections.unmodifiableSet(this.resources);
    }

    /**
     * Defines the known resource types (font, procset, file, pattern etc.).
     */
    static final Set RESOURCE_TYPES = new java.util.HashSet();

    static {
        RESOURCE_TYPES.add(PSResource.TYPE_FONT);
        RESOURCE_TYPES.add(PSResource.TYPE_PROCSET);
        RESOURCE_TYPES.add(PSResource.TYPE_FILE);
        RESOURCE_TYPES.add(PSResource.TYPE_PATTERN);
        RESOURCE_TYPES.add(PSResource.TYPE_FORM);
        RESOURCE_TYPES.add(PSResource.TYPE_ENCODING);
    }

    /** {@inheritDoc} */
    public void parseValue(String value) {
        List params = splitParams(value);
        String currentResourceType = null;
        Iterator iter = params.iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            if (RESOURCE_TYPES.contains(name)) {
                currentResourceType = name;
            }
            if (currentResourceType == null) {
                throw new IllegalArgumentException(
                        "<resources> must begin with a resource type. Found: " + name);
            }
            if (PSResource.TYPE_FONT.equals(currentResourceType)) {
                String fontname = (String)iter.next();
                addResource(new PSResource(name, fontname));
            } else if (PSResource.TYPE_FORM.equals(currentResourceType)) {
                String formname = (String)iter.next();
                addResource(new PSResource(name, formname));
            } else if (PSResource.TYPE_PROCSET.equals(currentResourceType)) {
                String procname = (String)iter.next();
                String version = (String)iter.next();
                String revision = (String)iter.next();
                addResource(new PSProcSet(procname,
                        Float.parseFloat(version), Integer.parseInt(revision)));
            } else if (PSResource.TYPE_FILE.equals(currentResourceType)) {
                String filename = (String)iter.next();
                addResource(new PSResource(name, filename));
            } else {
                throw new IllegalArgumentException("Invalid resource type: " + currentResourceType);
            }
        }
    }

    /** {@inheritDoc} */
    public void generate(PSGenerator gen) throws IOException {
        if (resources == null || resources.size() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("%%").append(getName()).append(": ");
        boolean first = true;
        for (Object resource : resources) {
            if (!first) {
                gen.writeln(sb.toString());
                sb.setLength(0);
                sb.append("%%+ ");
            }
            PSResource res = (PSResource) resource;
            sb.append(res.getResourceSpecification());
            first = false;
        }
        gen.writeln(sb.toString());
    }

}
