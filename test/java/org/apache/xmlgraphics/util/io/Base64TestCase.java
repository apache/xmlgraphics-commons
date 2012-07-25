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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * This test validates that the Base64 encoder/decoders work properly.
 *
 * @version $Id$
 */
public class Base64TestCase {

    private void innerBase64Test(String action, URL in, URL ref) throws Exception {
        InputStream inIS = in.openStream();

        if (action.equals("ROUND")) {
            ref = in;
        } else if (!action.equals("ENCODE") && !action.equals("DECODE")) {
            fail("Bad action string");
        }

        InputStream refIS = ref.openStream();

        if (action.equals("ENCODE") || action.equals("ROUND")) {
            // We need to encode the incomming data
            PipedOutputStream pos = new PipedOutputStream();
            OutputStream os = new Base64EncodeStream(pos);

            // Copy the input to the Base64 Encoder (in a seperate thread).
            Thread t = new StreamCopier(inIS, os);

            // Read that from the piped output stream.
            inIS = new PipedInputStream(pos);
            t.start();
        }

        if (action.equals("DECODE") || action.equals("ROUND")) {
            inIS = new Base64DecodeStream(inIS);
        }


        int mismatch = compareStreams(inIS, refIS, action.equals("ENCODE"));

        if (mismatch != -1) {
            fail("Wrong result");
        }
    }

    private void innerBase64Test(String action, String in, String ref) throws Exception {
        final String baseURL = "file:test/resources/org/apache/xmlgraphics/util/io/";
        innerBase64Test(action, new URL(baseURL + in), new URL(baseURL + ref));
    }

    private void innerBase64Test(String in) throws Exception {
        innerBase64Test("ROUND", in, in);
    }

    private void testBase64Group(String name) throws Exception {
        innerBase64Test("ENCODE", name, name + ".64");
        innerBase64Test("DECODE", name + ".64", name);
        innerBase64Test(name);
    }

    /**
     * This method will only throw exceptions if some aspect
     * of the test's internal operation fails.
     */
    @Test
    public void testBase64() throws Exception {
        System.out.println(new File(".").getCanonicalPath());
        testBase64Group("zeroByte");
        testBase64Group("oneByte");
        testBase64Group("twoByte");
        testBase64Group("threeByte");
        testBase64Group("fourByte");
        testBase64Group("tenByte");
        testBase64Group("small");
        testBase64Group("medium");
        innerBase64Test("DECODE", "medium.pc.64", "medium");
        innerBase64Test("large");
}

    /**
     * Returns true if the contents of <tt>is1</tt> match the
     * contents of <tt>is2</tt>
     */
    public static int compareStreams(InputStream is1, InputStream is2,
                              boolean skipws) {
        byte[] data1 = new byte[100];
        byte[] data2 = new byte[100];
        int off1 = 0;
        int off2 = 0;
        int idx = 0;

        try {
            while (true) {
                int len1 = is1.read(data1, off1, data1.length - off1);
                int len2 = is2.read(data2, off2, data2.length - off2);

                if (off1 != 0) {
                    if (len1 == -1) {
                        len1 = off1;
                    } else {
                        len1 += off1;
                    }
                }

                if (off2 != 0) {
                    if (len2 == -1) {
                        len2 = off2;
                    } else {
                        len2 += off2;
                    }
                }

                if (len1 == -1) {
                    if (len2 == -1) {
                        break; // Both done...
                    }
                    // Only is1 is done...
                    if (!skipws) {
                        return idx;
                    }
                    // check if the rest of is2 is whitespace...
                    for (int i2 = 0; i2 < len2; i2++) {
                        if ((data2[i2] != '\n') && (data2[i2] != '\r') && (data2[i2] != ' ')) {
                            return idx + i2;
                        }
                    }
                    off1 = off2 = 0;
                    continue;
                }

                if (len2 == -1) {
                    // Only is2 is done...
                    if (!skipws) {
                        return idx;
                    }

                    // Check if rest of is1 is whitespace...
                    for (int i1 = 0; i1 < len1; i1++) {
                        if ((data1[i1] != '\n') && (data1[i1] != '\r') && (data1[i1] != ' ')) {
                            return idx + i1;
                        }
                    }
                    off1 = off2 = 0;
                    continue;
                }

                int i1 = 0;
                int i2 = 0;
                while ((i1 < len1) && (i2 < len2)) {
                    if (skipws) {
                        if ((data1[i1] == '\n') || (data1[i1] == '\r') || (data1[i1] == ' ')) {
                            i1++;
                            continue;
                        }
                        if ((data2[i2] == '\n') || (data2[i2] == '\r') || (data2[i2] == ' ')) {
                            i2++;
                            continue;
                        }
                    }
                    if (data1[i1] != data2[i2]) {
                        return idx + i2;
                    }

                    i1++;
                    i2++;
                }

                if (i1 != len1) {
                    System.arraycopy(data1, i1, data1, 0, len1 - i1);
                }
                if (i2 != len2) {
                    System.arraycopy(data2, i2, data2, 0, len2 - i2);
                }
                off1 = len1 - i1;
                off2 = len2 - i2;
                idx += i2;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return idx;
        }

        return -1;
    }


    static class StreamCopier extends Thread {
        InputStream src;
        OutputStream dst;

        public StreamCopier(InputStream src,
                            OutputStream dst) {
            this.src = src;
            this.dst = dst;
        }

        public void run() {
            try {
                byte[] data = new byte[1000];
                while (true) {
                    int len = src.read(data, 0, data.length);
                    if (len == -1) {
                        break;
                    }

                    dst.write(data, 0, len);
                }
            } catch (IOException ioe) {
                // Nothing
            }
            try {
                dst.close();
            } catch (IOException ioe) {
                // Nothing
            }
        }
    }
}
