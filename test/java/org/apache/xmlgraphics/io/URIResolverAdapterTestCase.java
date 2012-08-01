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
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;

import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;

public class URIResolverAdapterTestCase {

    private final URI textFileURI = URI.create("test:catalog:resolver:testResource.txt");

    @Test
    public void testCatalogResolver() throws TransformerException, IOException {
        System.setProperty("xml.catalog.files",
                "test/resources/org/apache/xmlgraphics/io/test-catalog.xml");
        CatalogResolver catalogResolver = new CatalogResolver();
        Source src = catalogResolver.resolve(textFileURI.toASCIIString(), null);
        if (src instanceof SAXSource) {
            testInputStream(new URL(src.getSystemId()).openStream());
        }
    }

    @Test
    public void testCatalogResolverInAdapter() throws IOException {
        System.setProperty("xml.catalog.files",
                "test/resources/org/apache/xmlgraphics/io/test-catalog.xml");
        ResourceResolver resourceResolver = new URIResolverAdapter(new CatalogResolver(), null);
        testInputStream(resourceResolver.getResource(textFileURI));
    }

    private void testInputStream(InputStream stream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        assertEquals("This is a text file used to test the CatalogResolver\n", writer.toString());
    }
}
