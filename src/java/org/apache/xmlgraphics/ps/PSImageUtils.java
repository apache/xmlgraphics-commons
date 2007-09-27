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

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.util.io.ASCII85OutputStream;
import org.apache.xmlgraphics.util.io.Finalizable;
import org.apache.xmlgraphics.util.io.FlateEncodeOutputStream;
import org.apache.xmlgraphics.util.io.RunLengthEncodeOutputStream;

/**
 * Utility code for rendering images in PostScript. 
 */
public class PSImageUtils {

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
     */
    public static void writeImage(byte[] img,
            Dimension imgDim, String imgDescription,
            Rectangle2D targetRect, 
            boolean isJPEG, ColorSpace colorSpace,
            PSGenerator gen) throws IOException {
        gen.saveGraphicsState();
        gen.writeln(gen.formatDouble(targetRect.getX()) + " " 
                + gen.formatDouble(targetRect.getY()) + " translate");
        gen.writeln(gen.formatDouble(targetRect.getWidth()) + " " 
                + gen.formatDouble(targetRect.getHeight()) + " scale");

        gen.commentln("%AXGBeginBitmap: " + imgDescription);

        gen.writeln("{{");
        // Template: (RawData is used for the EOF signal only)
        // gen.write("/RawData currentfile <first filter> filter def");
        // gen.write("/Data RawData <second filter> <third filter> [...] def");
        if (isJPEG) {
            gen.writeln("/RawData currentfile /ASCII85Decode filter def");
            gen.writeln("/Data RawData << >> /DCTDecode filter def");
        } else {
            if (gen.getPSLevel() >= 3) {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /FlateDecode filter def");
            } else {
                gen.writeln("/RawData currentfile /ASCII85Decode filter def");
                gen.writeln("/Data RawData /RunLengthDecode filter def");
            }
        }
        writeImageCommand(imgDim, colorSpace, gen, "Data");
        /* the following two lines could be enabled if something still goes wrong
         * gen.write("Data closefile");
         * gen.write("RawData flushfile");
         */
        gen.writeln("} stopped {handleerror} if");
        gen.writeln("  RawData flushfile");
        gen.writeln("} exec");

        encodeBitmap(img, isJPEG, gen);

        gen.writeln("");
        gen.commentln("%AXGEndBitmap");
        gen.restoreGraphicsState();
    }

    private static void writeImageCommand(Dimension imgDim, ColorSpace colorSpace, 
            PSGenerator gen, String dataSource) throws IOException {
        boolean iscolor = colorSpace.getType() != ColorSpace.TYPE_GRAY;
        prepareColorspace(colorSpace, gen);
        gen.writeln("<< /ImageType 1");
        gen.writeln("  /Width " + imgDim.width);
        gen.writeln("  /Height " + imgDim.height);
        gen.writeln("  /BitsPerComponent 8");
        if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
            if (false /*TODO img.invertImage()*/) {
                gen.writeln("  /Decode [1 0 1 0 1 0 1 0]");
            } else {
                gen.writeln("  /Decode [0 1 0 1 0 1 0 1]");
            }
        } else if (iscolor) {
            gen.writeln("  /Decode [0 1 0 1 0 1]");
        } else {
            gen.writeln("  /Decode [0 1]");
        }
        // Setup scanning for left-to-right and top-to-bottom
        gen.writeln("  /ImageMatrix [" + imgDim.width + " 0 0 "
              + imgDim.height + " 0 0]");

        gen.writeln("  /DataSource " + dataSource);
        gen.writeln(">> image");
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
     */
    public static PSResource writeReusableImage(byte[] img,
            Dimension imgDim, String formName, String imageDescription,
            boolean isJPEG, ColorSpace colorSpace,
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
        if (isJPEG) {
            additionalFilters = "/ASCII85Decode filter /DCTDecode filter";
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
        writeImageCommand(imgDim, colorSpace, gen, dataSource); 
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
        encodeBitmap(img, isJPEG, gen);
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
     */
    public static void paintReusableImage(
            String formName,
            Rectangle2D targetRect, 
            PSGenerator gen) throws IOException {
        PSResource form = new PSResource(PSResource.TYPE_FORM, formName);
        paintForm(form, targetRect, gen);
    }
    
    /**
     * Paints a reusable image (previously added as a PostScript form).
     * @param form the PostScript form resource implementing the image
     * @param targetRect the target rectangle to place the image in
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O exception
     */
    public static void paintForm(
            PSResource form,
            Rectangle2D targetRect, 
            PSGenerator gen) throws IOException {
        gen.saveGraphicsState();
        gen.writeln(gen.formatDouble(targetRect.getX()) + " " 
                + gen.formatDouble(targetRect.getY()) + " translate");
        gen.writeln(gen.formatDouble(targetRect.getWidth()) + " " 
                + gen.formatDouble(targetRect.getHeight()) + " scale");
        gen.writeln(form.getName() + " execform");
        
        gen.getResourceTracker().notifyResourceUsageOnPage(form);
        gen.restoreGraphicsState();
    }
    
    private static void prepareColorspace(ColorSpace colorSpace, PSGenerator gen)
                throws IOException {
        if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
            gen.writeln("/DeviceCMYK setcolorspace");
        } else if (colorSpace.getType() == ColorSpace.TYPE_GRAY) {
            gen.writeln("/DeviceGray setcolorspace");
        } else {
            gen.writeln("/DeviceRGB setcolorspace");
        }
    }

    private static void encodeBitmap(byte[] img, boolean isJPEG, PSGenerator gen)
                throws IOException {
        OutputStream out = gen.getOutputStream();
        out = new ASCII85OutputStream(out);
        if (isJPEG) {
            //nop
        } else {
            if (gen.getPSLevel() >= 3) {
                out = new FlateEncodeOutputStream(out);
            } else {
                out = new RunLengthEncodeOutputStream(out);
            }
        }
        out.write(img);
        if (out instanceof Finalizable) {
            ((Finalizable)out).finalizeStream();
        } else {
            out.flush();
        }
        gen.newLine();
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
                float x, float y, float w, float h, PSGenerator gen)
                    throws IOException {
        byte[] imgmap = getBitmapBytes(img);

        String imgName = img.getClass().getName();
        Dimension imgDim = new Dimension(img.getWidth(), img.getHeight());
        Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);
        boolean isJPEG = false;
        writeImage(imgmap, imgDim, imgName, targetRect, isJPEG, 
                img.getColorModel().getColorSpace(), gen);
    }

    private static byte[] getBitmapBytes(RenderedImage img) {
        int[] tmpMap = getRGB(img, 0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        // Should take care of the ColorSpace and bitsPerPixel
        byte[] bitmaps = new byte[img.getWidth() * img.getHeight() * 3];
        for (int y = 0, my = img.getHeight(); y < my; y++) {
            for (int x = 0, mx = img.getWidth(); x < mx; x++) {
                int p = tmpMap[y * mx + x];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                bitmaps[3 * (y * mx + x)] = (byte)(r & 0xFF);
                bitmaps[3 * (y * mx + x) + 1] = (byte)(g & 0xFF);
                bitmaps[3 * (y * mx + x) + 2] = (byte)(b & 0xFF);
            }
        }
        return bitmaps;
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
            throw new IllegalArgumentException("Unknown data buffer type: "+
                                               dataType);
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
     * @param x x-coordinate of viewport in millipoints
     * @param y y-coordinate of viewport in millipoints
     * @param w width of viewport in millipoints
     * @param h height of viewport in millipoints
     * @param bboxx x-coordinate of EPS bounding box in points
     * @param bboxy y-coordinate of EPS bounding box in points
     * @param bboxw width of EPS bounding box in points
     * @param bboxh height of EPS bounding box in points
     * @param gen the PS generator
     * @throws IOException in case an I/O error happens during output
     */
    public static void renderEPS(byte[] rawEPS, String name,
                    float x, float y, float w, float h,
                    float bboxx, float bboxy, float bboxw, float bboxh,
                    PSGenerator gen) throws IOException {
        gen.getResourceTracker().notifyResourceUsageOnPage(PSProcSets.EPS_PROCSET);
        gen.writeln("%AXGBeginEPS: " + name);
        gen.writeln("BeginEPSF");

        gen.writeln(gen.formatDouble(x) + " " + gen.formatDouble(y) + " translate");
        gen.writeln("0 " + gen.formatDouble(h) + " translate");
        gen.writeln("1 -1 scale");
        float sx = w / bboxw;
        float sy = h / bboxh;
        if (sx != 1 || sy != 1) {
            gen.writeln(gen.formatDouble(sx) + " " + gen.formatDouble(sy) + " scale");
        }
        if (bboxx != 0 || bboxy != 0) {
            gen.writeln(gen.formatDouble(-bboxx) + " " + gen.formatDouble(-bboxy) + " translate");
        }
        gen.writeln(gen.formatDouble(bboxy) + " " + gen.formatDouble(bboxy) 
                + " " + gen.formatDouble(bboxw) + " " + gen.formatDouble(bboxh) + " re clip");
        gen.writeln("newpath");
        
        PSResource res = new PSResource(PSResource.TYPE_FILE, name);
        gen.getResourceTracker().notifyResourceUsageOnPage(res);
        gen.writeDSCComment(DSCConstants.BEGIN_DOCUMENT, res.getName());
        gen.writeByteArr(rawEPS);
        gen.writeDSCComment(DSCConstants.END_DOCUMENT);
        gen.writeln("EndEPSF");
        gen.writeln("%AXGEndEPS");
    }

}
