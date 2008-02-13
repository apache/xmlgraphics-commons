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
 
package org.apache.xmlgraphics.fonts;

import junit.framework.TestCase;

/**
 * Tests for the Glyphs class.
 */
public class GlyphsTest extends TestCase {

    public void testGetUnicodeCodePointsForGlyphName() throws Exception {
        String glyph;
        String unicodes;
        
        glyph = "Omega";
        unicodes = Glyphs.getUnicodeCodePointsForGlyphName(glyph);
        assertEquals(2, unicodes.length());
        assertTrue("Must contain 03A9 - GREEK CAPITAL LETTER OMEGA",
                unicodes.indexOf("\u03A9") >= 0);
        assertTrue("Must contain 2126 - OHM SIGN",
                unicodes.indexOf("\u03A9") >= 0);

        glyph = "A";
        unicodes = Glyphs.getUnicodeCodePointsForGlyphName(glyph);
        assertEquals(1, unicodes.length());
        assertTrue("Must contain 0041 - LATIN CAPITAL LETTER A",
                unicodes.indexOf("\u0041") >= 0);
    }
    
}
