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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.xmlgraphics.util.QName;

/**
 * Base class for schema-specific adapters that provide user-friendly access to XMP values.
 */
public class XMPSchemaAdapter {

    private static DateFormat pseudoISO8601DateFormat = new SimpleDateFormat(
                                                            "yyyy'-'MM'-'dd'T'HH':'mm':'ss");

    /** the Metadata object this schema instance operates on */
    protected Metadata meta;
    private XMPSchema schema;
    
    /**
     * Main constructor.
     * @param meta the Metadata object to wrao
     * @param schema the XMP schema for which this adapter was written
     */
    public XMPSchemaAdapter(Metadata meta, XMPSchema schema) {
        if (meta == null) {
            throw new NullPointerException("Parameter meta must not be null");
        }
        if (schema == null) {
            throw new NullPointerException("Parameter schema must not be null");
        }
        this.meta = meta;
        this.schema = schema;
    }
    
    /** @return the XMP schema associated with this adapter */
    public XMPSchema getSchema() {
        return this.schema;
    }
    
    /**
     * Returns the QName for a given property
     * @param propName the property name
     * @return the resulting QName
     */
    protected QName getQName(String propName) {
        return new QName(getSchema().getNamespace(), propName);
    }

    /**
     * Adds a String value to an array.
     * @param propName the property name
     * @param value the String value
     * @param arrayType the type of array to operate on
     */
    private void addStringToArray(String propName, String value, XMPArrayType arrayType) {
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        XMPArray array;
        if (prop == null) {
            array = new XMPArray(arrayType);
            array.add(value);
            prop = new XMPProperty(name, array);
            meta.setProperty(prop);
        } else {
            prop.convertSimpleValueToArray(arrayType);
            prop.getArrayValue().add(value);
        }
    }

    /**
     * Adds a String value to an ordered array.
     * @param propName the property name
     * @param value the String value
     */
    protected void addStringToSeq(String propName, String value) {
        addStringToArray(propName, value, XMPArrayType.SEQ);
    }

    /**
     * Adds a String value to an unordered array.
     * @param propName the property name
     * @param value the String value
     */
    protected void addStringToBag(String propName, String value) {
        addStringToArray(propName, value, XMPArrayType.BAG);
    }

    /**
     * Formats a Date using ISO 8601 format in the default time zone.
     * @param dt the date
     * @return the formatted date
     */
    public static String formatISO8601Date(Date dt) {
        //ISO 8601 cannot be expressed directly using SimpleDateFormat
        StringBuffer sb = new StringBuffer(pseudoISO8601DateFormat.format(dt));
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int offset = cal.get(Calendar.ZONE_OFFSET);
        offset += cal.get(Calendar.DST_OFFSET);
        offset /= (1000 * 60); //Convert to minutes
        
        if (offset == 0) {
            sb.append('Z');
        } else {
            int zoneOffsetHours = offset / 60;
            int zoneOffsetMinutes = Math.abs(offset % 60);
            if (zoneOffsetHours > 0) {
                sb.append('+');
            } else {
                sb.append('-');
            }
            if (zoneOffsetHours < 10) {
                sb.append('0');
            }
            sb.append(zoneOffsetHours);
            sb.append(':');
            if (zoneOffsetMinutes < 10) {
                sb.append('0');
            }
            sb.append(zoneOffsetMinutes);
        }
        
        return sb.toString();
    }
    
    /**
     * Adds a date value to an ordered array.
     * @param propName the property name
     * @param value the date value
     */
    protected void addDateToSeq(String propName, Date value) {
        String dt = formatISO8601Date(value);
        addStringToSeq(propName, dt);
    }
    
    /**
     * Set a date value.
     * @param propName the property name
     * @param value the date value
     */
    protected void setDateValue(String propName, Date value) {
        String dt = formatISO8601Date(value);
        setValue(propName, dt);
    }

    /**
     * Sets a language-dependent value.
     * @param propName the property name
     * @param lang the language ("x-default" or null for the default language)
     * @param value the value
     */
    protected void setLangAlt(String propName, String lang, String value) {
        if (lang == null) {
            lang = XMPConstants.DEFAULT_LANGUAGE;
        }
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        XMPArray array;
        if (prop == null) {
            array = new XMPArray(XMPArrayType.ALT);
            array.add(value, lang);
            prop = new XMPProperty(name, array);
            meta.setProperty(prop);
        } else {
            prop.convertSimpleValueToArray(XMPArrayType.ALT);
            removeLangAlt(lang, propName);
            prop.getArrayValue().add(value, lang);
        }
    }

    /**
     * Sets a simple value.
     * @param propName the property name
     * @param value the value
     */
    protected void setValue(String propName, String value) {
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        if (prop == null) {
            prop = new XMPProperty(name, value);
            meta.setProperty(prop);
        } else {
            prop.setValue(value);
        }
    }

    /**
     * Returns a simple value.
     * @param propName the property name
     * @return the requested value or null if it isn't set
     */
    protected String getValue(String propName) {
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        if (prop == null) {
            return null;
        } else {
            return prop.getValue().toString();
        }
    }

    /**
     * Removes a language-dependent value from an alternative array.
     * @param lang the language ("x-default" for the default language)
     * @param propName the property name
     */
    protected void removeLangAlt(String lang, String propName) {
        XMPProperty prop = meta.getProperty(getQName(propName));
        XMPArray array;
        if (prop != null && lang != null) {
            array = prop.getArrayValue();
            if (array != null) {
                array.removeLangValue(lang);
            } else {
                if (lang.equals(prop.getXMLLang())) {
                    prop.setValue(null);
                    prop.setXMLLang(null);
                }
            }
        }
    }
    
    /**
     * Returns a language-dependent value. If the value in the requested language is not available
     * the value for the default language is returned.
     * @param lang the language ("x-default" for the default language)
     * @param propName the property name
     * @return the requested value
     */
    protected String getLangAlt(String lang, String propName) {
        XMPProperty prop = meta.getProperty(getQName(propName));
        XMPArray array;
        if (prop == null) {
            return null;
        } else {
            array = prop.getArrayValue();
            if (array != null) {
                return array.getLangValue(lang);
            } else {
                return prop.getValue().toString();
            }
        }
    }

    /**
     * Returns an object array representation of the property's values.
     * @param propName the property name
     * @return the object array
     */
    protected Object[] getObjectArray(String propName) {
        XMPProperty prop = meta.getProperty(getQName(propName));
        XMPArray array = prop.getArrayValue();
        if (array != null) {
            return array.toObjectArray();
        } else {
            return new Object[] {prop.getValue()};
        }
    }

    /**
     * Returns a String array representation of the property's values. Complex values to converted
     * to Strings using the toString() method.
     * @param propName the property name
     * @return the String array
     */
    protected String[] getStringArray(String propName) {
        Object[] arr = getObjectArray(propName);
        String[] res = new String[arr.length];
        for (int i = 0, c = res.length; i < c; i++) {
            res[i] = arr[i].toString();
        }
        return res;
    }

    
}
