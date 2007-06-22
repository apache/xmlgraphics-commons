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
 
package org.apache.xmlgraphics.ps;

import org.apache.xmlgraphics.ps.PSGenerator;

import junit.framework.TestCase;

/**
 * Tests literal text string escaping.
 */
public class PSEscapeTestCase extends TestCase {

    public void testBasics() throws Exception {
        StringBuffer sb = new StringBuffer();
        
        PSGenerator.escapeChar('a', sb);
        PSGenerator.escapeChar('b', sb);
        PSGenerator.escapeChar('c', sb);
        PSGenerator.escapeChar('!', sb);
        assertEquals("abc!", sb.toString());
        
        sb.setLength(0);
        PSGenerator.escapeChar('0', sb);
        PSGenerator.escapeChar('\t', sb);
        PSGenerator.escapeChar('(', sb);
        PSGenerator.escapeChar('x', sb);
        PSGenerator.escapeChar(')', sb);
        PSGenerator.escapeChar('\n', sb);
        PSGenerator.escapeChar('\u001E', sb); //<RS>
        PSGenerator.escapeChar('\u00E4', sb); //a umlaut
        PSGenerator.escapeChar('\u20AC', sb); //EURO Sign
        assertEquals("0\\t\\(x\\)\\n\\036\\344?", sb.toString());
    }
    
    public void testStringToDSC() throws Exception {
        String escaped;
        escaped = PSGenerator.convertStringToDSC("0\t(x)\n\u001E\u00E4\u20AC");
        assertEquals("0\\t\\(x\\)\\n\\036\\344?", escaped);
        escaped = PSGenerator.convertStringToDSC("0\t(x)\n\u001E\u00E4 \u20AC");
        assertEquals("(0\\t\\(x\\)\\n\\036\\344 ?)", escaped);
        escaped = PSGenerator.convertStringToDSC("0\t(x)\n\u001E\u00E4\u20AC", true);
        assertEquals("(0\\t\\(x\\)\\n\\036\\344?)", escaped);
    }
    
}
