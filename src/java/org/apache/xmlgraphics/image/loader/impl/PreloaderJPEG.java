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
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.util.MimeConstants;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * Image preloader for JPEG images.
 */
public class PreloaderJPEG extends AbstractImagePreloader implements JPEGConstants {

    private static final int JPG_SIG_LENGTH = 3;
    private static final int[] BYTES_PER_COMPONENT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8}; // ignore 0
    private static final int EXIF = 0x45786966;
    private static final int II = 0x4949; // Intel
    private static final int MM = 0x4d4d; // Motorola
    private static final int X_RESOLUTION = 0x011a;
    private static final int Y_RESOLUTION = 0x011b;
    private static final int RESOLUTION_UNIT = 0x0128;

    /** {@inheritDoc}
     * @throws ImageException */
    public ImageInfo preloadImage(String uri, Source src, ImageContext context)
                throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, JPG_SIG_LENGTH);
        boolean supported = ((header[0] == (byte)MARK)
                && (header[1] == (byte)SOI)
                && (header[2] == (byte)MARK));

        if (supported) {
            ImageInfo info = new ImageInfo(uri, MimeConstants.MIME_JPEG);
            info.setSize(determineSize(in, context));
            return info;
        } else {
            return null;
        }
    }

    private ImageSize determineSize(ImageInputStream in, ImageContext context)
            throws IOException, ImageException {
        in.mark();
        try {
            ImageSize size = new ImageSize();
            JPEGFile jpeg = new JPEGFile(in);

            while (true) {
                int segID = jpeg.readMarkerSegment();
                //System.out.println("Segment: " + Integer.toHexString(segID));
                switch (segID) {
                case SOI:
                case NULL:
                    break;
                case APP0:
                    int reclen = jpeg.readSegmentLength();
                    in.skipBytes(7);
                    int densityUnits = in.read();
                    int xdensity = in.readUnsignedShort();
                    int ydensity = in.readUnsignedShort();
                    if (size.getDpiHorizontal() == 0) {
                        if (densityUnits == 2) {
                            //dots per centimeter
                            size.setResolution(
                                    xdensity * UnitConv.IN2CM,
                                    ydensity * UnitConv.IN2CM);
                        } else if (densityUnits == 1) {
                            //dots per inch
                            size.setResolution(xdensity, ydensity);
                        } else {
                            //resolution not specified
                            size.setResolution(context.getSourceResolution());
                        }
                    }
                    if (size.getWidthPx() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    in.skipBytes(reclen - 14);
                    break;
                case APP1:
                    // see http://www.media.mit.edu/pia/Research/deepview/exif.html
                    reclen = jpeg.readSegmentLength();
                    int bytesToEnd = reclen - 2;
                    // read Exif Header: 0x.45.78.69.66.00.00
                    int exif = in.readInt(); // 0x.45.78.69.66
                    int tail = in.readUnsignedShort(); // 0x.00.00
                    // in.skipBytes(6);
                    bytesToEnd -= 6;
                    if (exif != EXIF) {
                        // there may be multiple APP1 segments but we want the Exif one
                        in.skipBytes(bytesToEnd);
                        break;
                    }
                    // start TIFF data
                    int currentTIFFOffset = 0;
                    // byte align: 0x.49.49 (19789) means Intel, 0x.4D.4D means Motorola
                    int align = in.readUnsignedShort();
                    bytesToEnd -= 2;
                    currentTIFFOffset += 2;
                    ByteOrder originalByteOrder = in.getByteOrder();
                    // Intel = little, Motorola = big
                    in.setByteOrder(align == MM ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                    in.skipBytes(2); // 0x.2A.00 (Intel) or 0x.00.2A (Motorola)
                    bytesToEnd -= 2;
                    currentTIFFOffset += 2;
                    int firstIFDOffset = in.readInt();
                    bytesToEnd -= 4;
                    currentTIFFOffset += 4;
                    in.skipBytes(firstIFDOffset - 8);
                    bytesToEnd -= firstIFDOffset - 8;
                    currentTIFFOffset += firstIFDOffset - 8;
                    int directoryEntries = in.readUnsignedShort();
                    bytesToEnd -= 2;
                    currentTIFFOffset += 2;
                    int resolutionOffset = 0;
                    int resolutionFormat = 0;
                    int resolutionUnits = 0;
                    int resolution = 0;
                    boolean foundResolution = false;
                    for (int j = 0; j < directoryEntries; j++) {
                        int tag = in.readUnsignedShort();
                        if ((tag == X_RESOLUTION || tag == Y_RESOLUTION) && !foundResolution) {
                            // 0x011A (XResolution), 0x011B (YResolution)
                            int format = in.readUnsignedShort();
                            int components = in.readInt();
                            int dataByteLength = components * BYTES_PER_COMPONENT[format];
                            int value = in.readInt();
                            if (dataByteLength > 4) {
                                // value is offset to data value
                                resolutionOffset = value;
                            } else {
                                // value is data value
                                resolution = value;
                            }
                            resolutionFormat = format;
                            foundResolution = true;
                        } else if (tag == RESOLUTION_UNIT) {
                            // 0x0128 (ResolutionUnit)
                            int format = in.readUnsignedShort();
                            int components = in.readInt();
                            int dataByteLength = components * BYTES_PER_COMPONENT[format];
                            if (dataByteLength < 5 && format == 3) {
                                int value = in.readUnsignedShort();
                                in.skipBytes(2);
                                resolutionUnits = value;
                            } else {
                                in.skipBytes(4);
                            }
                        } else {
                            in.skipBytes(10);
                        }
                        bytesToEnd -= 12;
                        currentTIFFOffset += 12;
                    }
                    int nextIFDOffset = in.readInt(); // not needed, but has thumbnail info
                    bytesToEnd -= 4;
                    currentTIFFOffset += 4;
                    if (resolutionOffset != 0) {
                        in.skipBytes(resolutionOffset - currentTIFFOffset);
                        bytesToEnd -= resolutionOffset - currentTIFFOffset;
                        if (resolutionFormat == 5 || resolutionFormat == 10) {
                            int numerator = in.readInt();
                            int denominator = in.readInt();
                            resolution = numerator / denominator;
                            bytesToEnd -= 8;
                        }
                    }
                    in.skipBytes(bytesToEnd);
                    in.setByteOrder(originalByteOrder);
                    if (resolutionUnits == 3) {
                        // dots per centimeter
                        size.setResolution(resolution * UnitConv.IN2CM, resolution * UnitConv.IN2CM);
                    } else if (resolutionUnits == 2) {
                        // dots per inch
                        size.setResolution(resolution, resolution);
                    } else {
                        // resolution not specified
                        size.setResolution(context.getSourceResolution());
                    }
                    if (size.getWidthPx() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    break;
                case SOF0:
                case SOF1:
                case SOF2: // SOF2 and SOFA are only supported by PDF 1.3
                case SOFA:
                    reclen = jpeg.readSegmentLength();
                    in.skipBytes(1);
                    int height = in.readUnsignedShort();
                    int width = in.readUnsignedShort();
                    size.setSizeInPixels(width, height);
                    if (size.getDpiHorizontal() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    in.skipBytes(reclen - 7);
                    break;
                case SOS:
                case EOI:
                    // Break as early as possible (we don't want to read the whole file here)
                    if (size.getDpiHorizontal() == 0) {
                        size.setResolution(context.getSourceResolution());
                        size.calcSizeFromPixels();
                    }
                    return size;
                default:
                    jpeg.skipCurrentMarkerSegment();
                }
            }
        } finally {
            in.reset();
        }
    }

}
