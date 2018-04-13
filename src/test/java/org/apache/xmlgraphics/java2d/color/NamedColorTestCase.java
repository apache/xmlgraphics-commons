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

/**
 * Tests named color spaces (and the CIE Lab color space implementation).
 */
public class NamedColorTestCase {

    private static final float POSTGELB_X = 0.6763079f;
    private static final float POSTGELB_Y = 0.6263507f;
    private static final float POSTGELB_Z = 0.04217565f;

    @Test
    public void testNamedColorWithCIELab() {
        CIELabColorSpace lab = ColorSpaces.getCIELabColorSpaceD50();

        //CIE Lab definition of "Postgelb" (postal yellow) at D50 as defined by Swiss Post
        //Convert to XYZ
        float[] c1xyz = lab.toCIEXYZNative(83.25f, 16.45f, 96.89f);
        //Verify XYZ values are OK
        assertEquals(POSTGELB_X, c1xyz[0], 0.001f);
        assertEquals(POSTGELB_Y, c1xyz[1], 0.001f);
        assertEquals(POSTGELB_Z, c1xyz[2], 0.001f);

        //Build named color based on XYZ coordinates
        NamedColorSpace ncs = new NamedColorSpace("Postgelb", c1xyz);
        Color c1 = new Color(ncs, new float[] {1.0f}, 1.0f);

        assertEquals(ncs, c1.getColorSpace());
        float[] comp = c1.getColorComponents(null);
        assertEquals(1, comp.length);
        assertEquals(1.0f, comp[0], 0.001f);
        float[] xyz = ncs.toCIEXYZ(new float[] {1.0f});
        for (int i = 0; i < 3; i++) {
            assertEquals(c1xyz[i], xyz[i], 0.001f);
        }

        //NOTE: Allowing for some fuzziness due to differences in XYZ->sRGB calculation between
        //Java 1.5 and 6.
        assertEquals(254, c1.getRed(), 1f);
        assertEquals(195, c1.getGreen(), 2f);
        assertEquals(0, c1.getBlue());
    }

    @Test
    public void testEquals() {
        NamedColorSpace ncs1 = new NamedColorSpace("Postgelb",
                new float[] {POSTGELB_X, POSTGELB_Y, POSTGELB_Z});

        NamedColorSpace ncs2 = new NamedColorSpace("Postgelb",
                new float[] {POSTGELB_X, POSTGELB_Y, POSTGELB_Z});

        assertEquals(ncs1, ncs2);

        //Construct the same NamedColorSpace via two different methods
        CIELabColorSpace lab = ColorSpaces.getCIELabColorSpaceD50();
        Color postgelbLab = lab.toColor(83.25f, 16.45f, 96.89f, 1.0f);
        float[] xyz = lab.toCIEXYZ(postgelbLab.getColorComponents(null));
        xyz[0] = POSTGELB_X;
        xyz[1] = POSTGELB_Y;
        xyz[2] = POSTGELB_Z;
        ncs1 = new NamedColorSpace("Postgelb", postgelbLab);
        ncs2 = new NamedColorSpace("Postgelb", xyz);
        assertEquals(ncs1, ncs2);

        //Compare with a similar color coming from sRGB
        Color rgb = new Color(255, 184, 0);
        ncs2 = new NamedColorSpace("PostgelbFromRGB", rgb);
        assertFalse(ncs1.equals(ncs2));
    }
}
