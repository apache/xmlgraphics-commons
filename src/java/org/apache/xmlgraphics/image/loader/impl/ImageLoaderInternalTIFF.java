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
import java.util.Map;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.codec.util.ImageInputStreamSeekableStreamAdapter;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageLoader;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * An ImageLoader implementation based on Commons' internal TIFF codec.
 */
public class ImageLoaderInternalTIFF extends AbstractImageLoader {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageLoaderInternalTIFF.class);

    /**
     * Main constructor.
     */
    public ImageLoaderInternalTIFF() {
        //nop
    }

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return ImageFlavor.RENDERED_IMAGE;
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
            throws ImageException, IOException {

        Source src = session.needSource(info.getOriginalURI());
        ImageInputStream imgStream = ImageUtil.needImageInputStream(src);

        SeekableStream seekStream = new ImageInputStreamSeekableStreamAdapter(imgStream);
        org.apache.xmlgraphics.image.codec.tiff.TIFFImage img
            = new org.apache.xmlgraphics.image.codec.tiff.TIFFImage
                (seekStream, null, 0);
        // TODO: This may ignore ICC Profiles stored in TIFF images.
        return new ImageRendered(info, img, null);
    }

}
