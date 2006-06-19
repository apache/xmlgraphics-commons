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

package org.apache.xmlgraphics.xmp.merge;

import java.util.Map;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.XMPProperty;

/**
 * Represents a set of rules used to merge to XMP properties. By default, all properties are
 * merged by replacing any existing values with the value from the source XMP.
 */
public class MergeRuleSet {

    private Map rules = new java.util.HashMap();
    private PropertyMerger defaultMerger = new ReplacePropertyMerger();
    
    /** Main constructor. */
    public MergeRuleSet() {
    }

    /**
     * Returns the PropertyMerger that shall be used when merging the given property.
     * @param prop the property to be merged
     * @return the PropertyMerger to be used for merging the property
     */
    public PropertyMerger getPropertyMergerFor(XMPProperty prop) {
        PropertyMerger merger = (PropertyMerger)rules.get(prop.getName());
        return (merger != null ? merger : defaultMerger);
    }
    
    /**
     * Adds a merge rule to this set.
     * @param propName the name of the property
     * @param merger the property merger to be used for this property
     */
    public void addRule(QName propName, PropertyMerger merger) {
        rules.put(propName, merger);
    }
    
}
