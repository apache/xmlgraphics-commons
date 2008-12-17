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

import java.util.Map;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBeginDocument;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBeginResource;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentNeededResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentSuppliedResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentEndComments;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentEndOfFile;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentHiResBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentIncludeResource;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentLanguageLevel;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPage;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPageBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPageHiResBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPageResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentTitle;

/**
 * Factory for DSCComment subclasses.
 */
public class DSCCommentFactory {

    private static final Map DSC_FACTORIES = new java.util.HashMap();

    static {
        DSC_FACTORIES.put(DSCConstants.END_COMMENTS,
                DSCCommentEndComments.class);
        DSC_FACTORIES.put(DSCConstants.BEGIN_RESOURCE,
                DSCCommentBeginResource.class);
        DSC_FACTORIES.put(DSCConstants.INCLUDE_RESOURCE,
                DSCCommentIncludeResource.class);
        DSC_FACTORIES.put(DSCConstants.PAGE_RESOURCES,
                DSCCommentPageResources.class);
        DSC_FACTORIES.put(DSCConstants.BEGIN_DOCUMENT,
                DSCCommentBeginDocument.class);
        DSC_FACTORIES.put(DSCConstants.PAGE,
                DSCCommentPage.class);
        DSC_FACTORIES.put(DSCConstants.PAGES,
                DSCCommentPages.class);
        DSC_FACTORIES.put(DSCConstants.BBOX,
                DSCCommentBoundingBox.class);
        DSC_FACTORIES.put(DSCConstants.HIRES_BBOX,
                DSCCommentHiResBoundingBox.class);
        DSC_FACTORIES.put(DSCConstants.PAGE_BBOX,
                DSCCommentPageBoundingBox.class);
        DSC_FACTORIES.put(DSCConstants.PAGE_HIRES_BBOX,
                DSCCommentPageHiResBoundingBox.class);
        DSC_FACTORIES.put(DSCConstants.LANGUAGE_LEVEL,
                DSCCommentLanguageLevel.class);
        DSC_FACTORIES.put(DSCConstants.DOCUMENT_NEEDED_RESOURCES,
                DSCCommentDocumentNeededResources.class);
        DSC_FACTORIES.put(DSCConstants.DOCUMENT_SUPPLIED_RESOURCES,
                DSCCommentDocumentSuppliedResources.class);
        DSC_FACTORIES.put(DSCConstants.TITLE,
                DSCCommentTitle.class);
        DSC_FACTORIES.put(DSCConstants.EOF,
                DSCCommentEndOfFile.class);
        //TODO Add additional implementations as needed
    }

    /**
     * Creates and returns new instances for DSC comments with a given name.
     * @param name the name of the DSCComment (without the "%%" prefix)
     * @return the new instance or null if no particular subclass registered for the given
     *          DSC comment.
     */
    public static DSCComment createDSCCommentFor(String name) {
        Class clazz = (Class)DSC_FACTORIES.get(name);
        if (clazz == null) {
            return null;
        }
        try {
            return (DSCComment)clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Error instantiating instance for '" + name + "': "
                    + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access error while instantiating instance for '"
                    + name + "': " + e.getMessage());
        }
    }

}
