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

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides methods useful for processing JPEG files.
 */
public class JPEGFile implements JPEGConstants {

    /** logger */
    protected static Log log = LogFactory.getLog(JPEGFile.class);

    private DataInput in;

    /**
     * Constructor for ImageInputStreams.
     * @param in the input stream to read the image from
     */
    public JPEGFile(ImageInputStream in) {
        this.in = in;
    }

    /**
     * Constructor for InputStreams.
     * @param in the input stream to read the image from
     */
    public JPEGFile(InputStream in) {
        this.in = new java.io.DataInputStream(in);
    }

    /**
     * Returns the {@link DataInput} instance this object operates on.
     * @return the data input instance
     */
    public DataInput getDataInput() {
        return this.in;
    }

    /**
     * Reads the next marker segment identifier and returns it.
     * @return the marker segment identifier
     * @throws IOException if an I/O error occurs while reading from the image file
     */
    public int readMarkerSegment() throws IOException {
        int marker;
        do {
            marker = in.readByte() & 0xFF;
            //Skip any non-0xFF bytes (useful for JPEG files with bad record lengths)
        } while (marker != MARK);

        int segID;
        do {
            segID = in.readByte() & 0xFF;
            //Skip any pad bytes (0xFF) which are legal here.
        } while (segID == 0xFF);
        return segID;
    }

    /**
     * Reads the segment length of the current marker segment and returns it.
     * The method assumes the file cursor is right after the segment header.
     * @return the segment length
     * @throws IOException if an I/O error occurs while reading from the image file
     */
    public int readSegmentLength() throws IOException {
        int reclen = in.readUnsignedShort();
        return reclen;
    }

    /**
     * Skips the current marker segment.
     * The method assumes the file cursor is right after the segment header.
     * @throws IOException if an I/O error occurs while reading from the image file
     */
    public void skipCurrentMarkerSegment() throws IOException {
        int reclen = readSegmentLength();
        in.skipBytes(reclen - 2);
    }

}
