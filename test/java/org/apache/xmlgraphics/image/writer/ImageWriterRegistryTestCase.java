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

package org.apache.xmlgraphics.image.writer;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.writer.imageio.ImageIOPNGImageWriter;

/**
 * Tests the {@link ImageWriterRegistry}.
 */
public class ImageWriterRegistryTestCase {

    @Test
    public void testRegistry() throws Exception {
        ImageWriterRegistry registry = new ImageWriterRegistry();

        ImageWriter writer;
        writer = registry.getWriterFor("image/something");
        assertNull(writer);

        writer = registry.getWriterFor("image/png");
        assertTrue(writer instanceof ImageIOPNGImageWriter);

        registry.register(new DummyPNGWriter());

        ImageWriter dummy = registry.getWriterFor("image/png");
        assertEquals(DummyPNGWriter.class, dummy.getClass());

        registry.register(new OtherPNGWriter(), 50);

        dummy = registry.getWriterFor("image/png");
        assertEquals(OtherPNGWriter.class, dummy.getClass());
    }

    private static class DummyPNGWriter extends AbstractImageWriter {

        public String getMIMEType() {
            return "image/png";
        }

        public void writeImage(RenderedImage image, OutputStream out) throws IOException {
            //nop
        }

        public void writeImage(RenderedImage image, OutputStream out, ImageWriterParams params)
                throws IOException {
            //nop
        }
    }

    private static class OtherPNGWriter extends DummyPNGWriter {
    }
}
