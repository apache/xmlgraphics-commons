/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.xmlgraphics.xmp.schemas;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPSchema;
import org.apache.xmlgraphics.xmp.merge.ArrayAddPropertyMerger;
import org.apache.xmlgraphics.xmp.merge.MergeRuleSet;

/**
 * Schema class for Dublin Core.
 */
public class DublinCoreSchema extends XMPSchema {

    /** Namespace URI for Dublin Core */ 
    public static final String NAMESPACE = XMPConstants.DUBLIN_CORE_NAMESPACE;

    private static MergeRuleSet dcMergeRuleSet;
    
    static {
        dcMergeRuleSet = new MergeRuleSet();
        //Dates are added up not replaced
        dcMergeRuleSet.addRule(new QName(NAMESPACE, "date"), new ArrayAddPropertyMerger());
    }
    
    /** Creates a new schema instance for Dublin Core. */
    public DublinCoreSchema() {
        super(NAMESPACE, "dc");
    }
    
    /**
     * Creates and returns an adapter for this schema around the given metadata object.
     * @param meta the metadata object
     * @return the newly instantiated adapter
     */
    public static DublinCoreAdapter getAdapter(Metadata meta) {
        return new DublinCoreAdapter(meta);
    }

    /** @see org.apache.xmlgraphics.xmp.XMPSchema#getDefaultMergeRuleSet() */
    public MergeRuleSet getDefaultMergeRuleSet() {
        return dcMergeRuleSet;
    }
    
}
