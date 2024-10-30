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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;

public class ImageConverterG2D2BitmapTestCase {
    @Test
    public void testConvert() {
        ImageInfo info = new ImageInfo(null, null);
        ImageSize imageSize = new ImageSize(100, 100, 72);
        imageSize.calcSizeFromPixels();
        info.setSize(imageSize);
        HashMap<String, Object> hints = new HashMap<>();
        hints.put(ImageProcessingHints.TRANSPARENCY_INTENT, ImageProcessingHints.TRANSPARENCY_INTENT_IGNORE);
        hints.put("CMYK", true);
        ImageBuffered image = (ImageBuffered) new ImageConverterG2D2Bitmap().convert(
                new ImageGraphics2D(info, new EmptyGraphics2DImagePainter()), hints);
        Assert.assertEquals(image.getBufferedImage().getColorModel().getNumColorComponents(), 4);
        Assert.assertEquals(image.getColorSpace().getClass(), DeviceCMYKColorSpace.class);
    }

    private static class EmptyGraphics2DImagePainter implements Graphics2DImagePainter {
        public void paint(Graphics2D g2d, Rectangle2D area) {
        }

        public Dimension getImageSize() {
            return null;
        }
    }
}
