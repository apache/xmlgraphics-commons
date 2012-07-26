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

package org.apache.xmlgraphics.image.loader.cache;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;

/**
 * Tests for bundled ImageLoader implementations.
 */
public class ImageCacheTestCase {

    private static final boolean DEBUG = false;

    private MockImageContext imageContext = MockImageContext.getInstance();
    private ImageSessionContext sessionContext = imageContext.newSessionContext();
    private ImageManager manager = imageContext.getImageManager();
    private ImageCacheStatistics statistics = (DEBUG
                ? new ImageCacheLoggingStatistics(true) : new ImageCacheStatistics(true));

    /** {@inheritDoc} */
    @Before
    public void setUp() throws Exception {
        manager.getCache().clearCache();
        statistics.reset();
        manager.getCache().setCacheListener(statistics);
    }

    /**
     * Tests the ImageInfo cache.
     * @throws Exception if an error occurs
     */
    @Test
    public void testImageInfoCache() throws Exception {
        String invalid1 = "invalid1.jpg";
        String invalid2 = "invalid2.jpg";
        String valid1 = "bgimg300dpi.bmp";
        String valid2 = "big-image.png";


        ImageInfo info1;
        ImageInfo info2;
        info1 = manager.getImageInfo(valid1, sessionContext);
        assertNotNull(info1);
        assertEquals(valid1, info1.getOriginalURI());

        try {
            manager.getImageInfo(invalid1, sessionContext);
            fail("Expected FileNotFoundException for invalid URI");
        } catch (FileNotFoundException e) {
            //expected
        }

        //2 requests:
        assertEquals(0, statistics.getImageInfoCacheHits());
        assertEquals(2, statistics.getImageInfoCacheMisses());
        assertEquals(0, statistics.getInvalidHits());
        statistics.reset();

        //Cache Hit
        info1 = manager.getImageInfo(valid1, sessionContext);
        assertNotNull(info1);
        assertEquals(valid1, info1.getOriginalURI());

        //Cache Miss
        info2 = manager.getImageInfo(valid2, sessionContext);
        assertNotNull(info2);
        assertEquals(valid2, info2.getOriginalURI());

        try {
            //Invalid Hit
            manager.getImageInfo(invalid1, sessionContext);
            fail("Expected FileNotFoundException for invalid URI");
        } catch (FileNotFoundException e) {
            //expected
        }
        try {
            //Invalid (Cache Miss)
            manager.getImageInfo(invalid2, sessionContext);
            fail("Expected FileNotFoundException for invalid URI");
        } catch (FileNotFoundException e) {
            //expected
        }

        //4 requests:
        assertEquals(1, statistics.getImageInfoCacheHits());
        assertEquals(2, statistics.getImageInfoCacheMisses());
        assertEquals(1, statistics.getInvalidHits());
        statistics.reset();
    }

    @Test
    public void testInvalidURIExpiration() throws Exception {
        MockTimeStampProvider provider = new MockTimeStampProvider();
        ImageCache cache = new ImageCache(provider, new DefaultExpirationPolicy(2));
        cache.setCacheListener(statistics);

        String invalid1 = "invalid1.jpg";
        String invalid2 = "invalid2.jpg";
        String valid1 = "valid1.jpg";

        provider.setTimeStamp(1000);
        cache.registerInvalidURI(invalid1);
        provider.setTimeStamp(1100);
        cache.registerInvalidURI(invalid2);

        assertEquals(0, statistics.getInvalidHits());

        //not expired, yet
        provider.setTimeStamp(1200);
        assertFalse(cache.isInvalidURI(valid1));
        assertTrue(cache.isInvalidURI(invalid1));
        assertTrue(cache.isInvalidURI(invalid2));
        assertEquals(2, statistics.getInvalidHits());

        //first expiration time reached
        provider.setTimeStamp(3050);
        assertFalse(cache.isInvalidURI(valid1));
        assertFalse(cache.isInvalidURI(invalid1));
        assertTrue(cache.isInvalidURI(invalid2));
        assertEquals(3, statistics.getInvalidHits());

        //second expiration time reached
        provider.setTimeStamp(3200);
        assertFalse(cache.isInvalidURI(valid1));
        assertFalse(cache.isInvalidURI(invalid1));
        assertFalse(cache.isInvalidURI(invalid2));
        assertEquals(3, statistics.getInvalidHits());
    }

    /**
     * Tests the image cache reusing a cacheable Image created by the ImageLoader.
     * @throws Exception if an error occurs
     */
    @Test
    public void testImageCache1() throws Exception {
        String valid1 = "bgimg72dpi.gif";

        ImageInfo info = manager.getImageInfo(valid1, sessionContext);
        assertNotNull(info);

        ImageBuffered img1 = (ImageBuffered) manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        assertNotNull(img1);
        assertNotNull(img1.getBufferedImage());

        ImageBuffered img2 = (ImageBuffered) manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        //ImageBuffered does not have to be the same instance but we want at least the
        //BufferedImage to be reused.
        assertTrue("BufferedImage must be reused",
                img1.getBufferedImage() == img2.getBufferedImage());

        assertEquals(1, statistics.getImageCacheHits());
        assertEquals(1, statistics.getImageCacheMisses());
    }


    /**
     * Test to check if doInvalidURIHouseKeeping() throws a
     * ConcurrentModificationException.
     */
    @Test
    public void testImageCacheHouseKeeping() {
        ImageCache imageCache = new ImageCache(new TimeStampProvider(),
                new DefaultExpirationPolicy(1));
        imageCache.registerInvalidURI("invalid");
        imageCache.registerInvalidURI("invalid2");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        imageCache.doHouseKeeping();
    }
}
