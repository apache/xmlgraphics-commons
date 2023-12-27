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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xmlgraphics.util.QName;

/**
 * Helper class for XMP related tests
 */
public final class XMPUtil {

    private XMPUtil() {
        //added to remove the default constructor
    }

    public static String toSax(Metadata meta) throws SAXException {
        StringBuilder sb = new StringBuilder();

        meta.toSAX(new DefaultHandler() {
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                sb.append("<").append(qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    sb.append(" ").append(attributes.getQName(i)).append("=").append("\"")
                            .append(attributes.getValue(i)).append("\"");
                }
                sb.append(">\n");

                if (uri != null && !sb.toString().contains(uri)) {
                    sb.append(qName.split(":")[0]).append(":").append(uri).append("\n");
                }
            }

            public void characters(char[] ch, int start, int length) {
                sb.append(new String(ch)).append("\n");
            }

            public void startPrefixMapping(String prefix, String ns) {
                sb.append(prefix).append(":").append(ns).append("\n");
            }

            public void endElement(String uri, String localName, String qName) {
                sb.append("</").append(qName).append(">\n");
            }
        });

        return sb.toString();
    }

    public static XMPProperty createProperty(String prefix, String name, Object value) {
        return new XMPProperty(getQName(prefix, name), value);
    }

    public static QName getQName(String prefix, String name) {
        return new QName(XMPConstants.PDF_A_EXTENSION, prefix, name);
    }
}
