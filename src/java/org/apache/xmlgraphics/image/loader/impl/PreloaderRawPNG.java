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
package org.apache.xmlgraphics.image.loader.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.codec.png.PNGImageDecoder;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

public class PreloaderRawPNG extends AbstractImagePreloader {
    public ImageInfo preloadImage(String uri, Source src, ImageContext context) throws ImageException, IOException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        long bb = ByteBuffer.wrap(getHeader(in, 8)).getLong();
        if (bb != PNGConstants.PNG_SIGNATURE) {
            return null;
        }
        in.mark();
        ImageSize size = new ImageSize();
        //Resolution (first a default, then try to read the metadata)
        size.setResolution(context.getSourceResolution());
        try {
            PNGImageDecoder.readPNGHeader(in, size);
        } finally {
            in.reset();
        }

        ImageInfo info = new ImageInfo(uri, "image/png");
        info.setSize(size);
        return info;
    }

    public int getPriority() {
        return DEFAULT_PRIORITY * 2;
    }
}
