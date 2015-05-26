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

package org.apache.xmlgraphics.image.codec.png;

import java.io.DataInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PNGChunk {
    int length;
    int type;
    byte[] data;
    int crc;

    String typeString;

    /** logger */
    protected static final Log log = LogFactory.getLog(PNGChunk.class);

    /**
     * See http://en.wikipedia.org/wiki/Portable_Network_Graphics for a light explanation;
     * See http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html for the spec.
     */
    public enum ChunkType {
        IHDR, // IHDR must be the first chunk
        PLTE, // PLTE contains the palette
        IDAT, // IDAT contains the image, which may be split among multiple IDAT chunks
        IEND, // IEND marks the image end
        bKGD, // bKGD gives the default background color
        cHRM, // cHRM gives the chromaticity coordinates
        gAMA, // gAMA specifies gamma
        hIST, // hIST can store the histogram
        iCCP, // iCCP is an ICC color profile
        iTXt, // iTXt contains UTF-8 text
        pHYs, // pHYs holds the intended pixel size
        sBIT, // sBIT (significant bits) indicates the color-accuracy
        sPLT, // sPLT suggests a palette to use
        sRGB, // sRGB indicates that the standard sRGB color space is used
        sTER, // sTER stereo-image indicator chunk for stereoscopic images
        tEXt, // tEXt can store text that can be represented in ISO/IEC 8859-1
        tIME, // tIME stores the time that the image was last changed
        tRNS, // tRNS contains transparency information
        zTXt; // zTXt contains compressed text with the same limits as tEXt
    }

    public PNGChunk(int length, int type, byte[] data, int crc) {
        this.length = length;
        this.type = type;
        this.data = data;
        this.crc = crc;
        this.typeString = typeIntToString(this.type);
    }

    public int getLength() {
        return length;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        return typeString;
    }

    public byte[] getData() {
        return data;
    }

    public byte getByte(int offset) {
        return data[offset];
    }

    public int getInt1(int offset) {
        return data[offset] & 0xff;
    }

    public int getInt2(int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    public int getInt4(int offset) {
        return ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16)
                | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
    }

    public String getString4(int offset) {
        return "" + (char) data[offset] + (char) data[offset + 1] + (char) data[offset + 2]
                + (char) data[offset + 3];
    }

    public boolean isType(String typeName) {
        return typeString.equals(typeName);
    }

    /**
     * Reads the next chunk from the input stream.
     * @param distream the input stream
     * @return the chunk
     */
    public static PNGChunk readChunk(DataInputStream distream) {
        try {
            int length = distream.readInt();
            int type = distream.readInt();
            byte[] data = new byte[length];
            distream.readFully(data);
            int crc = distream.readInt();

            return new PNGChunk(length, type, data, crc);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the PNG chunk type, a four letter case sensitive ASCII type/name.
     * @param distream the input stream
     * @return a four letter case sensitive ASCII type/name
     */
    public static String getChunkType(DataInputStream distream) {
        try {
            distream.mark(8);
            /* int length = */distream.readInt();
            int type = distream.readInt();
            distream.reset();

            return typeIntToString(type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String typeIntToString(int type) {
        String typeString = "";
        typeString += (char) (type >> 24);
        typeString += (char) ((type >> 16) & 0xff);
        typeString += (char) ((type >> 8) & 0xff);
        typeString += (char) (type & 0xff);
        return typeString;
    }

    /**
     * Skips the next chunk from the input stream.
     * @param distream the input stream
     * @return true if skipping successful, false otherwise
     */
    public static boolean skipChunk(DataInputStream distream) {
        try {
            int length = distream.readInt();
            distream.readInt();
            // is this really faster than reading?
            int skipped = distream.skipBytes(length);
            distream.readInt();
            if (skipped != length) {
                log.warn("Incorrect number of bytes skipped.");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}
