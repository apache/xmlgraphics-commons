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

package org.apache.xmlgraphics.io;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class TempResourceURIGeneratorTestCase {

    private TempResourceURIGenerator sut = new TempResourceURIGenerator("test");

    @Test
    public void testGenerate() {
        URI first = sut.generate();
        URI second = sut.generate();
        Pattern regex = Pattern.compile("tmp:///test.*");
        assertTrue(regex.matcher(first.toASCIIString()).matches());
        assertTrue(regex.matcher(second.toASCIIString()).matches());
        assertNotSame(first, second);

        // Test that they are unique over a large number of calls to generate()
        Set<URI> uniqueSet = new HashSet<URI>();
        int numberOfTests = 1000;
        for (int i = 0; i < numberOfTests; i++) {
            uniqueSet.add(sut.generate());
        }
        assertEquals(numberOfTests, uniqueSet.size());
    }

    @Test
    public void testIsTemURI() {
        assertTrue(testTempURI("tmp:///test"));
        assertTrue(testTempURI("tmp://test"));
        assertTrue(testTempURI("tmp:/test"));
        assertTrue(testTempURI("tmp:test"));

        assertFalse(testTempURI("tmp/test"));
        assertFalse(testTempURI("temp:///test"));
    }

    private boolean testTempURI(String uriString) {
        return TempResourceURIGenerator.isTempURI(URI.create(uriString));
    }
}
