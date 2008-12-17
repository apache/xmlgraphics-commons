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

package org.apache.xmlgraphics;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.xmlgraphics.image.codec.png.PNGEncoderTest;
import org.apache.xmlgraphics.ps.PSEscapeTestCase;
import org.apache.xmlgraphics.ps.dsc.ListenerTestCase;
import org.apache.xmlgraphics.ps.dsc.events.DSCValueParserTestCase;
import org.apache.xmlgraphics.ps.dsc.tools.DSCToolsTestCase;
import org.apache.xmlgraphics.util.ClasspathResourceTest;
import org.apache.xmlgraphics.util.ServiceTest;
import org.apache.xmlgraphics.util.UnitConvTestCase;
import org.apache.xmlgraphics.util.io.ASCII85InputStreamTestCase;
import org.apache.xmlgraphics.util.io.ASCII85OutputStreamTestCase;
import org.apache.xmlgraphics.util.io.Base64Test;

/**
 * Test suite for basic functionality of XML Graphics Commons.
 */
public class StandardTestSuite {

    /**
     * Builds the test suite
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Basic functionality test suite for XML Graphics Commons");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(Base64Test.class));
        suite.addTest(new TestSuite(ASCII85InputStreamTestCase.class));
        suite.addTest(new TestSuite(ASCII85OutputStreamTestCase.class));
        suite.addTest(new TestSuite(PNGEncoderTest.class));
        suite.addTest(new TestSuite(ServiceTest.class));
        suite.addTest(new TestSuite(ClasspathResourceTest.class));
        suite.addTest(new TestSuite(PSEscapeTestCase.class));
        suite.addTest(new TestSuite(DSCValueParserTestCase.class));
        suite.addTest(new TestSuite(DSCToolsTestCase.class));
        suite.addTest(new TestSuite(ListenerTestCase.class));
        suite.addTest(new TestSuite(UnitConvTestCase.class));
        //$JUnit-END$
        return suite;
    }
}
