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
 * Interface that is used to delegate the handling of nested documents (EPS files, data sections)
 * in a PostScript document. The implementation receives a parser instance so it can step forward
 * until the end of the nested document is reached at which point control is given back to the
 * original consumer.
 */
public interface NestedDocumentHandler {

    /**
     * Handle a DSC event. Implementations may issue additional calls to the DSC parser and may
     * modify its state. When returning from the call, state information such as filters should
     * be restored.
     * @param event the DSC event to handle
     * @param parser the DSC parser to work with
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    void handle(DSCEvent event, DSCParser parser) throws IOException, DSCException;

}