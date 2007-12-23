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
import java.util.Set;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.XMLizable;
import org.apache.xmlgraphics.xmp.merge.MergeRuleSet;
import org.apache.xmlgraphics.xmp.merge.PropertyMerger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class represents the root of an XMP metadata tree. It's more or less equivalent to the
 * x:xmpmeta element together with its nested rdf:RDF element.
 */
public class Metadata implements XMLizable {

    private Map properties = new java.util.HashMap();

    /**
     * Sets a property.
     * @param prop the property
     */
    public void setProperty(XMPProperty prop) {
        properties.put(prop.getName(), prop);
    }
    
    /**
     * Returns a property
     * @param uri the namespace URI of the property
     * @param localName the local name of the property
     * @return the requested property or null if it's not available
     */
    public XMPProperty getProperty(String uri, String localName) {
        return getProperty(new QName(uri, localName));
    }
    
    /**
     * Returns a property.
     * @param name the name of the property
     * @return the requested property or null if it's not available
     */
    public XMPProperty getProperty(QName name) {
        XMPProperty prop = (XMPProperty)properties.get(name);
        return prop;
    }
    
    /** @return the number of properties in this metadata object. */
    public int getPropertyCount() {
        return this.properties.size();
    }
    
    /**
     * Merges this metadata object into a given target metadata object. The merge rule set provided
     * by each schema is used for the merge.
     * @param target the target metadata to merge the local metadata into
     */
    public void mergeInto(Metadata target) {
        XMPSchemaRegistry registry = XMPSchemaRegistry.getInstance();
        Iterator iter = properties.values().iterator();
        while (iter.hasNext()) {
            XMPProperty prop = (XMPProperty)iter.next();
            XMPSchema schema = registry.getSchema(prop.getNamespace());
            MergeRuleSet rules = schema.getDefaultMergeRuleSet();
            PropertyMerger merger = rules.getPropertyMergerFor(prop);
            merger.merge(prop, target);
        }
    }
    
    /** @see org.apache.xmlgraphics.util.XMLizable#toSAX(org.xml.sax.ContentHandler) */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        handler.startElement(XMPConstants.XMP_NAMESPACE, "xmpmeta", "x:xmpmeta", atts);
        handler.startElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:RDF", atts);
        //Get all property namespaces
        Set namespaces = new java.util.HashSet();
        Iterator iter = properties.keySet().iterator();
        while (iter.hasNext()) {
            namespaces.add(((QName)iter.next()).getNamespaceURI());
        }
        //One Description element per namespace
        iter = namespaces.iterator();
        while (iter.hasNext()) {
            String ns = (String)iter.next();
            XMPSchema schema = XMPSchemaRegistry.getInstance().getSchema(ns);
            String prefix = (schema != null ? schema.getPreferredPrefix() : null);
            if (prefix != null) {
                handler.startPrefixMapping(prefix, ns);
            }
            
            atts.clear();
            atts.addAttribute(XMPConstants.RDF_NAMESPACE, "about", "rdf:about", "CDATA", "");
            handler.startElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:Description", atts);
            
            Iterator props = properties.values().iterator();
            while (props.hasNext()) {
                XMPProperty prop = (XMPProperty)props.next();
                if (prop.getName().getNamespaceURI().equals(ns)) {
                    prop.toSAX(handler);
                }
            }
            handler.endElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:Description");
            if (prefix != null) {
                handler.endPrefixMapping(prefix);
            }
        }
        
        handler.endElement(XMPConstants.RDF_NAMESPACE, "RDF", "rdf:RDF");
        handler.endElement(XMPConstants.XMP_NAMESPACE, "xmpmeta", "x:xmpmeta");
    }
}
