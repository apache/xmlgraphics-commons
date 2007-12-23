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
import org.apache.xmlgraphics.util.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is the base class for all XMP properties.
 */
public class XMPProperty implements XMLizable {

    private QName name;
    private Object value;
    private String xmllang;

    /**
     * Creates a new XMP property.
     * @param name the name of the property
     * @param value the value for the property
     */
    public XMPProperty(QName name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    /** @return the qualified name of the property (namespace URI + local name) */
    public QName getName() {
        return this.name;
    }
    
    /** @return the namespace URI of the property */
    public String getNamespace() {
        return getName().getNamespaceURI();
    }
    
    /**
     * Sets the value of the property
     * @param value the new value
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * @return the property value (can be a normal Java object (normally a String) or a descendant
     *         of XMPComplexValue.
     */
    public Object getValue() {
        return this.value;
    }
    
    /**
     * Sets the xml:lang value for this property
     * @param lang the language ("x-default" for the default language, null to make the value
     *             language-independent)
     */
    public void setXMLLang(String lang) {
        this.xmllang = lang;
    }
    
    /**
     * @return the language for language-dependent values ("x-default" for the default language)
     */
    public String getXMLLang() {
        return this.xmllang;
    }
    
    /** @return the XMPArray for an array or null if the value is not an array. */
    public XMPArray getArrayValue() {
        return (value instanceof XMPArray ? (XMPArray)value : null);
    }
    
    /**
     * Converts a simple value to an array of a given type if the value is not already an array.
     * @param type the desired type of array
     */
    public void convertSimpleValueToArray(XMPArrayType type) {
        if (getArrayValue() == null) {
            XMPArray array = new XMPArray(type);
            if (getXMLLang() != null) {
                array.add(getValue().toString(), getXMLLang());
            } else {
                array.add(getValue());
            }
            setValue(array);
            setXMLLang(null);
        }
    }
    
    private String getEffectiveQName() {
        String prefix = getName().getPrefix();
        if (prefix == null || "".equals(prefix)) {
            XMPSchema schema = XMPSchemaRegistry.getInstance().getSchema(getNamespace());
            prefix = schema.getPreferredPrefix();
        }
        return prefix + ":" + getName().getLocalName();
    }
    
    /** @see org.apache.xmlgraphics.util.XMLizable#toSAX(org.xml.sax.ContentHandler) */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        String qName = getEffectiveQName();
        handler.startElement(getName().getNamespaceURI(), 
                getName().getLocalName(), qName, atts);
        if (value instanceof XMPComplexValue) {
            XMPComplexValue cv = ((XMPComplexValue)value);
            Object obj = cv.getSimpleValue();
            if (obj != null) {
                char[] chars = obj.toString().toCharArray();
                handler.characters(chars, 0, chars.length);
            } else {
                cv.toSAX(handler);
            }
        } else {
            char[] chars = value.toString().toCharArray();
            handler.characters(chars, 0, chars.length);
        }
        handler.endElement(getName().getNamespaceURI(), 
                getName().getLocalName(), qName);
    }
}
