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

import java.io.IOException;

import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

/**
 * Listener interface for the DSC parser. It can be used to be notified
 */
public interface DSCListener {

    /**
     * Called for each DSC event. You can call methods on the DSC parser to skip comments,
     * for example. But implementations need to be good citizens and take into account that
     * multiple listeners can be active at the same time and that they might interfere with
     * other listeners. When returning from the call, state information such as filters should
     * be restored.
     * @param event the DSC event
     * @param parser the DSC parser
     * @throws IOException if an I/O error occurs
     * @throws DSCException if a DSC-specific error occurs
     */
    void processEvent(DSCEvent event, DSCParser parser) throws IOException, DSCException;

}
