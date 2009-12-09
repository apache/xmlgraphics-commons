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

import junit.framework.TestCase;

import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;

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
        dc.setTitle("de", "Der Titel");
        String title = dc.removeTitle("en");
        assertEquals("The title", title);
        title = dc.removeTitle("en");
        assertNull(title);
        
        title = dc.removeTitle("de");
        assertEquals("Der Titel", title);
        title = dc.removeTitle("de");
        assertNull(title);
    }

}
