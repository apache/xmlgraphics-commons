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

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * Tests for AbstractImageSessionContext.
 */
public class ImageSessionContextTestCase {

    private MockImageContext imageContext = MockImageContext.getInstance();

    @Test
    public void testStreamSourceWithSystemID() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    return new StreamSource(base + filename);
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        ImageSource imgSrc = checkImageInputStreamAvailable(uri, resolver);
        assertTrue(imgSrc.isFastSource()); //Access through local file system
    }

    @Test
    public void testStreamSourceWithInputStream() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    try {
                        return new StreamSource(new java.io.FileInputStream(
                                new File(MockImageSessionContext.IMAGE_BASE_DIR, filename)));
                    } catch (FileNotFoundException e) {
                        throw new TransformerException(e);
                    }
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        ImageSource imgSrc = checkImageInputStreamAvailable(uri, resolver);
        //We don't pass in the URI, so no fast source is possible
        assertTrue(!imgSrc.isFastSource());
    }

    @Test
    public void testStreamSourceWithFile() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    File f = new File(MockImageSessionContext.IMAGE_BASE_DIR, filename);
                    return new StreamSource(f);
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        ImageSource imgSrc = checkImageInputStreamAvailable(uri, resolver);
        assertTrue(imgSrc.isFastSource()); //Accessed through the local file system
    }

    @Test
    public void testStreamSourceWithInputStreamAndSystemID() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    try {
                        File f = new File(MockImageSessionContext.IMAGE_BASE_DIR, filename);
                        return new StreamSource(
                                new java.io.FileInputStream(f),
                                f.toURI().toASCIIString());
                    } catch (FileNotFoundException e) {
                        throw new TransformerException(e);
                    }
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        ImageSource imgSrc = checkImageInputStreamAvailable(uri, resolver);
        assertTrue(imgSrc.isFastSource()); //Access through local file system (thanks to the URI
                                           //being passed through by the URIResolver)
    }

    @Test
    public void testStreamSourceWithReader() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    return new StreamSource(new java.io.StringReader(filename));
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        Source src = resolve(uri, resolver);
        assertTrue(src instanceof StreamSource); //Source remains unchanged
        assertTrue(ImageUtil.hasReader(src));
    }

    private ImageSource checkImageInputStreamAvailable(String uri, URIResolver resolver) {
        Source src = resolve(uri, resolver);
        assertNotNull("Source must not be null", src);
        assertTrue("Source must be an ImageSource", src instanceof ImageSource);
        ImageSource imgSrc = (ImageSource) src;
        assertTrue(ImageUtil.hasImageInputStream(imgSrc));
        return imgSrc;
    }

    private Source resolve(String uri, URIResolver resolver) {
        ImageSessionContext sessionContext = new SimpleURIResolverBasedImageSessionContext(
                imageContext, MockImageSessionContext.IMAGE_BASE_DIR, resolver);
        Source src = sessionContext.newSource(uri);
        return src;
    }

    @Test
    public void testSAXSourceWithSystemID() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    InputSource is = new InputSource(base + filename);
                    return new SAXSource(is);
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        ImageSource imgSrc = checkImageInputStreamAvailable(uri, resolver);
        assertTrue(imgSrc.isFastSource());
    }

    @Test
    public void testSAXSourceWithInputStream() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    InputSource is;
                    try {
                        is = new InputSource(new java.io.FileInputStream(
                                new File(MockImageSessionContext.IMAGE_BASE_DIR, filename)));
                    } catch (FileNotFoundException e) {
                        throw new TransformerException(e);
                    }
                    return new SAXSource(is);
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        checkImageInputStreamAvailable(uri, resolver);
    }

    @Test
    public void testSAXSourceWithReader() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("img:")) {
                    String filename = href.substring(4);
                    InputSource is;
                    is = new InputSource(new java.io.StringReader(filename));
                    return new SAXSource(is);
                } else {
                    return null;
                }
            }
        };
        String uri = "img:asf-logo.png";

        Source src = resolve(uri, resolver);
        assertTrue(src instanceof SAXSource); //Source remains unchanged
        assertTrue(ImageUtil.hasReader(src));
    }

    private static final String SOME_XML = "<root><child id='1'>Hello World!</child></root>";

    @Test
    public void testSAXSourceWithXMLReader() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("xml:")) {
                    String xml = href.substring(4);
                    InputSource is = new InputSource(new java.io.StringReader(xml));
                    return new SAXSource(createSomeXMLReader(), is);
                } else {
                    return null;
                }
            }
        };
        String uri = "xml:" + SOME_XML;

        Source src = resolve(uri, resolver);
        assertTrue(src instanceof SAXSource); //Source remains unchanged
        SAXSource saxSrc = (SAXSource) src;
        assertNotNull(saxSrc.getXMLReader());
        assertNotNull(saxSrc.getInputSource());
    }

    @Test
    public void testDOMSource() throws Exception {
        URIResolver resolver = new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("xml:")) {
                    String xml = href.substring(4);
                    InputSource is = new InputSource(new java.io.StringReader(xml));
                    SAXSource sax = new SAXSource(createSomeXMLReader(), is);

                    //Convert SAXSource to DOMSource
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();
                    DOMResult res = new DOMResult();
                    transformer.transform(sax, res);
                    return new DOMSource(res.getNode());
                } else {
                    return null;
                }
            }
        };
        String uri = "xml:" + SOME_XML;

        Source src = resolve(uri, resolver);
        assertTrue(src instanceof DOMSource); //Source remains unchanged
        DOMSource domSrc = (DOMSource) src;
        assertNotNull(domSrc.getNode());
    }

    private XMLReader createSomeXMLReader() {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
            return parser.getXMLReader();
        } catch (Exception e) {
            fail("Could not create XMLReader");
            return null;
        }
    }

}
