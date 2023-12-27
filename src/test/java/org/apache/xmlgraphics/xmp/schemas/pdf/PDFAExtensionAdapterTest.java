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

import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPUtil;

public class PDFAExtensionAdapterTest {

    @Test
    public void testPDFAExtensionElement() throws SAXException {
        Metadata meta = new Metadata();

        PDFAExtensionXMPSchema.getAdapter(meta);

        assertEquals("x:adobe:ns:meta/\n"
                + "<x:xmpmeta>\n"
                + "rdf:http://www.w3.org/1999/02/22-rdf-syntax-ns#\n"
                + "<rdf:RDF>\n"
                + "pdfaExtension:http://www.aiim.org/pdfa/ns/extension/\n"
                + "<rdf:Description rdf:about=\"\">\n"
                + "<pdfaExtension:schemas>\n"
                + "<rdf:Bag>\n"
                + "<rdf:li rdf:parseType=\"Resource\">\n"
                + "<pdfaSchema:schema>\n"
                + "pdfaSchema:http://www.aiim.org/pdfa/ns/schema#\n"
                + "PDF/UA identification schema\n"
                + "</pdfaSchema:schema>\n"
                + "<pdfaSchema:namespaceURI>\n"
                + "http://www.aiim.org/pdfua/ns/id/\n"
                + "</pdfaSchema:namespaceURI>\n"
                + "<pdfaSchema:prefix>\n"
                + "pdfuaid\n"
                + "</pdfaSchema:prefix>\n"
                + "<pdfaSchema:property>\n"
                + "<rdf:Seq>\n"
                + "<rdf:li rdf:parseType=\"Resource\">\n"
                + "<pdfaProperty:name>\n"
                + "pdfaProperty:http://www.aiim.org/pdfa/ns/property#\n"
                + "part\n"
                + "</pdfaProperty:name>\n"
                + "<pdfaProperty:valueType>\n"
                + "Integer\n"
                + "</pdfaProperty:valueType>\n"
                + "<pdfaProperty:category>\n"
                + "internal\n"
                + "</pdfaProperty:category>\n"
                + "<pdfaProperty:description>\n"
                + "Indicates, which part of ISO 14289 standard is followed\n"
                + "</pdfaProperty:description>\n"
                + "</rdf:li>\n"
                + "</rdf:Seq>\n"
                + "</pdfaSchema:property>\n"
                + "</rdf:li>\n"
                + "</rdf:Bag>\n"
                + "</pdfaExtension:schemas>\n"
                + "</rdf:Description>\n"
                + "</rdf:RDF>\n"
                + "</x:xmpmeta>\n", XMPUtil.toSax(meta));
    }
}
