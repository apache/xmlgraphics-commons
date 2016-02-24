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

package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.color.ColorSpace;

import org.apache.xmlgraphics.image.codec.util.PropertyUtil;

enum ImageType {
    UNSUPPORTED(-1),
    BILEVEL_WHITE_IS_ZERO(0),
    BILEVEL_BLACK_IS_ZERO(1),
    GRAY(1),
    PALETTE(3),
    RGB(2),
    CMYK(5),
    YCBCR(6),
    CIELAB(8),
    GENERIC(1);

    private final int photometricInterpretation;

    private ImageType(int photometricInterpretation) {
        this.photometricInterpretation = photometricInterpretation;
    }

    int getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    static ImageType getTypeFromRGB(int mapSize, byte[] r, byte[] g, byte[] b,
            int dataTypeSize, int numBands) {
        if (numBands == 1) {
            if (dataTypeSize == 1) { // Bilevel image
                if (mapSize != 2) {
                    throw new IllegalArgumentException(PropertyUtil.getString("TIFFImageEncoder7"));
                }

                if (isBlackZero(r, g, b)) {
                    return BILEVEL_BLACK_IS_ZERO;
                } else if (isWhiteZero(r, g, b)) {
                    return BILEVEL_WHITE_IS_ZERO;
                }
            }
            return PALETTE;
        }
        return UNSUPPORTED;
    }

    private static boolean rgbIsValueAt(byte[] r, byte[] g, byte[] b, byte value, int i) {
        return r[i] == value && g[i] == value && b[i] == value;
    }

    private static boolean bilevelColorValue(byte[] r, byte[] g, byte[] b, int blackValue,
            int whiteValue) {
        return rgbIsValueAt(r, g, b, (byte) blackValue, 0)
                && rgbIsValueAt(r, g, b, (byte) whiteValue, 1);
    }

    private static boolean isBlackZero(byte[] r, byte[] g, byte[] b) {
        return bilevelColorValue(r, g, b, 0, 0xff);
    }

    private static boolean isWhiteZero(byte[] r, byte[] g, byte[] b) {
        return bilevelColorValue(r, g, b, 0xff, 0);
    }

    static ImageType getTypeFromColorSpace(ColorSpace colorSpace, TIFFEncodeParam params) {
        switch (colorSpace.getType()) {
        case ColorSpace.TYPE_CMYK:
            return CMYK;
        case ColorSpace.TYPE_GRAY:
            return GRAY;
        case ColorSpace.TYPE_Lab:
            return CIELAB;
        case ColorSpace.TYPE_RGB:
            if (params.getJPEGCompressRGBToYCbCr()) {
                return ImageType.YCBCR;
            } else {
                return ImageType.RGB;
            }
        case ColorSpace.TYPE_YCbCr:
            return YCBCR;
        default:
            return GENERIC;
        }
    }
}
