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

package org.apache.xmlgraphics.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.ImageSource;
import org.apache.xmlgraphics.image.loader.util.ImageInputStreamAdapter;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * A utility class for handling {@link Source} objects, more specficially the streams that back
 * the {@link Source}.
 */
public final class XmlSourceUtil {

    private XmlSourceUtil() {
    }

    /**
     * Returns the {@link InputStream} that is backing the given {@link Source} object.
     *
     * @param src is backed by an {@link InputStream}
     * @return the input stream
     */
    public static InputStream getInputStream(Source src) {
        try {
            if (src instanceof StreamSource) {
                return ((StreamSource) src).getInputStream();
            } else if (src instanceof DOMSource) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                StreamResult xmlSource = new StreamResult(outStream);
                TransformerFactory.newInstance().newTransformer().transform(src, xmlSource);
                return new ByteArrayInputStream(outStream.toByteArray());
            } else if (src instanceof SAXSource) {
                return ((SAXSource) src).getInputSource().getByteStream();
            } else if (src instanceof ImageSource) {
                return new ImageInputStreamAdapter(((ImageSource) src).getImageInputStream());
            }
        } catch (Exception e) {
            // TODO: How do we want to handle these? They all come from the TransformerFactory
        }
        return null;
    }

    /**
     * Returns the InputStream of a Source object. This method throws an IllegalArgumentException
     * if there's no InputStream instance available from the Source object.
     * @param src the Source object
     * @return the InputStream
     */
    public static InputStream needInputStream(Source src) {
        InputStream in = getInputStream(src);
        if (in != null) {
            return in;
        } else {
            throw new IllegalArgumentException("Source must be a StreamSource with an InputStream"
                    + " or an ImageSource");
        }
    }

    /**
     * Indicates whether the Source object has a Reader instance.
     * @param src the Source object
     * @return true if an Reader is available
     */
    public static boolean hasReader(Source src) {
        if (src instanceof StreamSource) {
            Reader reader = ((StreamSource) src).getReader();
            return (reader != null);
        } else if (src instanceof SAXSource) {
            InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                return (is.getCharacterStream() != null);
            }
        }
        return false;
    }

    /**
     * Removes any references to InputStreams or Readers from the given Source to prohibit
     * accidental/unwanted use by a component further downstream.
     * @param src the Source object
     */
    public static void removeStreams(Source src) {
        if (src instanceof ImageSource) {
            ImageSource isrc = (ImageSource) src;
            isrc.setImageInputStream(null);
        } else if (src instanceof StreamSource) {
            StreamSource ssrc = (StreamSource) src;
            ssrc.setInputStream(null);
            ssrc.setReader(null);
        } else if (src instanceof SAXSource) {
            InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                is.setByteStream(null);
                is.setCharacterStream(null);
            }
        }
    }

    /**
     * Closes the InputStreams or ImageInputStreams of Source objects. Any exception occurring
     * while closing the stream is ignored.
     * @param src the Source object
     */
    public static void closeQuietly(Source src) {
        if (src instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) src;
            IOUtils.closeQuietly(streamSource.getReader());
        } else if (src instanceof ImageSource) {
            if (ImageUtil.getImageInputStream(src) != null) {
                try {
                    ImageUtil.getImageInputStream(src).close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } else if (src instanceof SAXSource) {
            InputSource is = ((SAXSource) src).getInputSource();
            if (is != null) {
                IOUtils.closeQuietly(is.getByteStream());
                IOUtils.closeQuietly(is.getCharacterStream());
            }
        }
        removeStreams(src);
    }

    /**
     * Indicates whether the Source object has an InputStream instance.
     * @param src the Source object
     * @return true if an InputStream is available
     */
    public static boolean hasInputStream(Source src) {
        return getInputStream(src) != null;
    }
}
