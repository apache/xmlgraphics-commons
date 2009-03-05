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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import junit.framework.TestCase;

import org.apache.xmlgraphics.ps.dsc.DSCCommentFactory;

public class DSCCommentBoundingBoxTestCase extends TestCase {

    public void testBoundingBox() throws Exception {
        DSCComment comment = DSCCommentFactory.createDSCCommentFor("BoundingBox");
        DSCCommentBoundingBox bbox = (DSCCommentBoundingBox)comment;
        bbox.parseValue("289 412 306 429");
        Rectangle refRect = new Rectangle(289, 412, 306 - 289, 429 - 412);
        assertEquals(refRect, bbox.getBoundingBox());

        comment = DSCCommentFactory.createDSCCommentFor("BoundingBox");
        bbox = (DSCCommentBoundingBox)comment;
        bbox.parseValue("289.12 412.2 306.777 429.11");
        Rectangle2D refRect2D = new Rectangle2D.Double(
                289.12, 412.2, 306.777 - 289.12, 429.11 - 412.2);
        assertEquals(refRect2D, bbox.getBoundingBox());

        comment = DSCCommentFactory.createDSCCommentFor("HiResBoundingBox");
        bbox = (DSCCommentHiResBoundingBox)comment;
        bbox.parseValue("289.12 412.2 306.777 429.11");
        refRect2D = new Rectangle2D.Double(
                289.12, 412.2, 306.777 - 289.12, 429.11 - 412.2);
        assertEquals(refRect2D, bbox.getBoundingBox());
    }

}
