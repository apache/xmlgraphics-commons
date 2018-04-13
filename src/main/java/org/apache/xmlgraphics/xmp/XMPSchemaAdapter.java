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

import java.util.Date;
import java.util.TimeZone;

import org.apache.xmlgraphics.util.DateFormatUtil;
import org.apache.xmlgraphics.util.QName;

/**
 * Base class for schema-specific adapters that provide user-friendly access to XMP values.
 */
public class XMPSchemaAdapter {

    /** the Metadata object this schema instance operates on */
    protected Metadata meta;
    private XMPSchema schema;
    private boolean compact = true;

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
        return new QName(getSchema().getNamespace(), getSchema().getPreferredPrefix(), propName);
    }

    /**
     * Adds a String value to an array.
     * @param propName the property name
     * @param value the String value
     * @param arrayType the type of array to operate on
     */
    private void addStringToArray(String propName, String value, XMPArrayType arrayType) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("'" + propName + "' value must not be empty");
        }
        addObjectToArray(propName, value, arrayType);
    }

    /**
     * Adds a Object value to an array.
     * @param propName the property name
     * @param value the Object value
     * @param arrayType the type of array to operate on
     */
    protected void addObjectToArray(String propName, Object value, XMPArrayType arrayType) {
        if (value == null) {
            throw new IllegalArgumentException("'" + propName + "' value must not be null");
        }
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        if (prop == null) {
            prop = new XMPProperty(name, value);
            meta.setProperty(prop);
            if (!compact) {
                prop.convertSimpleValueToArray(arrayType);
            }
        } else {
            prop.convertSimpleValueToArray(arrayType);
            prop.getArrayValue().add(value);
        }
    }

    /**
     * Removes a value from an array.
     * @param propName the name of the property
     * @param value the value to be removed
     * @return true if the value was removed, false if it was not found
     */
    protected boolean removeStringFromArray(String propName, String value) {
        if (value == null) {
            return false;
        }
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        if (prop != null) {
            if (prop.isArray()) {
                XMPArray arr = prop.getArrayValue();
                boolean removed = arr.remove(value);
                if (arr.isEmpty()) {
                    meta.removeProperty(name);
                }
                return removed;
            } else {
                Object currentValue = prop.getValue();
                if (value.equals(currentValue)) {
                    meta.removeProperty(name);
                    return true;
                }
            }
        }
        return false;
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
        return formatISO8601Date(dt, TimeZone.getDefault());
    }

    /**
     * Formats a Date using ISO 8601 format in the given time zone.
     * @param dt the date
     * @param tz the time zone
     * @return the formatted date
     */
    public static String formatISO8601Date(Date dt, TimeZone tz) {
        return DateFormatUtil.formatISO8601(dt, tz);
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
     * Returns a date value.
     * @param propName the property name
     * @return the date value or null if the value is not set
     */
    protected Date getDateValue(String propName) {
        String dt = getValue(propName);
        if (dt == null) {
            return null;
        } else {
            return DateFormatUtil.parseISO8601Date(dt);
        }
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
            if (value != null && value.length() > 0) {
                prop = new XMPProperty(name, value);
                prop.setXMLLang(lang);
                meta.setProperty(prop);
            }
        } else {
            prop.convertSimpleValueToArray(XMPArrayType.ALT);
            array = prop.getArrayValue();
            array.removeLangValue(lang);
            if (value != null && value.length() > 0) {
                array.add(value, lang);
            } else {
                if (array.isEmpty()) {
                    meta.removeProperty(name);
                }
            }
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
        if (value != null && value.length() > 0) {
            if (prop != null) {
                prop.setValue(value);
            } else {
                prop = new XMPProperty(name, value);
                meta.setProperty(prop);
            }
        } else {
            if (prop != null) {
                meta.removeProperty(name);
            }
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
     * @return the removed value
     */
    protected String removeLangAlt(String lang, String propName) {
        QName name = getQName(propName);
        XMPProperty prop = meta.getProperty(name);
        XMPArray array;
        if (prop != null && lang != null) {
            array = prop.getArrayValue();
            if (array != null) {
                String removed = array.removeLangValue(lang);
                if (array.isEmpty()) {
                    meta.removeProperty(name);
                }
                return removed;
            } else {
                String removed = prop.getValue().toString();
                if (lang.equals(prop.getXMLLang())) {
                    meta.removeProperty(name);
                }
                return removed;
            }
        }
        return null;
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
     * Finds a structure that matches a given qualifier.
     * @param propName the property name
     * @param qualifier the qualifier
     * @param qualifierValue the qualifier value
     * @return the structure if a match was found (or null if no match was found)
     */
    protected PropertyAccess findQualifiedStructure(String propName,
            QName qualifier, String qualifierValue) {
        XMPProperty prop = meta.getProperty(getQName(propName));
        XMPArray array;
        if (prop != null) {
            array = prop.getArrayValue();
            if (array != null) {
                for (int i = 0, c = array.getSize(); i < c; i++) {
                    Object value = array.getValue(i);
                    if (value instanceof PropertyAccess) {
                        PropertyAccess pa = (PropertyAccess)value;
                        XMPProperty q = pa.getProperty(qualifier);
                        if (q != null && q.getValue().equals(qualifierValue)) {
                            return pa;
                        }
                    }
                }
            } else if (prop.getStructureValue() != null) {
                PropertyAccess pa = prop.getStructureValue();
                XMPProperty q = pa.getProperty(qualifier);
                if (q != null && q.getValue().equals(qualifierValue)) {
                    return pa;
                }
            }
        }
        return null;
    }

    /**
     * Finds a value that matches a given qualifier.
     * @param propName the property name
     * @param qualifier the qualifier
     * @param qualifierValue the qualifier value
     * @return the value if a match was found (or null if no match was found)
     */
    protected Object findQualifiedValue(String propName,
            QName qualifier, String qualifierValue) {
        PropertyAccess pa = findQualifiedStructure(propName, qualifier, qualifierValue);
        if (pa != null) {
            XMPProperty rdfValue = pa.getValueProperty();
            if (rdfValue != null) {
                return rdfValue.getValue();
            }
        }
        return null;
    }

    /**
     * Returns an object array representation of the property's values.
     * @param propName the property name
     * @return the object array or null if the property isn't set
     */
    protected Object[] getObjectArray(String propName) {
        XMPProperty prop = meta.getProperty(getQName(propName));
        if (prop == null) {
            return null;
        }
        XMPArray array = prop.getArrayValue();
        if (array != null) {
            return array.toObjectArray();
        } else {
            return new Object[] {prop.getValue()};
        }
    }

    /**
     * Returns a String array representation of the property's values. Complex values are converted
     * to Strings using the toString() method.
     * @param propName the property name
     * @return the String array or null if the property isn't set
     */
    protected String[] getStringArray(String propName) {
        Object[] arr = getObjectArray(propName);
        if (arr == null) {
            return null;
        }
        String[] res = new String[arr.length];
        for (int i = 0, c = res.length; i < c; i++) {
            Object o = arr[i];
            if (o instanceof PropertyAccess) {
                XMPProperty prop = ((PropertyAccess)o).getValueProperty();
                res[i] = prop.getValue().toString();
            } else {
                res[i] = o.toString();
            }
        }
        return res;
    }

    /**
     * Returns a Date array representation of the property's values.
     * @param propName the property name
     * @return the Date array or null if the property isn't set
     */
    protected Date[] getDateArray(String propName) {
        Object[] arr = getObjectArray(propName);
        if (arr == null) {
            return null;
        }
        Date[] res = new Date[arr.length];
        for (int i = 0, c = res.length; i < c; i++) {
            Object obj = arr[i];
            if (obj instanceof Date) {
                res[i] = (Date) ((Date) obj).clone();
            } else {
                res[i] = DateFormatUtil.parseISO8601Date(obj.toString());
            }
        }
        return res;
    }

    public void setCompact(boolean c) {
        compact = c;
    }
}
