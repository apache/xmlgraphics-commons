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

    private static final String CONTRIBUTOR = "contributor";
    private static final String COVERAGE = "coverage";
    private static final String CREATOR = "creator";
    private static final String DATE = "date";
    private static final String DESCRIPTION = "description";
    private static final String FORMAT = "format";
    private static final String IDENTIFIER = "identifier";
    private static final String LANGUAGE = "language";
    private static final String PUBLISHER = "publisher";
    private static final String RELATION = "relation";
    private static final String RIGHTS = "rights";
    private static final String SOURCE = "source";
    private static final String SUBJECT = "subject";
    private static final String TITLE = "title";
    private static final String TYPE = "type";

    /**
     * Constructs a new adapter for Dublin Core around the given metadata object.
     * @param meta the underlying metadata
     */
    public DublinCoreAdapter(Metadata meta) {
        super(meta, XMPSchemaRegistry.getInstance().getSchema(DublinCoreSchema.NAMESPACE));
    }

    /**
     * Adds a new entry to the list of contributors (other than the authors).
     * @param value the new value
     */
    public void addContributor(String value) {
        addStringToBag(CONTRIBUTOR, value);
    }

    /**
     * Removes an entry from the list of contributors.
     * @param value the value to be removed
     * @return the removed entry
     */
    public boolean removeContributor(String value) {
        return removeStringFromArray(CONTRIBUTOR, value);
    }

    /**
     * Returns an array of all contributors.
     * @return a String array of all contributors (or null if not set)
     */
    public String[] getContributors() {
        return getStringArray(CONTRIBUTOR);
    }

    /**
     * Sets the extent or scope of the resource.
     * @param value the new value.
     */
    public void setCoverage(String value) {
        setValue(COVERAGE, value);
    }

    /**
     * Returns the extent or scope of the resource.
     * @return the property value (or null if not set)
     */
    public String getCoverage() {
        return getValue(COVERAGE);
    }

    /**
     * Adds a new entry to the list of creators (authors of the resource).
     * @param value the new value
     */
    public void addCreator(String value) {
        addStringToSeq(CREATOR, value);
    }

    /**
     * Removes an entry from the list of creators (authors of the resource).
     * @param value the value to be removed
     * @return the removed entry
     */
    public boolean removeCreator(String value) {
        return removeStringFromArray(CREATOR, value);
    }

    /**
     * Returns an array of all creators.
     * @return a String array of all creators (or null if not set)
     */
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
     * Returns a list of dates indicating point in time something interesting happened to the
     * resource.
     * @return the list of dates or null if no dates are set
     */
    public Date[] getDates() {
        return getDateArray(DATE);
    }

    /**
     * Returns a latest date indicating point in time something interesting happened to the
     * resource.
     * @return the last date or null
     */
    public Date getDate() {
        Date[] dates = getDates();
        if (dates != null) {
            Date latest = null;
            for (Date date : dates) {
                if (latest == null || date.getTime() > latest.getTime()) {
                    latest = date;
                }
            }
            return latest;
        } else {
            return null;
        }

    }

    /**
     * Sets the description of the content of the resource.
     * @param lang the language of the value ("x-default" or null for the default language)
     * @param value the new value
     */
    public void setDescription(String lang, String value) {
        setLangAlt(DESCRIPTION, lang, value);
    }

    /**
     * Returns the description of the content of the resource (in the default language).
     * @return the description of the content of the resource (or null if not set)
     */
    public String getDescription() {
        return getDescription(null);
    }

    /**
     * Returns the description of the content of the resource in a language-dependant way.
     * @param lang the language ("x-default" or null for the default language)
     * @return the language-dependent value (or null if not set)
     */
    public String getDescription(String lang) {
        return getLangAlt(lang, DESCRIPTION);
    }

    /**
     * Sets the file format used when saving the resource. Tools and
     * applications should set this property to the save format of the
     * data. It may include appropriate qualifiers.
     * @param value a MIME type
     */
    public void setFormat(String value) {
        setValue(FORMAT, value);
    }

    /**
     * Returns the file format used when saving this resource.
     * @return the MIME type of the file format (or null if not set)
     */
    public String getFormat() {
        return getValue(FORMAT);
    }

    /**
     * Sets the unique identifier of the resource.
     * @param value the new value
     */
    public void setIdentifier(String value) {
        setValue(IDENTIFIER, value);
    }

    /**
     * Returns the unique identifier of the resource.
     * @return the unique identifier (or null if not set)
     */
    public String getIdentifier() {
        return getValue(IDENTIFIER);
    }

    /**
     * Adds a new entry to the list of languages (RFC 3066).
     * @param value the new value
     */
    public void addLanguage(String value) {
        addStringToBag(LANGUAGE, value);
    }

    /**
     * Returns an array of languages.
     * @return a String array of all languages (or null if not set)
     */
    public String[] getLanguages() {
        return getStringArray(LANGUAGE);
    }

    /**
     * Adds a new entry to the list of publishers.
     * @param value the new value
     */
    public void addPublisher(String value) {
        addStringToBag(PUBLISHER, value);
    }

    /**
     * Returns an array of publishers.
     * @return a String array of all publishers (or null if not set)
     */
    public String[] getPublisher() {
        return getStringArray(PUBLISHER);
    }

    /**
     * Adds a new entry to the list of relationships to other documents.
     * @param value the new value
     */
    public void addRelation(String value) {
        addStringToBag(RELATION, value);
    }

    /**
     * Returns an array of all relationship to other documents.
     * @return a String array of all relationships (or null if none are set)
     */
    public String[] getRelations() {
        return getStringArray(RELATION);
    }

    /**
     * Sets the informal rights statement.
     * @param lang the language of the value ("x-default" or null for the default language)
     * @param value the new value
     */
    public void setRights(String lang, String value) {
        setLangAlt(RIGHTS, lang, value);
    }

    /**
     * Returns the informal rights statement.
     * @return the informal right statement (or null if not set)
     */
    public String getRights() {
        return getRights(null);
    }

    /**
     * Returns the informal rights statement in a language-dependant way.
     * @param lang the language ("x-default" or null for the default language)
     * @return the language-dependent value (or null if not set)
     */
    public String getRights(String lang) {
        return getLangAlt(lang, RIGHTS);
    }

    /**
     * Sets the unique identifier of the work from which this resource was derived.
     * @param value the new value
     */
    public void setSource(String value) {
        setValue(SOURCE, value);
    }

    /**
     * Returns unique identifier of the work from which this resource was derived.
     * @return the source (or null if not set)
     */
    public String getSource() {
        return getValue(SOURCE);
    }

    /**
     * Adds a new entry to the list of subjects (descriptive phrases or keywords that
     * specify the topic of the content of the resource).
     * @param value the new value
     */
    public void addSubject(String value) {
        addStringToBag(SUBJECT, value);
    }

    /**
     * Returns an array of all subjects.
     * @return a String array of all subjects
     */
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

    /**
     * Returns the title of the resource (in the default language).
     * @return the title of the resource (in the default language)
     */
    public String getTitle() {
        return getTitle(null);
    }

    /**
     * Returns the title of the resource in a language-dependant way.
     * @param lang the language ("x-default" or null for the default language)
     * @return the language-dependent value (or null if not set)
     */
    public String getTitle(String lang) {
        return getLangAlt(lang, TITLE);
    }

    /**
     * Removes a title of the resource.
     * @param lang the language variant to be removed
     * @return the previously set value or null if this language variant wasn't set
     */
    public String removeTitle(String lang) {
        return removeLangAlt(lang, TITLE);
    }

    /**
     * Adds a new entry to the list of document types (for example: novel, poem or working paper).
     * @param value the new value
     */
    public void addType(String value) {
        addStringToBag(TYPE, value);
    }

    /**
     * Returns an array of all document types.
     * @return a String array of all document types (or null if not set)
     */
    public String[] getTypes() {
        return getStringArray(TYPE);
    }


}
