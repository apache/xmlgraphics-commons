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

package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

import org.apache.xmlgraphics.util.io.IOUtils;

public class TIFFImageEncoderTestCase {

    @Test
    public void testGrayImage() throws IOException {
        testImage(BufferedImage.TYPE_BYTE_GRAY, "gray.tiff");
    }

    @Test
    public void testBilevelImage() throws IOException {
        testImage(BufferedImage.TYPE_BYTE_BINARY, "bilevel.tiff");
    }

    private void testImage(int imageType, String imageFileName) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        TIFFImageEncoder encoder = new TIFFImageEncoder(byteStream, null);
        encoder.encode(getImage(imageType));
        byte[] actualArray = IOUtils.toByteArray(getClass().getResource(imageFileName).openStream());
        assertArrayEquals(byteStream.toByteArray(), actualArray);
    }

    private RenderedImage getImage(int imageType) {
        BufferedImage img = new BufferedImage(400, 400, imageType);
        img.getSampleModel();
        Graphics gfx = img.getGraphics();
        gfx.setColor(Color.RED);
        gfx.fillRect(10, 10, 100, 100);
        gfx.setColor(Color.WHITE);
        gfx.fillRect(50, 50, 100, 100);
        return img;
    }
}
