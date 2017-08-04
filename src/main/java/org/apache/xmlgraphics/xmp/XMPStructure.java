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

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.QName;

/**
 * Represents an XMP structure as defined by the XMP specification.
 */
public class XMPStructure extends XMPComplexValue implements PropertyAccess {

    private Map properties = new java.util.HashMap();

    /**
     * Main constructor
     */
    public XMPStructure() {
    }

    /** {@inheritDoc} */
    public Object getSimpleValue() {
        return null;
    }

    /** {@inheritDoc} */
    public void setProperty(XMPProperty prop) {
        properties.put(prop.getName(), prop);
    }

    /** {@inheritDoc} */
    public XMPProperty getProperty(String uri, String localName) {
        return getProperty(new QName(uri, localName));
    }

    /** {@inheritDoc} */
    public XMPProperty getValueProperty() {
        return getProperty(XMPConstants.RDF_VALUE);
    }

    /** {@inheritDoc} */
    public XMPProperty getProperty(QName name) {
        XMPProperty prop = (XMPProperty)properties.get(name);
        return prop;
    }

    /** {@inheritDoc} */
    public XMPProperty removeProperty(QName name) {
        return (XMPProperty)properties.remove(name);
    }

    /** {@inheritDoc} */
    public int getPropertyCount() {
        return this.properties.size();
    }

    /** {@inheritDoc} */
    public Iterator iterator() {
        return this.properties.keySet().iterator();
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.clear();
        handler.startElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:Description", atts);

        for (Object o : properties.values()) {
            XMPProperty prop = (XMPProperty) o;
            //if (prop.getName().getNamespaceURI().equals(ns)) {
            prop.toSAX(handler);
            //}
        }
        handler.endElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:Description");
    }

    /** {@inheritDoc} */
    public String toString() {
        return "XMP structure: " + getPropertyCount();
    }


}
