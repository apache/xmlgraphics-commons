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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAExtensionXMPSchema;

public class XMPSchemaRegistryTest {

    @Test
    public void testSchema() {
        XMPSchema schema = XMPSchemaRegistry.getInstance().getSchema(XMPConstants.PDF_A_EXTENSION);
        XMPSchema expectedSchema = new PDFAExtensionXMPSchema();

        assertNotNull("The namespace must have a registered schema", schema);
        assertEquals("The schema must be a PDFAExtension Schema",
                expectedSchema.getClass(), schema.getClass());
        assertEquals("The namespace needs to match the one passed as the argument",
                XMPConstants.PDF_A_EXTENSION, schema.getNamespace());
        assertEquals("The namespace needs to match the PDFAExtension Schema namespace",
                expectedSchema.getNamespace(), schema.getNamespace());
        assertEquals("The prefix used must be the one expected", "pdfaExtension",
                schema.getPreferredPrefix());
        assertEquals("The prefix used must be the PDFAExtension Schema prefix",
                expectedSchema.getPreferredPrefix(), schema.getPreferredPrefix());
    }
}
