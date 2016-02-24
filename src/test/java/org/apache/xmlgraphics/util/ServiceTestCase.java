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

package org.apache.xmlgraphics.util;

import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.image.writer.ImageWriter;

/**
 * Test for the Service class.
 */
public class ServiceTestCase {

    /**
     * Tests the mode where Service returns instances.
     * @throws Exception in case of an error
     */
    @Test
    public void testWithInstances() throws Exception {
        Class cls = ImageWriter.class;
        boolean found = false;
        Object writer1 = null;
        Object writer2 = null;

        //First run: Find a writer implementation (one of the two must be available)
        Iterator iter = Service.providers(cls);
        while (iter.hasNext()) {
            Object obj = iter.next();
            assertNotNull(obj);
            String className = obj.getClass().getName();
            if ("org.apache.xmlgraphics.image.writer.internal.PNGImageWriter".equals(className)) {
                writer1 = obj;
                found = true;
                break;
            } else if ("org.apache.xmlgraphics.image.writer.imageio.ImageIOPNGImageWriter".equals(
                    className)) {
                writer2 = obj;
                found = true;
                break;
            }
        }
        assertTrue("None of the expected classes found", found);

        //Second run: verify that the same instances are returned
        iter = Service.providers(cls);
        while (iter.hasNext()) {
            Object obj = iter.next();
            assertNotNull(obj);
            String className = obj.getClass().getName();
            if ("org.apache.xmlgraphics.image.writer.internal.PNGImageWriter".equals(className)) {
                assertTrue(obj == writer1);
                break;
            } else if ("org.apache.xmlgraphics.image.writer.imageio.ImageIOPNGImageWriter".equals(
                    className)) {
                assertTrue(obj == writer2);
                break;
            }
        }
    }

    /**
     * Tests the mode where Service returns class names.
     * @throws Exception in case of an error
     */
    @Test
    public void testWithClassNames() throws Exception {
        Class cls = ImageWriter.class;
        boolean found = true;
        Iterator iter = Service.providerNames(cls);
        while (iter.hasNext()) {
            Object obj = iter.next();
            assertNotNull(obj);
            assertTrue("Returned object must be a class name", obj instanceof String);
            if ("org.apache.xmlgraphics.image.writer.internal.PNGImageWriter".equals(obj)
                    || ("org.apache.xmlgraphics.image.writer.imageio.ImageIOPNGImageWriter".equals(
                                obj))) {
                found = true;
            }
        }
        assertTrue("None of the expected classes found", found);

        //Do it a second time to make sure the cache works as expected
        iter = Service.providerNames(cls);
        while (iter.hasNext()) {
            Object obj = iter.next();
            assertNotNull(obj);
            assertTrue("Returned object must be a class name", obj instanceof String);
        }
    }
}
