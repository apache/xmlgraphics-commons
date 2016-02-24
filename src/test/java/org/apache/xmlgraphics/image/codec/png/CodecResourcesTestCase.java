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

import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.fail;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.codec.util.MemoryCacheSeekableStream;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;

/**
 * Checks for the presence of message resources for the internal codecs.
 */
public class CodecResourcesTestCase {

    @Test
    public void testResources() throws Exception {

        InputStream in = new java.io.FileInputStream("test/images/barcode.eps");
        SeekableStream seekStream = new MemoryCacheSeekableStream(in);
        try {
            new PNGImage(seekStream, null);
            fail("Exception expected");
        } catch (RuntimeException re) {
            String msg = re.getMessage();
            if ("PNGImageDecoder0".equals(msg)) {
                re.printStackTrace();
                fail("Message resource don't seem to be present! Message is: " + msg);
            } else if (msg.toLowerCase().indexOf("magic") < 0) {
                fail("Message not as expected! Message is: " + msg);
            }
        } finally {
            IOUtils.closeQuietly(seekStream);
            IOUtils.closeQuietly(in);
        }
    }
}
