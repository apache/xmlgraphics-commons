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

package org.apache.xmlgraphics.image.loader.mocks;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageLoaderFactory;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Mock implementation posing as a TIFF-compatible loader.
 */
public class MockImageLoaderFactoryTIFF extends AbstractImageLoaderFactory {

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        return new ImageFlavor[] {ImageFlavor.BUFFERED_IMAGE, ImageFlavor.RENDERED_IMAGE};
    }

    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return new String[] {MimeConstants.MIME_TIFF};
    }

    private void checkSuppportedFlavor(String mime, ImageFlavor flavor) {
        ImageFlavor[] flavors = getSupportedFlavors(mime);
        boolean found = false;
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(flavor)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        return true;
    }

    /** {@inheritDoc} */
    public boolean isSupported(ImageInfo imageInfo) {
        return MimeConstants.MIME_TIFF.equals(imageInfo.getMimeType());
    }

    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        checkSuppportedFlavor(MimeConstants.MIME_TIFF, targetFlavor);
        return new ImageLoaderImpl(targetFlavor);
    }

    /** Mock image loader implementation. */
    private static class ImageLoaderImpl implements ImageLoader {

        private ImageFlavor flavor;

        public ImageLoaderImpl(ImageFlavor flavor) {
            this.flavor = flavor;
        }

        public ImageFlavor getTargetFlavor() {
            return flavor;
        }

        public int getUsagePenalty() {
            return 0;
        }

        public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
                throws ImageException, IOException {
            throw new UnsupportedOperationException("not implemented");
        }

        public Image loadImage(ImageInfo info, ImageSessionContext session) throws ImageException,
                IOException {
            throw new UnsupportedOperationException("not implemented");
        }

    }

}
