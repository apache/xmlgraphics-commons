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
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

/**
 * Schema adapter implementation for the XMP Basic schema.
 */
public class XMPBasicAdapter extends XMPSchemaAdapter {

    private static final String CREATOR_TOOL = "CreatorTool";
    private static final String CREATE_DATE = "CreateDate";
    private static final String MODIFY_DATE = "ModifyDate";
    private static final String METADATA_DATE = "MetadataDate";

    /**
     * Constructs a new adapter for XMP Basic around the given metadata object.
     * @param meta the underlying metadata
     */
    public XMPBasicAdapter(Metadata meta, String namespace) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
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
