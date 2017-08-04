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
import java.util.List;

import org.apache.xmlgraphics.ps.dsc.events.DSCComment;

/**
 * DSCHandler implementation that records DSC events.
 */
public class EventRecorder implements DSCHandler {

    private List events = new java.util.ArrayList();

    /**
     * Replays the recorded events to a specified DSCHandler instance.
     * @param handler the DSCHandler to send the recorded events to
     * @throws IOException In case of an I/O error
     */
    public void replay(DSCHandler handler) throws IOException {
        for (Object obj : events) {
            if (obj instanceof PSLine) {
                handler.line(((PSLine) obj).getLine());
            } else if (obj instanceof PSComment) {
                handler.comment(((PSComment) obj).getComment());
            } else if (obj instanceof DSCComment) {
                handler.handleDSCComment((DSCComment) obj);
            } else {
                throw new IllegalStateException("Unsupported class type");
            }
        }
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.DSCHandler#comment(java.lang.String)
     */
    public void comment(String comment) throws IOException {
        events.add(new PSComment(comment));
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.DSCHandler#handleDSCComment(
     *          org.apache.xmlgraphics.ps.dsc.events.DSCComment)
     */
    public void handleDSCComment(DSCComment comment) throws IOException {
        events.add(comment);
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.DSCHandler#line(java.lang.String)
     */
    public void line(String line) throws IOException {
        events.add(new PSLine(line));
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.DSCHandler#startDocument(java.lang.String)
     */
    public void startDocument(String header) throws IOException {
        throw new UnsupportedOperationException(
                getClass().getName() + " is only used to handle parts of a document");
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.DSCHandler#endDocument()
     */
    public void endDocument() throws IOException {
        throw new UnsupportedOperationException(
                getClass().getName() + " is only used to handle parts of a document");
    }

    private static class PSComment {

        private String comment;

        public PSComment(String comment) {
            this.comment = comment;
        }

        public String getComment() {
            return this.comment;
        }
    }

    private static class PSLine {

        private String line;

        public PSLine(String line) {
            this.line = line;
        }

        public String getLine() {
            return this.line;
        }
    }

}
