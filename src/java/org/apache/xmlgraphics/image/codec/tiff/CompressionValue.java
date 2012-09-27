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


/** Enumerates the possible compression values for TIFF images. */
public enum CompressionValue {
    /** No compression. */
    NONE(1),
    /**
     * Modified Huffman Compression (CCITT Group 3 1D facsimile compression).
     * <p><b>Not currently supported.</b>
     */
    GROUP3_1D(2),
    /**
     * CCITT T.4 bilevel compression (CCITT Group 3 2D facsimile compression).
     * <p><b>Not currently supported.</b>
     */
    GROUP3_2D(3),
    /**
     * CCITT T.6 bilevel compression (CCITT Group 4 facsimile compression).
     * <p><b>Not currently supported.</b>
     */
    GROUP4(4),
    /** LZW compression. <p><b>Not supported.</b> */
    LZW(5),
    /**
     * Code for original JPEG-in-TIFF compression which has been depricated (for many good reasons)
     * in favor of Tech Note 2 JPEG compression (compression scheme 7).
     * <p><b>Not supported.</b>
     */
    JPEG_BROKEN(6),
    /** <a href="ftp://ftp.sgi.com/graphics/tiff/TTN2.draft.txt"> JPEG-in-TIFF</a> compression. */
    JPEG_TTN2(7),
    /** Byte-oriented run-length encoding "PackBits" compression. */
    PACKBITS(32773),
    /**
     * <a href="http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1951.txt">
     * DEFLATE</a> lossless compression (also known as "Zip-in-TIFF").
     */
    DEFLATE(32946);

    private final int compressionValue;

    private CompressionValue(int compressionValue) {
        this.compressionValue = compressionValue;
    }

    int getValue() {
        return compressionValue;
    }

    /**
     * Gets the compression value given the name of the compression type.
     * @param name the compression name
     * @return the compression value
     */
    public static CompressionValue getValue(String name) {
        if (name == null) {
            return PACKBITS;
        }
        for (CompressionValue cv : CompressionValue.values()) {
            if (cv.toString().equalsIgnoreCase(name)) {
                return cv;
            }
        }
        throw new IllegalArgumentException("Unknown compression value: " + name);
    }
}
