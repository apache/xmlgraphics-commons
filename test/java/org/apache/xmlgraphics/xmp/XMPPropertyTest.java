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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

/**
 * Tests property access methods.
 */
public class XMPPropertyTest extends TestCase {

    public void testPropertyAccess() throws Exception {
        Metadata xmp = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(xmp);
        assertNull(dc.getContributors());

        dc.addContributor("Contributor1");
        assertEquals(1, dc.getContributors().length);
        assertEquals("Contributor1", dc.getContributors()[0]);
        dc.removeContributor("Contributor1");
        assertNull(dc.getContributors());

        dc.addContributor("Contributor1");
        assertEquals(1, dc.getContributors().length);
        dc.addContributor("Contributor2");
        assertEquals(2, dc.getContributors().length);
        assertFalse(dc.removeContributor("DoesNotExist"));
        assertTrue(dc.removeContributor("Contributor1"));
        assertEquals(1, dc.getContributors().length);
        assertTrue(dc.removeContributor("Contributor2"));
        assertFalse(dc.removeContributor("Contributor2"));
        assertNull(dc.getContributors());
    }

    public void testPropertyRemovalLangAlt() throws Exception {
        Metadata xmp = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(xmp);

        //dc:title is a "Lang Alt"
        dc.setTitle("en", "The title");
        String title = dc.removeTitle("en");
        assertEquals("The title", title);
        dc.setTitle("en", "The title");
        dc.setTitle("de", "Der Titel");
        title = dc.removeTitle("en");
        assertEquals("The title", title);
        title = dc.removeTitle("en");
        assertNull(title);

        title = dc.removeTitle("de");
        assertEquals("Der Titel", title);
        title = dc.removeTitle("de");
        assertNull(title);
    }

    public void testPropertyValues() throws Exception {
        Metadata xmp = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(xmp);

        String format = dc.getFormat();
        assertNull(format);

        dc.setFormat("application/pdf");
        format = dc.getFormat();
        assertEquals("application/pdf", format);

        dc.setFormat("image/jpeg");
        format = dc.getFormat();
        assertEquals("image/jpeg", format);

        dc.setFormat(null);
        format = dc.getFormat();
        assertNull(format);

        dc.setFormat(""); //Empty string same as null value
        format = dc.getFormat();
        assertNull(format);

        dc.setTitle("title");
        String title = dc.getTitle();
        assertEquals("title", title);

        dc.setTitle("Titel");
        title = dc.getTitle();
        assertEquals("Titel", title);

        dc.setTitle(null);
        title = dc.getTitle();
        assertNull(title);

        dc.setTitle("");
        title = dc.getTitle();
        assertNull(title);
    }

    public void testDates() throws Exception {
        Metadata xmp = new Metadata();
        XMPBasicAdapter basic = XMPBasicSchema.getAdapter(xmp);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
        cal.set(2008, Calendar.FEBRUARY, 07, 15, 11, 07);
        cal.set(Calendar.MILLISECOND, 0);
        Date dt = cal.getTime();

        assertNull(basic.getCreateDate());
        basic.setCreateDate(dt);
        Date dt2 = basic.getCreateDate();
        assertEquals(dt2, dt);
    }

}
