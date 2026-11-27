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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.PNGFilter;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;
import org.apache.xmlgraphics.image.writer.imageio.ImageIOImageWriter;
import org.apache.xmlgraphics.image.writer.imageio.ImageIOPNGImageWriter;

public class DirectPNGEncoderTestCase {

    /** Odd width so packed rows end in a partly used byte. */
    private static final int WIDTH = 37;
    private static final int HEIGHT = 23;

    private static final int[] TYPES = {
        BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_BYTE_GRAY,
        BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED
    };

    @Test
    public void testStandardTypesRoundTrip() throws IOException {
        for (int type : TYPES) {
            BufferedImage image = draw(new BufferedImage(WIDTH, HEIGHT, type));
            assertTrue("type " + type, DirectPNGEncoder.canEncode(image));
            for (PNGFilter filter : PNGFilter.values()) {
                assertRoundTrip("type " + type + ", " + filter, image, params(4, filter));
            }
            assertRoundTrip("type " + type + ", default", image, null);
        }
    }

    @Test
    public void testPalettesRoundTrip() throws IOException {
        for (int bits : new int[] {1, 2, 4, 8}) {
            BufferedImage image = noise(palette(bits, false), 1 << bits);
            assertEquals(3, colorType(encode(image, null)));
            assertEquals(bits, bitDepth(encode(image, null)));
            for (PNGFilter filter : PNGFilter.values()) {
                assertRoundTrip(bits + " bits, " + filter, image, params(6, filter));
            }
        }
    }

    @Test
    public void testPaletteWithTransparency() throws IOException {
        BufferedImage image = noise(palette(4, true), 3);
        byte[] png = encode(image, null);
        List<String> chunks = chunks(png);
        assertEquals(Arrays.asList("IHDR", "PLTE", "tRNS", "IDAT", "IEND"), chunks);
        assertRoundTrip("transparent palette", image, null);
    }

    @Test
    public void testGrayPaletteWrittenAsGray() throws IOException {
        // TYPE_BYTE_BINARY is a black and white palette, which PNG stores as 1 bit gray (as Image I/O)
        byte[] png = encode(draw(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_BINARY)), null);
        assertEquals(0, colorType(png));
        assertEquals(1, bitDepth(png));
        assertFalse(chunks(png).contains("PLTE"));
    }

    @Test
    public void testGrayAlpha() throws IOException {
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        WritableRaster raster = colorModel.createCompatibleWritableRaster(WIDTH, HEIGHT);
        BufferedImage image = noise(new BufferedImage(colorModel, raster, false, null), 256);
        assertTrue(DirectPNGEncoder.canEncode(image));
        assertEquals(4, colorType(encode(image, null)));
        assertRoundTrip("gray alpha", image, null);
    }

    @Test
    public void testSubimage() throws IOException {
        for (int type : TYPES) {
            BufferedImage image = draw(new BufferedImage(WIDTH + 20, HEIGHT + 10, type));
            // packed 1 bit pixels can only be copied as bytes when the subimage starts on a byte
            int x = type == BufferedImage.TYPE_BYTE_BINARY ? 8 : 5;
            BufferedImage sub = image.getSubimage(x, 3, WIDTH, HEIGHT);
            assertTrue("type " + type, DirectPNGEncoder.canEncode(sub));
            assertRoundTrip("subimage of type " + type, sub, null);
        }
        BufferedImage binary = new BufferedImage(WIDTH + 20, HEIGHT, BufferedImage.TYPE_BYTE_BINARY);
        assertFalse(DirectPNGEncoder.canEncode(binary.getSubimage(3, 0, WIDTH, HEIGHT)));
    }

    @Test
    public void testLargeImageSpansSeveralIDATChunks() throws IOException {
        BufferedImage image = noise(new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB), 1 << 24);
        byte[] png = encode(image, params(9, PNGFilter.ADAPTIVE));
        int idats = 0;
        for (String chunk : chunks(png)) {
            if ("IDAT".equals(chunk)) {
                idats++;
            }
        }
        assertTrue(idats > 1);
        assertRoundTrip("large", image, params(0, null));
        assertRoundTrip("large", image, params(1, null));
    }

    @Test
    public void testResolutionAsImageIO() throws IOException {
        BufferedImage image = draw(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));
        ImageWriterParams params = new ImageWriterParams();
        params.setPNGReduceColors(false);
        params.setXResolution(300);
        params.setYResolution(150);
        ByteArrayOutputStream imageIO = new ByteArrayOutputStream();
        new ImageIOImageWriter("image/png").writeImage(image, imageIO, params);
        byte[] direct = encode(image, params);
        assertArrayEquals(chunk(imageIO.toByteArray(), "pHYs"), chunk(direct, "pHYs"));
        assertEquals(Arrays.asList("IHDR", "pHYs", "IDAT", "IEND"), chunks(direct));

        params.setResolutionUnit(ResolutionUnit.CENTIMETER);
        byte[] perCentimetre = chunk(encode(image, params), "pHYs");
        assertEquals(30000, readInt(perCentimetre, 0));
        assertEquals(15000, readInt(perCentimetre, 4));
    }

    @Test
    public void testUnsupportedImagesFallBackToImageIO() throws IOException {
        BufferedImage[] images = {
            new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_GRAY),
            new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE),
            new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_565_RGB)
        };
        for (BufferedImage image : images) {
            draw(image);
            assertFalse("type " + image.getType(), DirectPNGEncoder.canEncode(image));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ImageIOPNGImageWriter().writeImage(image, out, null);
            assertPixelsEqual("type " + image.getType(), image, decode(out.toByteArray()));
        }
    }

    @Test
    public void testWritersUseDirectEncoder() throws IOException {
        BufferedImage image = noise(palette(2, false), 4);
        byte[] expected = encode(image, null);
        ByteArrayOutputStream imageIO = new ByteArrayOutputStream();
        new ImageIOPNGImageWriter().writeImage(image, imageIO, null);
        assertArrayEquals(expected, imageIO.toByteArray());
        ByteArrayOutputStream internal = new ByteArrayOutputStream();
        new PNGImageWriter().writeImage(image, internal, null);
        assertArrayEquals(expected, internal.toByteArray());
    }

    @Test
    public void testDefaultFilters() throws IOException {
        // no filter for every layout unless one is set
        assertEquals(0, firstRowFilter(draw(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB))));
        assertEquals(0, firstRowFilter(noise(palette(8, false), 200)));
        assertEquals(0, firstRowFilter(noise(palette(4, false), 16)));
        assertEquals(0, firstRowFilter(noise(palette(2, false), 4)));
        assertEquals(1, firstRowFilter(noise(palette(2, false), 4), PNGFilter.SUB));
        assertEquals(2, firstRowFilter(noise(palette(8, false), 200), PNGFilter.UP));
    }

    @Test
    public void testTruecolorWithFewColorsWrittenAsPalette() throws IOException {
        int[] types = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR,
            BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_4BYTE_ABGR};
        int[][] colorsAndBits = {{2, 1}, {3, 2}, {4, 2}, {5, 4}, {16, 4}, {17, 8}, {256, 8}};
        for (int type : types) {
            for (int[] expected : colorsAndBits) {
                BufferedImage image = colors(new BufferedImage(WIDTH * 4, HEIGHT, type), expected[0], false);
                byte[] png = encode(image, null);
                String message = "type " + type + ", " + expected[0] + " colors";
                assertEquals(message, 3, colorType(png));
                assertEquals(message, expected[1], bitDepth(png));
                assertEquals(message, expected[0] * 3, chunk(png, "PLTE").length);
                assertPixelsEqual(message, image, decode(png));
            }
        }
    }

    @Test(timeout = 10000)
    public void testSingleColor() throws IOException {
        int[] types = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_3BYTE_BGR,
            BufferedImage.TYPE_4BYTE_ABGR};
        for (int type : types) {
            for (int color : new int[] {0xFFFFFFFF, 0xFF000000, 0x00000000}) {
                BufferedImage image = new BufferedImage(WIDTH, HEIGHT, type);
                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 0; x < WIDTH; x++) {
                        image.setRGB(x, y, color);
                    }
                }
                byte[] png = encode(image, null);
                String message = "type " + type + ", color " + Integer.toHexString(color);
                boolean transparent = color >>> 24 == 0 && image.getColorModel().hasAlpha();
                // opaque black or white is 1 bit gray, transparent black a palette with tRNS
                assertEquals(message, transparent ? 3 : 0, colorType(png));
                assertEquals(message, 1, bitDepth(png));
                assertPixelsEqual(message, image, decode(png));
            }
        }
    }

    @Test
    public void testTransparentColorsComeFirstInPalette() throws IOException {
        BufferedImage image = colors(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB), 10, true);
        byte[] png = encode(image, null);
        assertEquals(3, colorType(png));
        // colors() makes every second color translucent: 5 of 10
        assertEquals(5, chunk(png, "tRNS").length);
        assertPixelsEqual("translucent", image, decode(png));
    }

    @Test
    public void testManyColorsStayTruecolor() throws IOException {
        BufferedImage rgb = colors(new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_INT_RGB), 257, false);
        assertEquals(2, colorType(encode(rgb, null)));
        assertRoundTrip("257 colors", rgb, null);

        // alpha opaque everywhere is left out
        BufferedImage opaque = colors(new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR), 300,
                false);
        assertEquals(2, colorType(encode(opaque, null)));
        assertRoundTrip("opaque ABGR", opaque, null);
        BufferedImage opaqueInt = colors(new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_INT_ARGB), 300,
                false);
        assertEquals(2, colorType(encode(opaqueInt, null)));
        assertRoundTrip("opaque ARGB", opaqueInt, null);

        BufferedImage translucent = colors(new BufferedImage(WIDTH * 8, HEIGHT, BufferedImage.TYPE_INT_ARGB),
                300, true);
        assertEquals(6, colorType(encode(translucent, null)));
        assertRoundTrip("translucent ARGB", translucent, null);
    }

    @Test
    public void testPaletteWithFewUsedColorsGetsFewerBits() throws IOException {
        BufferedImage image = noise(palette(8, true), 3);
        byte[] png = encode(image, null);
        assertEquals(3, colorType(png));
        assertEquals(2, bitDepth(png));
        assertEquals(9, chunk(png, "PLTE").length);
        assertPixelsEqual("3 of 256 colors", image, decode(png));

        BufferedImage sixteen = noise(palette(8, false), 16);
        assertEquals(4, bitDepth(encode(sixteen, null)));
        assertRoundTrip("16 of 256 colors", sixteen, null);
    }

    @Test
    public void testReduceColorsOff() throws IOException {
        ImageWriterParams params = new ImageWriterParams();
        params.setPNGReduceColors(false);
        BufferedImage rgb = colors(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB), 3, false);
        assertEquals(2, colorType(encode(rgb, params)));
        BufferedImage argb = colors(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB), 3, false);
        assertEquals(6, colorType(encode(argb, params)));
        BufferedImage palette = noise(palette(8, false), 3);
        assertEquals(8, bitDepth(encode(palette, params)));
        assertRoundTrip("rgb", rgb, params);
        assertRoundTrip("palette", palette, params);
    }

    @Test
    public void testGrayWithFewLevels() throws IOException {
        // levels; color type and bit depth from TYPE_BYTE_GRAY; from TYPE_INT_RGB
        // (8 bit gray is linear in Java, so it only becomes 1 bit gray when black and white)
        Object[][] cases = {
            {new int[] {0, 255}, 0, 1, 0, 1},
            {new int[] {0, 85, 170, 255}, 0, 8, 0, 2},
            {new int[] {0, 17, 34, 51, 255}, 0, 8, 0, 4},
            {new int[] {0, 17, 34}, 0, 8, 3, 2}, // a 2 bit palette is smaller than 4 bit gray
            {new int[] {0, 128, 255}, 0, 8, 3, 2},
            {new int[] {3, 7, 9, 11, 13, 15, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39}, 0, 8, 3, 8}
        };
        for (Object[] c : cases) {
            int[] levels = (int[]) c[0];
            BufferedImage gray = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage rgb = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            WritableRaster raster = gray.getRaster();
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int level = levels[(x + y * WIDTH) % levels.length];
                    raster.setSample(x, y, 0, level);
                    rgb.setRGB(x, y, level * 0x010101);
                }
            }
            BufferedImage[] images = {gray, rgb};
            for (int i = 0; i < images.length; i++) {
                String message = Arrays.toString(levels) + " type " + images[i].getType();
                byte[] png = encode(images[i], null);
                assertEquals(message, c[1 + 2 * i], colorType(png));
                assertEquals(message, c[2 + 2 * i], bitDepth(png));
                assertPixelsEqual(message, images[i], decode(png));
            }
        }
    }

    @Test
    public void testGrayTruecolorStaysColor() throws IOException {
        // gray ARGB with more than 256 colors: not gray + alpha, which Java reads as linear gray
        BufferedImage argb = new BufferedImage(WIDTH * 4, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < argb.getWidth() * argb.getHeight(); i++) {
            int level = i % 251;
            int alpha = 1 + i % 254;
            argb.setRGB(i % argb.getWidth(), i / argb.getWidth(), alpha << 24 | level * 0x010101);
        }
        assertEquals(6, colorType(encode(argb, null)));
        assertRoundTrip("gray ARGB", argb, null);

        // gray + alpha source that is opaque: gray
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        BufferedImage grayAlpha = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(WIDTH,
                HEIGHT), false, null);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grayAlpha.getRaster().setSample(x, y, 0, (x * 7 + y) % 256);
                grayAlpha.getRaster().setSample(x, y, 1, 255);
            }
        }
        byte[] png = encode(grayAlpha, null);
        assertEquals(0, colorType(png));
        assertEquals(8, bitDepth(png));
        assertRoundTrip("opaque gray alpha", grayAlpha, null);
    }

    @Test
    public void testPackedPaletteWithFewUsedEntries() throws IOException {
        // 4 bit palette using 3 entries: 2 bits
        BufferedImage four = noise(palette(4, false), 3);
        byte[] png = encode(four, null);
        assertEquals(3, colorType(png));
        assertEquals(2, bitDepth(png));
        assertPixelsEqual("4 bit, 3 used", four, decode(png));

        // 2 bit palette using entries 0 to 2: stays 2 bits, the unused last entry is left out
        BufferedImage two = noise(palette(2, false), 3);
        png = encode(two, null);
        assertEquals(2, bitDepth(png));
        assertEquals(9, chunk(png, "PLTE").length);
        assertPixelsEqual("2 bit, 3 used", two, decode(png));
    }

    @Test
    public void testPaletteWrittenUpToLastUsedEntry() throws IOException {
        int size = 256;
        byte[] levels = new byte[size];
        for (int i = 0; i < size; i++) {
            levels[i] = (byte) (255 - i); // gray, but not in gray order
        }
        IndexColorModel colorModel = new IndexColorModel(8, size, levels, levels, levels);
        BufferedImage image = noise(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, colorModel),
                100);
        // a gray palette stays a palette (8 bit gray would be read as linear gray), but only the
        // entries up to the last one used are written
        byte[] png = encode(image, null);
        assertEquals(3, colorType(png));
        assertEquals(8, bitDepth(png));
        assertEquals(300, chunk(png, "PLTE").length);
        assertPixelsEqual("gray palette", image, decode(png));
    }

    @Test
    public void testReduceColorsOffKeepsEveryMode() throws IOException {
        ImageWriterParams off = new ImageWriterParams();
        off.setPNGReduceColors(false);
        BufferedImage gray = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY); // all black
        assertEquals(0, colorType(encode(gray, off)));
        assertEquals(8, bitDepth(encode(gray, off)));
        BufferedImage four = noise(palette(4, false), 3);
        assertEquals(4, bitDepth(encode(four, off)));
        assertEquals(48, chunk(encode(four, off), "PLTE").length);
        BufferedImage two = noise(palette(2, false), 3);
        assertEquals(12, chunk(encode(two, off), "PLTE").length);
        BufferedImage white = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        assertEquals(2, colorType(encode(white, off)));
        for (BufferedImage image : new BufferedImage[] {gray, four, two, white}) {
            assertRoundTrip("off", image, off);
        }
    }

    @Test
    public void testEightBitGrayRampPaletteStaysPalette() throws IOException {
        // 8 bit gray would be read back as linear gray and change every color
        byte[] levels = new byte[256];
        for (int i = 0; i < 256; i++) {
            levels[i] = (byte) i;
        }
        IndexColorModel colorModel = new IndexColorModel(8, 256, levels, levels, levels);
        BufferedImage image = noise(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, colorModel),
                256);
        ImageWriterParams off = new ImageWriterParams();
        off.setPNGReduceColors(false);
        assertEquals(3, colorType(encode(image, off)));
        assertRoundTrip("8 bit gray ramp, reduce off", image, off);
        assertRoundTrip("8 bit gray ramp", image, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompressionLevelRange() {
        new ImageWriterParams().setCompressionLevel(10);
    }

    private static ImageWriterParams params(int level, PNGFilter filter) {
        ImageWriterParams params = new ImageWriterParams();
        params.setCompressionLevel(level);
        params.setPNGFilter(filter);
        return params;
    }

    private static BufferedImage draw(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(new Color(200, 30, 40, 180));
        g.fillOval(3, 2, image.getWidth() - 6, image.getHeight() - 4);
        g.setColor(Color.BLACK);
        g.drawLine(0, image.getHeight() - 1, image.getWidth() - 1, 0);
        g.setColor(new Color(0, 0, 255, 0));
        g.fillRect(0, 0, 4, 4);
        g.dispose();
        return image;
    }

    /** Fills every band with seeded random samples below {@code limit}. */
    private static BufferedImage noise(BufferedImage image, int limit) {
        Random random = new Random(limit);
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int b = 0; b < raster.getNumBands(); b++) {
                    int max = Math.min(limit, 1 << raster.getSampleModel().getSampleSize(b));
                    raster.setSample(x, y, b, random.nextInt(max));
                }
            }
        }
        return image;
    }

    /**
     * Fills an image with exactly {@code count} colors (it needs at least that many pixels);
     * with {@code translucent} every second color has an alpha below 255.
     */
    private static BufferedImage colors(BufferedImage image, int count, boolean translucent) {
        int pixels = image.getWidth() * image.getHeight();
        for (int i = 0; i < pixels; i++) {
            int c = i % count;
            int alpha = translucent && c % 2 == 1 ? 40 + c % 200 : 255;
            int rgb = (c * 97 % 256) << 16 | (c * 31 % 256) << 8 | c % 256 ^ c / 256 * 0x55;
            image.setRGB(i % image.getWidth(), i / image.getWidth(), alpha << 24 | rgb);
        }
        return image;
    }

    private static BufferedImage palette(int bits, boolean transparent) {
        int size = 1 << bits;
        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        byte[] a = new byte[size];
        for (int i = 0; i < size; i++) {
            r[i] = (byte) (i * 37);
            g[i] = (byte) (255 - i * 11);
            b[i] = (byte) (i * 101);
            a[i] = (byte) (transparent && i < size / 2 ? i * 40 : 255);
        }
        IndexColorModel colorModel = new IndexColorModel(bits, size, r, g, b, a);
        int type = bits == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY;
        return new BufferedImage(WIDTH, HEIGHT, type, colorModel);
    }

    private static byte[] encode(BufferedImage image, ImageWriterParams params) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DirectPNGEncoder.encode(image, out, params);
        return out.toByteArray();
    }

    private static BufferedImage decode(byte[] png) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image);
        return image;
    }

    private static void assertRoundTrip(String message, BufferedImage image, ImageWriterParams params)
            throws IOException {
        assertPixelsEqual(message, image, decode(encode(image, params)));
    }

    private static void assertPixelsEqual(String message, BufferedImage expected, BufferedImage actual) {
        assertEquals(message, expected.getWidth(), actual.getWidth());
        assertEquals(message, expected.getHeight(), actual.getHeight());
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int e = expected.getRGB(x, y);
                int a = actual.getRGB(x, y);
                if ((e >>> 24) == 0 && (a >>> 24) == 0) {
                    continue; // fully transparent: color does not matter
                }
                assertEquals(message + " at " + x + "," + y, Integer.toHexString(e), Integer.toHexString(a));
            }
        }
    }

    private static int firstRowFilter(BufferedImage image) throws IOException {
        return firstRowFilter(image, null);
    }

    private static int firstRowFilter(BufferedImage image, PNGFilter filter) throws IOException {
        // level 0 stores the data: zlib header (2), stored block header (5), then the first row
        byte[] idat = chunk(encode(image, params(0, filter)), "IDAT");
        return idat[7];
    }

    private static int colorType(byte[] png) {
        return chunk(png, "IHDR")[9];
    }

    private static int bitDepth(byte[] png) {
        return chunk(png, "IHDR")[8];
    }

    private static List<String> chunks(byte[] png) {
        List<String> types = new ArrayList<String>();
        for (int pos = 8; pos < png.length; pos += 12 + readInt(png, pos)) {
            types.add(new String(png, pos + 4, 4, java.nio.charset.StandardCharsets.US_ASCII));
        }
        return types;
    }

    /** The data of the first chunk of a type. */
    private static byte[] chunk(byte[] png, String type) {
        for (int pos = 8; pos < png.length; pos += 12 + readInt(png, pos)) {
            if (type.equals(new String(png, pos + 4, 4, java.nio.charset.StandardCharsets.US_ASCII))) {
                return Arrays.copyOfRange(png, pos + 8, pos + 8 + readInt(png, pos));
            }
        }
        throw new AssertionError("no " + type + " chunk");
    }

    private static int readInt(byte[] data, int pos) {
        return (data[pos] & 0xFF) << 24 | (data[pos + 1] & 0xFF) << 16 | (data[pos + 2] & 0xFF) << 8
                | data[pos + 3] & 0xFF;
    }
}
