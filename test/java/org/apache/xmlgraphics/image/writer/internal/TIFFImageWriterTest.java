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

package org.apache.xmlgraphics.image.writer.internal;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

public class TIFFImageWriterTest extends TestCase {

    public void testJPEGWritingWithoutParams() throws Exception {
        //This used to generate a NPE because the JPEG encoding params were not set
        OutputStream out = new NullOutputStream();
        org.apache.xmlgraphics.image.writer.ImageWriter imageWriter = new TIFFImageWriter();
        MultiImageWriter writer = null;
        try {
            writer = imageWriter.createMultiImageWriter(out);
            // retrieve writer
            if (imageWriter != null) {
                ImageWriterParams iwp = new ImageWriterParams();
                iwp.setCompressionMethod("JPEG");

                for (int pageNumber = 0; pageNumber <= 2; pageNumber++) {
                    BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY);
                    writer.writeImage(image, iwp);
                }
            }
        } finally {
            writer.close();
        }
    }
}
