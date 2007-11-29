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

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

/**
 * Default implementation of the NestedDocumentHandler interface which automatically skips data
 * between Begin/EndDocument and Begin/EndData.
 */
public class DefaultNestedDocumentHandler implements DSCParserConstants, NestedDocumentHandler {

    private PSGenerator gen;
    
    /**
     * Creates a new instance.
     * @param gen PSGenerator to pass through the skipped content
     */
    public DefaultNestedDocumentHandler(PSGenerator gen) {
        this.gen = gen;
    }
    
    /**
     * @see org.apache.xmlgraphics.ps.dsc.NestedDocumentHandler#handle(org.apache.xmlgraphics.ps.dsc.events.DSCEvent, org.apache.xmlgraphics.ps.dsc.DSCParser)
     */
    public void handle(DSCEvent event, DSCParser parser) throws IOException, DSCException {
        if (event.isDSCComment()) {
            DSCComment comment = event.asDSCComment();
            if (DSCConstants.BEGIN_DOCUMENT.equals(comment.getName())) {
                comment.generate(gen);
                parser.setCheckEOF(false);
                comment = parser.nextDSCComment(DSCConstants.END_DOCUMENT, gen);
                if (comment == null) {
                    throw new DSCException("File is not DSC-compliant: Didn't find an " 
                            + DSCConstants.END_DOCUMENT);
                }
                comment.generate(gen);
                parser.setCheckEOF(true);
                parser.next();
            } else if (DSCConstants.BEGIN_DATA.equals(comment.getName())) {
                comment.generate(gen);
                parser.setCheckEOF(false);
                comment = parser.nextDSCComment(DSCConstants.END_DATA, gen);
                if (comment == null) {
                    throw new DSCException("File is not DSC-compliant: Didn't find an " 
                            + DSCConstants.END_DATA);
                }
                comment.generate(gen);
                parser.setCheckEOF(true);
                parser.next();
            } 
        }
    }
    
}
