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
 * Returns time stamps for the image cache for entry expiration functionality. This functionality
 * is in its own class so it's easy to write a mock class for testing.
 */
class TimeStampProvider {

    /**
     * Returns the current time stamp.
     * @return the current time stamp (the value returned follows the semantics of
     *                   {@link System#currentTimeMillis()})
     */
    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

}
