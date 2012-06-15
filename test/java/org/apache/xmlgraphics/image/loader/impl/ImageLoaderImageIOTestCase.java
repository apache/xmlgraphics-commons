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

package org.apache.xmlgraphics.image.loader.impl;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.MockImageContext;
import org.apache.xmlgraphics.image.loader.MockImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.imageio.ImageLoaderImageIO;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Tests for {@link ImageLoaderImageIO}.
 */
public class ImageLoaderImageIOTestCase {

    /**
     * Tests a grayscale PNG that has a CMYK color profile. ImageLoaderImageIO used
     * to fail on that with an IllegalArgumentException.
     * @throws Exception if an error occurs
     */
    @Test
    public void testGrayPNGWithCMYKProfile() throws Exception {
        URL imageURL = getClass().getResource("gray-vs-cmyk-profile.png");
        Assert.assertNotNull(imageURL);
        String uri = imageURL.toURI().toASCIIString();

        ImageLoaderImageIO loader = new ImageLoaderImageIO(ImageFlavor.RENDERED_IMAGE);
        ImageContext context = MockImageContext.newSafeInstance();
        ImageSessionContext session = new MockImageSessionContext(context);
        ImageInfo info = new ImageInfo(uri, MimeConstants.MIME_PNG);
        Image im = loader.loadImage(info, null, session);
        assertTrue(im instanceof ImageRendered);
    }

}
