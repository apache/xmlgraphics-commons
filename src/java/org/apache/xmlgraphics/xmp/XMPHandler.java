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

package org.apache.xmlgraphics.xmp;

import java.util.Stack;

import org.apache.xmlgraphics.util.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Passive XMP parser implemented as a SAX DefaultHandler. After the XML document has been parsed
 * the Metadata object can be retrieved.
 */
public class XMPHandler extends DefaultHandler {

    private Metadata meta;
    
    private StringBuffer content = new StringBuffer();
    //private Attributes lastAttributes;
    private Stack attributesStack = new Stack();
    //private Stack contextStack = new Stack();
    
    private QName currentPropertyName;
    private XMPProperty currentProperty;
    private XMPComplexValue currentComplexValue;
    
    /** @return the parsed metadata, available after the parsing. */
    public Metadata getMetadata() {
        return this.meta;
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
        } else if (XMPConstants.RDF_NAMESPACE.equals(uri)) {
            if ("RDF".equals(localName)) {
                if (this.meta == null) {
                    this.meta = new Metadata();
                }
            } else if ("Description".equals(localName)) {
                if (currentPropertyName == null) {
                    //rdf:RDF is the parent
                    String about = attributes.getValue(XMPConstants.RDF_NAMESPACE, "about");
                } else {
                    //a structured property is the parent
                }
            } else if ("Seq".equals(localName)) {
                this.currentComplexValue = new XMPArray(XMPArrayType.SEQ);
            } else if ("Bag".equals(localName)) {
                this.currentComplexValue = new XMPArray(XMPArrayType.BAG);
            } else if ("Alt".equals(localName)) {
                this.currentComplexValue = new XMPArray(XMPArrayType.ALT);
            } else if ("li".equals(localName)) {
            }
        } else {
            this.currentPropertyName = new QName(uri, qName);
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
        } else if (XMPConstants.RDF_NAMESPACE.equals(uri)) {
            if ("li".equals(localName)) {
                String s = content.toString().trim();
                if (s.length() > 0) {
                    getCurrentArray().add(s);
                }
            } else {
                //nop
            }
        } else {
            if (this.currentComplexValue != null) {
                this.currentProperty = new XMPProperty(this.currentPropertyName, 
                                this.currentComplexValue);
                this.currentComplexValue = null;
            } else {
                String s = content.toString().trim();
                this.currentProperty = new XMPProperty(this.currentPropertyName, s);
                String lang = atts.getValue(XMPConstants.XML_NS, "lang");
                if (lang != null) {
                    this.currentProperty.setXMLLang(lang);
                }
            }
            this.meta.setProperty(this.currentProperty);
            this.currentProperty = null;
            this.currentPropertyName = null;
        }
        content.setLength(0); //Reset text buffer (see characters())
        super.endElement(uri, localName, qName);
    }

    private XMPArray getCurrentArray() {
        return (XMPArray)this.currentComplexValue;
    }

    /** @see org.xml.sax.ContentHandler#characters(char[], int, int) */
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

}
