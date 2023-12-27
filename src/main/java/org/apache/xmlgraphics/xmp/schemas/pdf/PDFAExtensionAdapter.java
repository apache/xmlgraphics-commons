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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPArray;
import org.apache.xmlgraphics.xmp.XMPArrayType;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPProperty;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class PDFAExtensionAdapter extends XMPSchemaAdapter {

    private static final String SCHEMAS = "schemas";

    private static final String PDFA_PROPERTY = "pdfaProperty";

    private static final String PDFA_SCHEMA = "pdfaSchema";

    /**
     * Constructs a new adapter for PDF/A around the given metadata object.
     *
     * @param meta      the underlying metadata
     * @param namespace the namespace to access the schema (must be one of the PDF/A schema
     *                  namespaces)
     */
    public PDFAExtensionAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));

        QName schema = new QName(XMPConstants.PDF_A_SCHEMA, PDFA_SCHEMA, "schema");
        QName namespaceURI = new QName(XMPConstants.PDF_A_SCHEMA, PDFA_SCHEMA, "namespaceURI");
        QName prefix = new QName(XMPConstants.PDF_A_SCHEMA, PDFA_SCHEMA, "prefix");
        QName property = new QName(XMPConstants.PDF_A_SCHEMA, PDFA_SCHEMA, "property");

        QName name = new QName(XMPConstants.PDF_A_PROPERTY, PDFA_PROPERTY, "name");
        QName valueType = new QName(XMPConstants.PDF_A_PROPERTY, PDFA_PROPERTY, "valueType");
        QName category = new QName(XMPConstants.PDF_A_PROPERTY, PDFA_PROPERTY, "category");
        QName description = new QName(XMPConstants.PDF_A_PROPERTY, PDFA_PROPERTY, "description");


        List<XMPProperty> subPropertyList = new ArrayList<>();
        subPropertyList.add(new XMPProperty(name, "part"));
        subPropertyList.add(new XMPProperty(valueType, "Integer"));
        subPropertyList.add(new XMPProperty(category, "internal"));
        subPropertyList.add(new XMPProperty(description, "Indicates, which part of ISO 14289 standard is followed"));

        XMPArray subArray = new XMPArray(XMPArrayType.SEQ);
        subArray.add(subPropertyList, null, "Resource");

        List<XMPProperty> propertyList = new ArrayList<>();
        propertyList.add(new XMPProperty(schema, "PDF/UA identification schema"));
        propertyList.add(new XMPProperty(namespaceURI, "http://www.aiim.org/pdfua/ns/id/"));
        propertyList.add(new XMPProperty(prefix, "pdfuaid"));
        propertyList.add(new XMPProperty(property, subArray));

        XMPArray array = new XMPArray(XMPArrayType.BAG);
        array.add(propertyList, null, "Resource");

        XMPProperty prop = new XMPProperty(new QName(namespace, SCHEMAS), array);

        meta.setProperty(prop);
    }
}
