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

package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.xmp.XMPArray;
import org.apache.xmlgraphics.xmp.XMPArrayType;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPProperty;
import org.apache.xmlgraphics.xmp.XMPUtil;
import org.apache.xmlgraphics.xmp.merge.PropertyMerger;

public class PDFAExtensionXMPSchemaTest {

    @Test
    public void testPDFAExtensionSchema() {
        PDFAExtensionXMPSchema schema = new PDFAExtensionXMPSchema();

        assertEquals("pdfaExtension", schema.getPreferredPrefix());
        assertEquals(XMPConstants.PDF_A_EXTENSION, schema.getNamespace());
        assertFalse("Must have the namespaces of child properties", schema.getExtraNamespaces().isEmpty());

        assertTrue("The prefix must be the key", schema.getExtraNamespaces().containsKey("pdfaSchema"));
        assertTrue("The prefix must be the key", schema.getExtraNamespaces().containsKey("pdfaProperty"));


        assertEquals("Namespace must match the correct prefix`", XMPConstants.PDF_A_SCHEMA,
                schema.getExtraNamespaces().get("pdfaSchema"));
        assertEquals("Namespace must match the correct prefix`", XMPConstants.PDF_A_PROPERTY,
                schema.getExtraNamespaces().get("pdfaProperty"));
    }

    @Test
    public void testMergerForPDFAExtensionSchemas() {
        XMPProperty prop = XMPUtil.createProperty("pdfaExtension", "schemas", new XMPArray(XMPArrayType.BAG));

        PropertyMerger merger = new PDFAExtensionXMPSchema().getDefaultMergeRuleSet().getPropertyMergerFor(prop);
        assertNotNull("Each property must have a merger associated with it, or a default merger", merger);
        assertEquals("The default merge rule for the schemas property must be the ArrayAddPropertyMerger",
                "ArrayAddPropertyMerger", merger.getClass().getSimpleName());
    }
}
