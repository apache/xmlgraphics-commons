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

import java.awt.geom.Rectangle2D;

import org.apache.xmlgraphics.ps.DSCConstants;

/**
 * Represents a %%PageHiResBoundingBox DSC comment.
 */
public class DSCCommentPageHiResBoundingBox extends DSCCommentHiResBoundingBox {

    /**
     * Creates a new instance.
     */
    public DSCCommentPageHiResBoundingBox() {
        super();
    }

    /**
     * Creates a new instance.
     * @param bbox the bounding box
     */
    public DSCCommentPageHiResBoundingBox(Rectangle2D bbox) {
        super(bbox);
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return DSCConstants.PAGE_HIRES_BBOX;
    }

}
