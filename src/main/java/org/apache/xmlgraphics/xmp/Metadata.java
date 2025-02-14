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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.XMLizable;
import org.apache.xmlgraphics.xmp.merge.MergeRuleSet;
import org.apache.xmlgraphics.xmp.merge.PropertyMerger;

/**
 * This class represents the root of an XMP metadata tree. It's more or less equivalent to the
 * x:xmpmeta element together with its nested rdf:RDF element.
 */
public class Metadata implements XMLizable, PropertyAccess {

    private Map<QName, XMPProperty> properties = new java.util.HashMap<>();

    /** {@inheritDoc} */
    public void setProperty(XMPProperty prop) {
        properties.put(prop.getName(), prop);
    }

    /** {@inheritDoc} */
    public XMPProperty getProperty(String uri, String localName) {
        return getProperty(new QName(uri, localName));
    }

    /** {@inheritDoc} */
    public XMPProperty getProperty(QName name) {
        return properties.get(name);
    }

    /** {@inheritDoc} */
    public XMPProperty removeProperty(QName name) {
        return properties.remove(name);
    }

    /** {@inheritDoc} */
    public XMPProperty getValueProperty() {
        return getProperty(XMPConstants.RDF_VALUE);
    }

    /** {@inheritDoc} */
    public int getPropertyCount() {
        return this.properties.size();
    }

    /** {@inheritDoc} */
    public Iterator iterator() {
        return this.properties.keySet().iterator();
    }

    /**
     * Merges this metadata object into a given target metadata object. The merge rule set provided
     * by each schema is used for the merge.
     * @param target the target metadata to merge the local metadata into
     */
    public void mergeInto(Metadata target, List<Class> exclude) {
        XMPSchemaRegistry registry = XMPSchemaRegistry.getInstance();
        for (Object o : properties.values()) {
            XMPProperty prop = (XMPProperty) o;
            XMPSchema schema = registry.getSchema(prop.getNamespace());
            if (!exclude.contains(schema.getClass())) {
                MergeRuleSet rules = schema.getDefaultMergeRuleSet();
                PropertyMerger merger = rules.getPropertyMergerFor(prop);
                merger.merge(prop, target);
            }
        }
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        handler.startPrefixMapping("x", XMPConstants.XMP_NAMESPACE);
        handler.startElement(XMPConstants.XMP_NAMESPACE, "xmpmeta", "x:xmpmeta", atts);
        handler.startPrefixMapping("rdf", XMPConstants.RDF_NAMESPACE);
        handler.startElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:RDF", atts);

        writeCustomDescription(handler);

        //Get all property namespaces
        Set namespaces = new java.util.HashSet();
        Iterator iter = properties.keySet().iterator();
        while (iter.hasNext()) {
            QName n = ((QName)iter.next());
            namespaces.add(n.getNamespaceURI());
        }
        //One Description element per namespace
        iter = namespaces.iterator();
        while (iter.hasNext()) {
            String ns = (String)iter.next();
            XMPSchema schema = XMPSchemaRegistry.getInstance().getSchema(ns);
            String prefix = (schema != null ? schema.getPreferredPrefix() : null);

            boolean first = true;
            boolean empty = true;

            for (Object o : properties.values()) {
                XMPProperty prop = (XMPProperty) o;
                if (prop.getName().getNamespaceURI().equals(ns) && !prop.attribute) {
                    if (first) {
                        if (prefix == null) {
                            prefix = prop.getName().getPrefix();
                        }
                        atts.clear();
                        atts.addAttribute(XMPConstants.RDF_NAMESPACE,
                                "about", "rdf:about", "CDATA", "");
                        if (prefix != null) {
                            handler.startPrefixMapping(prefix, ns);
                        }

                        if (schema != null) {
                            for (Map.Entry<String, String> entry : schema.getExtraNamespaces().entrySet()) {
                                handler.startPrefixMapping(entry.getKey(), entry.getValue());
                            }
                        }

                        handler.startElement(XMPConstants.RDF_NAMESPACE,
                                "Description", "rdf:Description", atts);
                        empty = false;
                        first = false;
                    }
                    prop.toSAX(handler);
                }
            }
            if (!empty) {
                handler.endElement(XMPConstants.RDF_NAMESPACE, "Description", "rdf:Description");
                if (prefix != null) {
                    handler.endPrefixMapping(prefix);
                }

                if (schema != null) {
                    for (String extraPrefix : schema.getExtraNamespaces().keySet()) {
                        handler.endPrefixMapping(extraPrefix);
                    }
                }
            }
        }

        handler.endElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:RDF");
        handler.endPrefixMapping("rdf");
        handler.endElement(XMPConstants.XMP_NAMESPACE, "xmpmeta", "x:xmpmeta");
        handler.endPrefixMapping("x");
    }

    private void writeCustomDescription(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        boolean empty = true;
        for (XMPProperty prop : properties.values()) {
            if (prop.attribute) {
                atts.addAttribute(prop.getNamespace(), prop.getName().getLocalName(), prop.getName().getQName(),
                        "CDATA", (String) prop.getValue());
                if (prop.getName().getPrefix() != null) {
                    handler.startPrefixMapping(prop.getName().getPrefix(), prop.getNamespace());
                    handler.endPrefixMapping(prop.getName().getPrefix());
                }
                empty = false;
            }
        }
        if (!empty) {
            atts.addAttribute(XMPConstants.RDF_NAMESPACE,
                    "about", "rdf:about", "CDATA", "");
            handler.startElement(XMPConstants.RDF_NAMESPACE,
                    "RDF", "rdf:Description", atts);
            handler.endElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:Description");
        }
    }

}
