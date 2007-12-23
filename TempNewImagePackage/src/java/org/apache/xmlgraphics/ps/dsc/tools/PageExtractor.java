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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCFilter;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.DSCParserConstants;
import org.apache.xmlgraphics.ps.dsc.DefaultNestedDocumentHandler;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPage;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;
import org.apache.xmlgraphics.ps.dsc.events.DSCHeaderComment;

/**
 * This class can extract a certain range of pages from a DSC-compliant PostScript file.
 */
public class PageExtractor implements DSCParserConstants {
    
    /**
     * Parses a DSC-compliant file and pipes the content through to the OutputStream omitting
     * all pages not within the range.
     * @param in the InputStream to parse from 
     * @param out the OutputStream to write the modified file to
     * @param from the starting page (1-based)
     * @param to the last page (inclusive, 1-based)
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public static void extractPages(InputStream in, OutputStream out, int from, int to) 
                throws IOException, DSCException {
        if (from <= 0) {
            throw new IllegalArgumentException("'from' page number must be 1 or higher");
        }
        if (to < from) {
            throw new IllegalArgumentException(
                    "'to' page number must be equal or larger than the 'from' page number");
        }
        
        DSCParser parser = new DSCParser(in);
        PSGenerator gen = new PSGenerator(out);
        parser.setNestedDocumentHandler(new DefaultNestedDocumentHandler(gen));
        int pageCount = 0;
        
        //Skip DSC header
        DSCHeaderComment header = DSCTools.checkAndSkipDSC30Header(parser);
        header.generate(gen);
        //Set number of pages
        DSCCommentPages pages = new DSCCommentPages(to - from + 1);
        pages.generate(gen);

        parser.setFilter(new DSCFilter() {
            public boolean accept(DSCEvent event) {
                if (event.isDSCComment()) {
                    //Filter %%Pages which we add manually above
                    return !event.asDSCComment().getName().equals(DSCConstants.PAGES);
                } else {
                    return true;
                }
            }
        });

        //Skip the prolog and to the first page
        DSCComment pageOrTrailer = parser.nextDSCComment(DSCConstants.PAGE, gen);
        if (pageOrTrailer == null) {
            throw new DSCException("Page expected, but none found");
        }
        parser.setFilter(null); //Remove filter
        
        //Process individual pages (and skip as necessary)
        while (true) {
            DSCCommentPage page = (DSCCommentPage)pageOrTrailer;
            boolean validPage = (page.getPagePosition() >= from && page.getPagePosition() <= to);
            if (validPage) {
                page.setPagePosition(page.getPagePosition() - from + 1);
                page.generate(gen);
                pageCount++;
            }
            pageOrTrailer = DSCTools.nextPageOrTrailer(parser, (validPage ? gen : null));
            if (pageOrTrailer == null) {
                throw new DSCException("File is not DSC-compliant: Unexpected end of file");
            } else if (!DSCConstants.PAGE.equals(pageOrTrailer.getName())) {
                pageOrTrailer.generate(gen);
                break;
            }
        }
        
        //Write the rest
        while (parser.hasNext()) {
            DSCEvent event = parser.nextEvent();
            event.generate(gen);
        }
    }
    
}