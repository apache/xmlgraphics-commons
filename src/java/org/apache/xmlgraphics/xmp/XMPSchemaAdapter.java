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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.xmlgraphics.util.QName;

/**
 * Base class for schema-specific adapters that provide user-friendly access to XMP values.
 */
public class XMPSchemaAdapter {

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
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Value must not be empty");
        }
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
                if (arr.getSize() == 0) {
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

    private static DateFormat createPseudoISO8601DateFormat() {
        DateFormat df = new SimpleDateFormat(
                "yyyy'-'MM'-'dd'T'HH':'mm':'ss", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df;
    }

    /**
     * Formats a Date using ISO 8601 format in the given time zone.
     * @param dt the date
     * @param tz the time zone
     * @return the formatted date
     */
    public static String formatISO8601Date(Date dt, TimeZone tz) {
        //ISO 8601 cannot be expressed directly using SimpleDateFormat
        Calendar cal = Calendar.getInstance(tz, Locale.ENGLISH);
        cal.setTime(dt);
        int offset = cal.get(Calendar.ZONE_OFFSET);
        offset += cal.get(Calendar.DST_OFFSET);

        //DateFormat is operating on GMT so adjust for time zone offset
        Date dt1 = new Date(dt.getTime() + offset);
        StringBuffer sb = new StringBuffer(createPseudoISO8601DateFormat().format(dt1));

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
            sb.append(Math.abs(zoneOffsetHours));
            sb.append(':');
            if (zoneOffsetMinutes < 10) {
                sb.append('0');
            }
            sb.append(zoneOffsetMinutes);
        }

        return sb.toString();
    }

    /**
     * Parses an ISO 8601 date and time value.
     * @param dt the date and time value as an ISO 8601 string
     * @return the parsed date/time
     * @todo Parse formats other than yyyy-mm-ddThh:mm:ssZ
     */
    public static Date parseISO8601Date(final String dt) {
        int offset = 0;
        String parsablePart;
        if (dt.endsWith("Z")) {
            parsablePart = dt.substring(0, dt.length() - 1);
        } else {
            int pos;
            int neg = 1;
            pos = dt.lastIndexOf('+');
            if (pos < 0) {
                pos = dt.lastIndexOf('-');
                neg = -1;
            }
            if (pos >= 0) {
                String timeZonePart = dt.substring(pos);
                parsablePart = dt.substring(0, pos);
                offset = Integer.parseInt(timeZonePart.substring(1, 3)) * 60;
                offset += Integer.parseInt(timeZonePart.substring(4, 6));
                offset *= neg;
            } else {
                parsablePart = dt;
            }
        }
        Date d;
        try {
            d = createPseudoISO8601DateFormat().parse(parsablePart);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid ISO 8601 date format: " + dt);
        }
        d.setTime(d.getTime() - offset * 60 * 1000);
        return d;
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
            return parseISO8601Date(dt);
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
        if (prop == null && value != null && value.length() > 0) {
            prop = new XMPProperty(name, value);
            meta.setProperty(prop);
        } else if (value != null) {
            prop.setValue(value);
        } else {
            meta.removeProperty(name);
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
                if (array.getSize() == 0) {
                    meta.removeProperty(name);
                }
                return removed;
            } else {
                String removed = prop.getValue().toString();
                if (lang.equals(prop.getXMLLang())) {
                    prop.clear();
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
     * Returns a String array representation of the property's values. Complex values to converted
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
            res[i] = arr[i].toString();
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
                res[i] = (Date)((Date)obj).clone();
            } else {
                res[i] = parseISO8601Date(obj.toString());
            }
        }
        return res;
    }


}
