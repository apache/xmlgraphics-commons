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

package org.apache.xmlgraphics.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.util.HexUtil;

/**
 * Test case for ASCII85InputStream.
 * <p>
 * ATTENTION: Some of the tests here depend on the correct behaviour of
 * ASCII85OutputStream. If something fails here make sure
 * ASCII85OutputStreamTestCase runs!
 */
public class ASCII85InputStreamTestCase {

    private static final boolean DEBUG = false;

    private byte[] decode(String text) throws Exception {
        byte[] ascii85 = text.getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(ascii85);
        InputStream decoder = new ASCII85InputStream(in);
        return IOUtils.toByteArray(decoder);
    }

    private byte[] getChunk(int count) {
        byte[] buf = new byte[count];
        System.arraycopy(ASCII85OutputStreamTestCase.DATA, 0, buf, 0, buf.length);
        return buf;
    }

    private String encode(byte[] data, int len) throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        java.io.OutputStream out = new ASCII85OutputStream(baout);
        out.write(data, 0, len);
        out.close();
        return new String(baout.toByteArray(), "US-ASCII");
    }


    private void innerTestDecode(byte[] data) throws Exception {
        String encoded = encode(data, data.length);
        if (DEBUG) {
            if (data[0] == 0) {
                System.out.println("self-encode: " + data.length + " chunk 000102030405...");
            } else {
                System.out.println("self-encode: " + new String(data, "US-ASCII")
                    + " " + HexUtil.toHex(data));
            }
            System.out.println("  ---> " + encoded);
        }
        byte[] decoded = decode(encoded);
        if (DEBUG) {
            if (data[0] == 0) {
                System.out.println("decoded: " + data.length + " chunk 000102030405...");
            } else {
                System.out.println("decoded: " + new String(decoded, "US-ASCII")
                    + " " + HexUtil.toHex(decoded));
            }
        }
        assertEquals(HexUtil.toHex(data), HexUtil.toHex(decoded));
    }

    /**
     * Tests the output of ASCII85.
     * @throws Exception if an error occurs
     */
    @Test
    public void testDecode() throws Exception {
        innerTestDecode("1. Bodypart".getBytes("US-ASCII"));
        if (DEBUG) {
            System.out.println("===========================================");
        }

        innerTestDecode(getChunk(1));
        innerTestDecode(getChunk(2));
        innerTestDecode(getChunk(3));
        innerTestDecode(getChunk(4));
        innerTestDecode(getChunk(5));
        if (DEBUG) {
            System.out.println("===========================================");
        }

        innerTestDecode(getChunk(10));
        innerTestDecode(getChunk(62));
        innerTestDecode(getChunk(63));
        innerTestDecode(getChunk(64));
        innerTestDecode(getChunk(65));

        if (DEBUG) {
            System.out.println("===========================================");
        }
        String sz;
        sz = HexUtil.toHex(decode("zz~>"));
        assertEquals(HexUtil.toHex(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}), sz);
        sz = HexUtil.toHex(decode("z\t \0z\n~>"));
        assertEquals(HexUtil.toHex(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}), sz);
        if (DEBUG) {
            System.out.println("===========================================");
        }
        try {
            decode("vz~>");
            fail("Illegal character should be detected");
        } catch (IOException ioe) {
            //expected
        }
        /* DISABLED because of try/catch in InputStream.read(byte[], int, int).
         * Only the exception happening on the first byte in a block is being
         * reported. BUG in JDK???
         *
        try {
            decode("zv~>");
            fail("Illegal character should be detected");
        } catch (IOException ioe) {
            //expected
        }*/
    }

    private byte[] getFullASCIIRange() {
        java.io.ByteArrayOutputStream baout = new java.io.ByteArrayOutputStream(256);
        for (int i = 254; i < 256; i++) {
            baout.write(i);
        }
        return baout.toByteArray();
    }

    /**
     * Tests the full 8-bit ASCII range.
     * @throws Exception if an error occurs
     */
    @Test
    public void testFullASCIIRange() throws Exception {
        innerTestDecode(getFullASCIIRange());
    }

}
