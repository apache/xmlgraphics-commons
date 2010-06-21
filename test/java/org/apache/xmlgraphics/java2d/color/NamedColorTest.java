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

import junit.framework.TestCase;

/**
 * Tests named color spaces (and the CIE Lab color space implementation).
 */
public class NamedColorTest extends TestCase {

    public void testNamedColorWithCIELab() throws Exception {
        CIELabColorSpace lab = new CIELabColorSpace(CIELabColorSpace.getD50WhitePoint());

        //CIE Lab definition of "Postgelb" (postal yellow) at D50 as defined by Swiss Post
        float[] c1lab = new float[] {83.25f, 16.45f, 96.89f};

        //Convert to XYZ
        float[] c1xyz = lab.toCIEXYZ(c1lab);
        //Verify XYZ values are OK
        assertEquals(0.67631, c1xyz[0], 0.001f);
        assertEquals(0.62634, c1xyz[1], 0.001f);
        assertEquals(0.042191, c1xyz[2], 0.001f);

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

        assertEquals(254, c1.getRed());
        assertEquals(195, c1.getGreen());
        assertEquals(0, c1.getBlue());
    }


}
