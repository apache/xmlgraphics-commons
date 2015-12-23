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

import java.util.Date;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class PDFVTAdapter extends XMPSchemaAdapter {
    /**
     * Constructs a new adapter for PDF/VT around the given metadata object.
     * @param meta the underlying metadata
     * @param namespace the namespace to access the schema (must be one of the PDF/VT schema
     *                  namespaces)
     */
    public PDFVTAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
    }

    public void setVersion(String v) {
        setValue("GTS_PDFVTVersion", v);
    }

    public void setModifyDate(Date modifyDate) {
        setDateValue("GTS_PDFVTModDate", modifyDate);
    }
}
