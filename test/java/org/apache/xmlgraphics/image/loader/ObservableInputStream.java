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
 
package org.apache.xmlgraphics.image.loader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a proxying input stream that records whether a stream has been closed or not.
 */
public class ObservableInputStream extends FilterInputStream implements ObservableStream {

    /** logger */
    protected static Log log = LogFactory.getLog(ObservableInputStream.class);
    
    private boolean closed;
    private String systemID;
    
    /**
     * Main constructor.
     * @param in the underlying input stream
     * @param systemID the system ID for the input stream for reference
     */
    public ObservableInputStream(InputStream in, String systemID) {
        super(in);
        this.systemID = systemID;
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        if (!closed) {
            log.debug("Stream is being closed: " + getSystemID());
            try {
                this.in.close();
            } catch (IOException ioe) {
                log.error("Error while closing underlying stream: " + ioe.getMessage());
            }
            closed = true;
        } else {
            throw new IllegalStateException("Stream is already closed!");
        }
    }
    
    /** {@inheritDoc} */
    public boolean isClosed() {
        return this.closed;
    }
    
    /** {@inheritDoc} */
    public String getSystemID() {
        return this.systemID;
    }

}
