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
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.codec.tiff.TIFFEncodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageEncoder;
import org.apache.xmlgraphics.image.writer.AbstractImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

/**
 * ImageWriter implementation that uses the internal TIFF codec to 
 * write TIFF files.
 *
 * @version $Id$
 */
public class TIFFImageWriter extends AbstractImageWriter {

    /**
     * @see ImageWriter#writeImage(java.awt.image.RenderedImage, java.io.OutputStream)
     */
    public void writeImage(RenderedImage image, OutputStream out)
            throws IOException {
        writeImage(image, out, null);
    }

    /**
     * @see ImageWriter#writeImage(java.awt.image.RenderedImage, java.io.OutputStream, ImageWriterParams)
     */
    public void writeImage(RenderedImage image, OutputStream out,
            ImageWriterParams params) throws IOException {
        TIFFEncodeParam encodeParams = createTIFFEncodeParams(params);
        updateParams(encodeParams, params, image);
        TIFFImageEncoder encoder = new TIFFImageEncoder(out, encodeParams);
        encoder.encode(image);
    }

    /**
     * This method updates the encode parameters based on the image to be encoded. One thing done
     * here is to initialize the JPEG encoding parameters if JPEG compression is selected.
     * @param encodeParams the TIFF encoding parameters
     * @param image the image to be encoded
     */
    private void updateParams(TIFFEncodeParam encodeParams, ImageWriterParams params, 
            RenderedImage image) {
        if (encodeParams.getCompression() == TIFFEncodeParam.COMPRESSION_JPEG_TTN2) {
            ColorModel cm = image.getColorModel();
            int imageType = cm.getColorSpace().getType();
            int colorID;
            //The following translation table is taken from the Javadoc for JPEGEncodeParam.
            switch (imageType) {
            case ColorSpace.TYPE_GRAY:
                colorID = JPEGEncodeParam.COLOR_ID_GRAY;
                break;
            case ColorSpace.TYPE_RGB:
                if (cm.hasAlpha()) {
                    colorID = JPEGEncodeParam.COLOR_ID_YCbCrA;
                } else {
                    colorID = JPEGEncodeParam.COLOR_ID_YCbCr;
                }
                break;
            case ColorSpace.TYPE_YCbCr:
                if (cm.hasAlpha()) {
                    colorID = JPEGEncodeParam.COLOR_ID_YCbCrA;
                } else {
                    colorID = JPEGEncodeParam.COLOR_ID_YCbCr;
                }
                break;
            case ColorSpace.TYPE_CMYK:
                colorID = JPEGEncodeParam.COLOR_ID_CMYK;
                break;
            default:
                //TODO Don't know how to determine whether image is PYCC or not 
                //(see JPEGEncodeParam)
                colorID = JPEGEncodeParam.COLOR_ID_UNKNOWN;
            }
            JPEGEncodeParam jpegParam = JPEGCodec.getDefaultJPEGEncodeParam(
                    image.getData(), colorID);
            if (params.getJPEGQuality() != null || params.getJPEGForceBaseline() != null) {
                float qual = (params.getJPEGQuality() != null 
                        ? params.getJPEGQuality().floatValue() : 0.75f);
                boolean force = (params.getJPEGForceBaseline() != null
                        ? params.getJPEGForceBaseline().booleanValue() : false);
                jpegParam.setQuality(qual, force); 
            }
            encodeParams.setJPEGEncodeParam(jpegParam);
        }
    }

    private TIFFEncodeParam createTIFFEncodeParams(ImageWriterParams params) {
        TIFFEncodeParam encodeParams = new TIFFEncodeParam();
        if (params.getCompressionMethod() == null) {
            //PackBits as default
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
        } else if ("PackBits".equalsIgnoreCase(params.getCompressionMethod())) {
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
        } else if ("NONE".equalsIgnoreCase(params.getCompressionMethod())) {
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
        } else if ("JPEG".equalsIgnoreCase(params.getCompressionMethod())) {
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
        } else if ("Deflate".equalsIgnoreCase(params.getCompressionMethod())) {
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
        } else {
            throw new UnsupportedOperationException("Compression method not supported: "
                    + params.getCompressionMethod());
        }
        
        if (params.getResolution() != null) {
            // Set target resolution
            float pixSzMM = 25.4f / params.getResolution().floatValue();
            // num Pixs in 100 Meters
            int numPix = (int)(((1000 * 100) / pixSzMM) + 0.5); 
            int denom = 100 * 100;  // Centimeters per 100 Meters;
            long [] rational = {numPix, denom};
            TIFFField [] fields = {
                new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT, 
                              TIFFField.TIFF_SHORT, 1, 
                              new char[] {(char)3}),
                new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION, 
                              TIFFField.TIFF_RATIONAL, 1, 
                              new long[][] {rational}),
                new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION, 
                              TIFFField.TIFF_RATIONAL, 1, 
                              new long[][] {rational}) 
                    };
            encodeParams.setExtraFields(fields);
        }
        return encodeParams;
    }
    
    /**
     * @see ImageWriter#getMIMEType()
     */
    public String getMIMEType() {
        return "image/tiff";
    }
    
    /**
     * @see org.apache.xmlgraphics.image.writer.ImageWriter#createMultiImageWriter(
     *          java.io.OutputStream)
     */
    public MultiImageWriter createMultiImageWriter(OutputStream out) throws IOException {
        return new TIFFMultiImageWriter(out);
    }

    /** @see org.apache.xmlgraphics.image.writer.ImageWriter#supportsMultiImageWriter() */
    public boolean supportsMultiImageWriter() {
        return true;
    }

    private class TIFFMultiImageWriter implements MultiImageWriter {

        private OutputStream out;
        private TIFFEncodeParam encodeParams;
        private TIFFImageEncoder encoder;
        private Object context;
        
        public TIFFMultiImageWriter(OutputStream out) throws IOException {
            this.out = out;
        }
        
        public void writeImage(RenderedImage image, ImageWriterParams params) throws IOException {
            if (encoder == null) {
                encodeParams = createTIFFEncodeParams(params);
                updateParams(encodeParams, params, image);
                encoder = new TIFFImageEncoder(out, encodeParams);
            }
            context = encoder.encodeMultiple(context, image);
        }
        
        public void close() throws IOException {
            if (encoder != null) {
                encoder.finishMultiple(context);
            }
            encoder = null;
            encodeParams = null;
            out.flush();
        }

    }

    
}
