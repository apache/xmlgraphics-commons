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
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.util.io.ASCII85OutputStream;
import org.apache.xmlgraphics.util.io.Finalizable;
import org.apache.xmlgraphics.util.io.FlateEncodeOutputStream;
import org.apache.xmlgraphics.util.io.RunLengthEncodeOutputStream;

// CSOFF: HideUtilityClassConstructor

/**
 * Utility code for rendering images in PostScript.
 */
public class PSImageUtils {

    public PSImageUtils() {
    }

    /**
     * Writes a bitmap image to the PostScript stream.
     * @param img the bitmap image as a byte array
     * @param imgDim the dimensions of the image
     * @param imgDescription the name of the image
     * @param targetRect the target rectangle to place the image in
     * @param isJPEG true if "img" contains a DCT-encoded images, false if "img" contains the
     *               decoded bitmap
     * @param colorSpace the color space of the image
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     * @deprecated Please use the variant with the more versatile ImageEncoder as parameter
     */
    public static void writeImage(final byte[] img,
            Dimension imgDim, String imgDescription,
            Rectangle2D targetRect,
            final boolean isJPEG, ColorSpace colorSpace,
            PSGenerator gen) throws IOException {
        ImageEncoder encoder = new ImageEncoder() {
            public void writeTo(OutputStream out) throws IOException {
                out.write(img);
            }

            public String getImplicitFilter() {
                if (isJPEG) {
                    return "<< >> /DCTDecode";
                } else {
                    return null;
                }
            }
        };
        writeImage(encoder, imgDim, imgDescription, targetRect, colorSpace, 8, false, gen);
    }

    /**
     * Writes a bitmap image to the PostScript stream.
     * @param encoder the image encoder
     * @param imgDim the dimensions of the image
     * @param imgDescription the name of the image
     * @param targetRect the target rectangle to place the image in
     * @param colorSpace the color space of the image
     * @param bitsPerComponent the number of bits per component
     * @param invertImage true if the image shall be inverted
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     */
    public static void writeImage(ImageEncoder encoder,
            Dimension imgDim, String imgDescription,
            Rectangle2D targetRect,
            ColorSpace colorSpace, int bitsPerComponent, boolean invertImage,
            PSGenerator gen) throws IOException {
        gen.saveGraphicsState();
        translateAndScale(gen, null, targetRect);

        gen.commentln("%AXGBeginBitmap: " + imgDescription);

        gen.writeln("{{");
        // Template: (RawData is used for the EOF signal only)
        // gen.write("/RawData currentfile <first filter> filter def");
        // gen.write("/Data RawData <second filter> <third filter> [...] def");
        String implicitFilter = encoder.getImplicitFilter();
        if (implicitFilter != null) {
            gen.writeln("/RawData currentfile /ASCII85Decode filter def");
            gen.writeln("/Data RawData " + implicitFilter + " filter def");
        } else {
            if (gen.getPSLevel() >= 3) {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /FlateDecode filter def");
            } else {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /RunLengthDecode filter def");
            }
        }
        PSDictionary imageDict = new PSDictionary();
        imageDict.put("/DataSource", "Data");
        imageDict.put("/BitsPerComponent", Integer.toString(bitsPerComponent));
        writeImageCommand(imageDict, imgDim, colorSpace, invertImage, gen);
        /* the following two lines could be enabled if something still goes wrong
         * gen.write("Data closefile");
         * gen.write("RawData flushfile");
         */
        gen.writeln("} stopped {handleerror} if");
        gen.writeln("  RawData flushfile");
        gen.writeln("} exec");

        compressAndWriteBitmap(encoder, gen);

        gen.newLine();
        gen.commentln("%AXGEndBitmap");
        gen.restoreGraphicsState();
    }

    public static void writeImage(ImageEncoder encoder, Dimension imgDim, String imgDescription,
                                  Rectangle2D targetRect, ColorModel colorModel, PSGenerator gen) throws IOException {
        writeImage(encoder, imgDim, imgDescription, targetRect, colorModel, gen, null);
    }

    /**
     * Writes a bitmap image to the PostScript stream.
     * @param encoder the image encoder
     * @param imgDim the dimensions of the image
     * @param imgDescription the name of the image
     * @param targetRect the target rectangle to place the image in
     * @param colorModel the color model of the image
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     */
    public static void writeImage(ImageEncoder encoder, Dimension imgDim, String imgDescription,
            Rectangle2D targetRect, ColorModel colorModel, PSGenerator gen, RenderedImage ri)
            throws IOException {

        gen.saveGraphicsState();
        translateAndScale(gen, null, targetRect);
        gen.commentln("%AXGBeginBitmap: " + imgDescription);
        gen.writeln("{{");

        String implicitFilter = encoder.getImplicitFilter();
        if (implicitFilter != null) {
            gen.writeln("/RawData currentfile /ASCII85Decode filter def");
            gen.writeln("/Data RawData " + implicitFilter + " filter def");
        } else {
            if (gen.getPSLevel() >= 3) {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /FlateDecode filter def");
            } else {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /RunLengthDecode filter def");
            }
        }

        PSDictionary imageDict = new PSDictionary();
        imageDict.put("/DataSource", "Data");

        populateImageDictionary(imgDim, colorModel, imageDict);

        if (ri != null) {
            DataBuffer buffer = ri.getData().getDataBuffer();
            if (!(buffer instanceof DataBufferByte)) {
                imageDict.put("/BitsPerComponent", 8);
            }
        }
        writeImageCommand(imageDict, colorModel, gen);

        /*
         * the following two lines could be enabled if something still goes wrong
         * gen.write("Data closefile");
         * gen.write("RawData flushfile");
         */
        gen.writeln("} stopped {handleerror} if");
        gen.writeln("  RawData flushfile");
        gen.writeln("} exec");

        compressAndWriteBitmap(encoder, gen);

        gen.newLine();
        gen.commentln("%AXGEndBitmap");
        gen.restoreGraphicsState();
    }

    /**
     * Writes a bitmap image to the PostScript stream.
     * @param encoder the image encoder
     * @param imgDim the dimensions of the image
     * @param imgDescription the name of the image
     * @param targetRect the target rectangle to place the image in
     * @param colorModel the color model of the image
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     */
    public static void writeImage(ImageEncoder encoder, Dimension imgDim, String imgDescription,
            Rectangle2D targetRect, ColorModel colorModel, PSGenerator gen, RenderedImage ri,
            Color maskColor)
            throws IOException {

        gen.saveGraphicsState();
        translateAndScale(gen, null, targetRect);
        gen.commentln("%AXGBeginBitmap: " + imgDescription);
        gen.writeln("{{");

        String implicitFilter = encoder.getImplicitFilter();
        if (implicitFilter != null) {
            gen.writeln("/RawData currentfile /ASCII85Decode filter def");
            gen.writeln("/Data RawData " + implicitFilter + " filter def");
        } else {
            if (gen.getPSLevel() >= 3) {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /FlateDecode filter def");
            } else {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /RunLengthDecode filter def");
            }
        }

        PSDictionary imageDict = new PSDictionary();
        imageDict.put("/DataSource", "Data");

        populateImageDictionary(imgDim, colorModel, imageDict, maskColor);

        if (ri != null) {
            DataBuffer buffer = ri.getData().getDataBuffer();
            if (!(buffer instanceof DataBufferByte)) {
                imageDict.put("/BitsPerComponent", 8);
            }
        }
        writeImageCommand(imageDict, colorModel, gen);

        /*
         * the following two lines could be enabled if something still goes wrong
         * gen.write("Data closefile");
         * gen.write("RawData flushfile");
         */
        gen.writeln("} stopped {handleerror} if");
        gen.writeln("  RawData flushfile");
        gen.writeln("} exec");

        compressAndWriteBitmap(encoder, gen);

        gen.newLine();
        gen.commentln("%AXGEndBitmap");
        gen.restoreGraphicsState();
    }

    private static ColorModel populateImageDictionary(Dimension imgDim, ColorModel colorModel,
            PSDictionary imageDict) {
        imageDict.put("/ImageType", "1");
        colorModel = writeImageDictionary(imgDim, imageDict, colorModel);
        return colorModel;
    }

    private static ColorModel populateImageDictionary(Dimension imgDim, ColorModel colorModel,
            PSDictionary imageDict, Color maskColor) {
        imageDict.put("/ImageType", "4");

        colorModel = writeImageDictionary(imgDim, imageDict, colorModel);
        imageDict.put("/MaskColor", String.format("[ %d %d %d ]", maskColor.getRed(),
                maskColor.getGreen(), maskColor.getBlue()));
        return colorModel;
    }

    private static ColorModel writeImageDictionary(Dimension imgDim, PSDictionary imageDict,
            ColorModel colorModel) {
        String w = Integer.toString(imgDim.width);
        String h = Integer.toString(imgDim.height);
        imageDict.put("/Width", w);
        imageDict.put("/Height", h);

        boolean invertColors = false;
        String decodeArray = getDecodeArray(colorModel.getNumColorComponents(), invertColors);
        int bitsPerComp = colorModel.getComponentSize(0);

        // Setup scanning for left-to-right and top-to-bottom
        imageDict.put("/ImageMatrix", "[" + w + " 0 0 " + h + " 0 0]");

        if ((colorModel instanceof IndexColorModel)) {
            IndexColorModel indexColorModel = (IndexColorModel) colorModel;
            int c = indexColorModel.getMapSize();
            int hival = c - 1;
            if (hival > 4095) {
                throw new UnsupportedOperationException("hival must not go beyond 4095");
            }
            bitsPerComp = indexColorModel.getPixelSize();
            int ceiling = ((int) Math.pow(2, bitsPerComp)) - 1;
            decodeArray = "[0 " + ceiling + "]";
        }
        imageDict.put("/BitsPerComponent", Integer.toString(bitsPerComp));
        imageDict.put("/Decode", decodeArray);
        return colorModel;
    }

    private static String getDecodeArray(int numComponents, boolean invertColors) {
        String decodeArray;
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < numComponents; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (invertColors) {
                sb.append("1 0");
            } else {
                sb.append("0 1");
            }
        }
        sb.append("]");
        decodeArray = sb.toString();
        return decodeArray;
    }

    private static void prepareColorspace(PSGenerator gen, ColorSpace colorSpace)
            throws IOException {
        gen.writeln(getColorSpaceName(colorSpace) + " setcolorspace");
    }

    private static void prepareColorSpace(PSGenerator gen, ColorModel cm) throws IOException {
        //Prepare color space
        if ((cm instanceof IndexColorModel)) {
            ColorSpace cs = cm.getColorSpace();
            IndexColorModel im = (IndexColorModel)cm;
            boolean isDeviceGray;
            int c = im.getMapSize();
            int[] palette = new int[c];
            im.getRGBs(palette);
            byte[] reds = new byte[c];
            byte[] greens = new byte[c];
            byte[] blues = new byte[c];
            im.getReds(reds);
            im.getGreens(greens);
            im.getBlues(blues);
            int hival = c - 1;
            if (hival > 4095) {
                throw new UnsupportedOperationException("hival must not go beyond 4095");
            }
            isDeviceGray = Arrays.equals(reds, blues) && Arrays.equals(blues, greens);
            if (isDeviceGray) {
                gen.write("[/Indexed " + "/DeviceGray");
            } else {
                gen.write("[/Indexed " + getColorSpaceName(cs));
            }
            gen.writeln(" " + Integer.toString(hival));
            gen.write("  <");
            if (isDeviceGray) {
                gen.write(toHexString(blues));
            } else {
                for (int i = 0; i < c; i++) {
                    if (i > 0) {
                        if ((i % 8) == 0) {
                            gen.newLine();
                            gen.write("   ");
                        } else {
                            gen.write(" ");
                        }
                    }
                    gen.write(rgb2Hex(palette[i]));
                }
            }
            gen.writeln(">");
            gen.writeln("] setcolorspace");
        } else {
            gen.writeln(getColorSpaceName(cm.getColorSpace()) + " setcolorspace");
        }
    }

    static String toHexString(byte[] color) {
        char[] hexChars = new char[color.length * 2];
        int x;
        for (int i = 0; i < color.length; i++) {
            x = color[i] & 0xFF;
            hexChars[i * 2] = HEX[x >>> 4];
            hexChars[i * 2 + 1] = HEX[x & 0x0F];
        }
        return new String(hexChars);
    }

    static void writeImageCommand(RenderedImage img,
            PSDictionary imageDict, PSGenerator gen) throws IOException {
        ImageEncodingHelper helper = new ImageEncodingHelper(img, true);
        ColorModel cm = helper.getEncodedColorModel();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());

        populateImageDictionary(imgDim, cm, imageDict);
        writeImageCommand(imageDict, cm, gen);
    }

    static void writeImageCommand(PSDictionary imageDict, ColorModel cm, PSGenerator gen)
                throws IOException {
        prepareColorSpace(gen, cm);
        gen.write(imageDict.toString());
        gen.writeln(" image");
    }

    static void writeImageCommand(PSDictionary imageDict,
            Dimension imgDim, ColorSpace colorSpace, boolean invertImage,
            PSGenerator gen) throws IOException {
        imageDict.put("/ImageType", "1");
        imageDict.put("/Width", Integer.toString(imgDim.width));
        imageDict.put("/Height", Integer.toString(imgDim.height));
        String decodeArray = getDecodeArray(colorSpace.getNumComponents(), invertImage);
        imageDict.put("/Decode", decodeArray);
        // Setup scanning for left-to-right and top-to-bottom
        imageDict.put("/ImageMatrix", "[" + imgDim.width + " 0 0 " + imgDim.height + " 0 0]");

        prepareColorspace(gen, colorSpace);
        gen.write(imageDict.toString());
        gen.writeln(" image");
    }

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

    private static String rgb2Hex(int rgb) {
        StringBuffer sb = new StringBuffer();
        for (int i = 5; i >= 0; i--) {
            int shift = i * 4;
            int n = (rgb & (15 << shift)) >> shift;
            sb.append(HEX[n % 16]);
        }
        return sb.toString();
    }

    /**
     * Renders a bitmap image to PostScript.
     * @param img image to render
     * @param x x position
     * @param y y position
     * @param w width
     * @param h height
     * @param gen PS generator
     * @throws IOException In case of an I/O problem while rendering the image
     */
    public static void renderBitmapImage(RenderedImage img,
                float x, float y, float w, float h, PSGenerator gen, Color mask)
                    throws IOException {
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        ImageEncoder encoder = ImageEncodingHelper.createRenderedImageEncoder(img);
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        String imgDescription = img.getClass().getName();
        ImageEncodingHelper helper = new ImageEncodingHelper(img);
        ColorModel cm = helper.getEncodedColorModel();

        if (mask == null) {
            writeImage(encoder, imgDim, imgDescription, targetRect, cm, gen, img);
        } else {
            writeImage(encoder, imgDim, imgDescription, targetRect, cm, gen, img, mask);
        }
    }

    /**
     * Writes a bitmap image as a PostScript form enclosed by DSC resource wrappers to the
     * PostScript file.
     * @param img the raw bitmap data
     * @param imgDim the dimensions of the image
     * @param formName the name of the PostScript form to use
     * @param imageDescription a description of the image added as a DSC Title comment
     * @param isJPEG true if "img" contains a DCT-encoded images, false if "img" contains the
     *               decoded bitmap
     * @param colorSpace the color space of the image
     * @param gen the PostScript generator
     * @return a PSResource representing the form for resource tracking
     * @throws IOException In case of an I/O exception
     * @deprecated Please use {@link FormGenerator}
     */
    public static PSResource writeReusableImage(final byte[] img,
            Dimension imgDim, String formName, String imageDescription,
            final boolean isJPEG, ColorSpace colorSpace,
            PSGenerator gen) throws IOException {
        ImageEncoder encoder = new ImageEncoder() {
            public void writeTo(OutputStream out) throws IOException {
                out.write(img);
            }
            public String getImplicitFilter() {
                if (isJPEG) {
                    return "<< >> /DCTDecode";
                } else {
                    return null;
                }
            }
        };
        return writeReusableImage(encoder, imgDim, formName,
                imageDescription, colorSpace, false, gen);
    }

    /**
     * Writes a bitmap image as a PostScript form enclosed by DSC resource wrappers to the
     * PostScript file.
     * @param encoder the ImageEncoder that will provide the raw bitmap data
     * @param imgDim the dimensions of the image
     * @param formName the name of the PostScript form to use
     * @param imageDescription a description of the image added as a DSC Title comment
     * @param colorSpace the color space of the image
     * @param invertImage true if the image shall be inverted
     * @param gen the PostScript generator
     * @return a PSResource representing the form for resource tracking
     * @throws IOException In case of an I/O exception
     * @deprecated Please use {@link FormGenerator}
     */
    protected static PSResource writeReusableImage(ImageEncoder encoder,
            Dimension imgDim,
            String formName, String imageDescription,
            ColorSpace colorSpace, boolean invertImage,
            PSGenerator gen) throws IOException {
        if (gen.getPSLevel() < 2) {
            throw new UnsupportedOperationException(
                    "Reusable images requires at least Level 2 PostScript");
        }
        String dataName = formName + ":Data";
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, formName);
        if (imageDescription != null) {
            gen.writeDSCComment(DSCConstants.TITLE, imageDescription);
        }

        String additionalFilters;
        String implicitFilter = encoder.getImplicitFilter();
        if (implicitFilter != null) {
            additionalFilters = "/ASCII85Decode filter " + implicitFilter + " filter";
        } else {
            if (gen.getPSLevel() >= 3) {
                additionalFilters = "/ASCII85Decode filter /FlateDecode filter";
            } else {
                additionalFilters = "/ASCII85Decode filter /RunLengthDecode filter";
            }
        }

        gen.writeln("/" + formName);
        gen.writeln("<< /FormType 1");
        gen.writeln("  /BBox [0 0 " + imgDim.width + " " + imgDim.height + "]");
        gen.writeln("  /Matrix [1 0 0 1 0 0]");
        gen.writeln("  /PaintProc {");
        gen.writeln("    pop");
        gen.writeln("    gsave");
        if (gen.getPSLevel() == 2) {
            gen.writeln("    userdict /i 0 put"); //rewind image data
        } else {
            gen.writeln("    " + dataName + " 0 setfileposition"); //rewind image data
        }
        String dataSource;
        if (gen.getPSLevel() == 2) {
            dataSource = "{ " + dataName + " i get /i i 1 add store } bind";
        } else {
            dataSource = dataName;
        }
        PSDictionary imageDict = new PSDictionary();
        imageDict.put("/DataSource", dataSource);
        imageDict.put("/BitsPerComponent", Integer.toString(8));
        writeImageCommand(imageDict, imgDim, colorSpace, invertImage, gen);
        gen.writeln("    grestore");
        gen.writeln("  } bind");
        gen.writeln(">> def");
        gen.writeln("/" + dataName + " currentfile");
        gen.writeln(additionalFilters);
        if (gen.getPSLevel() == 2) {
            //Creates a data array from the inline file
            gen.writeln("{ /temp exch def ["
                    + " { temp 16384 string readstring not {exit } if } loop ] } exec");
        } else {
            gen.writeln("/ReusableStreamDecode filter");
        }
        compressAndWriteBitmap(encoder, gen);
        gen.writeln("def");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        PSResource res = new PSResource(PSResource.TYPE_FORM, formName);
        gen.getResourceTracker().registerSuppliedResource(res);
        return res;
    }

    /**
     * Paints a reusable image (previously added as a PostScript form).
     * @param formName the name of the PostScript form implementing the image
     * @param targetRect the target rectangle to place the image in
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     * @deprecated Please use {@link #paintForm(PSResource, Dimension2D, Rectangle2D, PSGenerator)}
     *          instead.
     */
    public static void paintReusableImage(
            String formName,
            Rectangle2D targetRect,
            PSGenerator gen) throws IOException {
        PSResource form = new PSResource(PSResource.TYPE_FORM, formName);
        paintForm(form, null, targetRect, gen);
    }

    /**
     * Paints a reusable image (previously added as a PostScript form).
     * @param form the PostScript form resource implementing the image
     * @param targetRect the target rectangle to place the image in
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     * @deprecated Please use {@link #paintForm(PSResource, Dimension2D, Rectangle2D, PSGenerator)}
     *          instead.
     */
    public static void paintForm(
            PSResource form,
            Rectangle2D targetRect,
            PSGenerator gen) throws IOException {
        paintForm(form, null, targetRect, gen);
    }

    /**
     * Paints a reusable image (previously added as a PostScript form).
     * @param form the PostScript form resource implementing the image
     * @param formDimensions the original dimensions of the form
     * @param targetRect the target rectangle to place the image in
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     */
    public static void paintForm(
            PSResource form,
            Dimension2D formDimensions,
            Rectangle2D targetRect,
            PSGenerator gen) throws IOException {
        gen.saveGraphicsState();
        translateAndScale(gen, formDimensions, targetRect);
        gen.writeln(form.getName() + " execform");

        gen.getResourceTracker().notifyResourceUsageOnPage(form);
        gen.restoreGraphicsState();
    }

    private static String getColorSpaceName(ColorSpace colorSpace) {
        if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
            return "/DeviceCMYK";
        } else if (colorSpace.getType() == ColorSpace.TYPE_GRAY) {
            return "/DeviceGray";
        } else {
            return "/DeviceRGB";
        }
    }

    static void compressAndWriteBitmap(ImageEncoder encoder, PSGenerator gen)
                throws IOException {
        OutputStream out = gen.getOutputStream();
        out = new ASCII85OutputStream(out);
        String implicitFilter = encoder.getImplicitFilter();
        if (implicitFilter != null) {
            //nop
        } else {
            if (gen.getPSLevel() >= 3) {
                out = new FlateEncodeOutputStream(out);
            } else {
                out = new RunLengthEncodeOutputStream(out);
            }
        }
        encoder.writeTo(out);
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        } else {
            out.flush();
        }
        gen.newLine(); //Just to be sure
    }

    /**
     * Generates commands to modify the current transformation matrix so an image fits
     * into a given rectangle.
     * @param gen the PostScript generator
     * @param imageDimensions the image's dimensions
     * @param targetRect the target rectangle
     * @throws IOException if an I/O error occurs
     */
    public static void translateAndScale(PSGenerator gen,
            Dimension2D imageDimensions, Rectangle2D targetRect)
                throws IOException {
        gen.writeln(gen.formatDouble(targetRect.getX()) + " "
                + gen.formatDouble(targetRect.getY()) + " translate");
        if (imageDimensions == null) {
            imageDimensions = new Dimension(1, 1);
        }
        double sx = targetRect.getWidth() / imageDimensions.getWidth();
        double sy = targetRect.getHeight() / imageDimensions.getHeight();
        if (sx != 1 || sy != 1) {
            gen.writeln(gen.formatDouble(sx) + " "
                    + gen.formatDouble(sy) + " scale");
        }
    }

    /**
     * Extracts a packed RGB integer array of a RenderedImage.
     * @param img the image
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param w the width of the cropped image
     * @param h the height of the cropped image
     * @param rgbArray the prepared integer array to write to
     * @param offset offset in the target array
     * @param scansize width of a row in the target array
     * @return the populated integer array previously passed in as rgbArray parameter
     */
    public static int[] getRGB(RenderedImage img,
                int startX, int startY,
                int w, int h,
                int[] rgbArray, int offset, int scansize) {
        Raster raster = img.getData();
        int yoff = offset;
        int off;
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
            throw new IllegalArgumentException("Unknown data buffer type: "
                                               + dataType);
        }

        if (rgbArray == null) {
            rgbArray = new int[offset + h * scansize];
        }

        ColorModel colorModel = img.getColorModel();
        for (int y = startY; y < startY + h; y++, yoff += scansize) {
            off = yoff;
            for (int x = startX; x < startX + w; x++) {
                rgbArray[off++] = colorModel.getRGB(raster.getDataElements(x, y, data));
            }
        }

        return rgbArray;
    }

    /**
     * Places an EPS file in the PostScript stream.
     * @param rawEPS byte array containing the raw EPS data
     * @param name name for the EPS document
     * @param x x-coordinate of viewport in points
     * @param y y-coordinate of viewport in points
     * @param w width of viewport in points
     * @param h height of viewport in points
     * @param bboxx x-coordinate of EPS bounding box in points
     * @param bboxy y-coordinate of EPS bounding box in points
     * @param bboxw width of EPS bounding box in points
     * @param bboxh height of EPS bounding box in points
     * @param gen the PS generator
     * @throws IOException in case an I/O error happens during output
     * @deprecated Please use the variant with the InputStream as parameter
     */
    public static void renderEPS(byte[] rawEPS, String name,
                    float x, float y, float w, float h,
                    float bboxx, float bboxy, float bboxw, float bboxh,
                    PSGenerator gen) throws IOException {
       renderEPS(new java.io.ByteArrayInputStream(rawEPS), name,
               new Rectangle2D.Float(x, y, w, h),
               new Rectangle2D.Float(bboxx, bboxy, bboxw, bboxh),
               gen);
    }

    /**
     * Places an EPS file in the PostScript stream.
     * @param in the InputStream that contains the EPS stream
     * @param name name for the EPS document
     * @param viewport the viewport in points in which to place the EPS
     * @param bbox the EPS bounding box in points
     * @param gen the PS generator
     * @throws IOException in case an I/O error happens during output
     */
    public static void renderEPS(InputStream in, String name,
            Rectangle2D viewport, Rectangle2D bbox,
                    PSGenerator gen) throws IOException {
        gen.getResourceTracker().notifyResourceUsageOnPage(PSProcSets.EPS_PROCSET);
        gen.writeln("%AXGBeginEPS: " + name);
        gen.writeln("BeginEPSF");

        gen.writeln(gen.formatDouble(viewport.getX())
                + " " + gen.formatDouble(viewport.getY()) + " translate");
        gen.writeln("0 " + gen.formatDouble(viewport.getHeight()) + " translate");
        gen.writeln("1 -1 scale");
        double sx = viewport.getWidth() / bbox.getWidth();
        double sy = viewport.getHeight() / bbox.getHeight();
        if (sx != 1 || sy != 1) {
            gen.writeln(gen.formatDouble(sx) + " " + gen.formatDouble(sy) + " scale");
        }
        if (bbox.getX() != 0 || bbox.getY() != 0) {
            gen.writeln(gen.formatDouble(-bbox.getX())
                    + " " + gen.formatDouble(-bbox.getY()) + " translate");
        }
        gen.writeln(gen.formatDouble(bbox.getX())
                + " " + gen.formatDouble(bbox.getY())
                + " " + gen.formatDouble(bbox.getWidth())
                + " " + gen.formatDouble(bbox.getHeight()) + " re clip");
        gen.writeln("newpath");

        PSResource res = new PSResource(PSResource.TYPE_FILE, name);
        gen.getResourceTracker().registerSuppliedResource(res);
        gen.getResourceTracker().notifyResourceUsageOnPage(res);
        gen.writeDSCComment(DSCConstants.BEGIN_DOCUMENT, res.getName());
        IOUtils.copy(in, gen.getOutputStream());
        gen.newLine();
        gen.writeDSCComment(DSCConstants.END_DOCUMENT);
        gen.writeln("EndEPSF");
        gen.writeln("%AXGEndEPS");
    }

}
