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
package org.apache.xmlgraphics.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateFormatUtil {

    private static final String ISO_8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private DateFormatUtil() {
    }

    /**
     * Formats the date according to PDF format. See section 3.8.2 of the PDF 1.4 specification.
     * @param date The date time to format
     * @param timeZone The time zone used to format the date
     * @return a formatted date according to PDF format (based on ISO 8824)
     */
    public static String formatPDFDate(Date date, TimeZone timeZone) {
        DateFormat dateFormat = createDateFormat("'D:'yyyyMMddHHmmss", timeZone);
        return formatDate(date, dateFormat, '\'', true);
    }

    /**
     * Formats the date according to ISO 8601 standard.
     * @param date The date time to format
     * @param timeZone The time zone used to format the date
     * @return a formatted date according to ISO 8601
     */
    public static String formatISO8601(Date date, TimeZone timeZone) {
        DateFormat dateFormat = createDateFormat(ISO_8601_DATE_PATTERN, timeZone);
        return formatDate(date, dateFormat, ':', false);
    }

    private static DateFormat createDateFormat(String format, TimeZone timeZone) {
        DateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        dateFormat.setTimeZone(timeZone);
        return dateFormat;
    }

    /**
     * Formats the date according to the specified format and returns as a string.
     * @param date The date / time object to format
     * @param dateFormat The date format to use when outputting the date
     * @param delimiter The character used to separate the time zone difference hours and minutes
     * @param endWithDelimiter Determines whether the date string will end with the delimiter character
     * @return the formatted date string
     */
    private static String formatDate(Date date, DateFormat dateFormat, char delimiter,
            boolean endWithDelimiter) {
        Calendar cal = Calendar.getInstance(dateFormat.getTimeZone(), Locale.ENGLISH);
        cal.setTime(date);
        int offset = getOffsetInMinutes(cal);
        StringBuilder sb = new StringBuilder(dateFormat.format(date));
        appendOffset(sb, delimiter, offset, endWithDelimiter);
        return sb.toString();
    }

    private static int getOffsetInMinutes(Calendar cal) {
        int offset = cal.get(Calendar.ZONE_OFFSET);
        offset += cal.get(Calendar.DST_OFFSET);
        offset /= (1000 * 60);
        return offset;
    }

    private static void appendOffset(StringBuilder sb, char delimiter, int offset, boolean endWithDelimiter) {
        if (offset == 0) {
            appendOffsetUTC(sb);
        } else {
            appendOffsetNoUTC(sb, delimiter, offset, endWithDelimiter);
        }
    }

    private static void appendOffsetUTC(StringBuilder sb) {
        sb.append('Z');
    }

    private static void appendOffsetNoUTC(StringBuilder sb, char delimiter, int offset,
            boolean endWithDelimiter) {
        int zoneOffsetHours = offset / 60;
        appendOffsetSign(sb, zoneOffsetHours);
        appendPaddedNumber(sb, Math.abs(zoneOffsetHours));
        sb.append(delimiter);
        appendPaddedNumber(sb, Math.abs(offset % 60));
        if (endWithDelimiter) {
            sb.append(delimiter);
        }
    }

    private static void appendOffsetSign(StringBuilder sb, int zoneOffsetHours) {
        if (zoneOffsetHours >= 0) {
            sb.append('+');
        } else {
            sb.append('-');
        }
    }

    private static void appendPaddedNumber(StringBuilder sb, int number) {
        if (number < 10) {
            sb.append('0');
        }
        sb.append(number);
    }

    /**
     * Parses an ISO 8601 date and time value.
     * @param date the date and time value as an ISO 8601 string
     * @return the parsed date/time
     */
    public static Date parseISO8601Date(String date) {
        final String errorMessage = "Invalid ISO 8601 date format: ";
        date = formatDateToParse(date, errorMessage);
        DateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_PATTERN + "Z");
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(errorMessage + date);
        }
    }

    private static String formatDateToParse(String date, String errorMessage) {
        /* Remove the colon from the time zone difference (+08:00) so that it can be parsed
         * by the SimpleDateFormat string.
         */
        if (!date.contains("Z")) {
            int lastColonIndex = date.lastIndexOf(":");
            if (lastColonIndex < 0) {
                throw new IllegalArgumentException(errorMessage + date);
            }
            date = date.substring(0, lastColonIndex) + date.substring(lastColonIndex + 1, date.length());
        } else {
            date = date.replace("Z", "+0000");
        }
        return date;
    }

}
