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

package org.apache.xmlgraphics.ps.dsc;

import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

/**
 * Filter interface for DSC events used by the DSC parser.
 */
public interface DSCFilter {

    /**
     * Indicates whether a particular event is acceptable or if it should be skipped/ignored.
     * @param event the DSC event
     * @return true if the event should be accepted
     */
    boolean accept(DSCEvent event);
    
}
