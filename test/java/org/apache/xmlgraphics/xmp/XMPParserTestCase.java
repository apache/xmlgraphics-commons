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

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFAdapter;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;

/**
 * Tests for the XMP parser.
 */
public class XMPParserTestCase {

    @Test
    public void testParseBasics() throws Exception {
        URL url = getClass().getResource("test-basics.xmp");
        Metadata meta = XMPParser.parseXMP(url);

        DublinCoreAdapter dcAdapter = DublinCoreSchema.getAdapter(meta);
        XMPBasicAdapter basicAdapter = XMPBasicSchema.getAdapter(meta);
        AdobePDFAdapter pdfAdapter = AdobePDFSchema.getAdapter(meta);

        XMPProperty prop;
        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "creator");
        XMPArray array;
        array = prop.getArrayValue();
        assertEquals(1, array.getSize());
        assertEquals("John Doe", array.getValue(0).toString());
        assertEquals("John Doe", dcAdapter.getCreators()[0]);

        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "title");
        assertEquals("Example document", prop.getValue().toString());
        assertEquals("Example document", dcAdapter.getTitle());
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreateDate");
        //System.out.println("Creation Date: " + prop.getValue() + " " + prop.getClass().getName());
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreatorTool");
        assertEquals("An XML editor", prop.getValue().toString());
        assertEquals("An XML editor", basicAdapter.getCreatorTool());
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "Producer");
        assertEquals("Apache FOP Version SVN trunk", prop.getValue().toString());
        assertEquals("Apache FOP Version SVN trunk", pdfAdapter.getProducer());
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "PDFVersion");
        assertEquals("1.4", prop.getValue().toString());
        assertEquals("1.4", pdfAdapter.getPDFVersion());
    }

    @Test
    public void testParse1() throws Exception {
        URL url = getClass().getResource("unknown-schema.xmp");
        Metadata meta = XMPParser.parseXMP(url);

        DublinCoreAdapter dcAdapter = DublinCoreSchema.getAdapter(meta);

        XMPProperty prop;
        //Access through the known schema as reference
        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "title");
        assertEquals("Unknown Schema", prop.getValue().toString());
        assertEquals("Unknown Schema", dcAdapter.getTitle());

        //Access through a schema unknown to the XMP framework
        prop = meta.getProperty("http://unknown.org/something", "dummy");
        assertEquals("Dummy!", prop.getValue().toString());
    }

    @Test
    public void testParseStructures() throws Exception {
        URL url = getClass().getResource("test-structures.xmp");
        Metadata meta = XMPParser.parseXMP(url);

        XMPProperty prop;

        String testns = "http://foo.bar/test/";
        prop = meta.getProperty(testns, "something");
        assertEquals("blablah", prop.getValue().toString());

        prop = meta.getProperty(testns, "ingredients");
        XMPArray array = prop.getArrayValue();
        assertEquals(3, array.getSize());
        XMPStructure struct = array.getStructure(0);
        assertEquals(2, struct.getPropertyCount());
        prop = struct.getValueProperty();
        assertEquals("Apples", prop.getValue());
        prop = struct.getProperty(testns, "amount");
        assertEquals("4", prop.getValue());

        prop = meta.getProperty(testns, "villain");
        XMPProperty prop1;
        prop1 = prop.getStructureValue().getProperty(testns, "name");
        assertEquals("Darth Sidious", prop1.getValue());
        prop1 = prop.getStructureValue().getProperty(testns, "other-name");
        assertEquals("Palpatine", prop1.getValue());

        //Test shorthand form
        prop = meta.getProperty(testns, "project");
        prop1 = prop.getStructureValue().getProperty(testns, "name");
        assertEquals("Apache XML Graphics", prop1.getValue());
        prop1 = prop.getStructureValue().getProperty(testns, "url");
        assertEquals("http://xmlgraphics.apache.org/", prop1.getValue());

    }

    @Test
    public void testAttributeValues() throws Exception {
        URL url = getClass().getResource("test-attribute-values.xmp");
        Metadata meta = XMPParser.parseXMP(url);

        DublinCoreAdapter dcAdapter = DublinCoreSchema.getAdapter(meta);
        assertEquals("Ender's Game", dcAdapter.getTitle());
        assertEquals("Orson Scott Card", dcAdapter.getCreators()[0]);
    }

    @Test
    public void testParseDates() throws Exception {
        URL url = getClass().getResource("test-dates.xmp");
        Metadata meta = XMPParser.parseXMP(url);
        XMPProperty prop;

        DublinCoreAdapter dcAdapter = DublinCoreSchema.getAdapter(meta);

        //Simple adapter access
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
        cal.set(2006, Calendar.JUNE, 2, 10, 36, 40);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), dcAdapter.getDate());
        Date[] dates = dcAdapter.getDates();
        assertEquals(2, dates.length);

        //The second is the most recent and should match the simple value
        assertEquals(dates[1], dcAdapter.getDate());

        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "date");
        assertNotNull(prop.getArrayValue());
        assertEquals(2, prop.getArrayValue().getSize());

        //Now add a new date and check if the adapter's getDate() method returns the new date.
        cal.set(2008, Calendar.NOVEMBER, 1, 10, 10, 0);
        dcAdapter.addDate(cal.getTime());
        assertEquals(3, dcAdapter.getDates().length);
        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "date");
        assertNotNull(prop.getArrayValue());
        assertEquals(3, prop.getArrayValue().getSize());
        assertEquals(cal.getTime(), dcAdapter.getDate());
    }

    @Test
    public void testParseEmptyValues() throws Exception {
        URL url = getClass().getResource("empty-values.xmp");
        Metadata meta = XMPParser.parseXMP(url);

        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(meta);
        String title = dc.getTitle();
        assertEquals("empty", title);

        title = dc.getTitle("fr"); //Does not exist
        assertNull(title);

        title = dc.getTitle("de");
        assertNull(title); //Empty value treated same as not existant
    }

}
