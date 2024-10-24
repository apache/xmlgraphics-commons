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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * Tests for the ImageUtil class.
 */
public class ImageUtilTestCase {

    /**
     * Tests {@link ImageUtil.needPageIndexFromURI(String)}.
     */
    @Test
    public void testNeedPageIndex() {
        int pageIndex;

        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif");
        assertEquals(0, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif#page=3");
        assertEquals(2, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif#page=0");
        assertEquals(0, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif#page=");
        assertEquals(0, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif#page=x");
        assertEquals(0, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("http://localhost/images/scan1.tif#page=-1");
        assertEquals(0, pageIndex);
        pageIndex = ImageUtil.needPageIndexFromURI("#page=2");
        assertEquals(1, pageIndex);

        //Not a valid URI
        pageIndex = ImageUtil.needPageIndexFromURI("C:\\images\\scan1.tif#page=44");
        assertEquals(43, pageIndex);

        //Valid URI
        pageIndex = ImageUtil.needPageIndexFromURI("file:///C:/images/scan1.tif#page=44");
        assertEquals(43, pageIndex);

        pageIndex = ImageUtil.needPageIndexFromURI(
                "Balesetbiztosítás_ kötvénycsomag - e-mail_3000000637_Biztosítási kötvény melléklettel.pdf#page=1");
        assertEquals(0, pageIndex);
    }

    /**
     * Tests {@link ImageUtil.getPageIndexFromURI(String)}.
     */
    @Test
    public void testGetPageIndex() {
        Integer pageIndex;

        pageIndex = ImageUtil.getPageIndexFromURI("http://localhost/images/scan1.tif");
        assertNull(pageIndex);
        pageIndex = ImageUtil.getPageIndexFromURI("http://localhost/images/scan1.tif#page=3");
        assertEquals(2, pageIndex.intValue());
        //Note: no detailed test anymore as this is tested through needPageIndexFromURI().

        //getPageIndexFromURI only works on URIs, so ignore anything that doesn't have a '#'
        pageIndex = ImageUtil.getPageIndexFromURI("C:\\Temp\\scan1.tif");
        assertNull(pageIndex);
    }

}
