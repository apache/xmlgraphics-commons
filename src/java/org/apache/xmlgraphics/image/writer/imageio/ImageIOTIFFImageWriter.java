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

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.apache.xmlgraphics.image.writer.ImageWriterParams;

// CSOFF: MultipleVariableDeclarations

/**
 * ImageWriter that encodes TIFF images using Image I/O.
 *
 * @version $Id$
 */
public class ImageIOTIFFImageWriter extends ImageIOImageWriter {

    private static final String SUN_TIFF_NATIVE_FORMAT
            = "com_sun_media_imageio_plugins_tiff_image_1.0";

    /**
     * Main constructor.
     */
    public ImageIOTIFFImageWriter() {
        super("image/tiff");
    }

    /**
     * @see org.apache.xmlgraphics.image.writer.imageio.ImageIOImageWriter#updateMetadata(javax.imageio.metadata.IIOMetadata, org.apache.xmlgraphics.image.writer.ImageWriterParams)
     */
    protected IIOMetadata updateMetadata(IIOMetadata meta, ImageWriterParams params) {
        IIOMetadata ret = super.updateMetadata(meta, params);

        //We set the resolution manually using the native format since it appears that
        //it doesn't work properly through the standard metadata. Haven't figured out why
        //that happens.
        if (params.getResolution() != null) {
            if (SUN_TIFF_NATIVE_FORMAT.equals(meta.getNativeMetadataFormatName())) {

                //IIOMetadataNode root = (IIOMetadataNode)meta.getAsTree(SUN_TIFF_NATIVE_FORMAT);
                IIOMetadataNode root = new IIOMetadataNode(SUN_TIFF_NATIVE_FORMAT);

                IIOMetadataNode ifd = getChildNode(root, "TIFFIFD");
                if (ifd == null) {
                    ifd = new IIOMetadataNode("TIFFIFD");
                    ifd.setAttribute("tagSets",
                                "com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet");
                    root.appendChild(ifd);
                }
                ifd.appendChild(createResolutionField(282, "XResolution", params));
                ifd.appendChild(createResolutionField(283, "YResolution", params));

                //ResolutionUnit
                IIOMetadataNode field, arrayNode, valueNode;
                field = new IIOMetadataNode("TIFFField");
                field.setAttribute("number", Integer.toString(296));
                field.setAttribute("name", "ResolutionUnit");
                arrayNode = new IIOMetadataNode("TIFFShorts");
                field.appendChild(arrayNode);
                valueNode = new IIOMetadataNode("TIFFShort");
                valueNode.setAttribute("value", Integer.toString(3));
                valueNode.setAttribute("description", "Centimeter");
                arrayNode.appendChild(valueNode);

                try {
                    meta.mergeTree(SUN_TIFF_NATIVE_FORMAT, root);
                } catch (IIOInvalidTreeException e) {
                    throw new RuntimeException("Cannot update image metadata: "
                                + e.getMessage(), e);
                }
            }
        }

        return ret;
    }

    private IIOMetadataNode createResolutionField(int number, String name, ImageWriterParams params) {
        IIOMetadataNode field, arrayNode, valueNode;
        field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(number));
        field.setAttribute("name", name);
        arrayNode = new IIOMetadataNode("TIFFRationals");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFRational");
        arrayNode.appendChild(valueNode);

        // Set target resolution
        float pixSzMM = 25.4f / params.getResolution().floatValue();
        // num Pixs in 100 Meters
        int numPix = (int)(((1000 * 100) / pixSzMM) + 0.5);
        int denom = 100 * 100;  // Centimeters per 100 Meters;
        valueNode.setAttribute("value", numPix + "/" + denom);
        return field;
    }

}
