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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.events.DSCAtend;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;
import org.apache.xmlgraphics.ps.dsc.events.DSCHeaderComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptLine;
import org.apache.xmlgraphics.ps.dsc.events.UnparsedDSCComment;
import org.apache.xmlgraphics.ps.dsc.tools.DSCTools;

/**
 * Parser for DSC-compliant PostScript files (DSC = Document Structuring Conventions). The parser
 * is implemented as a pull parser but has the ability to act as a push parser through the
 * DSCHandler interface.
 */
public class DSCParser implements DSCParserConstants {
    private static final Log LOG = LogFactory.getLog(DSCParser.class);

    private InputStream in;
    private BufferedReader reader;
    private boolean eofFound;
    private boolean checkEOF = true;
    private DSCEvent currentEvent;
    private DSCEvent nextEvent;
    private DSCListener nestedDocumentHandler;
    private DSCListener filterListener;
    private List listeners;
    private boolean listenersDisabled;

    /**
     * Creates a new DSC parser.
     * @param in InputStream to read the PostScript file from
     *              (the stream is not closed by this class, the caller is responsible for that)
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public DSCParser(InputStream in) throws IOException, DSCException {
        if (in.markSupported()) {
            this.in = in;
        } else {
            //Decorate for better performance
            this.in = new java.io.BufferedInputStream(this.in);
        }
        String encoding = "US-ASCII";
        try {
            this.reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(this.in, encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Incompatible VM! " + e.getMessage());
        }
        parseNext();
    }

    /**
     * Returns the InputStream the PostScript code is read from.
     * @return the InputStream the PostScript code is read from
     */
    public InputStream getInputStream() {
        return this.in;
    }

    /**
     * This method is used to write out warning messages for the parsing process. Subclass to
     * override this method. The default implementation writes to logger.
     * @param msg the warning message
     */
    protected void warn(String msg) {
        LOG.warn(msg);
    }

    /**
     * Reads one line from the input file
     * @return the line or null if there are no more lines
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    protected String readLine() throws IOException, DSCException {
        String line;
        line = this.reader.readLine();
        checkLine(line);

        return line;
    }

    private void checkLine(String line) throws DSCException {
        if (line == null) {
            if (!eofFound) {
                throw new DSCException("%%EOF not found. File is not well-formed.");
            }
        } else if (line.length() > 255) {
            warn("Line longer than 255 characters. This file is not fully PostScript conforming.");
        }
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    private DSCComment parseDSCLine(String line) throws IOException, DSCException {
        int colon = line.indexOf(':');
        String name;
        StringBuilder value = new StringBuilder();
        if (colon > 0) {
            name = line.substring(2, colon);
            int startOfValue = colon + 1;
            if (startOfValue < line.length()) {
                if (isWhitespace(line.charAt(startOfValue))) {
                    startOfValue++;
                }
                value = new StringBuilder(line.substring(startOfValue).trim());
                if (value.toString().equals(DSCConstants.ATEND.toString())) {
                    return new DSCAtend(name);
                }
            }
            String nextLine;
            while (true) {
                this.reader.mark(512);
                nextLine = readLine();
                if (nextLine == null) {
                    break;
                } else if (!nextLine.startsWith("%%+")) {
                    break;
                }
                value.append(nextLine.substring(3));
            }
            this.reader.reset();
        } else {
            name = line.substring(2);
            return parseDSCComment(name, null);
        }
        return parseDSCComment(name, value.toString());
    }

    private DSCComment parseDSCComment(String name, String value) {
        DSCComment parsed = DSCCommentFactory.createDSCCommentFor(name);
        if (parsed != null) {
            try {
                parsed.parseValue(value);
                return parsed;
            } catch (Exception e) {
                //ignore and fall back to unparsed DSC comment
            }
        }
        UnparsedDSCComment unparsed = new UnparsedDSCComment(name);
        unparsed.parseValue(value);
        return unparsed;
    }

    /**
     * Starts the parser in push parsing mode sending events to the DSCHandler instance.
     * @param handler the DSCHandler instance to send the events to
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public void parse(DSCHandler handler) throws IOException, DSCException {
        DSCHeaderComment header = DSCTools.checkAndSkipDSC30Header(this);
        handler.startDocument("%!" + header.getComment());
        DSCEvent event;
        while (hasNext()) {
            event = nextEvent();
            switch (event.getEventType()) {
            case HEADER_COMMENT:
                handler.startDocument("%!" + ((DSCHeaderComment)event).getComment());
                break;
            case DSC_COMMENT:
                handler.handleDSCComment(event.asDSCComment());
                break;
            case COMMENT:
                handler.comment(((PostScriptComment)event).getComment());
                break;
            case LINE:
                handler.line(getLine());
                break;
            case EOF:
                handler.endDocument();
                break;
            default:
                throw new IllegalStateException("Illegal event type: " + event.getEventType());
            }
        }
    }

    /**
     * Indicates whether there are additional items.
     * @return true if there are additonal items, false if the end of the file has been reached
     */
    public boolean hasNext() {
        return (this.nextEvent != null);
    }

    /**
     * Steps to the next item indicating the type of event.
     * @return the type of event (See {@link DSCParserConstants})
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     * @throws NoSuchElementException If an attempt was made to advance beyond the end of the file
     */
    public int next() throws IOException, DSCException {
        if (hasNext()) {
            this.currentEvent = nextEvent;
            parseNext();

            processListeners();

            return this.currentEvent.getEventType();
        } else {
            throw new NoSuchElementException("There are no more events");
        }
    }

    private void processListeners() throws IOException, DSCException {
        if (isListenersDisabled()) {
            return;
        }
        if (this.filterListener != null) {
            //Filter always comes first
            this.filterListener.processEvent(this.currentEvent, this);
        }
        if (this.listeners != null) {
            for (Object listener : this.listeners) {
                ((DSCListener) listener).processEvent(this.currentEvent, this);
            }
        }
    }

    /**
     * Steps to the next item returning the new event.
     * @return the new event
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public DSCEvent nextEvent() throws IOException, DSCException {
        next();
        return getCurrentEvent();
    }

    /**
     * Returns the current event.
     * @return the current event
     */
    public DSCEvent getCurrentEvent() {
        return this.currentEvent;
    }

    /**
     * Returns the next event without moving the cursor to the next event.
     * @return the next event
     */
    public DSCEvent peek() {
        return this.nextEvent;
    }

    /**
     * Parses the next event.
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    protected void parseNext() throws IOException, DSCException {
        String line = readLine();
        if (line != null) {
            if (isCheckEOF() && eofFound && (line.length() > 0)) {
                throw new DSCException("Content found after EOF");
            }
            if (line.startsWith("%%")) {
                DSCComment comment = parseDSCLine(line);
                if (comment.getEventType() == EOF) {
                    this.eofFound = true;
                }
                this.nextEvent = comment;
            } else if (line.startsWith("%!")) {
                this.nextEvent = new DSCHeaderComment(line.substring(2));
            } else if (line.startsWith("%")) {
                this.nextEvent = new PostScriptComment(line.substring(1));
            } else {
                this.nextEvent = new PostScriptLine(line);
            }
        } else {
            this.nextEvent = null;
        }
    }

    /**
     * Returns the current PostScript line.
     * @return the current PostScript line
     * @throws IllegalStateException if the current event is not a normal PostScript line
     */
    public String getLine() {
        if (this.currentEvent.getEventType() == LINE) {
            return ((PostScriptLine)this.currentEvent).getLine();
        } else {
            throw new IllegalStateException("Current event is not a PostScript line");
        }
    }

    /**
     * Advances to the next DSC comment with the given name.
     * @param name the name of the DSC comment
     * @return the requested DSC comment or null if the end of the file is reached
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public DSCComment nextDSCComment(String name)
                throws IOException, DSCException {
        return nextDSCComment(name, null);
    }

    /**
     * Advances to the next DSC comment with the given name.
     * @param name the name of the DSC comment
     * @param gen PSGenerator to pass the skipped events though to
     * @return the requested DSC comment or null if the end of the file is reached
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public DSCComment nextDSCComment(String name, PSGenerator gen)
                throws IOException, DSCException {
        while (hasNext()) {
            DSCEvent event = nextEvent();
            if (event.isDSCComment()) {
                DSCComment comment = event.asDSCComment();
                if (name.equals(comment.getName())) {
                    return comment;
                }
            }
            if (gen != null) {
                event.generate(gen); //Pipe through to PSGenerator
            }
        }
        return null;
    }

    /**
     * Advances to the next PostScript comment with the given prefix. This is used to find
     * comments following the DSC extension mechanism.
     * <p>
     * Example: To find FOP's custom comments, pass in "FOP" as a prefix. This will find comments
     * like "%FOPFontSetup".
     * @param prefix the prefix of the extension comment
     * @param gen PSGenerator to pass the skipped events though to
     * @return the requested PostScript comment or null if the end of the file is reached
     * @throws IOException In case of an I/O error
     * @throws DSCException In case of a violation of the DSC spec
     */
    public PostScriptComment nextPSComment(String prefix, PSGenerator gen)
                    throws IOException, DSCException {
            while (hasNext()) {
                DSCEvent event = nextEvent();
                if (event.isComment()) {
                    PostScriptComment comment = (PostScriptComment)event;
                    if (comment.getComment().startsWith(prefix)) {
                        return comment;
                    }
                }
                if (gen != null) {
                    event.generate(gen); //Pipe through to PSGenerator
                }
            }
            return null;
    }

    /**
     * Sets a filter for DSC events.
     * @param filter the filter to use or null to disable filtering
     */
    public void setFilter(DSCFilter filter) {
        if (filter != null) {
            this.filterListener = new FilteringEventListener(filter);
        } else {
            this.filterListener = null;
        }
    }

    /**
     * Adds a DSC event listener.
     * @param listener the listener
     */
    public void addListener(DSCListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        if (this.listeners == null) {
            this.listeners = new java.util.ArrayList();
        }
        this.listeners.add(listener);
    }

    /**
     * Removes a DSC event listener.
     * @param listener the listener to remove
     */
    public void removeListener(DSCListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Allows to disable all listeners. This can be used to disable any filtering, for example in
     * nested documents.
     * @param value true to disable all listeners, false to re-enable them
     */
    public void setListenersDisabled(boolean value) {
        this.listenersDisabled = value;
    }

    /**
     * Indicates whether the listeners are currently disabled.
     * @return true if they are disabled
     */
    public boolean isListenersDisabled() {
        return this.listenersDisabled;
    }

    /**
     * Sets a NestedDocumentHandler which is used to skip nested documents like embedded EPS files.
     * You can also process those parts in a special way.
     * <p>
     * It is suggested to use the more generally usable {@link #addListener(DSCListener)} and
     * {@link #removeListener(DSCListener)} instead. NestedDocumentHandler is internally
     * mapped onto a {@link DSCListener}.
     * @param handler the NestedDocumentHandler instance or null to disable the feature
     */
    public void setNestedDocumentHandler(final NestedDocumentHandler handler) {
        if (handler == null) {
            removeListener(this.nestedDocumentHandler);
        } else {
            MyDSCListener l = new MyDSCListener();
            l.handler = handler;
            addListener(l);
        }
    }

    static class MyDSCListener implements DSCListener {
        private NestedDocumentHandler handler;
        public void processEvent(DSCEvent event, DSCParser parser) throws IOException,
                DSCException {
            handler.handle(event, parser);
        }
    }

    /**
     * Tells the parser whether to check for content after the EOF comment.
     * This can be disabled to skip nested documents.
     * @param value true if the check is enabled
     */
    public void setCheckEOF(boolean value) {
        this.checkEOF = value;
    }

    /**
     * Indicates whether the parser is configured to check for content after the EOF comment.
     * @return true if the check is enabled.
     */
    public boolean isCheckEOF() {
        return this.checkEOF;
    }

}
