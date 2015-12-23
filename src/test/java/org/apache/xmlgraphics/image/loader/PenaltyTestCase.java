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

package org.apache.xmlgraphics.image.loader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.loader.util.Penalty;

/**
 * Tests for the {@link Penalty}.
 */
public class PenaltyTestCase {

    /**
     * Tests for penalty handling.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTruncatePenalty() throws Exception {
        assertEquals(0, Penalty.truncate(0));
        long penalty = Integer.MAX_VALUE;
        assertEquals(Integer.MAX_VALUE, Penalty.truncate(penalty));

        //Force integer wrap-around
        penalty++;
        assertEquals(Integer.MAX_VALUE, Penalty.truncate(penalty));
        //For comparison, normal casting does this
        assertEquals(Integer.MIN_VALUE, (int) penalty);

        //Now on the other end of the spectrum...
        penalty = Integer.MIN_VALUE;
        assertEquals(Integer.MIN_VALUE, Penalty.truncate(penalty));

        //Force integer wrap-around
        penalty -= 500;
        assertEquals(Integer.MIN_VALUE, Penalty.truncate(penalty));
        //For comparison, normal casting does this
        assertEquals(Integer.MAX_VALUE - 499, (int) penalty);
    }

    /**
     * Tests for the {@link Penalty} class.
     * @throws Exception if an error occurs
     */
    @Test
    public void testPenalty() throws Exception {
        Penalty p1 = Penalty.toPenalty(100);
        assertEquals(100, p1.getValue());
        Penalty p2 = p1.add(Penalty.toPenalty(50));
        assertEquals(150, p2.getValue());

        p1 = Penalty.toPenalty(0);
        assertEquals(0, p1.getValue());

        p1 = Penalty.INFINITE_PENALTY;
        assertEquals(Integer.MAX_VALUE, p1.getValue());
        assertTrue(p1.isInfinitePenalty());
        p2 = p1.add(p2);
        assertEquals(Integer.MAX_VALUE, p2.getValue());
        assertTrue(p2.isInfinitePenalty());
    }

}
