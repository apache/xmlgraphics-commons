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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.xmlgraphics.util.QName;
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

    public void testReplaceLangAlt() throws Exception {
        Metadata xmp = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(xmp);
        dc.setTitle("Default title");
        StringWriter writer = new StringWriter();
        XMPSerializer.writeXML(xmp, new StreamResult(writer));
        String xmpString = writer.toString();
        xmp = XMPParser.parseXMP(new StreamSource(new java.io.StringReader(xmpString)));
        dc = DublinCoreSchema.getAdapter(xmp);
        assertEquals("Default title", dc.getTitle());
        dc.setTitle("Updated title");
        XMPProperty prop = xmp.getProperty(new QName(DublinCoreSchema.NAMESPACE, "title"));
        XMPArray array = prop.getArrayValue();
        assertNotNull(array);
        //Check that only one title is present. There used to be a bug that didn't set the
        //non-qualified value equal to the value qualified with "x-default".
        assertEquals(1, array.getSize());
        assertEquals("Updated title", array.getValue(0));
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

    public void testQualifiers() throws Exception {
        Metadata xmp = new Metadata();
        XMPBasicAdapter basic = XMPBasicSchema.getAdapter(xmp);

        basic.addIdentifier("x123");
        basic.addIdentifier("id1", "system1");
        basic.addIdentifier("12345", "system2");

        String[] ids = basic.getIdentifiers();
        assertEquals(3, ids.length);
        Set set = new java.util.HashSet(Arrays.asList(ids));
        assertTrue(set.contains("x123"));
        assertTrue(set.contains("id1"));
        assertTrue(set.contains("12345"));

        assertEquals("id1", basic.getIdentifier("system1"));
    }

}
