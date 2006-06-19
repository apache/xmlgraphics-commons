/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Schema adapter implementation for the Adobe PDF schema.
 */
public class AdobePDFAdapter extends XMPSchemaAdapter {

    /**
     * Constructs a new adapter for Adobe PDF around the given metadata object.
     * @param meta the underlying metadata
     */
    public AdobePDFAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
    }

    /** @return the keywords */
    public String getKeywords() {
        return getValue("Keywords");
    }
    
    /**
     * Sets the keywords.
     * @param value the keywords
     */
    public void setKeywords(String value) {
        setValue("Keywords", value);
    }

    /** @return the PDF version */
    public String getPDFVersion() {
        return getValue("PDFVersion");
    }
    
    /**
     * Sets the PDF version
     * @param value the PDF version (ex. "1.4")
     */
    public void setPDFVersion(String value) {
        setValue("PDFVersion", value);
    }

    /** @return the name of the tool that produced the PDF document */
    public String getProducer() {
        return getValue("Producer");
    }
    
    /**
     * Sets the name of the tool that produced the PDF document
     * @param value the producer
     */
    public void setProducer(String value) {
        setValue("Producer", value);
    }

}
