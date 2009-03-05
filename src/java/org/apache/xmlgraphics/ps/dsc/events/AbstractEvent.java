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

/**
 * Abstract base class for DSC events.
 */
public abstract class AbstractEvent implements DSCEvent {

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#isComment()
     */
    public boolean isComment() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#isDSCComment()
     */
    public boolean isDSCComment() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#isHeaderComment()
     */
    public boolean isHeaderComment() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#isLine()
     */
    public boolean isLine() {
        return false;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#asDSCComment()
     */
    public DSCComment asDSCComment() {
        throw new ClassCastException(this.getClass().getName());
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#asLine()
     */
    public PostScriptLine asLine() {
        throw new ClassCastException(this.getClass().getName());
    }

}
