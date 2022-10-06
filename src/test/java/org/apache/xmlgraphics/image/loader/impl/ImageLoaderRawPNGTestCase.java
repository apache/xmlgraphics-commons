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

import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSource;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.MockImageSessionContext;
import org.apache.xmlgraphics.util.MimeConstants;

public class ImageLoaderRawPNGTestCase {

    private ImageLoaderRawPNG ilrpng = new ImageLoaderRawPNG();

    @Test
    public void testGetUsagePenalty() {
        assertEquals(0, ilrpng.getUsagePenalty());
    }

    @Test
    public void testLoadImageBadMime() throws ImageException, IOException {
        ImageContext context = MockImageContext.newSafeInstance();
        ImageSessionContext session = new MockImageSessionContext(context);
        ImageInfo info = new ImageInfo("basn2c08.png", MimeConstants.MIME_JPEG);
        try {
            ImageRawPNG irpng = (ImageRawPNG) ilrpng.loadImage(info, null, session);
            fail("An exception should have been thrown above");
        } catch (IllegalArgumentException e) {
            // do nothing; this was expected
        }
    }

    @Test
    public void testGetTargetFlavor() {
        assertEquals(ImageFlavor.RAW_PNG, ilrpng.getTargetFlavor());
    }

    @Test
    public void testLoadImageGoodMime() throws ImageException, IOException {
        ImageContext context = MockImageContext.newSafeInstance();
        ImageSessionContext session = new MockImageSessionContext(context);
        ImageInfo info = new ImageInfo("basn2c08.png", MimeConstants.MIME_PNG);
        Image im = ilrpng.loadImage(info, null, session);
        assertTrue(im instanceof ImageRawPNG);
    }

    @Test
    public void testPreloaderRawPNG() throws IOException, ImageException {
        ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream("test/images/tbbn3p08.png"));
        ImageContext context = MockImageContext.newSafeInstance();
        ImageInfo imageInfo = new PreloaderRawPNG().preloadImage(null, new ImageSource(iis, null, true), context);
        assertEquals(imageInfo.getMimeType(), "image/png");
        assertEquals(imageInfo.getSize().getWidthPx(), 32);
    }
}
