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

package org.apache.xmlgraphics.image.writer.imageio;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageWriteParam;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.internal.DirectPNGEncoder;

/**
 * ImageWriter that encodes PNG images. Images with a common layout (see
 * {@link DirectPNGEncoder#canEncode(RenderedImage)}) are written directly, which is faster and
 * gives smaller files than the Image I/O PNG writer; all other images are written using Image I/O.
 *
 * @version $Id$
 */
public class ImageIOPNGImageWriter extends ImageIOImageWriter {

    /**
     * Main constructor.
     */
    public ImageIOPNGImageWriter() {
        super("image/png");
    }

    /** {@inheritDoc} */
    @Override
    public void writeImage(RenderedImage image, OutputStream out, ImageWriterParams params)
            throws IOException {
        if (DirectPNGEncoder.canEncode(image)) {
            DirectPNGEncoder.encode(image, out, params);
        } else {
            super.writeImage(image, out, params);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected ImageWriteParam getDefaultWriteParam(javax.imageio.ImageWriter iiowriter,
            RenderedImage image, ImageWriterParams params) {
        ImageWriteParam param = super.getDefaultWriteParam(iiowriter, image, params);
        if (params != null && params.getCompressionLevel() != null && param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // the JDK PNG writer uses level 9 - round(9 * quality)
            param.setCompressionQuality((9 - params.getCompressionLevel()) / 9f);
        }
        return param;
    }

}
