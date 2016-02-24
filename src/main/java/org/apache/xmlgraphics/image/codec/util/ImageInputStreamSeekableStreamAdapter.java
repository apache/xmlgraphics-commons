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

package org.apache.xmlgraphics.image.codec.util;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/**
 * A subclass of <code>SeekableStream</code> that may be used to wrap
 * a regular <code>ImageInputStream</code>.
 */
public class ImageInputStreamSeekableStreamAdapter extends SeekableStream {

    /** The source stream. */
    private ImageInputStream stream;

    /**
     * Constructs a <code>SeekableStream</code> that takes
     * its source data from a regular <code>ImageInputStream</code>.
     * @param stream the underlying ImageInputStream to use
     */
    public ImageInputStreamSeekableStreamAdapter(ImageInputStream stream)
        throws IOException {
        this.stream = stream;
    }

    /** {@inheritDoc} */
    public boolean canSeekBackwards() {
        return true;
    }

    /** {@inheritDoc} */
    public long getFilePointer() throws IOException {
        return stream.getStreamPosition();
    }

    /** {@inheritDoc} */
    public void seek(long pos) throws IOException {
        stream.seek(pos);
    }

    /** {@inheritDoc} */
    public int read() throws IOException {
        return stream.read();
    }

    /** {@inheritDoc} */
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    /** {@inheritDoc} */
    public void close() throws IOException {
        super.close();
        stream.close();
    }
}
