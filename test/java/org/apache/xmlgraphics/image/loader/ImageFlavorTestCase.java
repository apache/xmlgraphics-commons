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

package org.apache.xmlgraphics.image.loader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Tests for image flavors.
 */
public class ImageFlavorTestCase {

    @Test
    public void testBasicFlavors() throws Exception {
        ImageFlavor f1;
        ImageFlavor f2;

        f1 = ImageFlavor.RAW_JPEG;
        f2 = ImageFlavor.RAW_PNG;
        assertFalse(f1.equals(f2));
        assertEquals(MimeConstants.MIME_JPEG, f1.getMimeType());
        assertNull(f1.getNamespace());
        assertEquals(MimeConstants.MIME_PNG, f2.getMimeType());
        assertNull(f2.getNamespace());

        f1 = ImageFlavor.GRAPHICS2D;
        f2 = new ImageFlavor(ImageFlavor.GRAPHICS2D.getName());
        assertTrue(f1.equals(f2));
        assertNull(f1.getMimeType());
        assertNull(f1.getNamespace());
    }

    @Test
    public void testRefinedFlavors() throws Exception {
        ImageFlavor f1;
        ImageFlavor f2;

        f1 = ImageFlavor.RENDERED_IMAGE;
        f2 = ImageFlavor.BUFFERED_IMAGE;
        assertFalse(f1.equals(f2));
        assertTrue(f2.isCompatible(f1));
        assertFalse(f1.isCompatible(f2));

        assertNull(f1.getMimeType());
        assertNull(f1.getNamespace());
        assertNull(f2.getMimeType());
        assertNull(f2.getNamespace());

        f1 = ImageFlavor.XML_DOM;
        f2 = new XMLNamespaceEnabledImageFlavor(ImageFlavor.XML_DOM, "http://www.w3.org/2000/svg");
        assertFalse(f1.equals(f2));
        assertTrue(f2.isCompatible(f1));
        assertFalse(f1.isCompatible(f2));

        assertEquals("text/xml", f1.getMimeType());
        assertNull(f1.getNamespace());
        assertEquals("text/xml", f2.getMimeType());
        assertEquals("http://www.w3.org/2000/svg", f2.getNamespace());
    }

}
