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

import java.awt.color.ICC_Profile;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory;

/**
 * Tests for bundled ImageLoader implementations.
 */
public class ImageLoaderTestCase extends TestCase {

    private MockImageContext imageContext = MockImageContext.getInstance();

    public ImageLoaderTestCase(String name) {
        super(name);
    }

    private MyImageSessionContext createImageSessionContext() {
        return new MyImageSessionContext(imageContext);
    }

    public void testPNG() throws Exception {
        String uri = "asf-logo.png";

        MyImageSessionContext sessionContext = createImageSessionContext();
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

        sessionContext.checkAllStreamsClosed();
    }

    public void testGIF() throws Exception {
        String uri = "bgimg72dpi.gif";

        MyImageSessionContext sessionContext = createImageSessionContext();
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

        sessionContext.checkAllStreamsClosed();
    }

    public void testEPSASCII() throws Exception {
        String uri = "barcode.eps";

        MyImageSessionContext sessionContext = createImageSessionContext();
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

        sessionContext.checkAllStreamsClosed();
    }

    public void testEPSBinary() throws Exception {
        String uri = "img-with-tiff-preview.eps";

        MyImageSessionContext sessionContext = createImageSessionContext();
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

        sessionContext.checkAllStreamsClosed();
    }

    public void testICCProfiles() throws Exception {
        MyImageSessionContext sessionContext = createImageSessionContext();
        List/* <ICC_Profile> */profiles = new ArrayList();

        runReaders(profiles, sessionContext, "iccTest.png", "image/png",
                ImageFlavor.RAW_PNG);
        runReaders(profiles, sessionContext, "iccTest.jpg", "image/jpeg",
                ImageFlavor.RAW_JPEG);

        ICC_Profile first = (ICC_Profile) profiles.get(0);
        byte[] firstData = first.getData();
        for (int i = 1; i < profiles.size(); i++) {
            ICC_Profile icc = (ICC_Profile) profiles.get(i);
            byte[] data = icc.getData();
            assertEquals("Embedded ICC Profiles are not the same size!",
                    firstData.length, data.length);
            for (int j = 0; j < firstData.length; j++) {
                assertEquals("Embedded ICC Profiles differ at index " + j,
                        firstData[j], data[j]);
            }
        }
    }
    
    private void runReaders(List profiles, ImageSessionContext isc, String uri,
            String mime, ImageFlavor rawFlavor) throws Exception {
        ImageLoaderFactory ilfs[] = ImageImplRegistry.getDefaultInstance()
                .getImageLoaderFactories(mime);
        if (ilfs != null)
            for (int i = 0; i < ilfs.length; i++) {
                ImageLoaderFactory ilf = ilfs[i];
                try {
                    final ImageLoader il = ilf.newImageLoader(rawFlavor);
                    final ImageInfo im = new ImageInfo(uri, mime);
                    final Image img = il.loadImage(im, isc);
                    final ICC_Profile icc = img.getICCProfile();
                    // Assume the profile can only be correct if the image could
                    // actually be interpreted.
                    if (img.getColorSpace() != null) {
                        profiles.add(icc);
                    }
                } catch (IllegalArgumentException e) {
                    // Ignore. This imageLoader does not support RAW
                }
                try {
                    final ImageLoader il = ilf
                            .newImageLoader(ImageFlavor.BUFFERED_IMAGE);
                    final ImageInfo im = new ImageInfo(uri, mime);
                    final Image img = il.loadImage(im, isc);
                    final ICC_Profile icc = img.getICCProfile();
                    profiles.add(icc);
                } catch (IllegalArgumentException e) {
                    // Ignore. This imageLoader does not support Buffered.
                }
            }
    }

    public void testBrokenIccPng() throws Exception {
        String uri = "corrupt-icc.png";

        MyImageSessionContext sessionContext = createImageSessionContext();
        ImageManager manager = imageContext.getImageManager();

        ImageInfo info = manager.preloadImage(uri, sessionContext);
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE, sessionContext);
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        assertEquals(400, imgRed.getRenderedImage().getWidth());
        assertEquals(300, imgRed.getRenderedImage().getHeight());

        sessionContext.checkAllStreamsClosed();
    }

    private static class MyImageSessionContext extends MockImageSessionContext {

        private List streams = new java.util.ArrayList();

        public MyImageSessionContext(ImageContext context) {
            super(context);
        }

        public Source newSource(String uri) {
            Source src = super.newSource(uri);
            if (src instanceof ImageSource) {
                ImageSource is = (ImageSource)src;
                ImageInputStream in = is.getImageInputStream();
                //in = new ObservableImageInputStream(in, is.getSystemId());
                in = ObservableStream.Factory.observe(in, is.getSystemId());
                streams.add(in);
                is.setImageInputStream(in);
            }
            return src;
        }

        /** {@inheritDoc} */
        protected Source resolveURI(String uri) {
            Source src = super.resolveURI(uri);
            if (src instanceof StreamSource) {
                StreamSource ss = (StreamSource)src;
                if (ss.getInputStream() != null) {
                    InputStream in = new ObservableInputStream(
                            ss.getInputStream(), ss.getSystemId());
                    streams.add(in);
                    ss.setInputStream(in);
                }
            }
            return src;
        }

        public void checkAllStreamsClosed() {
            Iterator iter = streams.iterator();
            while (iter.hasNext()) {
                ObservableStream stream = (ObservableStream)iter.next();
                iter.remove();
                if (!stream.isClosed()) {
                    fail(stream.getClass().getName() + " is NOT closed: " + stream.getSystemID());
                }
            }
        }

    }

}
