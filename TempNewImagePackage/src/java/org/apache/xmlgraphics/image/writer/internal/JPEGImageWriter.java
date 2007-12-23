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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.image.writer.AbstractImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * ImageWriter implementation that uses the sun.com.image.codec.jpeg
 * intefaces to write JPEG files.
 * <p>
 * Note: This class depends on a Sun runtime environment and is therefore not compatible with
 * non-Sun VMs (such as Harmony or GNU Classpath based VMs).
 *
 * @version $Id$
 */
public class JPEGImageWriter extends AbstractImageWriter {

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
        BufferedImage bi;
        if (image instanceof BufferedImage) {
            bi = (BufferedImage)image;
        } else {
            //TODO Is this the right way?
            bi = GraphicsUtil.makeLinearBufferedImage(
                    image.getWidth(), image.getHeight(), false);
        }
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        if (params != null) {
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            if (params.getJPEGQuality() != null) {
                param.setQuality(
                        params.getJPEGQuality().floatValue(),
                        params.getJPEGForceBaseline().booleanValue());
            }
            encoder.encode(bi, param);
        } else {
            encoder.encode(bi);
        }
    }

    /**
     * @see ImageWriter#getMIMEType()
     */
    public String getMIMEType() {
        return "image/jpeg";
    }
}
