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

import java.awt.Color;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.w3c.dom.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageLoader;
import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.io.XmlSourceUtil;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

/**
 * An ImageLoader implementation based on ImageIO for loading bitmap images.
 */
public class ImageLoaderImageIO extends AbstractImageLoader {

    /** logger */
    protected static final Log log = LogFactory.getLog(ImageLoaderImageIO.class);

    private ImageFlavor targetFlavor;

    private static final String PNG_METADATA_NODE = "javax_imageio_png_1.0";

    private static final String JPEG_METADATA_NODE = "javax_imageio_jpeg_image_1.0";

    private static final Set PROVIDERS_IGNORING_ICC = new HashSet();

    /**
     * Main constructor.
     * @param targetFlavor the target flavor
     */
    public ImageLoaderImageIO(ImageFlavor targetFlavor) {
        if (!(ImageFlavor.BUFFERED_IMAGE.equals(targetFlavor)
                || ImageFlavor.RENDERED_IMAGE.equals(targetFlavor))) {
            throw new IllegalArgumentException("Unsupported target ImageFlavor: " + targetFlavor);
        }
        this.targetFlavor = targetFlavor;
    }

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return this.targetFlavor;
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
            throws ImageException, IOException {
        RenderedImage imageData = null;
        IIOException firstException = null;

        IIOMetadata iiometa = (IIOMetadata)info.getCustomObjects().get(
                ImageIOUtil.IMAGEIO_METADATA);
        boolean ignoreMetadata = (iiometa != null);
        boolean providerIgnoresICC = false;

        Source src = session.needSource(info.getOriginalURI());
        ImageInputStream imgStream = ImageUtil.needImageInputStream(src);
        try {
            Iterator iter = ImageIO.getImageReaders(imgStream);
            while (iter.hasNext()) {
                ImageReader reader = (ImageReader)iter.next();
                try {
                    imgStream.mark();
                    reader.setInput(imgStream, false, ignoreMetadata);
                    ImageReadParam param = getParam(reader, hints);
                    final int pageIndex = ImageUtil.needPageIndexFromURI(info.getOriginalURI());
                    try {
//                        if (ImageFlavor.BUFFERED_IMAGE.equals(this.targetFlavor)) {
                            imageData = reader.read(pageIndex, param);
//                        } else {
//                            imageData = reader.read(pageIndex, param);
                            //imageData = reader.readAsRenderedImage(pageIndex, param);
                            //TODO Reenable the above when proper listeners are implemented
                            //to react to late pixel population (so the stream can be closed
                            //properly).
//                        }
                        if (iiometa == null) {
                            iiometa = reader.getImageMetadata(pageIndex);
                        }
                        providerIgnoresICC = checkProviderIgnoresICC(reader
                                .getOriginatingProvider());
                        break; //Quit early, we have the image
                    } catch (IndexOutOfBoundsException indexe) {
                        throw new ImageException("Page does not exist. Invalid image index: "
                                + pageIndex);
                    } catch (IllegalArgumentException iae) {
                        //Some codecs like com.sun.imageio.plugins.wbmp.WBMPImageReader throw
                        //IllegalArgumentExceptions when they have trouble parsing the image.
                        throw new ImageException("Error loading image using ImageIO codec", iae);
                    } catch (IIOException iioe) {
                        if (firstException == null) {
                            firstException = iioe;
                        } else {
                            log.debug("non-first error loading image: " + iioe.getMessage());
                        }
                    }
                    try {
                        //Try fallback for CMYK images
                        BufferedImage bi = getFallbackBufferedImage(reader, pageIndex, param);
                        imageData = bi;
                        firstException = null; //Clear exception after successful fallback attempt
                        break;
                    } catch (IIOException iioe) {
                        //ignore
                    }
                    imgStream.reset();
                } finally {
                    reader.dispose();
                }
            }
        } finally {
            XmlSourceUtil.closeQuietly(src);
            //TODO Some codecs may do late reading.
        }
        if (firstException != null) {
            throw new ImageException("Error while loading image: "
                    + firstException.getMessage(), firstException);
        }
        if (imageData == null) {
            throw new ImageException("No ImageIO ImageReader found .");
        }

        ColorModel cm = imageData.getColorModel();

        Color transparentColor = null;
        if (cm instanceof IndexColorModel) {
            //transparent color will be extracted later from the image
        } else {
            if (providerIgnoresICC && cm instanceof ComponentColorModel) {
                // Apply ICC Profile to Image by creating a new image with a new
                // color model.
                ICC_Profile iccProf = tryToExctractICCProfile(iiometa);
                if (iccProf != null) {
                    ColorModel cm2 = new ComponentColorModel(
                            new ICC_ColorSpace(iccProf), cm.hasAlpha(), cm
                                    .isAlphaPremultiplied(), cm
                                    .getTransparency(), cm.getTransferType());
                    WritableRaster wr = Raster.createWritableRaster(imageData
                            .getSampleModel(), null);
                    imageData.copyData(wr);
                    try {
                        BufferedImage bi = new BufferedImage(cm2, wr, cm2
                                .isAlphaPremultiplied(), null);
                        imageData = bi;
                        cm = cm2;
                    } catch (IllegalArgumentException iae) {
                        String msg = "Image " + info.getOriginalURI()
                                + " has an incompatible color profile."
                                + " The color profile will be ignored."
                                + "\nColor model of loaded bitmap: " + cm
                                + "\nColor model of color profile: " + cm2;
                        if (info.getCustomObjects().get("warningincustomobject") != null) {
                            info.getCustomObjects().put("warning", msg);
                        } else {
                            log.warn(msg);
                        }
                    }
                }
            }

            // ImageIOUtil.dumpMetadataToSystemOut(iiometa);
            // Retrieve the transparent color from the metadata
            if (iiometa != null && iiometa.isStandardMetadataFormatSupported()) {
                Element metanode = (Element)iiometa.getAsTree(
                        IIOMetadataFormatImpl.standardMetadataFormatName);
                Element dim = ImageIOUtil.getChild(metanode, "Transparency");
                if (dim != null) {
                    Element child;
                    child = ImageIOUtil.getChild(dim, "TransparentColor");
                    if (child != null) {
                        String value = child.getAttribute("value");
                        if (value.length() == 0) {
                            //ignore
                        } else if (cm.getNumColorComponents() == 1) {
                            int gray = Integer.parseInt(value);
                            transparentColor = new Color(gray, gray, gray);
                        } else {
                            StringTokenizer st = new StringTokenizer(value);
                            transparentColor = new Color(
                                    Integer.parseInt(st.nextToken()),
                                    Integer.parseInt(st.nextToken()),
                                    Integer.parseInt(st.nextToken()));
                        }
                    }
                }
            }
        }

        if (ImageFlavor.BUFFERED_IMAGE.equals(this.targetFlavor)) {
            return new ImageBuffered(info, (BufferedImage)imageData, transparentColor);
        } else {
            return new ImageRendered(info, imageData, transparentColor);
        }
    }

    private ImageReadParam getParam(ImageReader reader, Map hints) throws IOException {
        if (hints != null && Boolean.TRUE.equals(hints.get("CMYK"))) {
            Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
            while (types.hasNext()) {
                ImageTypeSpecifier type = types.next();
                if (type.getNumComponents() == 4) {
                    ImageReadParam param = new ImageReadParam();
                    param.setDestinationType(type);
                    return param;
                }
            }
        }
        return reader.getDefaultReadParam();
    }

    /**
     * Checks if the provider ignores the ICC color profile. This method will
     * assume providers work correctly, and return false if the provider is
     * unknown. This ensures backward-compatibility.
     *
     * @param provider
     *            the ImageIO Provider
     * @return true if we know the provider to be broken and ignore ICC
     *         profiles.
     */
    private boolean checkProviderIgnoresICC(IIOServiceProvider provider) {
        // TODO: This information could be cached.
        StringBuffer b = new StringBuffer(provider.getDescription(Locale.ENGLISH));
        b.append('/').append(provider.getVendorName());
        b.append('/').append(provider.getVersion());
        if (log.isDebugEnabled()) {
            log.debug("Image Provider: " + b.toString());
        }
        return ImageLoaderImageIO.PROVIDERS_IGNORING_ICC.contains(b.toString());
    }

    /**
     * Extract ICC Profile from ImageIO Metadata. This method currently only
     * supports PNG and JPEG metadata.
     *
     * @param iiometa
     *            The ImageIO Metadata
     * @return an ICC Profile or null.
     */
    private ICC_Profile tryToExctractICCProfile(IIOMetadata iiometa) {
        ICC_Profile iccProf = null;
        String[] supportedFormats = iiometa.getMetadataFormatNames();
        for (String format : supportedFormats) {
            Element root = (Element) iiometa.getAsTree(format);
            if (PNG_METADATA_NODE.equals(format)) {
                iccProf = this
                        .tryToExctractICCProfileFromPNGMetadataNode(root);
            } else if (JPEG_METADATA_NODE.equals(format)) {
                iccProf = this.tryToExctractICCProfileFromJPEGMetadataNode(root);
            }
        }
        return iccProf;
    }

    private ICC_Profile tryToExctractICCProfileFromPNGMetadataNode(
            Element pngNode) {
        ICC_Profile iccProf = null;
        Element iccpNode = ImageIOUtil.getChild(pngNode, "iCCP");
        if (iccpNode instanceof IIOMetadataNode) {
            IIOMetadataNode imn = (IIOMetadataNode) iccpNode;
            byte[] prof = (byte[]) imn.getUserObject();
            String comp = imn.getAttribute("compressionMethod");
            if ("deflate".equalsIgnoreCase(comp)) {
                Inflater decompresser = new Inflater();
                decompresser.setInput(prof);
                byte[] result = new byte[100];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                boolean failed = false;
                while (!decompresser.finished() && !failed) {
                    try {
                        int resultLength = decompresser.inflate(result);
                        bos.write(result, 0, resultLength);
                        if (resultLength == 0) {
                            // this means more data or an external dictionary is
                            // needed. Both of which are not available, so we
                            // fail.
                            log.debug("Failed to deflate ICC Profile");
                            failed = true;
                        }
                    } catch (DataFormatException e) {
                        log.debug("Failed to deflate ICC Profile", e);
                        failed = true;
                    }
                }
                decompresser.end();
                try {
                    iccProf = ColorProfileUtil.getICC_Profile(bos.toByteArray());
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to interpret embedded ICC Profile", e);
                    iccProf = null;
                }
            }
        }
        return iccProf;
    }

    private ICC_Profile tryToExctractICCProfileFromJPEGMetadataNode(
            Element jpgNode) {
        ICC_Profile iccProf = null;
        Element jfifNode = ImageIOUtil.getChild(jpgNode, "app0JFIF");
        if (jfifNode != null) {
            Element app2iccNode = ImageIOUtil.getChild(jfifNode, "app2ICC");
            if (app2iccNode instanceof IIOMetadataNode) {
                IIOMetadataNode imn = (IIOMetadataNode) app2iccNode;
                iccProf = (ICC_Profile) imn.getUserObject();
            }
        }
        return iccProf;
    }

    private BufferedImage getFallbackBufferedImage(ImageReader reader,
            int pageIndex, ImageReadParam param) throws IOException {
        //Work-around found at: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799903
        //There are some additional ideas there if someone wants to go further.

        // Try reading a Raster (no color conversion).
        Raster raster = reader.readRaster(pageIndex, param);

        // Arbitrarily select a BufferedImage type.
        int imageType;
        int numBands = raster.getNumBands();
        switch(numBands) {
        case 1:
            imageType = BufferedImage.TYPE_BYTE_GRAY;
            break;
        case 3:
            imageType = BufferedImage.TYPE_3BYTE_BGR;
            break;
        case 4:
            imageType = BufferedImage.TYPE_4BYTE_ABGR;
            break;
        default:
            throw new UnsupportedOperationException("Unsupported band count: " + numBands);
        }

        // Create a BufferedImage.
        BufferedImage bi = new BufferedImage(raster.getWidth(),
                                  raster.getHeight(),
                                  imageType);

        // Set the image data.
        bi.getRaster().setRect(raster);
        return bi;
    }

    static {
        // TODO: This list could be kept in a resource file.
        PROVIDERS_IGNORING_ICC
                .add("Standard PNG image reader/Sun Microsystems, Inc./1.0");
        PROVIDERS_IGNORING_ICC
                .add("Standard PNG image reader/Oracle Corporation/1.0");
        PROVIDERS_IGNORING_ICC
                .add("Standard JPEG Image Reader/Sun Microsystems, Inc./0.5");
        PROVIDERS_IGNORING_ICC
                .add("Standard JPEG Image Reader/Oracle Corporation/0.5");
    }
}
