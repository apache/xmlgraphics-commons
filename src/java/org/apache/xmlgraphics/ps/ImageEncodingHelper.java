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

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.xmlgraphics.image.GraphicsUtil;

/**
 * Helper class for encoding bitmap images.
 */
public class ImageEncodingHelper {

    private static final ColorModel DEFAULT_RGB_COLOR_MODEL = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
            false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
    
    private RenderedImage image;
    private ColorModel encodedColorModel;
    private boolean firstTileDump;
    
    /**
     * Main constructor
     * @param image the image
     */
    public ImageEncodingHelper(RenderedImage image) {
        this.image = image;
        determineEncodedColorModel();
    }
    
    /**
     * Returns the associated image.
     * @return the image
     */
    public RenderedImage getImage() {
        return this.image;
    }
    
    /**
     * Returns the native {@link ColorModel} used by the image.
     * @return the native color model
     */
    public ColorModel getNativeColorModel() {
        return getImage().getColorModel();
    }
    
    /**
     * Returns the effective {@link ColorModel} used to encode the image. If this is different
     * from the value returned by {@link #getNativeColorModel()} this means that the image
     * is converted in order to encode it because no native encoding is currently possible. 
     * @return the effective color model
     */
    public ColorModel getEncodedColorModel() {
        return this.encodedColorModel;
    }
    
    /**
     * Indicates whether the image has an alpha channel.
     * @return true if the image has an alpha channel
     */
    public boolean hasAlpha() {
        return image.getColorModel().hasAlpha();
    }
    
    /**
     * Indicates whether the image is converted during encodation.
     * @return true if the image cannot be encoded in its native format
     */
    public boolean isConverted() {
        return getNativeColorModel() != getEncodedColorModel();
    }
    
    private void writeRGBTo(OutputStream out) throws IOException {
        encodeRenderedImageAsRGB(image, out);
    }
    
    /**
     * Writes a RenderedImage to an OutputStream by converting it to RGB.
     * @param image the image
     * @param out the OutputStream to write the pixels to
     * @throws IOException if an I/O error occurs
     */
    public static void encodeRenderedImageAsRGB(RenderedImage image, OutputStream out)
                throws IOException {
        Raster raster = image.getData();
        Object data;
        int nbands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            data = new byte[nbands];
            break;
        case DataBuffer.TYPE_USHORT:
            data = new short[nbands];
            break;
        case DataBuffer.TYPE_INT:
            data = new int[nbands];
            break;
        case DataBuffer.TYPE_FLOAT:
            data = new float[nbands];
            break;
        case DataBuffer.TYPE_DOUBLE:
            data = new double[nbands];
            break;
        default:
            throw new IllegalArgumentException("Unknown data buffer type: "+
                                               dataType);
        }
        
        ColorModel colorModel = image.getColorModel();
        int w = image.getWidth();
        int h = image.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = colorModel.getRGB(raster.getDataElements(x, y, data));
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                out.write(r);
                out.write(g);
                out.write(b);
            }
        }
    }
    
    private boolean optimizedWriteTo(OutputStream out)
            throws IOException {
        if (this.firstTileDump) {
            Raster raster = image.getTile(0, 0);
            DataBuffer buffer = raster.getDataBuffer();
            if (buffer instanceof DataBufferByte) {
                out.write(((DataBufferByte)buffer).getData());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Indicates whether the image consists of multiple tiles.
     * @return true if there are multiple tiles
     */
    protected boolean isMultiTile() {
        int tilesX = image.getNumXTiles();
        int tilesY = image.getNumYTiles();
        return (tilesX != 1 || tilesY != 1); 
    }
    
    /**
     * Determines the color model used for encoding the image.
     */
    protected void determineEncodedColorModel() {
        this.firstTileDump = false;
        this.encodedColorModel = DEFAULT_RGB_COLOR_MODEL;

        ColorModel cm = image.getColorModel();
        ColorSpace cs = cm.getColorSpace();

        int numComponents = cm.getNumComponents();

        if (!isMultiTile()) {
            if (numComponents == 1 && cs.getType() == ColorSpace.TYPE_GRAY) {
                if (cm.getTransferType() == DataBuffer.TYPE_BYTE) {
                    this.firstTileDump = true;
                    this.encodedColorModel = cm;
                }
            } else if (cm instanceof IndexColorModel) {
                if (cm.getTransferType() == DataBuffer.TYPE_BYTE) {
                    this.firstTileDump = true;
                    this.encodedColorModel = cm;
                }
            } else if (cm instanceof ComponentColorModel
                    && numComponents == 3
                    && !cm.hasAlpha()) {
                Raster raster = image.getTile(0, 0);
                DataBuffer buffer = raster.getDataBuffer();
                SampleModel sampleModel = raster.getSampleModel();
                if (sampleModel instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel piSampleModel;
                    piSampleModel = (PixelInterleavedSampleModel)sampleModel;
                    final int[] expectedOffsets = new int[] {0, 1, 2};
                    int[] offsets = piSampleModel.getBandOffsets();
                    if (!Arrays.equals(offsets, expectedOffsets)) {
                        return;
                    }
                }
                if (cm.getTransferType() == DataBuffer.TYPE_BYTE
                        && buffer.getOffset() == 0
                        && buffer.getNumBanks() == 1) {
                    this.firstTileDump = true;
                    this.encodedColorModel = cm;
                }
            }
        }

    }
    
    /**
     * Encodes the image and writes everything to the given OutputStream.
     * @param out the OutputStream
     * @throws IOException if an I/O error occurs
     */
    public void encode(OutputStream out) throws IOException {
        if (!isConverted()) {
            if (optimizedWriteTo(out)) {
                return;
            }
        }
        writeRGBTo(out);
    }
    
    /**
     * Encodes the image's alpha channel. If it doesn't have an alpha channel, an
     * {@link IllegalStateException} is thrown.
     * @param out the OutputStream
     * @throws IOException if an I/O error occurs
     */
    public void encodeAlpha(OutputStream out) throws IOException {
        if (!hasAlpha()) {
            throw new IllegalStateException("Image doesn't have an alpha channel");
        }
        Raster alpha = GraphicsUtil.getAlphaRaster(image);
        DataBuffer buffer = alpha.getDataBuffer();
        if (buffer instanceof DataBufferByte) {
            out.write(((DataBufferByte)buffer).getData());
        } else {
            throw new UnsupportedOperationException(
                    "Alpha raster not supported: " + buffer.getClass().getName());
        }
    }

    /**
     * Writes all pixels (color components only) of a RenderedImage to an OutputStream.
     * @param image the image to be encoded 
     * @param out the OutputStream to write to
     * @throws IOException if an I/O error occurs
     */
    public static void encodePackedColorComponents(RenderedImage image, OutputStream out)
                throws IOException {
        ImageEncodingHelper helper = new ImageEncodingHelper(image);
        helper.encode(out);
    }
    
    /**
     * Create an ImageEncoder for the given RenderImage instance.
     * @param img the image
     * @return the requested ImageEncoder
     */
    public static ImageEncoder createRenderedImageEncoder(RenderedImage img) {
        return new RenderedImageEncoder(img);
    }
    
    /**
     * ImageEncoder implementation for RenderedImage instances.
     */
    private static class RenderedImageEncoder implements ImageEncoder {

        private RenderedImage img;
        
        public RenderedImageEncoder(RenderedImage img) {
            this.img = img;
        }
        
        public void writeTo(OutputStream out) throws IOException {
            ImageEncodingHelper.encodePackedColorComponents(img, out);
        }

        public String getImplicitFilter() {
            return null; //No implicit filters with RenderedImage instances
        }
        
    }
    
}
