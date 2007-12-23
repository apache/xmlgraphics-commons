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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xmlgraphics.util.QName;

/**
 * Passive XMP parser implemented as a SAX DefaultHandler. After the XML document has been parsed
 * the Metadata object can be retrieved.
 */
public class XMPHandler extends DefaultHandler {

    private Metadata meta;
    
    private StringBuffer content = new StringBuffer();
    //private Attributes lastAttributes;
    private Stack attributesStack = new Stack();
    private Stack nestingInfoStack = new Stack();
    private Stack contextStack = new Stack();
    
    //private QName currentPropertyName;
    //private XMPProperty currentProperty;
    //private XMPComplexValue currentComplexValue;
    //private PropertyAccess currentProperties;
    
    /** @return the parsed metadata, available after the parsing. */
    public Metadata getMetadata() {
        return this.meta;
    }

    private boolean hasComplexContent() {
        Object obj = this.contextStack.peek();
        if (obj instanceof QName) {
            return false;
        } else {
            return true;
        }
    }
    
    private PropertyAccess getCurrentProperties() {
        Object obj = this.contextStack.peek();
        if (obj instanceof PropertyAccess) {
            return (PropertyAccess)obj;
        } else {
            return null;
        }
    }
    
    private QName getCurrentPropName() {
        Object obj = this.contextStack.peek();
        if (obj instanceof QName) {
            return (QName)obj;
        } else {
            return null;
        }
    }
    
    private QName popCurrentPropName() throws SAXException {
        Object obj = this.contextStack.pop();
        this.nestingInfoStack.pop();
        if (obj instanceof QName) {
            return (QName)obj;
        } else {
            throw new SAXException("Invalid XMP structure. Property name expected");
        }
    }
    
    private XMPComplexValue getCurrentComplexValue() {
        Object obj = this.contextStack.peek();
        if (obj instanceof XMPComplexValue) {
            return (XMPComplexValue)obj;
        } else {
            return null;
        }
    }
    
    private XMPStructure getCurrentStructure() {
        Object obj = this.contextStack.peek();
        if (obj instanceof XMPStructure) {
            return (XMPStructure)obj;
        } else {
            return null;
        }
    }
    
    private XMPArray getCurrentArray(boolean required) throws SAXException {
        Object obj = this.contextStack.peek();
        if (obj instanceof XMPArray) {
            return (XMPArray)obj;
        } else {
            if (required) {
                throw new SAXException("Invalid XMP structure. Not in array");
            } else {
                return null;
            }
        }
    }

    // --- Overrides ---
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(
     *      java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
                throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        content.setLength(0); //Reset text buffer (see characters())
        attributesStack.push(new AttributesImpl(attributes));
        
        if (XMPConstants.XMP_NAMESPACE.equals(uri)) {
            if (!"xmpmeta".equals(localName)) {
                throw new SAXException("Expected x:xmpmeta element, not " + qName);
            }
            if (this.meta != null) {
                throw new SAXException("Invalid XMP document. Root already received earlier.");
            }
            this.meta = new Metadata();
            //this.currentProperties = this.meta;
            this.contextStack.push(this.meta);
            this.nestingInfoStack.push("metadata");
        } else if (XMPConstants.RDF_NAMESPACE.equals(uri)) {
            if ("RDF".equals(localName)) {
                if (this.meta == null) {
                    this.meta = new Metadata();
                    //this.currentProperties = this.meta;
                    this.contextStack.push(this.meta);
                    this.nestingInfoStack.push("metadata");
                }
            } else if ("Description".equals(localName)) {
                String about = attributes.getValue(XMPConstants.RDF_NAMESPACE, "about");
                if (this.contextStack.peek().equals(this.meta)) {
                    //rdf:RDF is the parent
                } else {
                    if (about != null) {
                        throw new SAXException(
                                "Nested rdf:Description elements may not have an about property");
                    }
                    //a structured property is the parent
                    XMPStructure struct = new XMPStructure();
                    //this.currentComplexValue = struct;
                    this.contextStack.push(struct);
                    //this.currentProperties = struct;
                    this.nestingInfoStack.push("struct");
                }
            } else if ("Seq".equals(localName)) {
                XMPArray array = new XMPArray(XMPArrayType.SEQ);
                //this.currentComplexValue = array;
                this.contextStack.push(array);
                this.nestingInfoStack.push("Seq");
            } else if ("Bag".equals(localName)) {
                XMPArray array = new XMPArray(XMPArrayType.BAG);
                //this.currentComplexValue = array;
                this.contextStack.push(array);
                this.nestingInfoStack.push("Bag");
            } else if ("Alt".equals(localName)) {
                XMPArray array = new XMPArray(XMPArrayType.ALT);
                //this.currentComplexValue = array;
                this.contextStack.push(array);
                this.nestingInfoStack.push("Alt");
            } else if ("li".equals(localName)) {
                //nop, handle in endElement()
            } else if ("value".equals(localName)) {
                QName name = new QName(uri, qName);
                this.contextStack.push(name);
                this.nestingInfoStack.push("prop:" + name);
            } else {
                throw new SAXException("Unexpected element in the RDF namespace: " + localName);
            }
        } else {
            QName name = new QName(uri, qName);
            this.contextStack.push(name);
            this.nestingInfoStack.push("prop:" + name);
        }
    }
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Attributes atts = (Attributes)attributesStack.pop();
        if (XMPConstants.XMP_NAMESPACE.equals(uri)) {
            //nop
        } else if (XMPConstants.RDF_NAMESPACE.equals(uri) && !"value".equals(localName)) {
            if ("li".equals(localName)) {
                XMPStructure struct = getCurrentStructure();
                if (struct != null) {
                    //Pop the structure
                    Object obj = this.contextStack.pop();
                    this.nestingInfoStack.pop();
                    getCurrentArray(true).add(struct);
                } else {
                    String s = content.toString().trim();
                    if (s.length() > 0) {
                        String lang = atts.getValue(XMPConstants.XML_NS, "lang");
                        if (lang != null) {
                            getCurrentArray(true).add(s, lang);
                        } else {
                            getCurrentArray(true).add(s);
                        }
                    }
                }
            } else if ("Description".equals(localName)) {
                /*
                if (isInStructure()) {
                    //Description is indicating a structure
                    //this.currentProperties = (PropertyAccess)propertiesStack.pop();
                    this.nestingInfoStack.pop();
                }*/
            } else {
                //nop, don't pop stack so the parent element has access
                //this.contextStack.pop();
                //this.nestingInfoStack.pop();
            }
        } else {
            XMPProperty prop;
            QName name;
            if (hasComplexContent()) {
                //Pop content of property
                Object obj = this.contextStack.pop();
                this.nestingInfoStack.pop();
                
                name = popCurrentPropName();
                
                if (obj instanceof XMPComplexValue) {
                    XMPComplexValue complexValue = (XMPComplexValue)obj;
                    prop = new XMPProperty(name, complexValue);
                } else {
                    throw new UnsupportedOperationException("NYI");
                }
            } else {
                name = popCurrentPropName();

                String s = content.toString().trim();
                prop = new XMPProperty(name, s);
                String lang = atts.getValue(XMPConstants.XML_NS, "lang");
                if (lang != null) {
                    prop.setXMLLang(lang);
                }
            }
            if (prop.getName() == null) {
                throw new IllegalStateException("No content in XMP property");
            }
            getCurrentProperties().setProperty(prop);
            //this.currentProperties.setProperty(this.currentProperty);
            //this.currentProperty = null;
            //this.currentPropertyName = null;
        }

        content.setLength(0); //Reset text buffer (see characters())
        super.endElement(uri, localName, qName);
    }

    /*
    private boolean isInStructure() {
        return !propertiesStack.isEmpty();
    }
    */

    /** @see org.xml.sax.ContentHandler#characters(char[], int, int) */
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

}
