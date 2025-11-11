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

package org.apache.xmlgraphics.ps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageEncodingHelperTestCase {

    private BufferedImage prepareImage(BufferedImage image) {
        Graphics2D ig = image.createGraphics();
        ig.scale(.5, .5);
        ig.setPaint(new Color(128, 0, 0));
        ig.fillRect(0, 0, 100, 50);
        ig.setPaint(Color.orange);
        ig.fillRect(100, 0, 100, 50);
        ig.setPaint(Color.yellow);
        ig.fillRect(0, 50, 100, 50);
        ig.setPaint(Color.red);
        ig.fillRect(100, 50, 100, 50);
        ig.setPaint(new Color(255, 127, 127));
        ig.fillRect(0, 100, 100, 50);
        ig.setPaint(Color.black);
        ig.draw(new Rectangle2D.Double(0.5, 0.5, 199, 149));
        ig.dispose();
        return image;
    }

    /**
     * Tests encodeRenderedImageWithDirectColorModeAsRGB(). Tests the optimised method against the
     * non-optimised method(encodeRenderedImageAsRGB) to ensure the BufferedImage produced are the
     * same.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testEncodeRenderedImageWithDirectColorModelAsRGB() throws IOException {
        BufferedImage image = new BufferedImage(100, 75, BufferedImage.TYPE_INT_ARGB);
        image = prepareImage(image);

        ByteArrayOutputStream optimized = new ByteArrayOutputStream();
        ImageEncodingHelper.encodeRenderedImageWithDirectColorModelAsRGB(image, optimized);

        ByteArrayOutputStream nonoptimized = new ByteArrayOutputStream();
        ImageEncodingHelper.encodeRenderedImageAsRGB(image, nonoptimized);

        assertArrayEquals(nonoptimized.toByteArray(), optimized.toByteArray());

    }

    /**
     * Tests a BGR versus RBG image. Debugging shows the BGR follows the optimizeWriteTo() (which
     * is intended). The bytes are compared with the RBG image, which happens to follow the
     * writeRGBTo().
     *
     * @throws IOException
     */
    @Test
    public void testRGBAndBGRImages() throws IOException {
        BufferedImage imageBGR = new BufferedImage(100, 75, BufferedImage.TYPE_3BYTE_BGR);
        imageBGR = prepareImage(imageBGR);
        BufferedImage imageRGB = new BufferedImage(100, 75, BufferedImage.TYPE_INT_BGR);
        imageRGB = prepareImage(imageRGB);

        ImageEncodingHelper imageEncodingHelperBGR = new ImageEncodingHelper(imageBGR, false);
        ImageEncodingHelper imageEncodingHelperRGB = new ImageEncodingHelper(imageRGB, false);

        ByteArrayOutputStream baosBGR = new ByteArrayOutputStream();
        imageEncodingHelperBGR.encode(baosBGR);

        ByteArrayOutputStream baosRGB = new ByteArrayOutputStream();
        imageEncodingHelperRGB.encode(baosRGB);

        assertTrue(Arrays.equals(baosBGR.toByteArray(), baosRGB.toByteArray()));
    }

    /**
     * Tests encodeRenderedImageWithDirectColorModeAsRGB(). Uses mocking to test the method
     * implementation.
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testMockedEncodeRenderedImageWithDirectColorModelAsRGB() throws IOException {
        BufferedImage image = mock(BufferedImage.class);
        final int[] templateMasks = new int[] {0x00ff0000 /*R*/, 0x0000ff00 /*G*/,
                                                    0x000000ff /*B*/, 0xff000000 /*A*/};
        DirectColorModel dcm = new DirectColorModel(255, templateMasks[0], templateMasks[1],
                templateMasks[2], templateMasks[3]);

        WritableRaster raster = mock(WritableRaster.class);
        DataBuffer buffer = mock(DataBuffer.class);

        when(image.getColorModel()).thenReturn(dcm);
        when(image.getRaster()).thenReturn(raster);
        when(raster.getDataBuffer()).thenReturn(buffer);
        when(buffer.getDataType()).thenReturn(DataBuffer.TYPE_INT);
        when(image.getWidth()).thenReturn(3);
        when(image.getHeight()).thenReturn(3);
        final int expectedValue = 1 + 2 << 8 + 3 << 16;
        Answer<Object> ans = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                int[] data = (int[]) args[4];
                Arrays.fill(data, expectedValue);
                return null;
            }
        };
        when(raster.getDataElements(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenAnswer(ans);

        ByteArrayOutputStream optimized = new ByteArrayOutputStream();
        ImageEncodingHelper.encodeRenderedImageWithDirectColorModelAsRGB(image, optimized);

        byte[] expectedByteArray = new byte[27];
        Arrays.fill(expectedByteArray, (byte) expectedValue);
        assertArrayEquals(expectedByteArray, optimized.toByteArray());
    }
}
