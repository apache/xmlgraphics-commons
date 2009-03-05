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
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test case for SubInputStream.
 */
public class SubInputStreamTestCase extends TestCase {

    /**
     * Main constructor.
     * @param name the test case's name
     * @see junit.framework.TestCase#TestCase(String)
     */
    public SubInputStreamTestCase(String name) {
        super(name);
    }

    /**
     * Tests SubInputStream.
     * @throws Exception if an error occurs
     */
    public void testMain() throws Exception {
        //Initialize test data
        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(i & 0xff);
        }

        int v, c;
        byte[] buf;
        String s;

        SubInputStream subin = new SubInputStream(new ByteArrayInputStream(data), 10);
        v = subin.read();
        assertEquals(0, v);
        v = subin.read();
        assertEquals(1, v);

        buf = new byte[4];
        c = subin.read(buf);
        assertEquals(4, c);
        s = new String(buf, "US-ASCII");
        assertEquals("\u0002\u0003\u0004\u0005", s);

        Arrays.fill(buf, (byte)0);
        c = subin.read(buf, 2, 2);
        assertEquals(2, c);
        s = new String(buf, "US-ASCII");
        assertEquals("\u0000\u0000\u0006\u0007", s);

        Arrays.fill(buf, (byte)0);
        c = subin.read(buf);
        assertEquals(2, c);
        s = new String(buf, "US-ASCII");
        assertEquals("\u0008\u0009\u0000\u0000", s);
    }

}
