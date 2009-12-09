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

import org.apache.xmlgraphics.util.QName;

/**
 * This interface is implemented by the top-level Metadata class and stuctured properties.
 */
public interface PropertyAccess {

    /**
     * Sets a property.
     * @param prop the property
     */
    void setProperty(XMPProperty prop);

    /**
     * Returns a property
     * @param uri the namespace URI of the property
     * @param localName the local name of the property
     * @return the requested property or null if it's not available
     */
    XMPProperty getProperty(String uri, String localName);

    /**
     * Returns a property.
     * @param name the name of the property
     * @return the requested property or null if it's not available
     */
    XMPProperty getProperty(QName name);

    /**
     * Removes a property and returns it if it was found.
     * @param name the name of the property
     * @return the removed property or null if it was not found
     */
    XMPProperty removeProperty(QName name);

    /**
     * Returns the rdf:value property. This is a shortcut for getProperty(XMPConstants.RDF_VALUE).
     * @return the rdf:value property or null if it's no available
     */
    XMPProperty getValueProperty();

    /**
     * Returns the number of properties.
     * @return the number of properties in this metadata object.
     */
    int getPropertyCount();

    /**
     * Returns an Iterator over all properties in this structured property.
     * @return an Iterator over all properties
     */
    Iterator iterator();

}