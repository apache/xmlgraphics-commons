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

package org.apache.xmlgraphics.image.loader.impl;

import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.MockImageSessionContext;
import org.apache.xmlgraphics.util.MimeConstants;

public class PNGFileTestCase implements PNGConstants {

    @Test
    public void testColorTypeTwoPNG() throws ImageException, IOException {
        testColorTypePNG("basn2c08.png", PNG_COLOR_RGB);
    }

    @Test
    public void testColorTypeZeroPNG() throws ImageException, IOException {
        testColorTypePNG("basn0g08.png", PNG_COLOR_GRAY);
    }

    @Test
    public void testColorTypeSixPNG() throws ImageException, IOException {
        testColorTypePNG("basn6a08.png", PNG_COLOR_RGB_ALPHA);
    }

    @Test
    public void testColorTypeThreePNG() throws ImageException, IOException {
        testColorTypePNG("basn3p08.png", PNG_COLOR_PALETTE);
    }

    @Test
    public void testColorTypeFourPNG() throws ImageException, IOException {
        testColorTypePNG("basn4a08.png", PNG_COLOR_GRAY_ALPHA);
    }

    @Test
    public void testTransparentPNG() throws ImageException, IOException {
        testColorTypePNG("tbbn3p08.png", PNG_COLOR_PALETTE, true);
        testColorTypePNG("tbrn2c08.png", PNG_COLOR_RGB, true);
    }

    @Test
    public void testCorruptPNG() {
        ImageContext context = MockImageContext.newSafeInstance();
        ImageSessionContext session = new MockImageSessionContext(context);
        ImageInfo info = new ImageInfo("corrupt-image.png", MimeConstants.MIME_PNG);
        ImageLoaderRawPNG ilrpng = new ImageLoaderRawPNG();
        String exception = "";
        try {
            ilrpng.loadImage(info, null, session);
        } catch (Exception e) {
            exception = e.getCause().getMessage();
        }
        assertEquals("PNG unknown critical chunk: IBLA", exception);
    }

    private void testColorTypePNG(String imageName, int colorType) throws ImageException, IOException {
        testColorTypePNG(imageName, colorType, false);
    }

    private void testColorTypePNG(String imageName, int colorType, boolean isTransparent)
            throws ImageException, IOException {
        ImageContext context = MockImageContext.newSafeInstance();
        ImageSessionContext session = new MockImageSessionContext(context);
        ImageInfo info = new ImageInfo(imageName, MimeConstants.MIME_PNG);
        ImageLoaderRawPNG ilrpng = new ImageLoaderRawPNG();
        ImageRawPNG irpng = (ImageRawPNG) ilrpng.loadImage(info, null, session);
        ColorModel cm = irpng.getColorModel();
        if (colorType == PNG_COLOR_PALETTE) {
            assertTrue(cm instanceof IndexColorModel);
        } else {
            assertTrue(cm instanceof ComponentColorModel);
            int numComponents = 3;
            if (colorType == PNG_COLOR_GRAY) {
                numComponents = 1;
            } else if (colorType == PNG_COLOR_GRAY_ALPHA) {
                numComponents = 2;
            } else if (colorType == PNG_COLOR_RGB_ALPHA) {
                numComponents = 4;
            }
            assertEquals(numComponents, cm.getNumComponents());
        }
        if (isTransparent) {
            assertTrue(irpng.isTransparent());
        }
    }
}
