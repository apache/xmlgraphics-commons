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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
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
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

    private final RenderedImage image;
    private ColorModel encodedColorModel;
    private boolean firstTileDump;
    private boolean enableCMYK;
    private boolean isBGR;
    private boolean isKMYC;
    private boolean outputbw;
    private boolean bwinvert;

    /**
     * Main constructor
     * @param image the image
     */
    public ImageEncodingHelper(RenderedImage image) {
        this(image, true);
        outputbw = true;
    }

    /**
     * Main constructor
     * @param image the image
     * @param enableCMYK true to enable CMYK, false to disable
     */
    public ImageEncodingHelper(RenderedImage image, boolean enableCMYK) {
        this.image = image;
        this.enableCMYK = enableCMYK;
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
        boolean encoded = encodeRenderedImageWithDirectColorModelAsRGB(image, out);
        if (encoded) {
            return;
        }
        encodeRenderedImageAsRGB(image, out, outputbw, bwinvert);
    }

    public static void encodeRenderedImageAsRGB(RenderedImage image, OutputStream out)
            throws IOException {
        encodeRenderedImageAsRGB(image, out, false, false);
    }

    /**
     * Writes a RenderedImage to an OutputStream by converting it to RGB.
     * @param image the image
     * @param out the OutputStream to write the pixels to
     * @throws IOException if an I/O error occurs
     */
    public static void encodeRenderedImageAsRGB(RenderedImage image, OutputStream out,
            boolean outputbw, boolean bwinvert) throws IOException {
        Raster raster = getRaster(image);
        Object data;
        int nbands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            data = new byte[nbands];
            break;
        case DataBuffer.TYPE_USHORT:
            data = null;
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
            throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
        }

        ColorModel colorModel = image.getColorModel();
        int w = image.getWidth();
        int h = image.getHeight();
        int numDataElements = 3;
        if (colorModel.getPixelSize() == 1 && outputbw) {
            numDataElements = 1;
        }

        byte[] buf = new byte[w * numDataElements];

        for (int y = 0; y < h; y++) {
            int idx = -1;
            for (int x = 0; x < w; x++) {
                int rgb = colorModel.getRGB(raster.getDataElements(x, y, data));
                if (numDataElements > 1) {
                    buf[++idx] = (byte)(rgb >> 16);
                    buf[++idx] = (byte)(rgb >> 8);
                } else if (bwinvert && rgb == -1) {
                    rgb = 1;
                }
                buf[++idx] = (byte)(rgb);
            }
            out.write(buf);
        }
    }

    /**
     * Writes a RenderedImage to an OutputStream. This method optimizes the encoding
     * of the {@link DirectColorModel} as it is returned by {@link ColorModel#getRGBdefault}.
     * @param image the image
     * @param out the OutputStream to write the pixels to
     * @return true if this method encoded this image, false if the image is incompatible
     * @throws IOException if an I/O error occurs
     */
    public static boolean encodeRenderedImageWithDirectColorModelAsRGB(
            RenderedImage image, OutputStream out) throws IOException {
        ColorModel cm = image.getColorModel();
        if (cm.getColorSpace() != ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
            return false; //Need to go through color management
        }
        if (!(cm instanceof DirectColorModel)) {
            return false; //Only DirectColorModel is supported here
        }
        DirectColorModel dcm = (DirectColorModel)cm;
        final int[] templateMasks = new int[]
                {0x00ff0000 /*R*/, 0x0000ff00 /*G*/, 0x000000ff /*B*/, 0xff000000 /*A*/};
        int[] masks = dcm.getMasks();
        if (!Arrays.equals(templateMasks, masks)) {
            return false; //no flexibility here right now, might never be used anyway
        }

        Raster raster = getRaster(image);
        int dataType = raster.getDataBuffer().getDataType();
        if (dataType != DataBuffer.TYPE_INT) {
            return false; //not supported
        }

        int w = image.getWidth();
        int h = image.getHeight();

        int[] data = new int[w];
        byte[] buf = new byte[w * 3];
        for (int y = 0; y < h; y++) {
            int idx = -1;
            raster.getDataElements(0, y, w, 1, data);
            for (int x = 0; x < w; x++) {
                int rgb = data[x];
                buf[++idx] = (byte)(rgb >> 16);
                buf[++idx] = (byte)(rgb >> 8);
                buf[++idx] = (byte)(rgb);
            }
            out.write(buf);
        }

        return true;
    }

    private static Raster getRaster(RenderedImage image) {
        if (image instanceof BufferedImage) {
            return ((BufferedImage)image).getRaster();
        } else {
            //Note: this copies the image data (double memory consumption)
            //TODO Investigate encoding in stripes: RenderedImage.copyData(WritableRaster)
            return image.getData();
        }
    }

    /**
     * Converts a byte array containing 24 bit RGB image data to a grayscale
     * image.
     *
     * @param raw
     *            the buffer containing the RGB image data
     * @param width
     *            the width of the image in pixels
     * @param height
     *            the height of the image in pixels
     * @param bitsPerPixel
     *            the number of bits to use per pixel
     * @param out the OutputStream to write the pixels to
     *
     * @throws IOException if an I/O error occurs
     */
    public static void encodeRGBAsGrayScale(
            byte[] raw, int width, int height, int bitsPerPixel, OutputStream out)
    throws IOException {
        int pixelsPerByte = 8 / bitsPerPixel;
        int bytewidth = (width / pixelsPerByte);
        if ((width % pixelsPerByte) != 0) {
            bytewidth++;
        }

        //TODO Rewrite to encode directly from a RenderedImage to avoid buffering the whole RGB
        //image in memory
        byte[] linedata = new byte[bytewidth];
        byte ib;
        for (int y = 0; y < height; y++) {
            ib = 0;
            int i = 3 * y * width;
            for (int x = 0; x < width; x++, i += 3) {

                // see http://www.jguru.com/faq/view.jsp?EID=221919
                double greyVal = 0.212671d * (raw[i] & 0xff) + 0.715160d
                        * (raw[i + 1] & 0xff) + 0.072169d
                        * (raw[i + 2] & 0xff);
                switch (bitsPerPixel) {
                case 1:
                    if (greyVal < 128) {
                        ib |= (byte) (1 << (7 - (x % 8)));
                    }
                    break;
                case 4:
                    greyVal /= 16;
                    ib |= (byte) ((byte) greyVal << ((1 - (x % 2)) * 4));
                    break;
                case 8:
                    ib = (byte) greyVal;
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported bits per pixel: " + bitsPerPixel);
                }

                if ((x % pixelsPerByte) == (pixelsPerByte - 1)
                        || ((x + 1) == width)) {
                    linedata[(x / pixelsPerByte)] = ib;
                    ib = 0;
                }
            }
            out.write(linedata);
        }
    }

    private boolean optimizedWriteTo(OutputStream out)
            throws IOException {
        if (this.firstTileDump) {
            Raster raster = image.getTile(0, 0);
            DataBuffer buffer = raster.getDataBuffer();
            if (buffer instanceof DataBufferByte) {
                byte[] bytes = ((DataBufferByte) buffer).getData();
                // see determineEncodingColorModel() to see why we permute B and R here
                if (isBGR) {
                    byte[] bytesPermutated = new byte[bytes.length];
                    for (int i = 0; i < bytes.length; i += 3) {
                        bytesPermutated[i] = bytes[i + 2];
                        bytesPermutated[i + 1] = bytes[i + 1];
                        bytesPermutated[i + 2] = bytes[i];
                    }
                    out.write(bytesPermutated);
                } else if (isKMYC) {
                    byte[] bytesPermutated = new byte[bytes.length];
                    for (int i = 0; i < bytes.length; i += 4) {
                        bytesPermutated[i] = bytes[i + 3];
                        bytesPermutated[i + 1] = bytes[i + 2];
                        bytesPermutated[i + 2] = bytes[i + 1];
                        bytesPermutated[i + 3] = bytes[i];
                    }
                    out.write(bytesPermutated);
                } else {
                    out.write(bytes);
                }
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
                    && (numComponents == 3 || (enableCMYK && numComponents == 4))
                    && !cm.hasAlpha()) {
                Raster raster = image.getTile(0, 0);
                DataBuffer buffer = raster.getDataBuffer();
                SampleModel sampleModel = raster.getSampleModel();
                if (sampleModel instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel piSampleModel;
                    piSampleModel = (PixelInterleavedSampleModel)sampleModel;
                    int[] offsets = piSampleModel.getBandOffsets();
                    for (int i = 0; i < offsets.length; i++) {
                        if (offsets[i] != i && offsets[i] != offsets.length - 1 - i) {
                            //Don't encode directly as samples are not next to each other
                            //i.e. offsets are not 012 (RGB) or 0123 (CMYK)
                            // let also pass 210 BGR and 3210 (KYMC); 3210 will be skipped below
                            // if 210 (BGR) the B and R bytes will be permuted later in optimizeWriteTo()
                            return;
                        }
                    }
                    // check if we are in a BGR case; this is added here as a workaround for bug fix
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6549882 that causes some PNG
                    // images to be loaded as BGR with the consequence that performance was being impacted
                    this.isBGR = false;
                    if (offsets.length == 3 && offsets[0] == 2 && offsets[1] == 1 && offsets[2] == 0) {
                        this.isBGR = true;
                    }
                    // make sure we did not get here due to a KMYC image
                    if (offsets.length == 4 && offsets[0] == 3 && offsets[1] == 2 && offsets[2] == 1
                            && offsets[3] == 0) {
                        isKMYC = true;
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

        private final RenderedImage img;

        public RenderedImageEncoder(RenderedImage ri) {
            if (ri instanceof BufferedImage && ((BufferedImage) ri).getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                BufferedImage convertedImg =
                        new BufferedImage(ri.getWidth(), ri.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) convertedImg.getGraphics();
                g.setBackground(Color.WHITE);
                g.clearRect(0, 0, ri.getWidth(), ri.getHeight());
                g.drawImage((BufferedImage)ri, 0, 0, null);
                g.dispose();
                ri = convertedImg;
            }
            img = ri;
        }

        public void writeTo(OutputStream out) throws IOException {
            ImageEncodingHelper.encodePackedColorComponents(img, out);
        }

        public String getImplicitFilter() {
            return null; //No implicit filters with RenderedImage instances
        }
    }

    public void setBWInvert(boolean v) {
        bwinvert = v;
    }
}
