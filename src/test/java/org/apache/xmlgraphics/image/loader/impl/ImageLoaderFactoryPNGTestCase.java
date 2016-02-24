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

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.util.MimeConstants;

public class ImageLoaderFactoryPNGTestCase {

    private ImageLoaderFactoryPNG ilfpng = new ImageLoaderFactoryPNG();

    @Test
    public void testGetSupportedMIMETypes() {
        assertArrayEquals(new String[] {MimeConstants.MIME_PNG}, ilfpng.getSupportedMIMETypes());
    }

    @Test
    public void testGetSupportedFlavors() {
        assertArrayEquals(new ImageFlavor[] {ImageFlavor.RENDERED_IMAGE},
                ilfpng.getSupportedFlavors(MimeConstants.MIME_PNG));
        try {
            ilfpng.getSupportedFlavors(MimeConstants.MIME_JPEG);
            fail("An exception should have been thrown above....");
        } catch (IllegalArgumentException e) {
            // do nothing; this is expected
        }
    }

    @Test
    public void testNewImageLoader() {
        ImageLoader il = ilfpng.newImageLoader(ImageFlavor.RENDERED_IMAGE);
        assertTrue(il instanceof ImageLoaderPNG);
    }

    @Test
    public void testIsAvailable() {
        assertTrue(ilfpng.isAvailable());
    }

}
