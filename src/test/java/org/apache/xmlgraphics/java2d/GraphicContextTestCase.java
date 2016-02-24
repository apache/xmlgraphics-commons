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

package org.apache.xmlgraphics.java2d;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphicContextTestCase {

    @Test
    public void testSetPaint() {
        GraphicContext gc = new GraphicContext();
        Color red = Color.RED;
        gc.setPaint(red);
        assertEquals(red, gc.getColor());
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(50, 50);
        GradientPaint gp = new GradientPaint(start, Color.RED, end, Color.BLUE);
        gc.setPaint(gp);
        assertEquals(Color.BLACK, gc.getColor());
    }
}
