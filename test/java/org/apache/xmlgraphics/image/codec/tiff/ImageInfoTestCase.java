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
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.apache.xmlgraphics.image.codec.tiff.ExtraSamplesType.UNSPECIFIED;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.BILEVEL_BLACK_IS_ZERO;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.BILEVEL_WHITE_IS_ZERO;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.CIELAB;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.CMYK;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.GENERIC;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.GRAY;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.RGB;
import static org.apache.xmlgraphics.image.codec.tiff.ImageType.YCBCR;

public class ImageInfoTestCase {

    private ColorSpace colorSpace;
    private ColorModel colorModel;
    private RenderedImage image;
    private TIFFEncodeParam params;

    @Before
    public void setUp() {
        colorSpace = mock(ColorSpace.class);
        colorModel = new TestColorModel(colorSpace, true);
        image = mock(RenderedImage.class);
        params = mock(TIFFEncodeParam.class);
    }

    @Test
    public void testNullColorModel() {
        testImageInfo(ImageInfo.newInstance(image, 1, 1, null, params),
                BILEVEL_BLACK_IS_ZERO, 0, null, 0, UNSPECIFIED);

        for (int i = 2; i < 10; i += 2) {
            testImageInfo(ImageInfo.newInstance(image, 1, i, null, params),
                    GENERIC, i - 1, null, 0, UNSPECIFIED);
        }
    }

    @Test
    public void testNonIndexColorModel() {
        testTheColorSpaceType(ColorSpace.TYPE_CMYK, false, CMYK);
        testTheColorSpaceType(ColorSpace.TYPE_GRAY, false, GRAY);
        testTheColorSpaceType(ColorSpace.TYPE_RGB, true, YCBCR);
        testTheColorSpaceType(ColorSpace.TYPE_RGB, false, RGB);
    }

    private void testTheColorSpaceType(int colorSpaceType, boolean getJpegCompress, ImageType expectedType) {
        when(colorSpace.getType()).thenReturn(colorSpaceType);
        TIFFEncodeParam params = mock(TIFFEncodeParam.class);
        when(params.getJPEGCompressRGBToYCbCr()).thenReturn(getJpegCompress);

        testImageInfo(ImageInfo.newInstance(image, 1, 1, colorModel, params),
                expectedType, 0, null, 0, UNSPECIFIED);
    }

    @Test
    public void testNonIndexColorModelWithNumBandsGreaterThan1() {
        testWithNumOfBandsGreaterThan1(ColorSpace.TYPE_GRAY, GRAY, 3, 1);
        testWithNumOfBandsGreaterThan1(ColorSpace.TYPE_Lab, CIELAB, 6, 3);
        testWithNumOfBandsGreaterThan1(ColorSpace.TYPE_CMYK, CMYK, 5, 2);
    }

    private void testWithNumOfBandsGreaterThan1(int colorSpaceType, ImageType type, int numBands,
            int numComponents) {
        when(colorSpace.getType()).thenReturn(colorSpaceType);
        when(colorSpace.getNumComponents()).thenReturn(numComponents);
        testImageInfo(ImageInfo.newInstance(image, 2, numBands, colorModel, params),
                type, numBands - numComponents, null, 0, UNSPECIFIED);
    }

    private void testImageInfo(ImageInfo imageInfo, ImageType imageType, int numExtraSamples,
            char[] colormap, int colormapSize, ExtraSamplesType extraSamplesType) {
        assertEquals(imageType, imageInfo.getType());
        assertEquals(numExtraSamples, imageInfo.getNumberOfExtraSamples());
        assertArrayEquals(colormap, imageInfo.getColormap());
        assertEquals(colormapSize, imageInfo.getColormapSize());
        assertEquals(extraSamplesType, imageInfo.getExtraSamplesType());
    }

    @Test
    public void testIndexColorModel() {
        byte[] blackIsZero = new byte[] {0, (byte) 0xff};
        IndexColorModel icm = new IndexColorModel(1, 2, blackIsZero, blackIsZero, blackIsZero);
        testImageInfo(ImageInfo.newInstance(image, 1, 1, icm, params),
                BILEVEL_BLACK_IS_ZERO, 0, null, 0, UNSPECIFIED);

        byte[] whiteIsZero = new byte[] {(byte) 0xff, 0};
        icm = new IndexColorModel(1, 2, whiteIsZero, whiteIsZero, whiteIsZero);
        testImageInfo(ImageInfo.newInstance(image, 1, 1, icm, params),
                BILEVEL_WHITE_IS_ZERO, 0, null, 0, UNSPECIFIED);
    }

    @Test
    public void testTileWidthHeight() {
        when(params.getWriteTiled()).thenReturn(true);

        when(image.getWidth()).thenReturn(10);
        when(image.getHeight()).thenReturn(10);

        for (int i = 1; i < 10000; i += 200) {
            when(params.getTileWidth()).thenReturn(i);
            when(params.getTileHeight()).thenReturn(i);
            int numTiles = ((10 + i - 1) / i) * ((10 + i - 1) / i);
            long bytesPerRow = (long) Math.ceil((1 / 8.0) * i * 1);
            long bytesPerTile = bytesPerRow * i;

            testTileOnImageInfo(ImageInfo.newInstance(image, 1, 1, colorModel, params),
                    i, i, numTiles, bytesPerRow, bytesPerTile);
        }
    }

    private void testTileOnImageInfo(ImageInfo imageInfo, int tileWidth, int tileHeight,
            int numTiles, long bytesPerRow, long bytesPerTile) {
        assertEquals(tileWidth, imageInfo.getTileWidth());
        assertEquals(tileHeight, imageInfo.getTileHeight());
        assertEquals(numTiles, imageInfo.getNumTiles());
        assertEquals(bytesPerRow, imageInfo.getBytesPerRow());
        assertEquals(bytesPerTile, imageInfo.getBytesPerTile());
    }

    @Test
    public void testGetColormap() {
        ImageInfo sut = ImageInfo.newInstance(image, 1, 1,
                new IndexColorModel(1, 2, new byte[2], new byte[2], new byte[2], new byte[2]), params);
        char[] colormap = sut.getColormap();
        assertEquals(0, colormap[0]);
        colormap[0] = 1;
        //  Assert that getColormap() returns a defensive copy
        assertEquals(0, sut.getColormap()[0]);
    }

    private static final class TestColorModel extends ColorModel {

        protected TestColorModel(ColorSpace cspace, boolean isAlphaPremultiplied) {
            super(1, new int[] {1, 1}, cspace, isAlphaPremultiplied, isAlphaPremultiplied, 1, 1);
        }

        @Override
        public int getRed(int pixel) {
            return 0;
        }

        @Override
        public int getGreen(int pixel) {
            return 0;
        }

        @Override
        public int getBlue(int pixel) {
            return 0;
        }

        @Override
        public int getAlpha(int pixel) {
            return 0;
        }
    }
}
