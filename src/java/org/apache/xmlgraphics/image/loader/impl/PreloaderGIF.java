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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Image preloader for GIF images.
 */
public class PreloaderGIF extends AbstractImagePreloader {

    private static final int GIF_SIG_LENGTH = 10;

    /** {@inheritDoc} */
    public ImageInfo preloadImage(String uri, Source src, ImageContext context)
            throws IOException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, GIF_SIG_LENGTH);
        boolean supported = ((header[0] == 'G')
                && (header[1] == 'I')
                && (header[2] == 'F')
                && (header[3] == '8')
                && (header[4] == '7' || header[4] == '9')
                && (header[5] == 'a'));

        if (supported) {
            ImageInfo info = new ImageInfo(uri, MimeConstants.MIME_GIF);
            info.setSize(determineSize(header, context, in));
            return info;
        } else {
            return null;
        }
    }

    private ImageSize   determineSize(byte[] header, ImageContext context, ImageInputStream in) throws IOException {
        int [] dim = extractImageMetadata(in);
        ImageSize size = new ImageSize(dim[0], dim[1], context.getSourceResolution());
        size.calcSizeFromPixels();
        return size;
    }

    private int[] extractImageMetadata(ImageInputStream in) throws IOException {
        long startPos = in.getStreamPosition();
        Iterator readers = ImageIO.getImageReadersByFormatName("gif");
        ImageReader reader = (ImageReader) readers.next();
        reader.setInput(in, true);
        int width =  reader.getWidth(0);
        int height = reader.getHeight(0);
        int[] dim  = {width, height};
        in.seek(startPos);
        return dim;
    }

}
