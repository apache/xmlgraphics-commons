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

package org.apache.xmlgraphics.image.loader.cache;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests {@link DefaultExpirationPolicy}.
 */
public class DefaultExpirationPolicyTestCase {

    /**
     * Never expire.
     * @throws Exception if an error occurs
     */
    @Test
    public void testNeverExpire() throws Exception {
        ExpirationPolicy policy;
        policy = new DefaultExpirationPolicy(DefaultExpirationPolicy.EXPIRATION_NEVER);

        MockTimeStampProvider provider = new MockTimeStampProvider();

        long ts = 1000000;
        assertFalse(policy.isExpired(provider, ts));
        provider.setTimeStamp(ts + Integer.MAX_VALUE);
        assertFalse(policy.isExpired(provider, ts));
    }

    /**
     * Normal expiration
     * @throws Exception if an error occurs
     */
    @Test
    public void testNormalExpiration() throws Exception {
        ExpirationPolicy policy;
        policy = new DefaultExpirationPolicy(2);

        MockTimeStampProvider provider = new MockTimeStampProvider();

        long ts = 1000000;
        provider.setTimeStamp(ts);
        assertFalse(policy.isExpired(provider, ts));
        provider.setTimeStamp(ts + 1000);
        assertFalse(policy.isExpired(provider, ts));

        provider.setTimeStamp(ts + 2000);
        assertTrue(policy.isExpired(provider, ts));
        provider.setTimeStamp(ts + 3000);
        assertTrue(policy.isExpired(provider, ts));
    }

}
