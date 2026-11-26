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

package org.apache.xmlgraphics.image.writer.internal;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.PNGFilter;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;

/**
 * Writes PNG files straight from the pixel buffer of an image, without Image I/O.
 * <p>
 * Compared to the Image I/O PNG writer this
 * <ul>
 *   <li>reads rows straight from the data buffer instead of unpacking every sample to an int,</li>
 *   <li>uses no row filter by default, as the PNG specification recommends for palette images and
 *       images with less than 8 bits per pixel (section 12.7). The JDK writer has this inverted:
 *       it searches all five filters for palette images and uses none for truecolor images, see
 *       {@code com.sun.imageio.plugins.png.RowFilter}. On rendered pages (223 pages of the FOP
 *       examples, 432 labels) no filter also gave the smallest files for truecolor images: Up made
 *       palette images 13-20% and truecolor images 2-9% larger, and only helped labels with an 8 bit
 *       palette (12% smaller); {@link PNGFilter#ADAPTIVE} only helped truecolor pages at 300 dpi
 *       (4% smaller, for twice the time),</li>
 *   <li>writes every image in the smallest form for the colors it uses: truecolor (with or without
 *       alpha) with at most 256 colors as a palette or as 1, 2 or 4 bit gray, palettes that use few
 *       entries with fewer bits, black and white gray as 1 bit gray, without an alpha channel that
 *       is opaque everywhere, and palettes only up to the last entry used. Only forms that every
 *       decoder reads back as the same colors are used (Image I/O reads 8 bit gray as linear gray,
 *       all other forms as sRGB). An RGB label in three colors takes 4.7 KB instead of 12.6 KB and
 *       1.2 ms instead of 5.3 ms; {@link ImageWriterParams#setPNGReduceColors(boolean)} keeps the
 *       color type, bit depth and palette of the image instead,</li>
 *   <li>streams the compressed data in fixed-size IDAT chunks instead of buffering or caching
 *       it.</li>
 * </ul>
 * The chunks written are the ones Image I/O writes: IHDR, PLTE and tRNS for palette images, pHYs
 * when a resolution is given, IDAT and IEND.
 * <p>
 * Only common layouts are supported, see {@link #canEncode(RenderedImage)}; callers fall back to
 * another writer for the rest.
 */
public final class DirectPNGEncoder {

    /**
     * The deflate level used when the parameters do not set one: the default of the Image I/O
     * writer in current JDKs (Java 8 always uses level 9). On rendered pages level 6 gives 5-20%
     * smaller files for 9-30% more time; level 9 is several times slower.
     */
    public static final int DEFAULT_COMPRESSION_LEVEL = 4;

    private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private static final int IHDR = 0x49484452;
    private static final int PLTE = 0x504C5445;
    private static final int TRNS = 0x74524E53;
    private static final int PHYS = 0x70485973;
    private static final int IDAT = 0x49444154;
    private static final int IEND = 0x49454E44;

    private static final int COLOR_GRAY = 0;
    private static final int COLOR_RGB = 2;
    private static final int COLOR_PALETTE = 3;
    private static final int COLOR_GRAY_ALPHA = 4;
    private static final int COLOR_RGB_ALPHA = 6;

    private static final int FILTER_NONE = 0;
    private static final int FILTER_SUB = 1;
    private static final int FILTER_UP = 2;
    private static final int FILTER_AVERAGE = 3;
    private static final int FILTER_PAETH = 4;

    private static final int IDAT_SIZE = 16 * 1024;

    /** Rows are collected up to this size before they are handed to the deflater. */
    private static final int INPUT_SIZE = 8 * 1024;

    /** The most colors a palette can hold. */
    private static final int MAX_PALETTE = 256;

    /** How rows are read from the data buffer. */
    private enum Source {
        /** Rows are byte arrays as PNG stores them (palette, gray, packed 1/2/4 bits): copied. */
        BYTE_ROWS,
        /** Interleaved byte samples (3BYTE_BGR, 4BYTE_ABGR, gray + alpha ...): reordered. */
        BYTE_PIXELS,
        /** One int per pixel (INT_RGB, INT_ARGB, INT_BGR): unpacked. */
        INT_PIXELS
    }

    private DirectPNGEncoder() {
    }

    /**
     * Tells whether {@link #encode} can write an image.
     * @param image the image
     * @return true if the image has a layout this encoder supports
     */
    public static boolean canEncode(RenderedImage image) {
        return Layout.of(image, false) != null;
    }

    /**
     * Writes an image as PNG.
     * @param image an image {@link #canEncode} accepts
     * @param out the stream to write to (not closed)
     * @param params the resolution, compression level and row filter to use, may be null
     * @throws IOException if writing fails
     * @throws IllegalArgumentException if the image is not supported
     */
    public static void encode(RenderedImage image, OutputStream out, ImageWriterParams params)
            throws IOException {
        boolean reduceColors = params == null || params.isPNGReduceColors();
        Layout layout = Layout.of(image, reduceColors);
        if (layout == null) {
            throw new IllegalArgumentException("Unsupported image layout: " + image);
        }
        int level = DEFAULT_COMPRESSION_LEVEL;
        // no filter by default, see the class comment
        PNGFilter filter = PNGFilter.NONE;
        if (params != null) {
            if (params.getCompressionLevel() != null) {
                level = params.getCompressionLevel();
            }
            if (params.getPNGFilter() != null) {
                filter = params.getPNGFilter();
            }
        }
        new Writer(out).write(layout, params, level, filter);
    }

    /** Where the pixels of an image are and how PNG stores them. */
    private static final class Layout {
        private int width;
        private int height;
        private int colorType;
        private int bitDepth;
        private IndexColorModel palette;

        private Source source;
        private byte[] bytes;
        private int[] ints;
        /** Index of the first sample of the first row in {@link #bytes} or {@link #ints}. */
        private int offset;
        private int scanlineStride;
        private int pixelStride;
        /** Byte samples: the offsets of the PNG samples within a pixel, in PNG order. */
        private int[] bandOffsets;
        /** Int pixels: the shifts of the PNG samples, in PNG order. */
        private int[] shifts;

        /** Pixels read as colors and written as one sample: the sample of every color. */
        private ColorTable colorTable;
        /** Indices or gray levels written in another form: the new sample of every old one. */
        private byte[] indexMap;
        /** Bits per pixel of the source when {@link #indexMap} is used. */
        private int sourceBits;
        /** The number of palette entries to write; entries after the last one used are left out. */
        private int paletteSize = MAX_PALETTE;

        /** Bytes per complete pixel (at least 1), the distance the Sub filter looks back. */
        int bytesPerPixel() {
            return Math.max(1, bitDepth * samplesPerPixel() / 8);
        }

        int samplesPerPixel() {
            switch (colorType) {
            case COLOR_RGB: return 3;
            case COLOR_GRAY_ALPHA: return 2;
            case COLOR_RGB_ALPHA: return 4;
            default: return 1;
            }
        }

        int rowBytes() {
            return (width * bitDepth * samplesPerPixel() + 7) / 8;
        }

        static Layout of(RenderedImage image, boolean reduceColors) {
            Raster raster = rasterOf(image);
            if (raster == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }
            ColorModel colorModel = image.getColorModel();
            SampleModel sampleModel = raster.getSampleModel();
            DataBuffer buffer = raster.getDataBuffer();
            if (colorModel == null || colorModel.isAlphaPremultiplied() || buffer.getNumBanks() != 1) {
                return null;
            }
            Layout layout = new Layout();
            layout.width = image.getWidth();
            layout.height = image.getHeight();
            // position of the image's first pixel in the sample model
            int x = image.getMinX() - raster.getSampleModelTranslateX();
            int y = image.getMinY() - raster.getSampleModelTranslateY();

            boolean supported;
            if (colorModel instanceof IndexColorModel) {
                supported = layout.indexed((IndexColorModel) colorModel, sampleModel, buffer, x, y);
            } else if (colorModel instanceof DirectColorModel) {
                supported = layout.direct((DirectColorModel) colorModel, sampleModel, buffer, x, y);
            } else if (colorModel instanceof ComponentColorModel) {
                supported = layout.component(colorModel, sampleModel, buffer, x, y);
            } else {
                supported = false;
            }
            if (!supported) {
                return null;
            }
            if (reduceColors) {
                if (layout.colorType == COLOR_RGB || layout.colorType == COLOR_RGB_ALPHA
                        || layout.colorType == COLOR_GRAY_ALPHA) {
                    layout.reduceColors();
                } else {
                    // palette or gray: one sample of 1 to 8 bits per pixel
                    layout.reduceSamples();
                }
            }
            return layout;
        }

        /**
         * Finds the smallest form for an image with several samples per pixel: with at most 256
         * colors one sample per pixel (see {@link #choose}), otherwise without the alpha sample if
         * every pixel is opaque. Stops reading as soon as neither is possible any more.
         */
        private void reduceColors() {
            boolean alpha = colorType != COLOR_RGB;
            ColorTable table = new ColorTable();
            boolean argbInts = isArgbInts();
            int[] row = argbInts ? null : new int[width];
            // pixels are compared as stored; only a change is turned into a color and looked up
            int fill = alpha ? 0 : 0xFF000000;
            int last = argbInts ? ints[offset] : firstColor(row);
            int opaque = 0xFF000000 & (last | fill);
            boolean fits = table.add(last | fill) >= 0;
            boolean possible = true;
            for (int y = 0; y < height && possible; y++) {
                int[] data;
                int start;
                if (argbInts) {
                    data = ints;
                    start = offset + y * scanlineStride;
                } else {
                    readColors(this, y, row);
                    data = row;
                    start = 0;
                }
                for (int p = start, end = start + width; p < end; p++) {
                    int pixel = data[p];
                    if (pixel == last) {
                        continue; // runs of one color are the common case
                    }
                    last = pixel;
                    int color = pixel | fill;
                    opaque &= color;
                    if (fits && table.add(color) < 0) {
                        fits = false;
                    }
                    if (!fits && !(alpha && opaque == 0xFF000000)) {
                        possible = false;
                        break;
                    }
                }
            }
            boolean opaqueEverywhere = opaque == 0xFF000000;
            boolean linearGray = colorType == COLOR_GRAY_ALPHA;
            // gray + alpha can only become 1 bit gray (see choose), and only if opaque
            if (fits && (!linearGray || opaqueEverywhere && grayBitsFor(table.colors(), table.size()) == 1)) {
                table.sortTransparentFirst();
                table.renumber(choose(table.colors(), table.size(), linearGray));
                colorTable = table;
                return;
            }
            if (!alpha || !opaqueEverywhere) {
                return;
            }
            // drop the alpha sample, the last in PNG order
            int[] keep;
            if (linearGray) {
                keep = new int[] {0};
                colorType = COLOR_GRAY;
            } else {
                keep = new int[] {0, 1, 2};
                colorType = COLOR_RGB;
            }
            if (shifts != null) {
                shifts = select(shifts, keep);
            } else {
                bandOffsets = select(bandOffsets, keep);
            }
        }

        /**
         * Finds the smallest form for an image with one sample per pixel (palette or gray, 1 to 8
         * bits): fewer bits if fewer colors are used, gray instead of a gray palette. If the form
         * stays the same, the pixels are kept and only the palette entries after the last one used
         * are left out.
         */
        private void reduceSamples() {
            int bits = bitDepth;
            int entries = 1 << bits;
            boolean[] used = usedSamples();
            int count = 0;
            int maxUsed = 0;
            for (int i = 0; i < entries; i++) {
                if (used[i]) {
                    count++;
                    maxUsed = i;
                }
            }
            // the colors used, transparent ones first so the tRNS chunk stays short
            int[] sourceSamples = new int[count];
            int[] colors = new int[count];
            int next = 0;
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < entries; i++) {
                    int color = colorOfSample(i);
                    if (used[i] && (color >>> 24 != 0xFF) == (pass == 0)) {
                        sourceSamples[next] = i;
                        colors[next++] = color;
                    }
                }
            }
            int sourceType = colorType;
            IndexColorModel sourcePalette = palette;
            int[] samples = choose(colors, count, palette == null);
            if (bitDepth < bits || colorType != sourceType) {
                indexMap = new byte[MAX_PALETTE];
                for (int k = 0; k < count; k++) {
                    indexMap[sourceSamples[k]] = (byte) samples[k];
                }
                sourceBits = bits;
            } else {
                colorType = sourceType;
                bitDepth = bits;
                palette = sourcePalette;
                paletteSize = maxUsed + 1;
            }
        }

        /** The color of a palette index or gray level of the source. */
        private int colorOfSample(int sample) {
            if (palette == null) {
                return 0xFF000000 | sample * 0x010101; // 8 bit gray
            }
            return sample < palette.getMapSize() ? palette.getRGB(sample) : 0xFF000000;
        }

        /** Which palette indices or gray levels occur, found from the distinct bytes of the rows. */
        private boolean[] usedSamples() {
            int bits = bitDepth;
            int fullBytes = width * bits / 8;
            int rest = width - fullBytes * 8 / bits;
            boolean[] seen = new boolean[256];
            boolean[] used = new boolean[256];
            int mask = (1 << bits) - 1;
            for (int y = 0; y < height; y++) {
                int start = offset + y * scanlineStride;
                for (int p = start, end = start + fullBytes; p < end; p++) {
                    seen[bytes[p] & 0xFF] = true;
                }
                if (rest > 0) {
                    // the last byte is only partly used: its unused bits may hold anything
                    int last = bytes[start + fullBytes] & 0xFF;
                    for (int k = 0; k < rest; k++) {
                        used[last >> (8 - bits * (k + 1)) & mask] = true;
                    }
                }
            }
            for (int value = 0; value < 256; value++) {
                if (seen[value]) {
                    for (int shift = 8 - bits; shift >= 0; shift -= bits) {
                        used[value >> shift & mask] = true;
                    }
                }
            }
            return used;
        }

        /**
         * Chooses the smallest form for the colors used and sets color type, bit depth and
         * palette: gray if all colors are opaque gray levels that 1, 2 or 4 bit gray holds exactly
         * in no more bits than a palette, otherwise a palette with as few bits as the number of
         * colors allows.
         * <p>
         * Only forms that every decoder reads back as the same colors are chosen. Image I/O reads
         * 8 bit gray (with or without alpha) as linear gray, but gray of fewer bits and palettes
         * as sRGB, so a color image never becomes 8 bit gray, and an 8 bit gray image (a linear
         * gray {@link BufferedImage}) only becomes 1 bit gray, if it is black and white.
         * @param colors the colors, transparent ones first
         * @param count the number of colors
         * @param linearGray whether the colors come from 8 bit gray
         * @return the sample to write for each color
         */
        private int[] choose(int[] colors, int count, boolean linearGray) {
            int[] samples = new int[count];
            int paletteBits = bitsFor(count);
            int grayBits = grayBitsFor(colors, count);
            if (linearGray) {
                grayBits = grayBits == 1 ? 1 : 8;
                paletteBits = 8;
            }
            if (grayBits < 8 && grayBits <= paletteBits || linearGray) {
                colorType = COLOR_GRAY;
                bitDepth = grayBits;
                palette = null;
                int step = 255 / ((1 << grayBits) - 1);
                for (int i = 0; i < count; i++) {
                    samples[i] = (colors[i] & 0xFF) / step;
                }
                return samples;
            }
            colorType = COLOR_PALETTE;
            bitDepth = paletteBits;
            byte[] r = new byte[count];
            byte[] g = new byte[count];
            byte[] b = new byte[count];
            byte[] a = new byte[count];
            for (int i = 0; i < count; i++) {
                a[i] = (byte) (colors[i] >>> 24);
                r[i] = (byte) (colors[i] >> 16);
                g[i] = (byte) (colors[i] >> 8);
                b[i] = (byte) colors[i];
                samples[i] = i;
            }
            palette = new IndexColorModel(paletteBits, count, r, g, b, a);
            return samples;
        }

        /** The fewest gray bits that hold all colors exactly, or 9 if they are not all opaque gray. */
        private static int grayBitsFor(int[] colors, int count) {
            for (int i = 0; i < count; i++) {
                if (colors[i] >>> 24 != 0xFF || !isGray(colors[i])) {
                    return 9;
                }
            }
            for (int bits = 1; bits < 8; bits *= 2) {
                int step = 255 / ((1 << bits) - 1);
                boolean exact = true;
                for (int i = 0; i < count && exact; i++) {
                    exact = (colors[i] & 0xFF) % step == 0;
                }
                if (exact) {
                    return bits;
                }
            }
            return 8;
        }

        private static boolean isGray(int color) {
            int b = color & 0xFF;
            return (color >> 8 & 0xFF) == b && (color >> 16 & 0xFF) == b;
        }

        private static int[] select(int[] values, int[] keep) {
            int[] selected = new int[keep.length];
            for (int i = 0; i < keep.length; i++) {
                selected[i] = values[keep[i]];
            }
            return selected;
        }

        private int firstColor(int[] row) {
            readColors(this, 0, row);
            return row[0];
        }

        /** Whether pixels are ints in the order of TYPE_INT_RGB or TYPE_INT_ARGB. */
        boolean isArgbInts() {
            return source == Source.INT_PIXELS && shifts.length >= 3 && shifts[0] == 16 && shifts[1] == 8
                    && shifts[2] == 0 && (shifts.length == 3 || shifts[3] == 24);
        }

        private static int bitsFor(int colors) {
            return colors <= 2 ? 1 : colors <= 4 ? 2 : colors <= 16 ? 4 : 8;
        }

        private static Raster rasterOf(RenderedImage image) {
            if (image instanceof BufferedImage) {
                return ((BufferedImage) image).getRaster();
            }
            if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
                Raster tile = image.getTile(image.getMinTileX(), image.getMinTileY());
                if (tile != null && tile.getMinX() <= image.getMinX() && tile.getMinY() <= image.getMinY()
                        && tile.getMinX() + tile.getWidth() >= image.getMinX() + image.getWidth()
                        && tile.getMinY() + tile.getHeight() >= image.getMinY() + image.getHeight()) {
                    return tile;
                }
            }
            return null;
        }

        private boolean indexed(IndexColorModel colorModel, SampleModel sampleModel, DataBuffer buffer,
                int x, int y) {
            if (!(buffer instanceof DataBufferByte) || sampleModel.getNumBands() != 1) {
                return false;
            }
            bitDepth = sampleModel.getSampleSize(0);
            if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8) {
                return false;
            }
            // 8 bit gray is read back as linear gray, so only 1, 2 and 4 bit ramps become gray
            colorType = bitDepth < 8 && isGrayRamp(colorModel, bitDepth) ? COLOR_GRAY : COLOR_PALETTE;
            palette = colorModel;
            return byteRows(sampleModel, buffer, x, y);
        }

        /** Whether the palette is the PNG gray scale of its bit depth, so no palette is needed. */
        private static boolean isGrayRamp(IndexColorModel colorModel, int bits) {
            int entries = 1 << bits;
            if (colorModel.getMapSize() != entries || colorModel.hasAlpha()) {
                return false;
            }
            for (int i = 0; i < entries; i++) {
                int gray = i * 255 / (entries - 1);
                if (colorModel.getRed(i) != gray || colorModel.getGreen(i) != gray
                        || colorModel.getBlue(i) != gray) {
                    return false;
                }
            }
            return true;
        }

        /** Rows that are stored as PNG stores them: packed 1/2/4 bits or one byte per pixel. */
        private boolean byteRows(SampleModel sampleModel, DataBuffer buffer, int x, int y) {
            source = Source.BYTE_ROWS;
            bytes = ((DataBufferByte) buffer).getData();
            if (sampleModel instanceof MultiPixelPackedSampleModel) {
                MultiPixelPackedSampleModel packed = (MultiPixelPackedSampleModel) sampleModel;
                int firstBit = packed.getDataBitOffset() + x * bitDepth;
                if (packed.getPixelBitStride() != bitDepth || firstBit % 8 != 0) {
                    return false;
                }
                scanlineStride = packed.getScanlineStride();
                offset = buffer.getOffset() + y * scanlineStride + firstBit / 8;
                return true;
            }
            if (sampleModel instanceof ComponentSampleModel && bitDepth == 8) {
                ComponentSampleModel component = (ComponentSampleModel) sampleModel;
                if (component.getPixelStride() != 1) {
                    return false;
                }
                scanlineStride = component.getScanlineStride();
                offset = buffer.getOffset() + y * scanlineStride + x + component.getBandOffsets()[0];
                return true;
            }
            return false;
        }

        private boolean direct(DirectColorModel colorModel, SampleModel sampleModel, DataBuffer buffer,
                int x, int y) {
            if (!(buffer instanceof DataBufferInt) || !(sampleModel instanceof SinglePixelPackedSampleModel)
                    || !colorModel.getColorSpace().isCS_sRGB()) {
                return false;
            }
            SinglePixelPackedSampleModel packed = (SinglePixelPackedSampleModel) sampleModel;
            int[] masks = packed.getBitMasks();
            int[] bitOffsets = packed.getBitOffsets();
            int bands = colorModel.hasAlpha() ? 4 : 3;
            if (masks.length != bands) {
                return false;
            }
            for (int b = 0; b < bands; b++) {
                if (masks[b] != 0xFF << bitOffsets[b]) {
                    return false;
                }
            }
            source = Source.INT_PIXELS;
            colorType = bands == 4 ? COLOR_RGB_ALPHA : COLOR_RGB;
            bitDepth = 8;
            ints = ((DataBufferInt) buffer).getData();
            scanlineStride = packed.getScanlineStride();
            offset = buffer.getOffset() + y * scanlineStride + x;
            shifts = bitOffsets;
            return true;
        }

        private boolean component(ColorModel colorModel, SampleModel sampleModel, DataBuffer buffer,
                int x, int y) {
            if (!(buffer instanceof DataBufferByte) || !(sampleModel instanceof ComponentSampleModel)) {
                return false;
            }
            ColorSpace colorSpace = colorModel.getColorSpace();
            int bands = sampleModel.getNumBands();
            boolean alpha = colorModel.hasAlpha();
            if (colorSpace.isCS_sRGB() && bands == (alpha ? 4 : 3)) {
                colorType = alpha ? COLOR_RGB_ALPHA : COLOR_RGB;
            } else if (colorSpace == ColorSpace.getInstance(ColorSpace.CS_GRAY) && bands == (alpha ? 2 : 1)) {
                colorType = alpha ? COLOR_GRAY_ALPHA : COLOR_GRAY;
            } else {
                return false;
            }
            for (int b = 0; b < bands; b++) {
                if (sampleModel.getSampleSize(b) != 8) {
                    return false;
                }
            }
            ComponentSampleModel component = (ComponentSampleModel) sampleModel;
            for (int bank : component.getBankIndices()) {
                if (bank != 0) {
                    return false;
                }
            }
            bitDepth = 8;
            if (bands == 1) {
                return byteRows(sampleModel, buffer, x, y);
            }
            source = Source.BYTE_PIXELS;
            bytes = ((DataBufferByte) buffer).getData();
            scanlineStride = component.getScanlineStride();
            pixelStride = component.getPixelStride();
            offset = buffer.getOffset() + y * scanlineStride + x * pixelStride;
            bandOffsets = component.getBandOffsets();
            return true;
        }
    }

    /**
     * Reads a row of a truecolor image as ARGB values, alpha 0xFF if the image has none.
     * @param layout an RGB or RGBA layout reading int or byte pixels
     * @param y the row
     * @param row receives the colors
     */
    private static void readColors(Layout layout, int y, int[] row) {
        int start = layout.offset + y * layout.scanlineStride;
        int width = layout.width;
        if (layout.source == Source.INT_PIXELS) {
            int[] data = layout.ints;
            int[] shifts = layout.shifts;
            int r = shifts[0];
            int g = shifts[1];
            int b = shifts[2];
            if (shifts.length == 3) {
                if (r == 16 && g == 8 && b == 0) {
                    for (int x = 0; x < width; x++) {
                        row[x] = data[start + x] | 0xFF000000;
                    }
                    return;
                }
                for (int x = 0; x < width; x++) {
                    int pixel = data[start + x];
                    row[x] = 0xFF000000 | (pixel >>> r & 0xFF) << 16 | (pixel >>> g & 0xFF) << 8
                            | pixel >>> b & 0xFF;
                }
                return;
            }
            int a = shifts[3];
            if (r == 16 && g == 8 && b == 0 && a == 24) {
                System.arraycopy(data, start, row, 0, width);
                return;
            }
            for (int x = 0; x < width; x++) {
                int pixel = data[start + x];
                row[x] = (pixel >>> a & 0xFF) << 24 | (pixel >>> r & 0xFF) << 16 | (pixel >>> g & 0xFF) << 8
                        | pixel >>> b & 0xFF;
            }
            return;
        }
        byte[] data = layout.bytes;
        int stride = layout.pixelStride;
        int[] bands = layout.bandOffsets;
        if (bands.length == 2) {
            int gray = bands[0];
            int a = bands[1];
            for (int x = 0, p = start; x < width; x++, p += stride) {
                row[x] = (data[p + a] & 0xFF) << 24 | (data[p + gray] & 0xFF) * 0x010101;
            }
            return;
        }
        int r = bands[0];
        int g = bands[1];
        int b = bands[2];
        if (bands.length == 3) {
            for (int x = 0, p = start; x < width; x++, p += stride) {
                row[x] = 0xFF000000 | (data[p + r] & 0xFF) << 16 | (data[p + g] & 0xFF) << 8 | data[p + b] & 0xFF;
            }
            return;
        }
        int a = bands[3];
        for (int x = 0, p = start; x < width; x++, p += stride) {
            row[x] = (data[p + a] & 0xFF) << 24 | (data[p + r] & 0xFF) << 16 | (data[p + g] & 0xFF) << 8
                    | data[p + b] & 0xFF;
        }
    }

    /** The palette index of each of up to 256 colors, an open addressing hash table. */
    private static final class ColorTable {
        private static final int SLOTS = 1024;
        private final int[] keys = new int[SLOTS];
        /** Palette index + 1 per slot, 0 for an empty slot. */
        private final int[] values = new int[SLOTS];
        private final int[] colors = new int[MAX_PALETTE];
        private int size;
        private int lastColor;
        private int lastIndex = -1;

        int size() {
            return size;
        }

        /** Adds a color; returns its index, or -1 if the table is full. */
        int add(int color) {
            if (color == lastColor && lastIndex >= 0) {
                return lastIndex;
            }
            int slot = slot(color);
            while (values[slot] != 0) {
                if (keys[slot] == color) {
                    lastColor = color;
                    lastIndex = values[slot] - 1;
                    return lastIndex;
                }
                slot = (slot + 1) & (SLOTS - 1);
            }
            if (size == MAX_PALETTE) {
                return -1;
            }
            keys[slot] = color;
            colors[size] = color;
            values[slot] = ++size;
            lastColor = color;
            lastIndex = size - 1;
            return lastIndex;
        }

        /** The index of a color that is in the table. */
        int get(int color) {
            if (color == lastColor && lastIndex >= 0) {
                return lastIndex;
            }
            int slot = slot(color);
            while (keys[slot] != color || values[slot] == 0) {
                slot = (slot + 1) & (SLOTS - 1);
            }
            lastColor = color;
            lastIndex = values[slot] - 1;
            return lastIndex;
        }

        private static int slot(int color) {
            int h = color * 0x9E3779B1;
            return (h ^ h >>> 16) & (SLOTS - 1);
        }

        /** Gives transparent colors the lowest indices, so the tRNS chunk stays short. */
        void sortTransparentFirst() {
            int[] newIndex = new int[size];
            int[] sorted = new int[size];
            int next = 0;
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < size; i++) {
                    if ((colors[i] >>> 24 != 0xFF) == (pass == 0)) {
                        newIndex[i] = next;
                        sorted[next++] = colors[i];
                    }
                }
            }
            System.arraycopy(sorted, 0, colors, 0, size);
            for (int slot = 0; slot < SLOTS; slot++) {
                if (values[slot] != 0) {
                    values[slot] = newIndex[values[slot] - 1] + 1;
                }
            }
            lastIndex = -1;
        }

        int[] colors() {
            return colors;
        }

        /** Replaces the index of every color by {@code samples[index]}. */
        void renumber(int[] samples) {
            for (int slot = 0; slot < SLOTS; slot++) {
                if (values[slot] != 0) {
                    values[slot] = samples[values[slot] - 1] + 1;
                }
            }
            lastIndex = -1;
        }
    }

    /** Writes one image. */
    private static final class Writer {
        private final OutputStream out;
        private final CRC32 crc = new CRC32();
        private final byte[] head = new byte[8];
        private final byte[] tail = new byte[4];
        private byte[] idat;
        private int idatLength;

        Writer(OutputStream out) {
            this.out = out;
        }

        void write(Layout layout, ImageWriterParams params, int level, PNGFilter filter)
                throws IOException {
            out.write(SIGNATURE);
            byte[] header = new byte[13];
            putInt(header, 0, layout.width);
            putInt(header, 4, layout.height);
            header[8] = (byte) layout.bitDepth;
            header[9] = (byte) layout.colorType;
            // compression, filter and interlace method 0
            chunk(IHDR, header, header.length);
            if (layout.colorType == COLOR_PALETTE) {
                writePalette(layout.palette, layout.bitDepth, layout.paletteSize);
            }
            if (params != null && params.getXResolution() != null && params.getYResolution() != null) {
                double perMetre = params.getResolutionUnit() == ResolutionUnit.CENTIMETER ? 100 : 1 / 0.0254;
                byte[] resolution = new byte[9];
                putInt(resolution, 0, (int) Math.round(params.getXResolution() * perMetre));
                putInt(resolution, 4, (int) Math.round(params.getYResolution() * perMetre));
                resolution[8] = 1; // unit: metre
                chunk(PHYS, resolution, resolution.length);
            }
            writeImageData(layout, level, filter);
            chunk(IEND, tail, 0);
        }

        private void writePalette(IndexColorModel colorModel, int bitDepth, int paletteSize)
                throws IOException {
            int entries = Math.min(Math.min(colorModel.getMapSize(), 1 << bitDepth), paletteSize);
            byte[] rgb = new byte[entries * 3];
            byte[] alpha = new byte[entries];
            int alphaEntries = 0;
            for (int i = 0; i < entries; i++) {
                rgb[i * 3] = (byte) colorModel.getRed(i);
                rgb[i * 3 + 1] = (byte) colorModel.getGreen(i);
                rgb[i * 3 + 2] = (byte) colorModel.getBlue(i);
                alpha[i] = (byte) colorModel.getAlpha(i);
                if (alpha[i] != (byte) 0xFF) {
                    alphaEntries = i + 1;
                }
            }
            chunk(PLTE, rgb, rgb.length);
            if (alphaEntries > 0) {
                // entries after the last transparent one are opaque and can be left out
                chunk(TRNS, alpha, alphaEntries);
            }
        }

        private void writeImageData(Layout layout, int level, PNGFilter filter) throws IOException {
            int rowBytes = layout.rowBytes();
            int bpp = layout.bytesPerPixel();
            boolean adaptive = filter == PNGFilter.ADAPTIVE;
            // unused low bits of the last byte of a row, cleared so they do not cost space
            int padding = rowBytes * 8 - layout.width * layout.bitDepth * layout.samplesPerPixel();
            byte lastByteMask = (byte) (0xFF << padding);

            byte[] current = new byte[rowBytes];
            byte[] previous = new byte[rowBytes];
            byte[][] filtered = new byte[adaptive ? 5 : 1][rowBytes + 1];
            for (int f = 0; f < filtered.length; f++) {
                filtered[f][0] = (byte) f;
            }
            if (!adaptive) {
                filtered[0][0] = (byte) filter.ordinal();
            }
            boolean argbInts = layout.colorTable != null && layout.isArgbInts();
            int[] colors = layout.colorTable != null && !argbInts ? new int[layout.width] : null;
            byte[] indices = layout.colorTable != null || layout.indexMap != null ? new byte[layout.width] : null;
            int fill = layout.colorTable != null && layout.shifts != null && layout.shifts.length == 3
                    ? 0xFF000000 : 0;
            byte[] input = new byte[Math.max(INPUT_SIZE, rowBytes + 1)];
            int inputLength = 0;
            idat = new byte[IDAT_SIZE];
            idatLength = 0;
            // Deflater.FILTERED (as libpng) makes photos ~10% smaller but ~2x slower, labels ~4% larger
            Deflater deflater = new Deflater(level);
            try {
                for (int y = 0; y < layout.height; y++) {
                    // current changes every row (it is swapped with previous)
                    byte[] target = layout.bitDepth == 8 ? current : indices;
                    if (argbInts) {
                        mapColors(layout.colorTable, layout.ints, layout.offset + y * layout.scanlineStride,
                                layout.width, fill, target);
                        if (layout.bitDepth < 8) {
                            pack(indices, current, layout.bitDepth);
                        }
                    } else if (colors != null) {
                        readColors(layout, y, colors);
                        mapColors(layout.colorTable, colors, 0, layout.width, 0, target);
                        if (layout.bitDepth < 8) {
                            pack(indices, current, layout.bitDepth);
                        }
                    } else if (indices != null) {
                        int start = layout.offset + y * layout.scanlineStride;
                        if (layout.sourceBits == 8) {
                            System.arraycopy(layout.bytes, start, indices, 0, layout.width);
                        } else {
                            unpack(layout.bytes, start, layout.sourceBits, indices);
                        }
                        packIndices(layout.indexMap, indices, current, layout.bitDepth);
                    } else {
                        readRow(layout, y, current, rowBytes);
                    }
                    if (padding != 0) {
                        current[rowBytes - 1] &= lastByteMask;
                    }
                    byte[] line;
                    if (adaptive) {
                        line = filtered[filterRow(current, previous, filtered, rowBytes, bpp)];
                    } else {
                        line = filtered[0];
                        filterRow(filter, current, previous, line, rowBytes, bpp);
                    }
                    // a call into zlib per row costs more than this copy for short rows
                    if (inputLength + rowBytes + 1 > input.length) {
                        deflate(deflater, input, inputLength);
                        inputLength = 0;
                    }
                    System.arraycopy(line, 0, input, inputLength, rowBytes + 1);
                    inputLength += rowBytes + 1;
                    byte[] swap = previous;
                    previous = current;
                    current = swap;
                }
                deflate(deflater, input, inputLength);
                deflater.finish();
                while (!deflater.finished()) {
                    idatLength += deflater.deflate(idat, idatLength, idat.length - idatLength);
                    if (idatLength == idat.length) {
                        chunk(IDAT, idat, idatLength);
                        idatLength = 0;
                    }
                }
                if (idatLength > 0) {
                    chunk(IDAT, idat, idatLength);
                }
            } finally {
                deflater.end();
            }
        }

        private void deflate(Deflater deflater, byte[] input, int length) throws IOException {
            deflater.setInput(input, 0, length);
            while (!deflater.needsInput()) {
                idatLength += deflater.deflate(idat, idatLength, idat.length - idatLength);
                if (idatLength == idat.length) {
                    chunk(IDAT, idat, idatLength);
                    idatLength = 0;
                }
            }
        }

        /**
         * Writes the palette index of each of {@code width} pixels from {@code start}; {@code fill}
         * is or-ed to a pixel to give its color (the alpha of pixels without one).
         */
        private static void mapColors(ColorTable table, int[] pixels, int start, int width, int fill,
                byte[] target) {
            int last = ~pixels[start];
            byte index = 0;
            for (int x = 0, p = start; x < width; x++, p++) {
                int pixel = pixels[p];
                if (pixel != last) {
                    last = pixel;
                    index = (byte) table.get(pixel | fill);
                }
                target[x] = index;
            }
        }

        /** Writes the new samples of a row of source samples, packed to the bit depth. */
        private static void packIndices(byte[] map, byte[] indices, byte[] row, int bits) {
            byte[] target = bits == 8 ? row : indices;
            for (int x = 0; x < indices.length; x++) {
                target[x] = map[indices[x] & 0xFF];
            }
            if (bits < 8) {
                pack(indices, row, bits);
            }
        }

        /** Reads one sample of 1, 2 or 4 bits per pixel into a byte each. */
        private static void unpack(byte[] data, int start, int bits, byte[] indices) {
            int mask = (1 << bits) - 1;
            int perByte = 8 / bits;
            for (int x = 0; x < indices.length; x++) {
                int value = data[start + x / perByte] & 0xFF;
                indices[x] = (byte) (value >> (8 - bits * (x % perByte + 1)) & mask);
            }
        }

        /** Packs indices of 1, 2 or 4 bits, the leftmost pixel in the highest bits. */
        private static void pack(byte[] indices, byte[] row, int bits) {
            int n = indices.length;
            int x = 0;
            int i = 0;
            // unrolled per bit depth: a loop over the pixels of a byte is several times slower
            switch (bits) {
            case 4:
                for (; x + 1 < n; x += 2) {
                    row[i++] = (byte) (indices[x] << 4 | indices[x + 1]);
                }
                break;
            case 2:
                for (; x + 3 < n; x += 4) {
                    row[i++] = (byte) (indices[x] << 6 | indices[x + 1] << 4 | indices[x + 2] << 2
                            | indices[x + 3]);
                }
                break;
            default:
                for (; x + 7 < n; x += 8) {
                    row[i++] = (byte) (indices[x] << 7 | indices[x + 1] << 6 | indices[x + 2] << 5
                            | indices[x + 3] << 4 | indices[x + 4] << 3 | indices[x + 5] << 2
                            | indices[x + 6] << 1 | indices[x + 7]);
                }
            }
            if (x < n) {
                int packed = 0;
                for (int shift = 8 - bits; x < n; x++, shift -= bits) {
                    packed |= indices[x] << shift;
                }
                row[i] = (byte) packed;
            }
        }

        private static void readRow(Layout layout, int y, byte[] row, int rowBytes) {
            int start = layout.offset + y * layout.scanlineStride;
            switch (layout.source) {
            case BYTE_ROWS:
                System.arraycopy(layout.bytes, start, row, 0, rowBytes);
                break;
            case BYTE_PIXELS: {
                byte[] data = layout.bytes;
                int[] bands = layout.bandOffsets;
                int stride = layout.pixelStride;
                int i = 0;
                for (int p = start, end = start + layout.width * stride; p < end; p += stride) {
                    for (int band : bands) {
                        row[i++] = data[p + band];
                    }
                }
                break;
            }
            default: {
                int[] data = layout.ints;
                int[] shifts = layout.shifts;
                int i = 0;
                if (shifts.length == 3) {
                    int r = shifts[0];
                    int g = shifts[1];
                    int b = shifts[2];
                    for (int p = start, end = start + layout.width; p < end; p++) {
                        int pixel = data[p];
                        row[i++] = (byte) (pixel >>> r);
                        row[i++] = (byte) (pixel >>> g);
                        row[i++] = (byte) (pixel >>> b);
                    }
                } else {
                    for (int p = start, end = start + layout.width; p < end; p++) {
                        int pixel = data[p];
                        for (int shift : shifts) {
                            row[i++] = (byte) (pixel >>> shift);
                        }
                    }
                }
            }
            }
        }

        /** Applies one filter, writing the result after the filter type byte of {@code line}. */
        private static void filterRow(PNGFilter filter, byte[] cur, byte[] prev, byte[] line, int n,
                int bpp) {
            switch (filter) {
            case SUB:
                System.arraycopy(cur, 0, line, 1, Math.min(bpp, n));
                for (int i = bpp; i < n; i++) {
                    line[i + 1] = (byte) (cur[i] - cur[i - bpp]);
                }
                break;
            case UP:
                for (int i = 0; i < n; i++) {
                    line[i + 1] = (byte) (cur[i] - prev[i]);
                }
                break;
            case AVERAGE:
                for (int i = 0; i < n; i++) {
                    int a = i >= bpp ? cur[i - bpp] & 0xFF : 0;
                    line[i + 1] = (byte) (cur[i] - ((a + (prev[i] & 0xFF)) >>> 1));
                }
                break;
            case PAETH:
                for (int i = 0; i < n; i++) {
                    int a = i >= bpp ? cur[i - bpp] & 0xFF : 0;
                    int c = i >= bpp ? prev[i - bpp] & 0xFF : 0;
                    line[i + 1] = (byte) (cur[i] - paethPredictor(a, prev[i] & 0xFF, c));
                }
                break;
            default:
                System.arraycopy(cur, 0, line, 1, n);
            }
        }

        /**
         * Applies all five filters and returns the one whose output has the smallest sum of
         * absolute values (bytes taken as signed), the heuristic of libpng.
         */
        private static int filterRow(byte[] cur, byte[] prev, byte[][] out, int n, int bpp) {
            byte[] none = out[FILTER_NONE];
            byte[] sub = out[FILTER_SUB];
            byte[] up = out[FILTER_UP];
            byte[] avg = out[FILTER_AVERAGE];
            byte[] paeth = out[FILTER_PAETH];
            long sumNone = 0;
            long sumSub = 0;
            long sumUp = 0;
            long sumAvg = 0;
            long sumPaeth = 0;
            for (int i = 0; i < n; i++) {
                int c = cur[i] & 0xFF;
                int b = prev[i] & 0xFF;
                int a = i >= bpp ? cur[i - bpp] & 0xFF : 0;
                int d = i >= bpp ? prev[i - bpp] & 0xFF : 0;
                byte vNone = (byte) c;
                byte vSub = (byte) (c - a);
                byte vUp = (byte) (c - b);
                byte vAvg = (byte) (c - ((a + b) >>> 1));
                byte vPaeth = (byte) (c - paethPredictor(a, b, d));
                none[i + 1] = vNone;
                sub[i + 1] = vSub;
                up[i + 1] = vUp;
                avg[i + 1] = vAvg;
                paeth[i + 1] = vPaeth;
                sumNone += Math.abs(vNone);
                sumSub += Math.abs(vSub);
                sumUp += Math.abs(vUp);
                sumAvg += Math.abs(vAvg);
                sumPaeth += Math.abs(vPaeth);
            }
            int best = FILTER_NONE;
            long min = sumNone;
            if (sumSub < min) {
                best = FILTER_SUB;
                min = sumSub;
            }
            if (sumUp < min) {
                best = FILTER_UP;
                min = sumUp;
            }
            if (sumAvg < min) {
                best = FILTER_AVERAGE;
                min = sumAvg;
            }
            if (sumPaeth < min) {
                best = FILTER_PAETH;
            }
            return best;
        }

        private static int paethPredictor(int a, int b, int c) {
            int p = a + b - c;
            int pa = Math.abs(p - a);
            int pb = Math.abs(p - b);
            int pc = Math.abs(p - c);
            if (pa <= pb && pa <= pc) {
                return a;
            }
            return pb <= pc ? b : c;
        }

        private void chunk(int type, byte[] data, int length) throws IOException {
            putInt(head, 0, length);
            putInt(head, 4, type);
            crc.reset();
            crc.update(head, 4, 4);
            crc.update(data, 0, length);
            putInt(tail, 0, (int) crc.getValue());
            out.write(head);
            out.write(data, 0, length);
            out.write(tail);
        }

        private static void putInt(byte[] target, int offset, int value) {
            target[offset] = (byte) (value >> 24);
            target[offset + 1] = (byte) (value >> 16);
            target[offset + 2] = (byte) (value >> 8);
            target[offset + 3] = (byte) value;
        }
    }
}
