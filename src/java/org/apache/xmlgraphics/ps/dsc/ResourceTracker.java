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

package org.apache.xmlgraphics.ps.dsc;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentNeededResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentSuppliedResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPageResources;

/**
 * This class is used to track resources in a DSC-compliant PostScript file. The distinction is
 * made between supplied and needed resources. For the details of this distinction, please see
 * the DSC specification.
 */
public class ResourceTracker {

    private Set documentSuppliedResources;
    private Set documentNeededResources;
    private Set usedResources;
    private Set pageResources;
    
    /**
     * Returns the set of supplied resources.
     * @return the set of supplied resources
     */
    public Set getDocumentSuppliedResources() {
        if (documentSuppliedResources != null) {
            return Collections.unmodifiableSet(documentSuppliedResources);
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    /**
     * Returns the set of needed resources.
     * @return the set of needed resources
     */
    public Set getDocumentNeededResources() {
        if (documentNeededResources != null) {
            return Collections.unmodifiableSet(documentNeededResources);
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    /**
     * Notifies the resource tracker that a new page has been started and that the page resource
     * set can be cleared.
     */
    public void notifyStartNewPage() {
        if (pageResources != null) {
            pageResources.clear();
        }
    }
    
    /**
     * Registers a supplied resource. If the same resources is already in the set of needed
     * resources, it is removed there.
     * @param res the resource
     */
    public void registerSuppliedResource(PSResource res) {
        if (documentSuppliedResources == null) {
            documentSuppliedResources = new java.util.HashSet();
        }
        documentSuppliedResources.add(res);
        if (documentNeededResources != null) {
            documentNeededResources.remove(res);
        }
    }
    
    /**
     * Registers a needed resource. If the same resources is already in the set of supplied
     * resources, it is ignored, i.e. it is assumed to be supplied.
     * @param res the resource
     */
    public void registerNeededResource(PSResource res) {
        if (documentNeededResources == null) {
            documentNeededResources = new java.util.HashSet();
        }
        if (!documentSuppliedResources.contains(res)) {
            documentNeededResources.add(res);
        }
    }
    
    /**
     * Notifies the resource tracker about the usage of a resource on the current page.
     * @param res the resource being used
     */
    public void notifyResourceUsageOnPage(PSResource res) {
        if (pageResources == null) {
            pageResources = new java.util.HashSet();
        }
        pageResources.add(res);
    }

    /**
     * Notifies the resource tracker about the usage of resources on the current page.
     * @param resources the resources being used
     */
    public void notifyResourceUsageOnPage(Collection resources) {
        if (pageResources == null) {
            pageResources = new java.util.HashSet();
        }
        pageResources.addAll(resources);
    }

    /**
     * Indicates whether a particular resource is supplied, rather than needed.
     * @param res the resource
     * @return true if the resource is registered as being supplied.
     */
    public boolean isResourceSupplied(PSResource res) {
        return (documentSuppliedResources != null) && documentSuppliedResources.contains(res);
    }

    /**
     * Writes a DSC comment for the accumulated used resources, either at page level or
     * at document level.
     * @param pageLevel true if the DSC comment for the page level should be generated, 
     *                  false for the document level (in the trailer)
     * @param gen the PSGenerator to write the DSC comments with
     * @exception IOException In case of an I/O problem
     */
    public void writeResources(boolean pageLevel, PSGenerator gen) throws IOException {
        if (pageLevel) {
            new DSCCommentPageResources(pageResources).generate(gen);
            if (usedResources == null) {
                usedResources = new java.util.HashSet();
            }
            usedResources.addAll(pageResources);
        } else {
            if (usedResources != null) {
                Iterator iter = usedResources.iterator();
                while (iter.hasNext()) {
                    PSResource res = (PSResource)iter.next();
                    if (documentSuppliedResources == null 
                            || !documentSuppliedResources.contains(res)) {
                        registerNeededResource(res);
                    }
                }
            }
            new DSCCommentDocumentNeededResources(documentNeededResources).generate(gen);
            new DSCCommentDocumentSuppliedResources(documentSuppliedResources).generate(gen);
        }
    }
    
    
    
}
