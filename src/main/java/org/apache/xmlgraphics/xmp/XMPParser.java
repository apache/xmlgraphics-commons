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

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

/**
 * The XMP parser.
 */
public final class XMPParser {

    private XMPParser() {
    }

    /**
     * Parses an XMP file.
     * @param url the URL to load the file from
     * @return the parsed Metadata object
     * @throws TransformerException if an error occurs while parsing the file
     */
    public static Metadata parseXMP(URL url) throws TransformerException {
        return parseXMP(new StreamSource(url.toExternalForm()));
    }

    /**
     * Parses an XMP file.
     * @param src a JAXP Source object where the XMP file can be loaded from
     * @return the parsed Metadata object
     * @throws TransformerException if an error occurs while parsing the file
     */
    public static Metadata parseXMP(Source src) throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = tFactory.newTransformer();
        XMPHandler handler = createXMPHandler();
        SAXResult res = new SAXResult(handler);
        transformer.transform(src, res);
        return handler.getMetadata();
    }

    /**
     * Creates and returns an XMPHandler for passive XMP parsing.
     * @return the requested XMPHandler
     */
    public static XMPHandler createXMPHandler() {
        return new XMPHandler();
    }

}
