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

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Represents an XMP array as defined by the XMP specification.
 */
public class XMPArray extends XMPComplexValue {
    //TODO Property qualifiers are currently not supported, yet.

    private XMPArrayType type;
    private List values = new java.util.ArrayList();
    private List xmllang = new java.util.ArrayList();
    private List<String> parseTypes = new ArrayList<>();

    /**
     * Main constructor
     * @param type the intended type of array
     */
    public XMPArray(XMPArrayType type) {
        this.type = type;
    }

    /** @return the type of array */
    public XMPArrayType getType() {
        return this.type;
    }

    /**
     * Returns the value at a given position.
     * @param idx the index of the requested value
     * @return the value at the given position
     */
    public Object getValue(int idx) {
        return this.values.get(idx);
    }

    /**
     * Returns the structure at a given position. If the value is not a structure a
     * ClassCastException is thrown.
     * @param idx the index of the requested value
     * @return the structure at the given position
     */
    public XMPStructure getStructure(int idx) {
        return (XMPStructure)this.values.get(idx);
    }

    /** {@inheritDoc} */
    public Object getSimpleValue() {
        if (values.size() == 1) {
            return getValue(0);
        } else if (values.size() > 1) {
            return getLangValue(XMPConstants.DEFAULT_LANGUAGE);
        } else {
            return null;
        }
    }

    private String getParentLanguage(String lang) {
        if (lang == null) {
            return null;
        }
        int pos = lang.indexOf('-');
        if (pos > 0) {
            String parent = lang.substring(0, pos);
            return parent;
        }
        return null;
    }

    /**
     * Returns a language-dependent values (available for alternative arrays).
     * @param lang the language ("x-default" for the default value)
     * @return the requested value
     */
    public String getLangValue(String lang) {
        String v = null;
        String valueForParentLanguage = null;
        for (int i = 0, c = values.size(); i < c; i++) {
            String l = (String)xmllang.get(i);
            if ((l == null && lang == null) || (l != null && l.equals(lang))) {
                v = values.get(i).toString();
                break;
            }
            if (l != null && lang != null) {
                //Check for "parent" language, too ("en" matches "en-GB")
                String parent = getParentLanguage(l);
                if (parent != null && parent.equals(lang)) {
                    valueForParentLanguage = values.get(i).toString();
                }
            }
        }
        if (lang != null && v == null && valueForParentLanguage != null) {
            //Use value found for parent language
            v = valueForParentLanguage;
        }
        if (lang == null && v == null) {
            v = getLangValue(XMPConstants.DEFAULT_LANGUAGE);
            if (v == null && values.size() > 0) {
                v = getValue(0).toString(); //get first
            }
        }
        return v;
    }

    /**
     * Removes a language-dependent value.
     * @param lang the language ("x-default" for the default value)
     * @return the removed value (or null if no value was set)
     */
    public String removeLangValue(String lang) {
        if (lang == null || "".equals(lang)) {
            lang = XMPConstants.DEFAULT_LANGUAGE;
        }
        for (int i = 0, c = values.size(); i < c; i++) {
            String l = (String)xmllang.get(i);
            if ((XMPConstants.DEFAULT_LANGUAGE.equals(lang) && l == null) || lang.equals(l)) {
                String value = (String)values.remove(i);
                xmllang.remove(i);
                return value;
            }
        }
        return null;
    }

    /**
     * Adds a new value to the array
     * @param value the value
     */
    public void add(Object value, String lang, String parseType) {
        values.add(value);
        xmllang.add(lang);
        parseTypes.add(parseType);
    }

    /**
     * Adds a language-dependent value to the array. Make sure not to add the same language twice.
     * @param value the value
     * @param lang the language ("x-default" for the default value)
     */
    public void add(Object value, String lang) {
        add(value, lang, null);
    }

    public void add(Object value) {
        add(value, null, null);
    }

    /**
     * Removes a value from the array. If the value doesn't exist, nothing happens.
     * @param value the value to be removed
     * @return true if the value was removed, false if it wasn't found
     */
    public boolean remove(String value) {
        int idx = values.indexOf(value);
        if (idx >= 0) {
            values.remove(idx);
            xmllang.remove(idx);
            parseTypes.remove(idx);
            return true;
        }
        return false;

    }

    /**
     * Returns the current number of values in the array.
     * @return the current number of values in the array
     */
    public int getSize() {
        return this.values.size();
    }

    /**
     * Indicates whether the array is empty or not.
     * @return true if the array is empty
     */
    public boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Converts the array to an object array.
     * @return an object array of all values in the array
     */
    public Object[] toObjectArray() {
        Object[] res = new Object[getSize()];
        for (int i = 0, c = res.length; i < c; i++) {
            res[i] = getValue(i);
        }
        return res;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        handler.startElement(XMPConstants.RDF_NAMESPACE,
                type.getName(), "rdf:" + type.getName(), atts);
        for (int i = 0, c = values.size(); i < c; i++) {
            String lang = (String)xmllang.get(i);
            atts.clear();
            Object v = values.get(i);
            if (lang != null) {
                atts.addAttribute(XMPConstants.XML_NS, "lang", "xml:lang", "CDATA", lang);
            }
            if (v instanceof URI) {
                atts.addAttribute(XMPConstants.RDF_NAMESPACE, "resource",
                        "rdf:resource", "CDATA", ((URI)v).toString());
            }
            String parseType = parseTypes.get(i);
            if (parseType != null) {
                atts.addAttribute(XMPConstants.RDF_NAMESPACE, "parseType", "rdf:parseType", "CDATA", parseType);
            }
            handler.startElement(XMPConstants.RDF_NAMESPACE,
                    "li", "rdf:li", atts);
            if (v instanceof XMPComplexValue) {
                ((XMPComplexValue)v).toSAX(handler);
            } else if (!(v instanceof URI)) {
                String value = (String)values.get(i);
                char[] chars = value.toCharArray();
                handler.characters(chars, 0, chars.length);
            }
            handler.endElement(XMPConstants.RDF_NAMESPACE,
                    "li", "rdf:li");
        }
        handler.endElement(XMPConstants.RDF_NAMESPACE,
                type.getName(), "rdf:" + type.getName());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "XMP array: " + type + ", " + getSize();
    }

}
