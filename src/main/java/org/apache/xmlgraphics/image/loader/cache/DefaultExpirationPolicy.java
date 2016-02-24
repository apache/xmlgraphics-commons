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

/**
 * Implements the default expiration policy for the image cache.
 */
public class DefaultExpirationPolicy implements ExpirationPolicy {

    public static final int EXPIRATION_IMMEDIATE = 0;
    public static final int EXPIRATION_NEVER = -1;

    private int expirationAfter; //in seconds

    /**
     * Creates a new policy with default settings (expiration in 60 seconds).
     */
    public DefaultExpirationPolicy() {
        this(60);
    }

    /**
     * Creates a new policy.
     * @param expirationAfter the expiration in seconds (a negative value means: never expire)
     */
    public DefaultExpirationPolicy(int expirationAfter) {
        this.expirationAfter = expirationAfter;
    }

    private boolean isNeverExpired() {
        return (this.expirationAfter < 0);
    }

    /** {@inheritDoc} */
    public boolean isExpired(TimeStampProvider provider, long timestamp) {
        if (isNeverExpired()) {
            return false;
        } else {
            long now = provider.getTimeStamp();
            return now >= (timestamp + (this.expirationAfter * 1000L));
        }
    }

}
