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

package org.apache.xmlgraphics.image.loader.impl.imageio;

import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.AbstractImagePreloader;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * Image preloader for images supported by ImageIO.
 * <p>
 * Note: The implementation relies on the presence of a working ImageIO implementation which
 * provides accurate image metadata. This is particularly important for PNG image because the
 * PNG loader relies on that.
 */
public class PreloaderImageIO extends AbstractImagePreloader {

    /** {@inheritDoc}
     * @throws ImageException */
    public ImageInfo preloadImage(String uri, Source src, ImageContext context)
            throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        long startPos = in.getStreamPosition();
        Iterator iter = ImageIO.getImageReaders(in);
        if (!iter.hasNext()) {
            return null;
        }

        IOException firstIOException = null;
        IIOMetadata iiometa = null;
        ImageSize size = null;
        String mime = null;
        while (iter.hasNext()) {
            in.mark();

            ImageReader reader = (ImageReader)iter.next();
            try {
                reader.setInput(ImageUtil.ignoreFlushing(in), true, false);
                final int imageIndex = 0;
                iiometa = reader.getImageMetadata(imageIndex);
                size = new ImageSize();
                size.setSizeInPixels(reader.getWidth(imageIndex), reader.getHeight(imageIndex));
                mime = reader.getOriginatingProvider().getMIMETypes()[0];
                break;
            } catch (IOException ioe) {
                //remember the first exception, ignore all others and continue
                if (firstIOException == null) {
                    firstIOException = ioe;
                }
            } finally {
                reader.dispose();
                in.reset();
            }
        }

        if (iiometa == null) {
            in.seek(startPos);
            return null;
        }

        //Resolution (first a default, then try to read the metadata)
        size.setResolution(context.getSourceResolution());
        ImageIOUtil.extractResolution(iiometa, size);
        if (size.getWidthPx() <= 0 || size.getHeightPx() <= 0) {
            //Watch out for a special case: a TGA image was erroneously identified
            //as a WBMP image by a Sun ImageIO codec.
            return null;
        }
        if (size.getWidthMpt() == 0) {
            size.calcSizeFromPixels();
        }

        ImageInfo info = new ImageInfo(uri, mime);
        info.getCustomObjects().put(ImageIOUtil.IMAGEIO_METADATA, iiometa);
        info.setSize(size);

        return info;
    }

    /** {@inheritDoc} */
    public int getPriority() {
        //Lower priority than default to give the specialized preloaders a chance.
        return 2 * DEFAULT_PRIORITY;
    }

}
