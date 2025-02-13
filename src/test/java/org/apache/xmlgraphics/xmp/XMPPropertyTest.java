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

public class XMPPropertyTest {

    @Test
    public void testXMPPropertyAsValue() throws Exception {
        Metadata meta = new Metadata();

        XMPProperty subProperty = XMPUtil.createProperty("prefix1", "name1", "value1");

        meta.setProperty(XMPUtil.createProperty("xmlns", XMPConstants.PDF_A_PROPERTY, subProperty));

        assertEquals("Properties must be able to have child properties",
                "x:adobe:ns:meta/\n"
                + "<x:xmpmeta>\n"
                + "rdf:http://www.w3.org/1999/02/22-rdf-syntax-ns#\n"
                + "<rdf:RDF>\n"
                + "pdfaExtension:http://www.aiim.org/pdfa/ns/extension/\n"
                + "pdfaProperty:http://www.aiim.org/pdfa/ns/property#\n"
                + "pdfaSchema:http://www.aiim.org/pdfa/ns/schema#\n"
                + "<rdf:Description rdf:about=\"\">\n"
                + "<xmlns:http://www.aiim.org/pdfa/ns/property#>\n"
                + "<prefix1:name1>\n"
                + "value1\n"
                + "</prefix1:name1>\n"
                + "</xmlns:http://www.aiim.org/pdfa/ns/property#>\n"
                + "</rdf:Description>\n"
                + "</rdf:RDF>\n"
                + "</x:xmpmeta>\n",
                XMPUtil.toSax(meta));
    }
}
