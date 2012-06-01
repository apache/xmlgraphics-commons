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

package org.apache.xmlgraphics.ps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class ImageEncodingHelperTestCase extends TestCase {

    private BufferedImage prepareImage(BufferedImage image) {
        Graphics2D ig = image.createGraphics();
        ig.scale(.5, .5);
        ig.setPaint(new Color(128, 0, 0));
        ig.fillRect(0, 0, 100, 50);
        ig.setPaint(Color.orange);
        ig.fillRect(100, 0, 100, 50);
        ig.setPaint(Color.yellow);
        ig.fillRect(0, 50, 100, 50);
        ig.setPaint(Color.red);
        ig.fillRect(100, 50, 100, 50);
        ig.setPaint(new Color(255, 127, 127));
        ig.fillRect(0, 100, 100, 50);
        ig.setPaint(Color.black);
        ig.draw(new Rectangle2D.Double(0.5, 0.5, 199, 149));
        ig.dispose();
        return image;
    }

    /**
     * Tests a BGR versus RBG image. Debugging shows the BGR follows the optimizeWriteTo() (which is intended).
     * The bytes are compared with the RBG image, which happens to follow the writeRGBTo().
     * 
     * @throws IOException
     */
    public void testRGBAndBGRImages() throws IOException {
        BufferedImage imageBGR = new BufferedImage(100, 75, BufferedImage.TYPE_3BYTE_BGR);
        imageBGR = prepareImage(imageBGR);
        BufferedImage imageRGB = new BufferedImage(100, 75, BufferedImage.TYPE_INT_BGR);
        imageRGB = prepareImage(imageRGB);

        ImageEncodingHelper imageEncodingHelperBGR = new ImageEncodingHelper(imageBGR);
        ImageEncodingHelper imageEncodingHelperRGB = new ImageEncodingHelper(imageRGB);

        ByteArrayOutputStream baosBGR = new ByteArrayOutputStream();
        imageEncodingHelperBGR.encode(baosBGR);

        ByteArrayOutputStream baosRGB = new ByteArrayOutputStream();
        imageEncodingHelperRGB.encode(baosRGB);

        assertTrue(Arrays.equals(baosBGR.toByteArray(), baosRGB.toByteArray()));
    }

}
