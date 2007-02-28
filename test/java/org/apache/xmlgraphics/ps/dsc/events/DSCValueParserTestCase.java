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

package org.apache.xmlgraphics.ps.dsc.events;

import java.util.List;

import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBeginResource;

import junit.framework.TestCase;

public class DSCValueParserTestCase extends TestCase {

    private String[] toArray(List params) {
        return (String[])params.toArray(new String[params.size()]);
    }
    
    public void testText() throws Exception {
        DSCCommentBeginResource obj = new DSCCommentBeginResource();
        String[] res = toArray(obj.splitParams("procset Test"));
        assertEquals(2, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Test", res[1]);

        res = toArray(obj.splitParams("procset\tTest"));
        assertEquals(2, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Test", res[1]);
    }
    
    public void testParentheseText() throws Exception {
        DSCCommentBeginResource obj = new DSCCommentBeginResource();
        String[] res = toArray(obj.splitParams("procset (Hello World!)"));
        assertEquals(2, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Hello World!", res[1]);

        res = toArray(obj.splitParams("procset\t(Hello\t\\\\wonderful/ World!)"));
        assertEquals(2, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Hello\t\\wonderful/ World!", res[1]);

        res = toArray(obj.splitParams("procset (Hello \\042wonderful\\042 World!) blahblah"));
        assertEquals(3, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Hello \"wonderful\" World!", res[1]);
        assertEquals("blahblah", res[2]);

        //Parentheses not balanced
        res = toArray(obj.splitParams("procset (Hello (wonderful) World! blahblah"));
        assertEquals(2, res.length);
        assertEquals("procset", res[0]);
        assertEquals("Hello (wonderful) World! blahblah", res[1]);
    }
    
}
