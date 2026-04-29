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

package org.apache.xmlgraphics.xmp.merge;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPArray;
import org.apache.xmlgraphics.xmp.XMPArrayType;
import org.apache.xmlgraphics.xmp.XMPUtil;

public class ArrayAddPropertyMergerTestCase {

    @Test
    public void testArrayMerger() throws SAXException {
        Metadata meta = new Metadata();

        XMPArray array = new XMPArray(XMPArrayType.BAG);
        array.add(XMPUtil.createProperty("xmlns", "prefix", "value1"), "en", "parseType");

        XMPArray secondArray = new XMPArray(XMPArrayType.BAG);
        secondArray.add(XMPUtil.createProperty("xmlns", "prefix", "value2"), "en", "parseType");

        meta.setProperty(XMPUtil.createProperty("xmlns", "prefix", array));
        ArrayAddPropertyMerger merger = new ArrayAddPropertyMerger();
        merger.merge(XMPUtil.createProperty("xmlns", "prefix", secondArray), meta);

        assertEquals("Each property is it's own element of the array",
            "x:adobe:ns:meta/\n"
                + "<x:xmpmeta>\n"
                    + "rdf:http://www.w3.org/1999/02/22-rdf-syntax-ns#\n"
                    + "<rdf:RDF>\n"
                        + "pdfaExtension:http://www.aiim.org/pdfa/ns/extension/\n"
                        + "pdfaProperty:http://www.aiim.org/pdfa/ns/property#\n"
                        + "pdfaSchema:http://www.aiim.org/pdfa/ns/schema#\n"
                        + "<rdf:Description rdf:about=\"\">\n"
                            + "<xmlns:prefix>\n"
                                + "<rdf:Bag>\n"
                                    + "<rdf:li xml:lang=\"en\" rdf:parseType=\"parseType\">\n"
                                        + "<xmlns:prefix>\n"
                                            + "value1\n"
                                        + "</xmlns:prefix>\n"
                                    + "</rdf:li>\n"
                                    + "<rdf:li xml:lang=\"en\" rdf:parseType=\"parseType\">\n"
                                        + "<xmlns:prefix>\n"
                                            + "value2\n"
                                        + "</xmlns:prefix>\n"
                                    + "</rdf:li>\n"
                                + "</rdf:Bag>\n"
                            + "</xmlns:prefix>\n"
                        + "</rdf:Description>\n"
                    + "</rdf:RDF>\n"
                + "</x:xmpmeta>\n",
                XMPUtil.toSax(meta));
    }
}
