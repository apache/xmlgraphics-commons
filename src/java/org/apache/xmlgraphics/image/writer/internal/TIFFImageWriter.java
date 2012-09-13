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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.codec.tiff.TIFFEncodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageEncoder;
import org.apache.xmlgraphics.image.writer.AbstractImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;

/**
 * ImageWriter implementation that uses the internal TIFF codec to
 * write TIFF files.
 *
 * @version $Id$
 */
public class TIFFImageWriter extends AbstractImageWriter {

    /** {@inheritDoc} */
    public void writeImage(RenderedImage image, OutputStream out)
            throws IOException {
        writeImage(image, out, null);
    }

    /** {@inheritDoc} */
    public void writeImage(RenderedImage image, OutputStream out,
            ImageWriterParams params) throws IOException {
        TIFFEncodeParam encodeParams = createTIFFEncodeParams(params);
        TIFFImageEncoder encoder = new TIFFImageEncoder(out, encodeParams);
        encoder.encode(image);
    }

    private TIFFEncodeParam createTIFFEncodeParams(ImageWriterParams params) {
        TIFFEncodeParam encodeParams = new TIFFEncodeParam();
        if (params == null) {
            encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
        } else {
            if (params.getCompressionMethod() == null) {
                //PackBits as default
                encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
            } else if ("PackBits".equalsIgnoreCase(params.getCompressionMethod())) {
                encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
            } else if ("NONE".equalsIgnoreCase(params.getCompressionMethod())) {
                encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
            } else if ("Deflate".equalsIgnoreCase(params.getCompressionMethod())) {
                encodeParams.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
            } else {
                throw new UnsupportedOperationException("Compression method not supported: "
                        + params.getCompressionMethod());
            }

            if (params.getResolution() != null) {
                int numPixX;
                int numPixY;
                int denom;

                if (ResolutionUnit.INCH == params.getResolutionUnit()) {
                    numPixX = params.getXResolution().intValue();
                    numPixY = params.getYResolution().intValue();
                    denom = 1;
                } else {
                    // Set target resolution
                    float pixXSzMM = 25.4f / params.getXResolution().floatValue();
                    float pixYSzMM = 25.4f / params.getYResolution().floatValue();
                    // num Pixs in 100 Meters
                    numPixX = (int)(((1000 * 100) / pixXSzMM) + 0.5);
                    numPixY = (int)(((1000 * 100) / pixYSzMM) + 0.5);
                    denom = 100 * 100;  // Centimeters per 100 Meters;
                }

                long [] xRational = {numPixX, denom};
                long [] yRational = {numPixY, denom};
                TIFFField [] fields = {
                    new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT,
                                  TIFFField.TIFF_SHORT, 1,
                                  new char[] {(char)params.getResolutionUnit().getValue()}),
                    new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION,
                                  TIFFField.TIFF_RATIONAL, 1,
                                  new long[][] {xRational}),
                    new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION,
                                  TIFFField.TIFF_RATIONAL, 1,
                                  new long[][] {yRational})
                        };
                encodeParams.setExtraFields(fields);
            }
        }
        return encodeParams;
    }

    /** {@inheritDoc} */
    public String getMIMEType() {
        return "image/tiff";
    }

    /** {@inheritDoc} */
    @Override
    public MultiImageWriter createMultiImageWriter(OutputStream out) throws IOException {
        return new TIFFMultiImageWriter(out);
    }

    /** {@inheritDoc} */
    @Override
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
