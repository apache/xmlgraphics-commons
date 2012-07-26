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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.apache.xmlgraphics.image.codec.png.PNGEncoderTestCase;
import org.apache.xmlgraphics.ps.ImageEncodingHelperTestCase;
import org.apache.xmlgraphics.ps.PSEscapeTestCase;
import org.apache.xmlgraphics.ps.dsc.ListenerTestCase;
import org.apache.xmlgraphics.ps.dsc.events.DSCValueParserTestCase;
import org.apache.xmlgraphics.ps.dsc.tools.DSCToolsTestCase;
import org.apache.xmlgraphics.util.ClasspathResourceTestCase;
import org.apache.xmlgraphics.util.ServiceTestCase;
import org.apache.xmlgraphics.util.UnitConvTestCase;
import org.apache.xmlgraphics.util.io.ASCII85InputStreamTestCase;
import org.apache.xmlgraphics.util.io.ASCII85OutputStreamTestCase;
import org.apache.xmlgraphics.util.io.Base64TestCase;

/**
 * Test suite for basic functionality of XML Graphics Commons.
 */
@RunWith(Suite.class)
@SuiteClasses({
        Base64TestCase.class,
        ASCII85InputStreamTestCase.class,
        ASCII85OutputStreamTestCase.class,
        PNGEncoderTestCase.class,
        ServiceTestCase.class,
        ClasspathResourceTestCase.class,
        PSEscapeTestCase.class,
        ImageEncodingHelperTestCase.class,
        DSCValueParserTestCase.class,
        DSCToolsTestCase.class,
        ListenerTestCase.class,
        UnitConvTestCase.class
})
public class StandardTestSuite {
}
