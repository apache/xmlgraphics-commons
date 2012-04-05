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
import java.awt.color.ColorSpace;

import junit.framework.TestCase;

public class ColorConverterTest extends TestCase {

    public void testToGray() throws Exception {
        ColorConverter converter = GrayScaleColorConverter.getInstance();
        Color rgb = new Color(255, 184, 0);
        Color gray = converter.convert(rgb);

        ColorSpaceOrigin origin = ColorSpaces.getColorSpaceOrigin(gray.getColorSpace());
        assertEquals("#CMYK", origin.getProfileName());
        assertNull(origin.getProfileURI());
        assertEquals(ColorSpace.TYPE_CMYK, gray.getColorSpace().getType());
        float[] comps = gray.getColorComponents(null);
        assertEquals(4, comps.length);
        assertEquals(0.0f, comps[0], 0.1f);
        assertEquals(0.0f, comps[1], 0.1f);
        assertEquals(0.0f, comps[2], 0.1f);
        assertEquals(0.273f, comps[3], 0.01f);
        assertEquals(0xFFB9B9B9, gray.getRGB());
    }

}
