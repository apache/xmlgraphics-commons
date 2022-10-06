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

import java.util.Map;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Factory class for the ImageLoader for raw/undecoded images.
 */
public class ImageLoaderFactoryRaw extends AbstractImageLoaderFactory {

    /** MIME type for EMF (Windows Enhanced Metafile) */
    public static final String MIME_EMF = "image/x-emf";

    private static final String[] MIMES = new String[] {
        MimeConstants.MIME_PNG,
        MimeConstants.MIME_JPEG,
        MimeConstants.MIME_TIFF,
        MIME_EMF};

    private static final ImageFlavor[][] FLAVORS = new ImageFlavor[][] {
        {ImageFlavor.RAW_PNG},
        {ImageFlavor.RAW_JPEG},
        {ImageFlavor.RAW_TIFF},
        {ImageFlavor.RAW_EMF}};


    /**
     * Returns the MIME type for a given ImageFlavor if it is from a format that is consumed
     * without being undecoded. If the ImageFlavor is no raw flavor, an IllegalArgumentException
     * is thrown.
     * @param flavor the image flavor
     * @return the associated MIME type
     */
    public static String getMimeForRawFlavor(ImageFlavor flavor) {
        for (int i = 0, ci = FLAVORS.length; i < ci; i++) {
            for (int j = 0, cj = FLAVORS[i].length; j < cj; j++) {
                if (FLAVORS[i][j].equals(flavor)) {
                    return MIMES[i];
                }
            }
        }
        throw new IllegalArgumentException("ImageFlavor is not a \"raw\" flavor: " + flavor);
    }

    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return MIMES;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        for (int i = 0, c = MIMES.length; i < c; i++) {
            if (MIMES[i].equals(mime)) {
                return FLAVORS[i];
            }
        }
        throw new IllegalArgumentException("Unsupported MIME type: " + mime);
    }

    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        if (targetFlavor.equals(ImageFlavor.RAW_JPEG)) {
            return new ImageLoaderRawJPEG();
        } else if (targetFlavor.equals(ImageFlavor.RAW_PNG)) {
            return new ImageLoaderRawPNG();
        } else {
            return new ImageLoaderRaw(targetFlavor);
        }
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isSupported(ImageInfo imageInfo) {
        if ("image/png".equals(imageInfo.getMimeType())) {
            Map additionalPenalties = (Map) imageInfo.getCustomObjects().get("additionalPenalties");
            int penalty = 0;
            Penalty penaltyObj = ((Penalty)additionalPenalties.get(ImageLoaderRawPNG.class.getName()));
            if (penaltyObj != null) {
                penalty = penaltyObj.getValue();
            }
            IIOMetadata metadata = (IIOMetadata) imageInfo.getCustomObjects().get(IIOMetadata.class);
            if (metadata != null) {
                IIOMetadataNode children = (IIOMetadataNode)metadata.getAsTree("javax_imageio_png_1.0").getChildNodes();
                NamedNodeMap attr = children.getElementsByTagName("IHDR").item(0).getAttributes();
                String bitDepth = attr.getNamedItem("bitDepth").getNodeValue();
                String interlaceMethod = attr.getNamedItem("interlaceMethod").getNodeValue();
                String colorType = attr.getNamedItem("colorType").getNodeValue();
                if (!bitDepth.equals("8") || !interlaceMethod.equals("none")
                        || ((colorType.equals("RGBAlpha") || colorType.equals("GrayAlpha")) && penalty >= 0)) {
                    return false;
                }
                children = (IIOMetadataNode)metadata.getAsTree("javax_imageio_1.0").getChildNodes();
                Node numChannels = children.getElementsByTagName("NumChannels").item(0);
                String numChannelsStr = numChannels.getAttributes().getNamedItem("value").getNodeValue();
                if ("4".equals(numChannelsStr) && "Palette".equals(colorType) && penalty >= 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
