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

import junit.framework.TestCase;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;

/**
 * Tests for bundled ImageLoader implementations.
 */
public class ImageCacheTestCase extends TestCase {

    private MockImageContext imageContext = MockImageContext.getInstance();
    
    /**
     * Tests the ImageInfo cache.
     * @throws Exception if an error occurs
     */
    public void testImageInfoCache() throws Exception {

        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();
        
        String invalid1 = "invalid1.jpg";
        String invalid2 = "invalid2.jpg";
        String valid1 = "bgimg300dpi.bmp";
        String valid2 = "big-image.png";
        
        ImageCacheStatistics statistics = new ImageCacheLoggingStatistics(true);
        manager.getCache().setCacheListener(statistics);
        
        ImageInfo info1, info2;
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

    /**
     * Tests the image cache reusing a cacheable Image created by the ImageLoader.
     * @throws Exception if an error occurs
     */
    public void testImageCache1() throws Exception {
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();
        
        String valid1 = "bgimg72dpi.gif";
        
        ImageCacheStatistics statistics = new ImageCacheLoggingStatistics(true);
        manager.getCache().setCacheListener(statistics);
        
        ImageInfo info = manager.getImageInfo(valid1, sessionContext);
        assertNotNull(info);
        
        ImageBuffered img1 = (ImageBuffered)manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        assertNotNull(img1);
        assertNotNull(img1.getBufferedImage());
        
        ImageBuffered img2 = (ImageBuffered)manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        //ImageBuffered does not have to be the same instance but we want at least the
        //BufferedImage to be reused.
        assertTrue("BufferedImage must be reused",
                img1.getBufferedImage() == img2.getBufferedImage());
        
        assertEquals(1, statistics.getImageCacheHits());
        assertEquals(1, statistics.getImageCacheMisses());
    }
    
    /**
     * Tests the image cache reusing a cacheable Image created by one of the ImageConverters in
     * a converter pipeline.
     * @throws Exception if an error occurs
     */
    public void DISABLEDtestImageCache2() throws Exception {
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();
        
        String valid1 = "test/resources/images/img-w-size.svg";
        
        ImageCacheStatistics statistics = new ImageCacheLoggingStatistics(true);
        manager.getCache().setCacheListener(statistics);
        
        ImageInfo info = manager.getImageInfo(valid1, sessionContext);
        assertNotNull(info);
        
        ImageBuffered img1 = (ImageBuffered)manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        assertNotNull(img1);
        assertNotNull(img1.getBufferedImage());
        
        ImageBuffered img2 = (ImageBuffered)manager.getImage(
                info, ImageFlavor.BUFFERED_IMAGE, sessionContext);
        //ImageBuffered does not have to be the same instance but we want at least the
        //BufferedImage to be reused.
        assertTrue("BufferedImage must be reused",
                img1.getBufferedImage() == img2.getBufferedImage());

        assertEquals(1, statistics.getImageCacheHits()); //1=BufferedImage
        assertEquals(3, statistics.getImageCacheMisses()); //3=BufferedImage,Graphics2DImage,DOM
    }
    
}
