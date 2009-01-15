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

package org.apache.xmlgraphics.image.loader;

import org.apache.xmlgraphics.util.MimeConstants;

/**
 * The flavor of an image indicates in which form it is available. A bitmap image loaded into
 * memory might be represented as a BufferedImage (indicated by ImageFlavor.BUFFERED_IMAGE).
 * It is mostly used by consuming code to indicate what kind of flavors can be processed so a
 * processing pipeline can do the necessary loading operations and conversions.
 */
public class ImageFlavor {

    /** An image in form of a RenderedImage instance */
    public static final ImageFlavor RENDERED_IMAGE = new ImageFlavor("RenderedImage");
    /** An image in form of a BufferedImage instance */
    public static final ImageFlavor BUFFERED_IMAGE = new SimpleRefinedImageFlavor(
                                                            RENDERED_IMAGE, "BufferedImage");
    /** An image in form of a W3C DOM instance */
    private static final ImageFlavor DOM = new ImageFlavor("DOM");
    /** An XML-based image in form of a W3C DOM instance */
    public static final ImageFlavor XML_DOM = new MimeEnabledImageFlavor(DOM, "text/xml");
    /** An image in form of a raw PNG file/stream */
    public static final ImageFlavor RAW = new ImageFlavor("Raw");
    /** An image in form of a raw PNG file/stream */
    public static final ImageFlavor RAW_PNG = new MimeEnabledImageFlavor(RAW,
                                                        MimeConstants.MIME_PNG);
    /** An image in form of a raw JPEG/JFIF file/stream */
    public static final ImageFlavor RAW_JPEG = new MimeEnabledImageFlavor(RAW,
                                                        MimeConstants.MIME_JPEG);
    /** An image in form of a raw EMF (Windows Enhanced Metafile) file/stream */
    public static final ImageFlavor RAW_EMF = new MimeEnabledImageFlavor(RAW,
                                                        MimeConstants.MIME_EMF);
    /** An image in form of a raw EPS (Encapsulated PostScript) file/stream */
    public static final ImageFlavor RAW_EPS = new MimeEnabledImageFlavor(RAW,
                                                        MimeConstants.MIME_EPS);
    /** An image in form of a raw CCITTFax stream */
    public static final ImageFlavor RAW_CCITTFAX = new ImageFlavor("RawCCITTFax");
    /** An image in form of a Graphics2DImage (can be painted on a Graphics2D interface) */
    public static final ImageFlavor GRAPHICS2D = new ImageFlavor("Graphics2DImage");

    private String name;

    /**
     * Constructs a new ImageFlavor. Please reuse existing constants wherever possible!
     * @param name the name of the flavor (must be unique)
     */
    public ImageFlavor(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the ImageFlavor.
     * @return the flavor name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the MIME type that the image flavor represents if a MIME type is available. This
     * is only applicable to images which can also exist as files. For images flavors like
     * decoded in-memory images (Rendered/BufferedImage), this method will return null.
     * @return the MIME type or null if no MIME type can be provided (like for in-memory images)
     */
    public String getMimeType() {
        return null;
    }

    /**
     * Returns the XML namespace URI that the image flavor represents if such a namespace URI
     * is available. This is only applicable to images in XML form. Other image types will return
     * null.
     * @return the XML or null if no MIME type can be provided (like for in-memory images)
     */
    public String getNamespace() {
        return null;
    }

    /**
     * Indicates whether a particular image flavor is compatible with this one.
     * @param flavor the other image flavor
     * @return true if the two are compatible
     */
    public boolean isCompatible(ImageFlavor flavor) {
        return this.equals(flavor);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageFlavor other = (ImageFlavor)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }

}
