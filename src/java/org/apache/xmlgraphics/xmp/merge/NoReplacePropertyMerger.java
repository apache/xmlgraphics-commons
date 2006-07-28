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

package org.apache.xmlgraphics.xmp.merge;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPProperty;

/**
 * A basic PropertyMerger which only sets a value in the target metadata if there's not already
 * another value.
 */
public class NoReplacePropertyMerger implements PropertyMerger {

    /**
     * @see org.apache.xmlgraphics.xmp.merge.PropertyMerger#merge(
     *          org.apache.xmlgraphics.xmp.XMPProperty, org.apache.xmlgraphics.xmp.Metadata)
     */
    public void merge(XMPProperty sourceProp, Metadata target) {
        XMPProperty prop = target.getProperty(sourceProp.getName());
        if (prop == null) {
            target.setProperty(sourceProp);
        }
    }

}
