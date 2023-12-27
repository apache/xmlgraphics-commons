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
import static org.junit.Assert.assertNull;

public class XMPSchemaAdapterTest {

    public static final String PDFA_EXTENSION = "pdfaExtension";
    public static final String NAME = "name";
    public static final String VALUE = "value";

    @Test
    public void testSetPropertyNewProperty() throws Exception {
        Metadata meta = new Metadata();
        XMPSchemaAdapter adapter = new XMPSchemaAdapter(meta,
                XMPSchemaRegistry.getInstance().getSchema(XMPConstants.PDF_A_EXTENSION));

        XMPProperty subProperty = XMPUtil.createProperty(PDFA_EXTENSION, NAME, VALUE);

        adapter.setProperty(NAME, subProperty);
        XMPProperty expectedProperty = meta.getProperty(XMPUtil.getQName(PDFA_EXTENSION, NAME));

        assertEquals("Property must be added to the metadata with the expected name",
                XMPUtil.getQName(PDFA_EXTENSION, NAME), expectedProperty.getName());
        assertEquals("The value of the newly created property must be the subProperty that was given",
                subProperty, expectedProperty.getValue());
    }

    @Test
    public void testSetPropertyReplaceOldProperty() throws Exception {
        Metadata meta = new Metadata();
        XMPSchemaAdapter adapter = new XMPSchemaAdapter(meta,
                XMPSchemaRegistry.getInstance().getSchema(XMPConstants.PDF_A_EXTENSION));

        XMPProperty oldSubProperty = XMPUtil.createProperty(PDFA_EXTENSION, NAME, VALUE);
        XMPProperty newSubProperty = XMPUtil.createProperty(PDFA_EXTENSION, NAME, VALUE);

        //set old property
        adapter.setProperty(NAME, oldSubProperty);
        XMPProperty expectedProperty = meta.getProperty(XMPUtil.getQName(PDFA_EXTENSION, NAME));

        assertEquals("Confirming a new property was set",
                XMPUtil.getQName(PDFA_EXTENSION, NAME), expectedProperty.getName());
        assertEquals("Confirming the value of the new property",
                oldSubProperty, expectedProperty.getValue());

        //replace old property with a new one
        adapter.setProperty(NAME, newSubProperty);
        expectedProperty = meta.getProperty(XMPUtil.getQName(PDFA_EXTENSION, NAME));

        assertEquals("Confirming the property still exists after it's value was replaced",
                XMPUtil.getQName(PDFA_EXTENSION, NAME), expectedProperty.getName());
        assertEquals("Value must be updated on the previously created property",
                newSubProperty, expectedProperty.getValue());
    }

    @Test
    public void testSetPropertyDeleteOldProperty() throws Exception {
        Metadata meta = new Metadata();
        XMPSchemaAdapter adapter = new XMPSchemaAdapter(meta,
                XMPSchemaRegistry.getInstance().getSchema(XMPConstants.PDF_A_EXTENSION));

        XMPProperty oldSubProperty = XMPUtil.createProperty(PDFA_EXTENSION, NAME, VALUE);

        //set old property
        adapter.setProperty(NAME, oldSubProperty);
        XMPProperty expectedProperty = meta.getProperty(XMPUtil.getQName(PDFA_EXTENSION, NAME));

        assertEquals("Confirming a new property was set",
                XMPUtil.getQName(PDFA_EXTENSION, NAME), expectedProperty.getName());
        assertEquals("Confirming the value of the new property",
                oldSubProperty, expectedProperty.getValue());

        //delete old property
        adapter.setProperty(NAME, null);
        expectedProperty = meta.getProperty(XMPUtil.getQName(PDFA_EXTENSION, NAME));

        assertNull("When setting the property value to null, "
                + "the property with the respective name must be deleted", expectedProperty);
    }
}
