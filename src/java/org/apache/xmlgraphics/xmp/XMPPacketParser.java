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

package org.apache.xmlgraphics.xmp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * This class is a parser for XMP packets. By default, it tries to locate the first XMP packet
 * it finds and parses it.
 * <p>
 * Important: Before you use this class to look for an XMP packet in some random file, please read
 * the chapter on "Scanning Files for XMP Packets" in the XMP specification!
 */
public final class XMPPacketParser {

    private XMPPacketParser() {
    }

    private static final byte[] PACKET_HEADER;
    private static final byte[] PACKET_HEADER_END;
    private static final byte[] PACKET_TRAILER;

    static {
        try {
            PACKET_HEADER = "<?xpacket begin=".getBytes("US-ASCII");
            PACKET_HEADER_END = "?>".getBytes("US-ASCII");
            PACKET_TRAILER = "<?xpacket".getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Incompatible JVM! US-ASCII encoding not supported.");
        }
    }

    /**
     * Locates an XMP packet in a stream, parses it and returns the XMP metadata. If no
     * XMP packet is found until the stream ends, null is returned. Note: This method
     * only finds the first XMP packet in a stream. And it cannot determine whether it
     * has found the right XMP packet if there are multiple packets.
     * @param in the InputStream to search
     * @return the parsed XMP metadata or null if no XMP packet is found
     * @throws IOException if an I/O error occurs
     * @throws TransformerException if an error occurs while parsing the XMP packet
     */
    public static Metadata parse(InputStream in) throws IOException, TransformerException {
        if (!in.markSupported()) {
            in = new java.io.BufferedInputStream(in);
        }
        boolean foundXMP = skipAfter(in, PACKET_HEADER);
        if (!foundXMP) {
            return null;
        }
        //TODO Inspect "begin" attribute!
        if (!skipAfter(in, PACKET_HEADER_END)) {
            throw new IOException("Invalid XMP packet header!");
        }
        //TODO think about not buffering this but for example, parse in another thread
        //ex. using PipedInput/OutputStream
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        //TODO Do with TeeInputStream when Commons IO 1.4 is available
        if (!skipAfter(in, PACKET_TRAILER, baout)) {
            throw new IOException("XMP packet not properly terminated!");
        }

        Metadata metadata = XMPParser.parseXMP(
                new StreamSource(new ByteArrayInputStream(baout.toByteArray())));
        return metadata;
    }

    private static boolean skipAfter(InputStream in, byte[] match) throws IOException {
        return skipAfter(in, match, null);
    }

    private static boolean skipAfter(InputStream in, byte[] match, OutputStream out)
            throws IOException {
        int found = 0;
        int len = match.length;
        int b;
        while ((b = in.read()) >= 0) {
            if (b == match[found]) {
                found++;
                if (found == len) {
                    return true;
                }
            } else {
                if (out != null) {
                    if (found > 0) {
                        out.write(match, 0, found);
                    }
                    out.write(b);
                }
                found = 0;
            }
        }
        return false;
    }

}
