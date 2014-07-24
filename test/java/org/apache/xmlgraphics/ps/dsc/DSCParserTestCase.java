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

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class DSCParserTestCase {

    private final String correctDSC
            = "%!PS-Adobe-3.0\n"
            + "%%LanguageLevel: 3\n"
            + "%%EOF";

    private final String spuriousContentAfterEOF
            = "%!PS-Adobe-3.0\n"
            + "%%LanguageLevel: 3\n"
            + "%%EOF\n"
            + "%%SpuriousContent";

    @Test
    public void eofDetectedWhenCheckEOFEnabled() throws Exception {
        parseDSC(correctDSC, true);
    }

    @Test
    public void eofDetectedWhenCheckEOFDisabled() throws Exception {
        parseDSC(correctDSC, false);
    }

    @Test(expected = DSCException.class)
    public void spuriousContentDetected() throws Exception {
        parseDSC(spuriousContentAfterEOF, true);
    }

    @Test
    public void spuriousContentIgnored() throws Exception {
        parseDSC(spuriousContentAfterEOF, false);
    }

    private void parseDSC(String dsc, boolean checkEOF) throws Exception {
        DSCParser parser = new DSCParser(new ByteArrayInputStream(dsc.getBytes("US-ASCII")));
        parser.setCheckEOF(checkEOF);
        while (parser.hasNext()) {
            parser.next();
        }
    }

}
