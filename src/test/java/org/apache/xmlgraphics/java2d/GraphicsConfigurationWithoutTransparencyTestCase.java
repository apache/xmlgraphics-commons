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

package org.apache.xmlgraphics.java2d;

import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphicsConfigurationWithoutTransparencyTestCase {

    private ColorModel nonTransparencyColorModel;
    private GraphicsConfiguration sut;

    @Before
    public void setUp() {
        sut = new GraphicsConfigurationWithoutTransparency();
        nonTransparencyColorModel = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getColorModel();
    }

    @Test
    public void testGetNormalizationTransformation() {
        AffineTransform transform = new AffineTransform(2, 0, 0, 2, 0, 0);
        assertEquals(transform, sut.getNormalizingTransform());
    }

    void testImage(int width, int height, boolean hasTransparency, BufferedImage image) {
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(hasTransparency, image.getColorModel().hasAlpha());
    }

    @Test
    public void testCreateCompatibleImage() {
        testImage(1, 2, false, sut.createCompatibleImage(1, 2, Transparency.TRANSLUCENT));
        testImage(100, 90, false, sut.createCompatibleImage(100, 90, Transparency.TRANSLUCENT));
        testImage(1, 2, false, sut.createCompatibleImage(1, 2, Transparency.OPAQUE));
        testImage(1010, 2020, false, sut.createCompatibleImage(1010, 2020, Transparency.OPAQUE));

        // test the 2 argument overriden method
        testImage(1, 2, false, sut.createCompatibleImage(1, 2));
        testImage(1010, 2020, false, sut.createCompatibleImage(1010, 2020));
    }

    @Test
    public void testGetColorModel() {
        assertEquals(nonTransparencyColorModel, sut.getColorModel());

        assertEquals(nonTransparencyColorModel, sut.getColorModel(Transparency.TRANSLUCENT));
        assertEquals(nonTransparencyColorModel, sut.getColorModel(Transparency.OPAQUE));
    }
}
