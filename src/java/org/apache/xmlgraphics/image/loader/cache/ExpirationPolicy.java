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
 * Represents an expiration policy for cache entries that have a creation time stamp.
 */
public interface ExpirationPolicy {

    /**
     * Indicates whether a cache entry is expired given its creation time stamp.
     * @param provider the provider for new time stamps
     * @param timestamp the creation time stamp (the semantics of
     *                   {@link System#currentTimeMillis()} apply)
     * @return true if the entry is to be considered expired, false if not
     */
    boolean isExpired(TimeStampProvider provider, long timestamp);

}
