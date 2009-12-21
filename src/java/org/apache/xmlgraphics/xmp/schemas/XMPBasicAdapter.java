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

package org.apache.xmlgraphics.xmp.schemas;

import java.util.Date;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPArrayType;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPProperty;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;
import org.apache.xmlgraphics.xmp.XMPStructure;

/**
 * Schema adapter implementation for the XMP Basic schema.
 */
public class XMPBasicAdapter extends XMPSchemaAdapter {

    private static final String ADVISORY = "Advisory";
    private static final String BASE_URL = "BaseURL";
    private static final String CREATE_DATE = "CreateDate";
    private static final String CREATOR_TOOL = "CreatorTool";
    private static final String IDENTIFIER = "Identifier";
    private static final String LABEL = "Label";
    private static final String METADATA_DATE = "MetadataDate";
    private static final String MODIFY_DATE = "ModifyDate";
    private static final String NICKNAME = "Nickname";
    private static final String RATING = "Rating";
    private static final String THUMBNAILS = "Thumbnails";

    /**
     * Constructs a new adapter for XMP Basic around the given metadata object.
     * @param meta the underlying metadata
     */
    public XMPBasicAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
    }

    /**
     * Sets the base URL for relative URLs in the document content.
     * @param value the base URL
     */
    public void setBaseUrl(String value) {
        setValue(BASE_URL, value);
    }

    /**
     * Returns the base URL for relative URLs in the document content.
     * @return the base URL
     */
    public String getBaseUrl() {
        return getValue(BASE_URL);
    }

    /**
     * Sets the date and time the resource was originally created.
     * @param creationDate the creation date
     */
    public void setCreateDate(Date creationDate) {
        setDateValue(CREATE_DATE, creationDate);
    }

    /** @return the date and time the resource was originally created */
    public Date getCreateDate() {
        return getDateValue(CREATE_DATE);
    }

    /**
     * Sets the first known tool used to create the resource.
     * @param value the creator tool
     */
    public void setCreatorTool(String value) {
        setValue(CREATOR_TOOL, value);
    }

    /** @return the first known tool used to create the resource */
    public String getCreatorTool() {
        return getValue(CREATOR_TOOL);
    }

    /**
     * Adds an identifier that unambiguously identify the resource within a given context.
     * @param value the identifier value
     */
    public void addIdentifier(String value) {
        addStringToBag(IDENTIFIER, value);
    }

    /**
     * Adds a qualified identifier that unambiguously identify the resource within a given context.
     * As qualifier, <code>xmpidq:Scheme</code> is used.
     * @param value the identifier value
     */
    public void addIdentifier(String value, String qualifier) {
        XMPStructure struct = new XMPStructure();
        struct.setProperty(new XMPProperty(XMPConstants.RDF_VALUE, value));
        struct.setProperty(new XMPProperty(XMPBasicSchema.SCHEME_QUALIFIER, qualifier));
        addObjectToArray(IDENTIFIER, struct, XMPArrayType.BAG);
    }

    /**
     * Returns an array of all identifiers that unambiguously identify the resource within a
     * given context.
     * @return a String array of all identifiers (or null if not set)
     */
    public String[] getIdentifiers() {
        return getStringArray(IDENTIFIER);
    }

    /**
     * Returns an identifier that matches a given qualifier.
     * As qualifier, <code>xmpidq:Scheme</code> is used.
     * @param qualifier the qualifier
     * @return the identifier (or null if no matching value was found)
     */
    public String getIdentifier(String qualifier) {
        Object value = findQualifiedValue(IDENTIFIER, XMPBasicSchema.SCHEME_QUALIFIER, qualifier);
        return (value != null ? value.toString() : null);
    }

    /**
     * Sets the date and time the resource was last modified.
     * @param modifyDate the modification date
     */
    public void setModifyDate(Date modifyDate) {
        setDateValue(MODIFY_DATE, modifyDate);
    }

    /** @return the date and time the resource was last modified */
    public Date getModifyDate() {
        return getDateValue(MODIFY_DATE);
    }

    /**
     * Sets the date and time any metadata for this resource was last changed.
     * @param metadataDate the modification date for the metadata
     */
    public void setMetadataDate(Date metadataDate) {
        setDateValue(METADATA_DATE, metadataDate);
    }

    /** @return the date and time the resource was originally created */
    public Date getMetadataDate() {
        return getDateValue(METADATA_DATE);
    }

}
