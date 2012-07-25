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

package org.apache.xmlgraphics.java2d.color;

import java.awt.Color;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ColorWithAlternatives} class.
 */
public class ColorWithAlternativesTestCase {

    @Test
    public void testEquals() throws Exception {
        Color col1 = new ColorWithAlternatives(255, 204, 0, null);
        Color col2 = new Color(255, 204, 0);

        assertEquals(col1, col2);
        assertEquals(col2, col1);

        CIELabColorSpace lab = ColorSpaces.getCIELabColorSpaceD50();
        Color postgelbLab = lab.toColor(83.25f, 16.45f, 96.89f, 1.0f);
        col1 = new ColorWithAlternatives(255, 204, 0, new Color[] {postgelbLab});

        //java.awt.Color tests on the sRGB value only
        assertEquals(col1, col2);
        assertEquals(col2, col1);
    }

    @Test
    public void testSameColor() throws Exception {
        Color col1 = new ColorWithAlternatives(255, 204, 0, null);
        Color col2 = new Color(255, 204, 0);

        //No alternatives. Only sRGB counts.
        assertTrue(ColorUtil.isSameColor(col1, col2));

        CIELabColorSpace lab = ColorSpaces.getCIELabColorSpaceD50();
        Color postgelbLab = lab.toColor(83.25f, 16.45f, 96.89f, 1.0f);
        col1 = new ColorWithAlternatives(255, 204, 0, new Color[] {postgelbLab});

        //Same sRGB value but one color with alternatives:
        assertFalse(ColorUtil.isSameColor(col1, col2));

        //Once the spotcolor naked and once as part of a color with alternatives
        assertFalse(ColorUtil.isSameColor(postgelbLab, col1));

        //sRGB values is calculated from Lab color and doesn't exactly match the selected fallback
        assertFalse(postgelbLab.equals(col1));
    }
}
