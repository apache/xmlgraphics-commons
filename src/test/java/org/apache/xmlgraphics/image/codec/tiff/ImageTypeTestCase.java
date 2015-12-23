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

import java.awt.color.ColorSpace;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.apache.xmlgraphics.image.codec.tiff.ImageType.BILEVEL_BLACK_IS_ZERO;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.BILEVEL_WHITE_IS_ZERO;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.CIELAB;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.CMYK;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.GENERIC;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.GRAY;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.PALETTE;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.RGB;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.UNSUPPORTED;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.YCBCR;

public class ImageTypeTestCase {
    private static final class ColorContainer {
        private final byte[] r;
        private final byte[] g;
        private final byte[] b;

        private ColorContainer(byte[] r, byte[] g, byte[] b) {
            this.r = r;
            this.b = b;
            this.g = g;
        }
    }

    private ColorContainer blackIsZero;
    private ColorContainer whiteIsZero;

    @Before
    public void setUp() {
        byte[] blackSetToZero = {0, (byte) 0xff};
        blackIsZero = new ColorContainer(blackSetToZero, blackSetToZero, blackSetToZero);
        byte[] whiteSetToZero = {(byte) 0xff, 0};
        whiteIsZero = new ColorContainer(whiteSetToZero, whiteSetToZero, whiteSetToZero);
    }

    @Test
    public void testPhotometricInterpretationValue() {
        assertEquals(0, BILEVEL_WHITE_IS_ZERO.getPhotometricInterpretation());
        assertEquals(1, BILEVEL_BLACK_IS_ZERO.getPhotometricInterpretation());
        assertEquals(1, GRAY.getPhotometricInterpretation());
        assertEquals(3, PALETTE.getPhotometricInterpretation());
        assertEquals(2, RGB.getPhotometricInterpretation());
        assertEquals(5, CMYK.getPhotometricInterpretation());
        assertEquals(6, YCBCR.getPhotometricInterpretation());
        assertEquals(8, CIELAB.getPhotometricInterpretation());
        assertEquals(1, GENERIC.getPhotometricInterpretation());
    }

    @Test
    public void testGetTypeFromRGB() {
        assertEquals(BILEVEL_BLACK_IS_ZERO, ImageType.getTypeFromRGB(2,
                blackIsZero.r, blackIsZero.g, blackIsZero.b, 1, 1));
        assertEquals(BILEVEL_WHITE_IS_ZERO, ImageType.getTypeFromRGB(2,
                whiteIsZero.r, whiteIsZero.g, whiteIsZero.b, 1, 1));
        // Test all other values (i.e. not including 0xff)
        for (int b = 0; b < 255; b++) {
            assertEquals(PALETTE, ImageType.getTypeFromRGB(2,
                    make2ByteArray(0, b), make2ByteArray(0, b), make2ByteArray(0, b), 1, 1));
            assertEquals(PALETTE, ImageType.getTypeFromRGB(2,
                    make2ByteArray(b, 0), make2ByteArray(b, 0), make2ByteArray(b, 0), 1, 1));
            if (b != 1) {
                assertEquals(UNSUPPORTED, ImageType.getTypeFromRGB(2, null, null, null, 1, b));
            }
        }
    }

    private byte[] make2ByteArray(int b1, int b2) {
        return new byte[] {(byte) b1, (byte) b2};
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException() {
        assertEquals(UNSUPPORTED, ImageType.getTypeFromRGB(1, null, null, null, 1, 1));
    }

    @Test
    public void testGetTypeFromColorSpace() {
        testIndividualColorSpaceType(CMYK, ColorSpace.TYPE_CMYK, false);
        testIndividualColorSpaceType(GRAY, ColorSpace.TYPE_GRAY, false);
        testIndividualColorSpaceType(CIELAB, ColorSpace.TYPE_Lab, false);
        testIndividualColorSpaceType(YCBCR, ColorSpace.TYPE_YCbCr, false);
        testIndividualColorSpaceType(YCBCR, ColorSpace.TYPE_RGB, true);
        testIndividualColorSpaceType(RGB, ColorSpace.TYPE_RGB, false);
    }

    private void testIndividualColorSpaceType(ImageType expected, int type, boolean getJpegCompress) {
        ColorSpace colorSpace = mock(ColorSpace.class);
        when(colorSpace.getType()).thenReturn(type);
        TIFFEncodeParam params = mock(TIFFEncodeParam.class);
        when(params.getJPEGCompressRGBToYCbCr()).thenReturn(getJpegCompress);

        assertEquals(expected, ImageType.getTypeFromColorSpace(colorSpace, params));
    }
}
