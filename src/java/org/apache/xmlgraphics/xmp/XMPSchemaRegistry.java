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

import java.util.Map;

import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAOldXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAXMPSchema;

/**
 * This class is a registry of XMP schemas. It's implemented as a singleton.
 */
public class XMPSchemaRegistry {

    private static XMPSchemaRegistry instance = null;

    private Map schemas = new java.util.HashMap(); 
    
    private XMPSchemaRegistry() {
        init();
    }
    
    /** @return the singleton instance of the XMP schema registry. */
    public static XMPSchemaRegistry getInstance() {
        if (instance == null) {
            instance = new XMPSchemaRegistry();
        }
        return instance;
    }
    
    private void init() {
        addSchema(new DublinCoreSchema());
        addSchema(new PDFAXMPSchema());
        addSchema(new PDFAOldXMPSchema());
        addSchema(new XMPBasicSchema());
        addSchema(new AdobePDFSchema());
    }
    
    /**
     * Adds an XMP schema to the registry.
     * @param schema the XMP schema
     */
    public void addSchema(XMPSchema schema) {
        schemas.put(schema.getNamespace(), schema);
    }
    
    /**
     * Returns the XMP schema object for a given namespace.
     * @param namespace the namespace URI
     * @return the XMP schema or null if none is available
     */
    public XMPSchema getSchema(String namespace) {
        return (XMPSchema)schemas.get(namespace);
    }
    
}
