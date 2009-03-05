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

package org.apache.xmlgraphics.ps.dsc.tools;

import org.apache.xmlgraphics.ps.dsc.events.DSCCommentEndComments;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptLine;

import junit.framework.TestCase;

public class DSCToolsTestCase extends TestCase {

    public void testEndComment() throws Exception {
        DSCEvent event;

        event = new DSCCommentEndComments();
        assertTrue(DSCTools.headerCommentsEndHere(event));

        event = new PostScriptComment("FOPTest");
        assertFalse(DSCTools.headerCommentsEndHere(event));

        event = new DSCCommentPages(7);
        assertFalse(DSCTools.headerCommentsEndHere(event));

        event = new PostScriptComment(null);
        assertTrue(DSCTools.headerCommentsEndHere(event));

        event = new PostScriptComment("\t");
        assertTrue(DSCTools.headerCommentsEndHere(event));

        event = new PostScriptComment(" ***");
        assertTrue(DSCTools.headerCommentsEndHere(event));

        event = new PostScriptLine("/pgsave save def");
        assertTrue(DSCTools.headerCommentsEndHere(event));
    }

}
