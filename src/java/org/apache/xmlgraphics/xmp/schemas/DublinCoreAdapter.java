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
 * Schema adapter implementation for the Dublin Core schema.
 * <p>
 * Note: In Adobe's XMP specification dc:subject is defined as "bag Text", but in PDF/A-1 it is
 * defined as "Text". Here it is implemented as "bag Text". 
 */
public class DublinCoreAdapter extends XMPSchemaAdapter {

    private static final String CREATOR = "creator";
    private static final String DATE = "date";
    private static final String SUBJECT = "subject";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LANGUAGE = "language";
    
    /**
     * Constructs a new adapter for Dublin Core around the given metadata object.
     * @param meta the underlying metadata
     */
    public DublinCoreAdapter(Metadata meta) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(DublinCoreSchema.NAMESPACE));
    }
    
    /**
     * Adds a new entry to the list of creators (authors of the resource).
     * @param value the new value
     */
    public void addCreator(String value) {
        addStringToSeq(CREATOR, value);
    }
    
    /** @return a String array of all creators */
    public String[] getCreators() {
        return getStringArray(CREATOR);
    }
    
    /**
     * Adds a new entry to the list of dates indicating points in time something interesting
     * happened to the resource.
     * @param value the date value 
     */
    public void addDate(Date value) {
        addDateToSeq(DATE, value);
    }
    
    /**
     * Adds a new entry to the list of subjects (descriptive phrases or keywords that
     * specify the topic of the content of the resource).
     * @param value the new value
     */
    public void addSubject(String value) {
        addStringToBag(SUBJECT, value);
    }
    
    /** @return a String array of all subjects */
    public String[] getSubjects() {
        return getStringArray(SUBJECT);
    }
    
    /**
     * Sets the title of the resource (in the default language).
     * @param value the new value
     */
    public void setTitle(String value) {
        setTitle(null, value);
    }
    
    /**
     * Sets the title of the resource.
     * @param lang the language of the value ("x-default" or null for the default language)
     * @param value the new value
     */
    public void setTitle(String lang, String value) {
        setLangAlt(TITLE, lang, value);
    }
    
    /** @return the title of the resource (in the default language) */
    public String getTitle() {
        return getTitle(null);
    }
    
    /**
     * Returns the title of the resource in a language-dependant way.
     * @param lang the language ("x-default" or null for the default language)
     * @return the language-dependent value.
     */
    public String getTitle(String lang) {
        return getLangAlt(lang, TITLE);
    }
    
    /**
     * Sets the description of the content of the resource.
     * @param lang the language of the value ("x-default" or null for the default language)
     * @param value the new value
     */
    public void setDescription(String lang, String value) {
        setLangAlt(DESCRIPTION, lang, value);
    }
    
    /** @return the description of the content of the resource (in the default language) */
    public String getDescription() {
        return getDescription(null);
    }
    
    /**
     * Returns the description of the content of the resource in a language-dependant way.
     * @param lang the language ("x-default" or null for the default language)
     * @return the language-dependent value.
     */
    public String getDescription(String lang) {
        return getLangAlt(lang, DESCRIPTION);
    }
    
    /**
     * Adds a new entry to the list of languages (RFC 3066).
     * @param value the new value
     */
    public void addLanguage(String value) {
        addStringToBag(LANGUAGE, value);
    }
    
    /** @return a String array of all language */
    public String[] getLanguages() {
        return getStringArray(LANGUAGE);
    }
    
}
