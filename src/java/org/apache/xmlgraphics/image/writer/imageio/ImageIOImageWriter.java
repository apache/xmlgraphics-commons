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
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

/**
 * ImageWriter implementation that uses Image I/O to write images.
 *
 * @version $Id$
 */
public class ImageIOImageWriter implements ImageWriter, IIOWriteWarningListener {

    private static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";

    private String targetMIME;

    /**
     * Main constructor.
     * @param mime the MIME type of the image format
     */
    public ImageIOImageWriter(String mime) {
        this.targetMIME = mime;
    }

    /**
     * @see ImageWriter#writeImage(java.awt.image.RenderedImage, java.io.OutputStream)
     */
    public void writeImage(RenderedImage image, OutputStream out) throws IOException {
        writeImage(image, out, null);
    }

    /**
     * @see ImageWriter#writeImage(java.awt.image.RenderedImage, java.io.OutputStream, ImageWriterParams)
     */
    public void writeImage(RenderedImage image, OutputStream out,
            ImageWriterParams params)
                throws IOException {
        javax.imageio.ImageWriter iiowriter = getIIOImageWriter();
        iiowriter.addIIOWriteWarningListener(this);

        ImageOutputStream imgout = ImageIO.createImageOutputStream(out);
        try {

            ImageWriteParam iwParam = getDefaultWriteParam(iiowriter, image, params);

            ImageTypeSpecifier type;
            if (iwParam.getDestinationType() != null) {
                type = iwParam.getDestinationType();
            } else {
                type = ImageTypeSpecifier.createFromRenderedImage(image);
            }

            //Handle metadata
            IIOMetadata meta = iiowriter.getDefaultImageMetadata(
                    type, iwParam);
            //meta might be null for some JAI codecs as they don't support metadata
            if (params != null && meta != null) {
                meta = updateMetadata(meta, params);
            }

            //Write image
            iiowriter.setOutput(imgout);
            IIOImage iioimg = new IIOImage(image, null, meta);
            iiowriter.write(null, iioimg, iwParam);

        } finally {
            imgout.close();
            iiowriter.dispose();
        }
    }

    private javax.imageio.ImageWriter getIIOImageWriter() {
        Iterator iter = ImageIO.getImageWritersByMIMEType(getMIMEType());
        javax.imageio.ImageWriter iiowriter = null;
        if (iter.hasNext()) {
            iiowriter = (javax.imageio.ImageWriter)iter.next();
        }
        if (iiowriter == null) {
            throw new UnsupportedOperationException("No ImageIO codec for writing "
                    + getMIMEType() + " is available!");
        }
        return iiowriter;
    }

    /**
     * Returns the default write parameters for encoding the image.
     * @param iiowriter The IIO ImageWriter that will be used
     * @param image the image to be encoded
     * @param params the parameters for this writer instance
     * @return the IIO ImageWriteParam instance
     */
    protected ImageWriteParam getDefaultWriteParam(
            javax.imageio.ImageWriter iiowriter, RenderedImage image,
            ImageWriterParams params) {
        ImageWriteParam param = iiowriter.getDefaultWriteParam();
        //System.err.println("Param: " + params);
        if ((params != null) && (params.getCompressionMethod() != null)) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(params.getCompressionMethod());
        }
        return param;
    }

    /**
     * Updates the metadata information based on the parameters to this writer.
     * @param meta the metadata
     * @param params the parameters
     * @return the updated metadata
     */
    protected IIOMetadata updateMetadata(IIOMetadata meta, ImageWriterParams params) {
        if (meta.isStandardMetadataFormatSupported()) {
            IIOMetadataNode root = (IIOMetadataNode)meta.getAsTree(STANDARD_METADATA_FORMAT);
            IIOMetadataNode dim = getChildNode(root, "Dimension");
            IIOMetadataNode child;
            if (params.getResolution() != null) {
                child = getChildNode(dim, "HorizontalPixelSize");
                if (child == null) {
                    child = new IIOMetadataNode("HorizontalPixelSize");
                    dim.appendChild(child);
                }
                child.setAttribute("value",
                        Double.toString(params.getResolution().doubleValue() / 25.4));
                child = getChildNode(dim, "VerticalPixelSize");
                if (child == null) {
                    child = new IIOMetadataNode("VerticalPixelSize");
                    dim.appendChild(child);
                }
                child.setAttribute("value",
                        Double.toString(params.getResolution().doubleValue() / 25.4));
            }
            try {
                meta.mergeTree(STANDARD_METADATA_FORMAT, root);
            } catch (IIOInvalidTreeException e) {
                throw new RuntimeException("Cannot update image metadata: "
                            + e.getMessage());
            }
        }
        return meta;
    }

    /**
     * Returns a specific metadata child node
     * @param n the base node
     * @param name the name of the child
     * @return the requested child node
     */
    protected static IIOMetadataNode getChildNode(Node n, String name) {
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (name.equals(child.getNodeName())) {
                return (IIOMetadataNode)child;
            }
        }
        return null;
    }

    /** @see ImageWriter#getMIMEType() */
    public String getMIMEType() {
        return this.targetMIME;
    }

    /** @see org.apache.xmlgraphics.image.writer.ImageWriter#isFunctional() */
    public boolean isFunctional() {
        Iterator iter = ImageIO.getImageWritersByMIMEType(getMIMEType());
        //Only return true if an IIO ImageWriter is available in the current environment
        return (iter.hasNext());
    }

    /**
     * @see javax.imageio.event.IIOWriteWarningListener#warningOccurred(
     *          javax.imageio.ImageWriter, int, java.lang.String)
     */
    public void warningOccurred(javax.imageio.ImageWriter source,
            int imageIndex, String warning) {
        System.err.println("Problem while writing image using ImageI/O: "
                + warning);
    }

    /**
     * @see org.apache.xmlgraphics.image.writer.ImageWriter#createMultiImageWriter(
     *          java.io.OutputStream)
     */
    public MultiImageWriter createMultiImageWriter(OutputStream out) throws IOException {
        return new IIOMultiImageWriter(out);
    }

    /** @see org.apache.xmlgraphics.image.writer.ImageWriter#supportsMultiImageWriter() */
    public boolean supportsMultiImageWriter() {
        javax.imageio.ImageWriter iiowriter = getIIOImageWriter();
        try {
            return iiowriter.canWriteSequence();
        } finally {
            iiowriter.dispose();
        }
    }

    private class IIOMultiImageWriter implements MultiImageWriter {

        private javax.imageio.ImageWriter iiowriter;
        private ImageOutputStream imageStream;

        public IIOMultiImageWriter(OutputStream out) throws IOException {
            this.iiowriter = getIIOImageWriter();
            if (!iiowriter.canWriteSequence()) {
                throw new UnsupportedOperationException("This ImageWriter does not support writing"
                        + " multiple images to a single image file.");
            }
            iiowriter.addIIOWriteWarningListener(ImageIOImageWriter.this);

            imageStream = ImageIO.createImageOutputStream(out);
            iiowriter.setOutput(imageStream);
            iiowriter.prepareWriteSequence(null);
        }

        public void writeImage(RenderedImage image, ImageWriterParams params) throws IOException {
            if (iiowriter == null) {
                throw new IllegalStateException("MultiImageWriter already closed!");
            }
            ImageWriteParam iwParam = getDefaultWriteParam(iiowriter, image, params);

            ImageTypeSpecifier type;
            if (iwParam.getDestinationType() != null) {
                type = iwParam.getDestinationType();
            } else {
                type = ImageTypeSpecifier.createFromRenderedImage(image);
            }

            //Handle metadata
            IIOMetadata meta = iiowriter.getDefaultImageMetadata(
                    type, iwParam);
            //meta might be null for some JAI codecs as they don't support metadata
            if (params != null && meta != null) {
                meta = updateMetadata(meta, params);
            }

            //Write image
            IIOImage iioimg = new IIOImage(image, null, meta);
            iiowriter.writeToSequence(iioimg, iwParam);
        }

        public void close() throws IOException {
            imageStream.close();
            imageStream = null;
            iiowriter.dispose();
            iiowriter = null;
        }

    }

}
