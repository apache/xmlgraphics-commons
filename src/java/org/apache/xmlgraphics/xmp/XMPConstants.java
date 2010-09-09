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

import org.apache.xmlgraphics.util.QName;

/**
 * Constants used in XMP metadata.
 */
public interface XMPConstants {

    /** Namespace URI for the xml: prefix */
    String XML_NS = "http://www.w3.org/XML/1998/namespace";

    /** Namespace URI for the xmlns: prefix */
    String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

    /** Namespace URI for XMP */
    String XMP_NAMESPACE = "adobe:ns:meta/";

    /** Namespace URI for RDF */
    String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** Namespace URI for Dublin Core */
    String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/elements/1.1/";

    /** Namespace URI for the XMP Basic Schema */
    String XMP_BASIC_NAMESPACE = "http://ns.adobe.com/xap/1.0/";

    /** Namespace URI for the Adobe PDF Schema */
    String ADOBE_PDF_NAMESPACE = "http://ns.adobe.com/pdf/1.3/";

    /**
     * Namespace URI for the PDF/A Identification Schema
     * (from the technical corrigendum 1 of ISO 19005-1:2005, note that the trailing slash
     * was missing in the original ISO 19005-1:2005 specification)
     */
    String PDF_A_IDENTIFICATION = "http://www.aiim.org/pdfa/ns/id/";

    /** Default language for the xml:lang property */
    String DEFAULT_LANGUAGE = "x-default";

    /** QName for rdf:value */
    QName RDF_VALUE = new QName(RDF_NAMESPACE, "rdf", "value");

}
