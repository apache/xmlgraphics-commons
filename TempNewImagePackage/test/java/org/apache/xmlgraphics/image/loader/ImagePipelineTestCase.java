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

import java.awt.Dimension;
import java.io.File;

import junit.framework.TestCase;

import org.apache.xmlgraphics.image.loader.impl.ImageConverterBuffered2Rendered;
import org.apache.xmlgraphics.image.loader.impl.ImageConverterG2D2Bitmap;
import org.apache.xmlgraphics.image.loader.impl.ImageConverterRendered2PNG;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.imageio.ImageLoaderImageIO;
import org.apache.xmlgraphics.image.loader.pipeline.ImageProviderPipeline;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Tests for the image pipeline functionality.
 */
public class ImagePipelineTestCase extends TestCase {

    private MockImageContext imageContext = MockImageContext.getInstance();
    
    public ImagePipelineTestCase(String name) {
        super(name);
    }
    
    public void testPipelineWithLoader() throws Exception {
        String uri = "bgimg72dpi.gif";

        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();
        
        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);
        
        ImageLoader loader = new ImageLoaderImageIO(ImageFlavor.RENDERED_IMAGE);
        ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), loader);
        pipeline.addConverter(new ImageConverterRendered2PNG());
        
        Image img = pipeline.execute(info, null, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
    public void testPipelineWithoutLoader() throws Exception {

        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        Image original = createG2DImage();
        
        ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), null);
        pipeline.addConverter(new ImageConverterG2D2Bitmap());
        pipeline.addConverter(new ImageConverterBuffered2Rendered());
        pipeline.addConverter(new ImageConverterRendered2PNG());
        
        Image img = pipeline.execute(original.getInfo(), original, null,
                sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
        
        ((ImageRawStream)img).writeTo(new File("D:/out.png"));
    }
    
    private Image createG2DImage() {
        Graphics2DImagePainter painter = new DemoPainter();
        Dimension dim = painter.getImageSize();
        
        ImageSize size = new ImageSize();
        size.setSizeInMillipoints(dim.width, dim.height);
        size.setResolution(72);
        size.calcPixelsFromSize();
        
        ImageInfo info = new ImageInfo(null /*null is the intention here*/, null);
        info.setSize(size);
        ImageGraphics2D g2dImage = new ImageGraphics2D(info, painter);
        return g2dImage;
    }

    public void testPipelineFromURIThroughManager() throws Exception {
        String uri = "asf-logo.png";
        
        ImageSessionContext sessionContext = imageContext.newSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);

        ImageFlavor[] flavors = new ImageFlavor[] {
                ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
        };
        Image img = manager.getImage(info, flavors, sessionContext);
        
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
    public void testPipelineWithoutURIThroughManager() throws Exception {
        ImageManager manager = imageContext.getImageManager();
        
        Image original = createG2DImage();

        ImageFlavor[] flavors = new ImageFlavor[] {
                ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
        };
        Image img = manager.convertImage(original, flavors);
        
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
}
