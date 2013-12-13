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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.ImageSource;

public class PreloaderJPEGTestCase {

    @Test
    public void testAPP1Segment() throws IOException, ImageException {

        // example from http://www.media.mit.edu/pia/Research/deepview/exif.html (adapted and expanded)
        // the bytes below have three markers: 0xFFD8 (SOI), 0xFFE1 (APP1) and 0xFFC0 (SOF0); this all
        // it is needed to get the image size and resolution for this test
        byte[] jpegBytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1, (byte) 0x00, (byte) 0x42,
                (byte) 0x45, (byte) 0x78, (byte) 0x69, (byte) 0x66, (byte) 0x00, (byte) 0x00, (byte) 0x49,
                (byte) 0x49, (byte) 0x2A, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x03, (byte) 0x00, (byte) 0x1A, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x28, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x69, (byte) 0x87,
                (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11,
                (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xC0, (byte) 0xC6, (byte) 0x2D, (byte) 0x00, (byte) 0x10, (byte) 0x27, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0xC0, (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x0D,
                (byte) 0xB4, (byte) 0x09, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00};
        InputStream jpegInputStream = new ByteArrayInputStream(jpegBytes);
        MemoryCacheImageInputStream jpeg = new MemoryCacheImageInputStream(jpegInputStream);

        String uri = "image.jpg";
        ImageSource imageSource = mock(ImageSource.class);
        ImageContext context = mock(ImageContext.class);
        when(imageSource.getImageInputStream()).thenReturn(jpeg);

        PreloaderJPEG preloaderJPEG = new PreloaderJPEG();
        ImageInfo imageInfo = preloaderJPEG.preloadImage(uri, imageSource, context);
        ImageSize imageSize = imageInfo.getSize();

        double expectedDPI = 300.0;
        assertEquals(expectedDPI, imageSize.getDpiHorizontal(), 0.01);
    }

}
