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

package org.apache.xmlgraphics.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;

import junit.framework.TestCase;

/**
 * Tests date formatting for XMP.
 */
public class DateFormatUtilTestCase extends TestCase {

    /**
     * Checks date formatting for XMP.
     * @throws Exception if an error occurs
     */
    public void testDateFormattingISO8601() throws Exception {
        Date dt = createTestDate();

        String s = XMPSchemaAdapter.formatISO8601Date(dt, TimeZone.getTimeZone("GMT"));
        assertEquals("2008-02-07T15:11:07Z", s);
        assertEquals(dt, DateFormatUtil.parseISO8601Date(s));

        s = XMPSchemaAdapter.formatISO8601Date(dt, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("2008-02-07T17:11:07+02:00", s);
        assertEquals(dt, DateFormatUtil.parseISO8601Date(s));

        s = XMPSchemaAdapter.formatISO8601Date(dt, TimeZone.getTimeZone("GMT+02:30"));
        assertEquals("2008-02-07T17:41:07+02:30", s);
        assertEquals(dt, DateFormatUtil.parseISO8601Date(s));

        s = XMPSchemaAdapter.formatISO8601Date(dt, TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("2008-02-07T07:11:07-08:00", s);
        assertEquals(dt, DateFormatUtil.parseISO8601Date(s));

        s = XMPSchemaAdapter.formatISO8601Date(dt, TimeZone.getTimeZone("GMT-11:00"));
        assertEquals("2008-02-07T04:11:07-11:00", s);
        assertEquals(dt, DateFormatUtil.parseISO8601Date(s));
    }

    private Date createTestDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        cal.set(2008, Calendar.FEBRUARY, 07, 15, 11, 07);
        cal.set(Calendar.MILLISECOND, 0);
        Date dt = cal.getTime();
        return dt;
    }

    public void testDateFormattingPDF() throws Exception {
        Date dt = createTestDate();

        String s = DateFormatUtil.formatPDFDate(dt, TimeZone.getTimeZone("GMT"));
        assertEquals("D:20080207151107Z", s);

        s = DateFormatUtil.formatPDFDate(dt, TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("D:20080207171107+02'00'", s);

        s = DateFormatUtil.formatPDFDate(dt, TimeZone.getTimeZone("GMT+02:30"));
        assertEquals("D:20080207174107+02'30'", s);

        s = DateFormatUtil.formatPDFDate(dt, TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("D:20080207071107-08'00'", s);

        s = DateFormatUtil.formatPDFDate(dt, TimeZone.getTimeZone("GMT-11:00"));
        assertEquals("D:20080207041107-11'00'", s);
    }

    public void testParseInvalidDateNoColonUTC() {
        testInvalidDate("2008-02-07T151107Z");
    }

    public void testParseInvalidDateNoColonLocal() {
        testInvalidDate("2008-02-07T151107+0000");
    }

    public void testParseInvalidDateColonLast() {
        testInvalidDate("2008-02-07T151107Z:");
    }

    private void testInvalidDate(String date) {
        try {
            DateFormatUtil.parseISO8601Date(date);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
