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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Represents a %%BoundingBox DSC comment.
 */
public class DSCCommentBoundingBox extends AbstractDSCComment {

    private Rectangle2D bbox;
    
    /**
     * Creates a new instance.
     */
    public DSCCommentBoundingBox() {
    }

    /**
     * Creates a new instance.
     * @param bbox the bounding box
     */
    public DSCCommentBoundingBox(Rectangle2D bbox) {
        setBoundingBox(bbox);
    }
    
    /**
     * Returns the bounding box.
     * @return the bounding box
     */
    public Rectangle2D getBoundingBox() {
        return this.bbox;
    }
    
    /**
     * Sets the bounding box.
     * @param name the bounding box
     */
    public void setBoundingBox(Rectangle2D bbox) {
        this.bbox = bbox;
    }

    /** {@inheritDoc} */
    public String getName() {
        return DSCConstants.BBOX;
    }

    /** {@inheritDoc} */
    public boolean hasValues() {
        return true;
    }

    /** {@inheritDoc} */
    public void parseValue(String value) {
        List params = splitParams(value);
        Iterator iter = params.iterator();

        double x1 = Double.parseDouble((String)iter.next());
        double y1 = Double.parseDouble((String)iter.next());
        double x2 = Double.parseDouble((String)iter.next());
        double y2 = Double.parseDouble((String)iter.next());
        this.bbox = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }
    
    /** {@inheritDoc} */
    public void generate(PSGenerator gen) throws IOException {
        if (getBoundingBox() != null) {
            gen.writeDSCComment(getName(), new Object[] {
                new Integer((int)Math.round(this.bbox.getX())),
                new Integer((int)Math.round(this.bbox.getY())),
                new Integer((int)Math.round(this.bbox.getX() + this.bbox.getWidth())),
                new Integer((int)Math.round(this.bbox.getY() + this.bbox.getHeight()))});
        } else {
            gen.writeDSCComment(getName(), DSCConstants.ATEND);
        }
    }

}
