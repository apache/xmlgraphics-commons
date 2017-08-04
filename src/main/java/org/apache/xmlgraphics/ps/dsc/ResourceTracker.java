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
import java.util.Map;
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

    //Map<PSResource, Integer>
    private Map resourceUsageCounts;

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
        if (documentSuppliedResources == null || !documentSuppliedResources.contains(res)) {
            if (documentNeededResources == null) {
                documentNeededResources = new java.util.HashSet();
            }
            documentNeededResources.add(res);
        }
    }

    private void preparePageResources() {
        if (pageResources == null) {
            pageResources = new java.util.HashSet();
        }
    }

    private void prepareUsageCounts() {
        if (resourceUsageCounts == null) {
            resourceUsageCounts = new java.util.HashMap();
        }
    }

    /**
     * Notifies the resource tracker about the usage of a resource on the current page.
     * @param res the resource being used
     */
    public void notifyResourceUsageOnPage(PSResource res) {
        preparePageResources();
        pageResources.add(res);

        prepareUsageCounts();
        Counter counter = (Counter)resourceUsageCounts.get(res);
        if (counter == null) {
            resourceUsageCounts.put(res, new Counter());
        } else {
            counter.inc();
        }
    }

    /**
     * Notifies the resource tracker about the usage of resources on the current page.
     * @param resources the resources being used
     */
    public void notifyResourceUsageOnPage(Collection resources) {
        preparePageResources();
        for (Object resource : resources) {
            PSResource res = (PSResource) resource;
            notifyResourceUsageOnPage(res);
        }
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
            writePageResources(gen);
        } else {
            writeDocumentResources(gen);
        }
    }

    /**
     * Writes a DSC comment for the accumulated used resources on the current page. Then it commits
     * all those resources to the used resources on document level.
     * @param gen the PSGenerator to write the DSC comments with
     * @exception IOException In case of an I/O problem
     */
    public void writePageResources(PSGenerator gen) throws IOException {
        new DSCCommentPageResources(pageResources).generate(gen);
        if (usedResources == null) {
            usedResources = new java.util.HashSet();
        }
        usedResources.addAll(pageResources);
    }

    /**
     * Writes a DSC comment for the needed and supplied resourced for the current DSC document.
     * @param gen the PSGenerator to write the DSC comments with
     * @exception IOException In case of an I/O problem
     */
    public void writeDocumentResources(PSGenerator gen) throws IOException {
        if (usedResources != null) {
            for (Object usedResource : usedResources) {
                PSResource res = (PSResource) usedResource;
                if (documentSuppliedResources == null
                        || !documentSuppliedResources.contains(res)) {
                    registerNeededResource(res);
                }
            }
        }
        new DSCCommentDocumentNeededResources(documentNeededResources).generate(gen);
        new DSCCommentDocumentSuppliedResources(documentSuppliedResources).generate(gen);
    }

    /**
     * This method declares that the given resource will be inlined and can therefore
     * be removed from resource tracking. This is useful when you don't know beforehand
     * if a resource will be used multiple times. If it's only used once it's better
     * to inline the resource to lower the maximum memory needed inside the PostScript
     * interpreter.
     * @param res the resource
     */
    public void declareInlined(PSResource res) {
        if (this.documentNeededResources != null) {
            this.documentNeededResources.remove(res);
        }
        if (this.documentSuppliedResources != null) {
            this.documentSuppliedResources.remove(res);
        }
        if (this.pageResources != null) {
            this.pageResources.remove(res);
        }
        if (this.usedResources != null) {
            this.usedResources.remove(res);
        }
    }

    /**
     * Returns the number of times a resource has been used inside the current DSC document.
     * @param res the resource
     * @return the number of times the resource has been used
     */
    public long getUsageCount(PSResource res) {
        Counter counter = (Counter)resourceUsageCounts.get(res);
        return (counter != null ? counter.getCount() : 0);
    }

    private static class Counter {

        private long count = 1;

        public void inc() {
            this.count++;
        }

        public long getCount() {
            return this.count;
        }

        public String toString() {
            return Long.toString(this.count);
        }
    }

}
