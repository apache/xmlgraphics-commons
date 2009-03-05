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

package org.apache.xmlgraphics.ps.dsc.tools;

import java.io.IOException;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.DSCParserConstants;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;
import org.apache.xmlgraphics.ps.dsc.events.DSCHeaderComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptComment;

/**
 * Helper methods commonly used when dealing with DSC-compliant PostScript files.
 */
public class DSCTools implements DSCParserConstants {

    /**
     * Indicates whether the given event ends a header comment section according to the rules in
     * DSC 3.0, chapter 4.4.
     * @param event the event to check
     * @return true if a header comment section would be ended either explicitely or implicitely
     *              by the given event
     */
    public static boolean headerCommentsEndHere(DSCEvent event) {
        switch (event.getEventType()) {
        case DSC_COMMENT:
            DSCComment comment = event.asDSCComment();
            return (comment.getName().equals(DSCConstants.END_COMMENTS));
        case COMMENT:
            String s = ((PostScriptComment)event).getComment();
            if (s == null || s.length() == 0) {
                return true;
            } else {
                char c = s.charAt(0);
                return ("\n\t ".indexOf(c) >= 0);
            }
        default:
            return true;
        }
    }

    /**
     * Verifies that the file being parsed is a DSC 3.0 file.
     * @param parser the DSC parser
     * @return the header comment event
     * @throws DSCException In case of a violation of the DSC spec
     * @throws IOException In case of an I/O problem
     */
    public static DSCHeaderComment checkAndSkipDSC30Header(DSCParser parser)
                throws DSCException, IOException {
        if (!parser.hasNext()) {
            throw new DSCException("File has no content");
        }
        DSCEvent event = parser.nextEvent();
        if (event.getEventType() == HEADER_COMMENT) {
            DSCHeaderComment header = (DSCHeaderComment)event;
            if (!header.isPSAdobe30()) {
                throw new DSCException("PostScript file does not start with '"
                        + DSCConstants.PS_ADOBE_30 + "'");
            }
            return header;
        } else {
            throw new DSCException("PostScript file does not start with '"
                    + DSCConstants.PS_ADOBE_30 + "'");
        }
    }

    /**
     * Advances the parser to the next page or to the trailer or the end of file comment.
     * @param parser the DSC parser
     * @param gen the PSGenerator instance to pass the skipped events through to
     * @return the DSC comment found (Page, Trailer or EOF)
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public static DSCComment nextPageOrTrailer(DSCParser parser, PSGenerator gen)
                throws IOException, DSCException {
        while (parser.hasNext()) {
            DSCEvent event = parser.nextEvent();
            if (event.getEventType() == DSC_COMMENT) {
                DSCComment comment = event.asDSCComment();
                if (DSCConstants.PAGE.equals(comment.getName())) {
                    return comment;
                } else if (DSCConstants.TRAILER.equals(comment.getName())) {
                    return comment;
                }
            } else if (event.getEventType() == EOF) {
                //The Trailer may be missing
                return event.asDSCComment();
            }
            if (gen != null) {
                event.generate(gen); //Pipe through to PSGenerator
            }
        }
        return null;
    }

}
