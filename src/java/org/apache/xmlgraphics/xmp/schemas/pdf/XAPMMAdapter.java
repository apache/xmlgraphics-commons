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

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class XAPMMAdapter extends XMPSchemaAdapter {
    /**
     * Constructs a new adapter for XAP MM around the given metadata object.
     * @param meta the underlying metadata
     * @param namespace the namespace to access the schema (must be one of the PDF/VT schema
     *                  namespaces)
     */
    public XAPMMAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
    }

    public void setVersion(String v) {
        setValue("VersionID", v);
    }

    /**
     * The rendition class name for this resource.
     * @param c the value
     */
    public void setRenditionClass(String c) {
        setValue("RenditionClass", c);
    }

    public void setInstanceID(String v) {
        setValue("InstanceID", v);
    }

    public void setDocumentID(String v) {
        setValue("DocumentID", v);
    }
}
