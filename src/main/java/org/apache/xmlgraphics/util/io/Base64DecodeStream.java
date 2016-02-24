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

import java.io.IOException;
import java.io.InputStream;

// CSOFF: ConstantName
// CSOFF: MemberName
// CSOFF: MultipleVariableDeclarations
// CSOFF: NeedBraces
// CSOFF: OperatorWrap
// CSOFF: WhitespaceAround

/**
 * This class implements a Base64 Character decoder as specified in RFC1113.
 * Unlike some other encoding schemes there is nothing in this encoding that
 * tells the decoder where a buffer starts or stops, so to use it you will need
 * to isolate your encoded data into a single chunk and then feed them
 * this decoder. The simplest way to do that is to read all of the encoded
 * data into a string and then use:
 * <pre>
 *      byte    data[];
 *      InputStream is = new ByteArrayInputStream(data);
 *      is = new Base64DecodeStream(is);
 * </pre>
 *
 * On errors, this class throws a IOException with the following detail
 * strings:
 * <pre>
 *    "Base64DecodeStream: Bad Padding byte (2)."
 *    "Base64DecodeStream: Bad Padding byte (1)."
 * </pre>
 *
 * @version $Id$
 *
 * Originally authored by Thomas DeWeese, Vincent Hardy, and Chuck McManis.
 */

public class Base64DecodeStream extends InputStream {

    InputStream src;

    public Base64DecodeStream(InputStream src) {
        this.src = src;
    }

    private static final byte[] PEM_ARRAY = new byte[256];
    static {
        for (int i = 0; i < PEM_ARRAY.length; i++) {
            PEM_ARRAY[i] = -1;
        }

        int idx = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            PEM_ARRAY[c] = (byte)idx++;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            PEM_ARRAY[c] = (byte)idx++;
        }

        for (char c = '0'; c <= '9'; c++) {
            PEM_ARRAY[c] = (byte)idx++;
        }

        PEM_ARRAY['+'] = (byte)idx++;
        PEM_ARRAY['/'] = (byte)idx++;
    }

    public boolean markSupported() {
        return false;
    }

    public void close()
        throws IOException {
        eof = true;
    }

    public int available()
        throws IOException {
        return 3 - outOffset;
    }

    byte[] decodeBuffer = new byte[4];
    byte[] outBuffer = new byte[3];
    int  outOffset = 3;
    boolean eof;

    public int read() throws IOException {

        if (outOffset == 3) {
            if (eof || getNextAtom()) {
                eof = true;
                return -1;
            }
        }

        return ((int)outBuffer[outOffset++]) & 0xFF;
    }

    public int read(byte []out, int offset, int len)
        throws IOException {

        int idx = 0;
        while (idx < len) {
            if (outOffset == 3) {
                if (eof || getNextAtom()) {
                    eof = true;
                    if (idx == 0) {
                        return -1;
                    } else {
                        return idx;
                    }
                }
            }

            out[offset + idx] = outBuffer[outOffset++];

            idx++;
        }
        return idx;
    }

    final boolean getNextAtom() throws IOException {
        int count;
        int a;
        int b;
        int c;
        int d;

        int off = 0;
        while (off != 4) {
            count = src.read(decodeBuffer, off, 4 - off);
            if (count == -1) {
                return true;
            }

            int in = off;
            int out = off;
            while (in < off + count) {
                if ((decodeBuffer[in] != '\n')
                    && (decodeBuffer[in] != '\r')
                    && (decodeBuffer[in] != ' ')) {
                    decodeBuffer[out++] = decodeBuffer[in];
                }
                in++;
            }

            off = out;
        }

        a = PEM_ARRAY[((int)decodeBuffer[0]) & 0xFF];
        b = PEM_ARRAY[((int)decodeBuffer[1]) & 0xFF];
        c = PEM_ARRAY[((int)decodeBuffer[2]) & 0xFF];
        d = PEM_ARRAY[((int)decodeBuffer[3]) & 0xFF];

        outBuffer[0] = (byte)((a << 2) | (b >>> 4));
        outBuffer[1] = (byte)((b << 4) | (c >>> 2));
        outBuffer[2] = (byte)((c << 6) |  d);

        if (decodeBuffer[3] != '=') {
            // All three bytes are good.
            outOffset = 0;
        } else if (decodeBuffer[2] == '=') {
            // Only one byte of output.
            outBuffer[2] = outBuffer[0];
            outOffset = 2;
            eof = true;
        } else {
            // Only two bytes of output.
            outBuffer[2] = outBuffer[1];
            outBuffer[1] = outBuffer[0];
            outOffset = 1;
            eof = true;
        }

        return false;
    }
}
