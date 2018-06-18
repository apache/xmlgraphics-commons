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
import java.util.Arrays;
import java.util.Set;

import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.writer.Endianness;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;

// CSOFF: MultipleVariableDeclarations

/**
 * ImageWriter that encodes TIFF images using Image I/O.
 *
 * @version $Id$
 */
public class ImageIOTIFFImageWriter extends ImageIOImageWriter {

    private static final String SUN_TIFF_NATIVE_FORMAT
            = "com_sun_media_imageio_plugins_tiff_image_1.0";
    private static final String JAVA_TIFF_NATIVE_FORMAT
            = "javax_imageio_tiff_image_1.0";
    private static final String SUN_TIFF_NATIVE_STREAM_FORMAT
            = "com_sun_media_imageio_plugins_tiff_stream_1.0";
    private static final String JAVA_TIFF_NATIVE_STREAM_FORMAT
            = "javax_imageio_tiff_stream_1.0";

    /**
     * Main constructor.
     */
    public ImageIOTIFFImageWriter() {
        super("image/tiff");
    }

    /** {@inheritDoc} */
    @Override
    protected IIOMetadata updateMetadata(RenderedImage image, IIOMetadata meta,
            ImageWriterParams params) {
        meta = super.updateMetadata(image, meta, params);
        //We set the resolution manually using the native format since it appears that
        //it doesn't work properly through the standard metadata. Haven't figured out why
        //that happens.
        if (params.getResolution() != null) {
            if (SUN_TIFF_NATIVE_FORMAT.equals(meta.getNativeMetadataFormatName())
                    || JAVA_TIFF_NATIVE_FORMAT.equals(meta.getNativeMetadataFormatName())) {
                IIOMetadataNode root = new IIOMetadataNode(meta.getNativeMetadataFormatName());
                IIOMetadataNode ifd = getChildNode(root, "TIFFIFD");
                if (ifd == null) {
                    ifd = new IIOMetadataNode("TIFFIFD");
                    root.appendChild(ifd);
                }
                ifd.appendChild(createResolutionUnitField(params));
                ifd.appendChild(createResolutionField(TIFFImageDecoder.TIFF_X_RESOLUTION,
                        "XResolution", params.getXResolution(), params.getResolutionUnit()));
                ifd.appendChild(createResolutionField(TIFFImageDecoder.TIFF_Y_RESOLUTION,
                        "YResolution", params.getYResolution(), params.getResolutionUnit()));
                int rows = params.isSingleStrip() ? image.getHeight() : params.getRowsPerStrip();
                ifd.appendChild(createShortMetadataNode(TIFFImageDecoder.TIFF_ROWS_PER_STRIP,
                        "RowsPerStrip", Integer.toString(rows)));

                try {
                    meta.mergeTree(meta.getNativeMetadataFormatName(), root);
                } catch (IIOInvalidTreeException e) {
                    throw new RuntimeException("Cannot update image metadata: "
                                + e.getMessage(), e);
                }
            }
        }
        return meta;
    }

    //number of pixels in 100 Meters
    private static final String DENOMINATOR_CENTIMETER = "/" + (100 * 100);
    private static final String DENOMINATOR_INCH = "/" + 1;

    private IIOMetadataNode createResolutionField(int number, String name,
            Integer resolution, ResolutionUnit unit) {

        String value;

        if (unit == ResolutionUnit.INCH) {

            value = resolution + DENOMINATOR_INCH;

        } else {

            float pixSzMM = 25.4f / resolution.floatValue();
            int numPix = (int)(((1000 * 100) / pixSzMM) + 0.5);
            value = numPix + DENOMINATOR_CENTIMETER;

        }

        return createRationalMetadataNode(number, name, value);
    }

    /**
     * Generate a TIFFField for resolution unit based on the parameters.
     * @param params
     * @return the new metadata node
     */
    private IIOMetadataNode createResolutionUnitField(ImageWriterParams params) {
        return createShortMetadataNode(TIFFImageDecoder.TIFF_RESOLUTION_UNIT, "ResolutionUnit",
                Integer.toString(params.getResolutionUnit().getValue()),
                params.getResolutionUnit().getDescription());
    }

    /**
     * Utility to create a TIFFShort metadata child node of a TIFFShorts node for TIFF metadata.
     *
     * @param number value of the number attribute of the TIFField
     * @param name value of the name attribute of the TIFFField
     * @param value value of the value attribute of the TIFFShort
     * @return the new metadata node
     */
    public static final IIOMetadataNode createShortMetadataNode(int number,
            String name, String value) {

        return createShortMetadataNode(number, name, value, null);
    }

    /**
     * Utility to create a TIFFShort metadata child node of a TIFFShorts node for TIFF metadata.
     *
     * @param number value of the number attribute of the TIFField
     * @param name value of the name attribute of the TIFFField
     * @param value value of the value attribute of the TIFFShort
     * @param description value of the description attribute of the TIFFShort, ignored if null
     * @return the new metadata node
     */
    public static final IIOMetadataNode createShortMetadataNode(int number, String name,
            String value, String description) {

        IIOMetadataNode field = createMetadataField(number, name);
        IIOMetadataNode arrayNode;
        IIOMetadataNode valueNode;
        arrayNode = new IIOMetadataNode("TIFFShorts");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFShort");
        valueNode.setAttribute("value", value);
        if (description != null) {
            valueNode.setAttribute("description", description);
        }
        arrayNode.appendChild(valueNode);

        return field;
    }

    /**
     * Utility to create a TIFFRational metadata child node of a TIFFRationals node for
     * TIFF metadata.
     *
     * @param number value of the number attribute of the TIFField
     * @param name value of the name attribute of the TIFFField
     * @param value value of the value attribute of the TIFFRational
     * @return the new metadata node
     */
    public static final IIOMetadataNode createRationalMetadataNode(int number,
            String name, String value) {

        IIOMetadataNode field = createMetadataField(number, name);
        IIOMetadataNode arrayNode;
        IIOMetadataNode valueNode;
        arrayNode = new IIOMetadataNode("TIFFRationals");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFRational");
        valueNode.setAttribute("value", value);
        arrayNode.appendChild(valueNode);

        return field;
    }

    /**
     * Utility function to create a base TIFFField node for TIFF metadata.
     *
     * @param number value of the number attribute of the TIFField
     * @param name value of the name attribute of the TIFFField
     * @return the new metadata node
     */
    public static final IIOMetadataNode createMetadataField(int number, String name) {

        IIOMetadataNode field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(number));
        field.setAttribute("name", name);
        return field;
    }

    /** {@inheritDoc} */
    @Override
    protected IIOMetadata createStreamMetadata(javax.imageio.ImageWriter writer,
            ImageWriteParam writeParam, ImageWriterParams params) {
        Endianness endian = (params != null ? params.getEndianness() : Endianness.DEFAULT);
        if (endian == Endianness.DEFAULT || endian == null) {
            return super.createStreamMetadata(writer, writeParam, params);
        }

        //Try changing the Byte Order
        IIOMetadata streamMetadata = writer.getDefaultStreamMetadata(writeParam);
        if (streamMetadata != null) {
            Set<String> names = new java.util.HashSet<String>(
                    Arrays.asList(streamMetadata.getMetadataFormatNames()));
            setFromTree(names, streamMetadata, endian, SUN_TIFF_NATIVE_STREAM_FORMAT);
            setFromTree(names, streamMetadata, endian, JAVA_TIFF_NATIVE_STREAM_FORMAT);
        }
        return streamMetadata;
    }

    private void setFromTree(Set<String> names, IIOMetadata streamMetadata, Endianness endian, String format) {
        if (names.contains(format)) {
            Node root = streamMetadata.getAsTree(format);
            root.getFirstChild().getAttributes().item(0).setNodeValue(endian.toString());
            try {
                streamMetadata.setFromTree(format, root);
            } catch (IIOInvalidTreeException e) {
                //This should not happen since we check if the format is supported.
                throw new IllegalStateException(
                        "Could not replace TIFF stream metadata: " + e.getMessage(), e);
            }
        }
    }

}
