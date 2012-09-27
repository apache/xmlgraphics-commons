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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.ImageSource;

import static org.apache.xmlgraphics.io.XmlSourceUtil.closeQuietly;
import static org.apache.xmlgraphics.io.XmlSourceUtil.getInputStream;
import static org.apache.xmlgraphics.io.XmlSourceUtil.hasInputStream;
import static org.apache.xmlgraphics.io.XmlSourceUtil.hasReader;
import static org.apache.xmlgraphics.io.XmlSourceUtil.needInputStream;
import static org.apache.xmlgraphics.io.XmlSourceUtil.removeStreams;

public class XmlSourceUtilTestCase {

    private StreamSource streamSource;
    private SAXSource saxSource;
    private InputSource inputSource;
    private ImageSource imgSource;
    private ImageInputStream imgInStream;
    private DOMSource domSource;
    private StringWriter writer;
    private InputStream testStream;
    private Reader reader;

    @Before
    public void setUp() throws IOException, ParserConfigurationException {
        testStream = mock(InputStream.class);
        reader = mock(Reader.class);

        streamSource = mock(StreamSource.class);
        when(streamSource.getInputStream()).thenReturn(testStream);
        when(streamSource.getReader()).thenReturn(reader);

        saxSource = mock(SAXSource.class);
        inputSource = mock(InputSource.class);
        when(saxSource.getInputSource()).thenReturn(inputSource);
        when(inputSource.getByteStream()).thenReturn(testStream);
        when(inputSource.getCharacterStream()).thenReturn(reader);

        imgSource = mock(ImageSource.class);
        imgInStream = mock(ImageInputStream.class);
        when(imgSource.getImageInputStream()).thenReturn(imgInStream);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        domSource = new DOMSource(db.newDocument().createElement("test"));
        InputStream inStream = XmlSourceUtil.getInputStream(domSource);
        writer = new StringWriter();
        IOUtils.copy(inStream, writer);
    }

    @Test
    public void testGetInputStream() throws ParserConfigurationException, IOException {
        getInputStream(streamSource);
        verify(streamSource).getInputStream();

        getInputStream(saxSource);
        verify(inputSource).getByteStream();

        getInputStream(imgSource);
        verify(imgSource).getImageInputStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>", writer.toString());

        // Test negative case
        Source src = mock(Source.class);
        assertNull(getInputStream(src));

        getInputStream(null);
    }

    @Test
    public void testNeedInputStream() throws IOException, ParserConfigurationException {
        assertEquals(testStream, needInputStream(streamSource));

        assertEquals(testStream, needInputStream(saxSource));

        needInputStream(imgSource);
        verify(imgSource).getImageInputStream();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>", writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNeedInputStreamFailureCaseSource() {
        Source src = mock(Source.class);
        needInputStream(src);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNeedInputStreamFailureCaseStreamSource() {
        needInputStream(mock(StreamSource.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNeedInputStreamFailureCaseSAXSource() {
        needInputStream(mock(SAXSource.class));
    }

    public void testNeedInputStreamFailureCaseDOMSource() throws IOException {
        InputStream inStream = needInputStream(new DOMSource());
        StringWriter writer = new StringWriter();
        IOUtils.copy(inStream, writer);
        assertEquals("", writer.toString());
    }

    @Test(expected = AssertionError.class)
    public void testNeedInputStreamFailureCaseStreamImage() {
        needInputStream(mock(ImageSource.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNeedInputStreamFailureCaseNullArg() {
        needInputStream(null);
    }

    @Test
    public void testHasReader() {
        assertTrue(hasReader(streamSource));

        assertTrue(hasReader(saxSource));

        when(streamSource.getReader()).thenReturn(null);
        when(inputSource.getCharacterStream()).thenReturn(null);

        assertFalse(hasReader(streamSource));
        assertFalse(hasReader(saxSource));
        assertFalse(hasReader(imgSource));
        assertFalse(hasReader(domSource));

        hasReader(null);
    }

    @Test
    public void testRemoveStreams() {
        removeStreams(streamSource);
        verify(streamSource).setInputStream(null);
        verify(streamSource).setReader(null);

        removeStreams(imgSource);
        verify(imgSource).setImageInputStream(null);

        removeStreams(saxSource);
        verify(inputSource).setByteStream(null);
        verify(inputSource).setCharacterStream(null);

        removeStreams(null);
    }

    @Test
    public void testCloseQuietlyStreamSource() throws IOException {
        closeQuietly(streamSource);
        verify(reader).close();
        verify(streamSource).setInputStream(null);
        verify(streamSource).setReader(null);
    }

    @Test
    public void testCloseQuietlySaxSource() throws IOException {
        closeQuietly(saxSource);
        verify(testStream).close();
        verify(reader).close();
        verify(inputSource).setByteStream(null);
        verify(inputSource).setCharacterStream(null);
    }

    @Test
    public void testCloseQuietlyImageSource() throws IOException {
        closeQuietly(imgSource);
        verify(imgInStream).close();
        verify(imgSource).setImageInputStream(null);
    }

    @Test
    public void testCloseQuietlyNull() {
        XmlSourceUtil.closeQuietly(null);
    }

    @Test
    public void testHasInputStream() {
        assertTrue(hasInputStream(streamSource));
        assertTrue(hasInputStream(saxSource));
        assertTrue(hasInputStream(imgSource));
        assertTrue(hasInputStream(domSource));

        assertFalse(hasInputStream(mock(StreamSource.class)));
        assertFalse(hasInputStream(mock(SAXSource.class)));
        // Can't do the ImageSource test because of an assert, do we want that assert there?
        // assertFalse(hasInputStream(mock(ImageSource.class)));
        assertFalse(hasInputStream(mock(StreamSource.class)));
    }
}
