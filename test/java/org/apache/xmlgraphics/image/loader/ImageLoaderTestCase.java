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

package org.apache.xmlgraphics.image.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

/**
 * Tests for bundled ImageLoader implementations.
 */
public class ImageLoaderTestCase extends TestCase {

    private MockImageContext imageContext = MockImageContext.getInstance();
    
    public ImageLoaderTestCase(String name) {
        super(name);
    }
    
    public void testPNG() throws Exception {
        String uri = "asf-logo.png";
        
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        
        Image img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        assertEquals(169, imgRed.getRenderedImage().getWidth());
        assertEquals(51, imgRed.getRenderedImage().getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(126734, info.getSize().getWidthMpt());
        assertEquals(38245, info.getSize().getHeightMpt());
    }
    
    public void testGIF() throws Exception {
        String uri = "bgimg72dpi.gif";
        
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        
        Image img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        assertEquals(192, imgRed.getRenderedImage().getWidth());
        assertEquals(192, imgRed.getRenderedImage().getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(192000, info.getSize().getWidthMpt());
        assertEquals(192000, info.getSize().getHeightMpt());
    }
    
    public void testEPSASCII() throws Exception {
        String uri = "barcode.eps";
        
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        
        Image img = manager.getImage(info, ImageFlavor.RAW_EPS, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_EPS, img.getFlavor());
        ImageRawStream imgEPS = (ImageRawStream)img;
        InputStream in = imgEPS.createInputStream();
        try {
            assertNotNull(in);
            Reader reader = new InputStreamReader(in, "US-ASCII");
            char[] c = new char[4];
            reader.read(c);
            if (!("%!PS".equals(new String(c)))) {
                fail("EPS header expected");
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
 
    public void testEPSBinary() throws Exception {
        String uri = "img-with-tiff-preview.eps";
        
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        
        Image img = manager.getImage(info, ImageFlavor.RAW_EPS, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_EPS, img.getFlavor());
        ImageRawStream imgEPS = (ImageRawStream)img;
        InputStream in = imgEPS.createInputStream();
        try {
            assertNotNull(in);
            Reader reader = new InputStreamReader(in, "US-ASCII");
            char[] c = new char[4];
            reader.read(c);
            if (!("%!PS".equals(new String(c)))) {
                fail("EPS header expected");
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
 
}
