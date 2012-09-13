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

package org.apache.xmlgraphics.image.writer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.util.UnitConv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResolutionTestCase {

    @Test
    public void testResolution() throws IOException {
        File testDir = new File("./build/test/results");
        testDir.mkdirs();
        runChecksForFormat(testDir, "image/png", "png");
        runChecksForFormat(testDir, "image/jpeg", "jpg");
        /* TODO this test passed with jai_imagio.jar on the classpath.
         * Should this become a compile time dependency of XGC? */
        //runChecksForFormat(testDir, "image/tiff", "tif");
    }

    private void runChecksForFormat(File testDir, String format, String ext)
            throws FileNotFoundException, IOException {
        File testFile;
        ImageWriterParams params1 = new ImageWriterParams();
        params1.setResolution(300);

        ImageWriterParams params2 = new ImageWriterParams();
        params2.setResolutionUnit(ResolutionUnit.CENTIMETER);
        params2.setXResolution(118); //~300dpi
        params2.setYResolution(79); //~200dpi

        testFile = new File(testDir, "ResolutionTest1." + ext);
        writeImage(params1, testFile, format);
        checkStdMetadata(testFile, UnitConv.IN2MM / 300f, UnitConv.IN2MM / 300f);

        testFile = new File(testDir, "ResolutionTest2." + ext);
        writeImage(params2, testFile, format);
        checkStdMetadata(testFile, 10f / 118f, 10f / 79f);
    }

    private void writeImage(ImageWriterParams params, File testFile, String mime) throws FileNotFoundException,
            IOException {
        BufferedImage img = createTestImage();
        ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(mime);
        assertNotNull(writer);
        OutputStream out = new java.io.FileOutputStream(testFile);
        try {
            writer.writeImage(img, out, params);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void checkStdMetadata(File testFile, float xRes, float yRes) throws IOException {
        ImageInputStream in = ImageIO.createImageInputStream(testFile);
        try {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(in);
            assertTrue(iter.hasNext());
            ImageReader reader = iter.next();
            reader.setInput(in);
            IIOMetadata iiometa = reader.getImageMetadata(0);
            assertNotNull(iiometa);
            assertTrue(iiometa.isStandardMetadataFormatSupported());
            Element metanode = (Element)iiometa.getAsTree(
                    IIOMetadataFormatImpl.standardMetadataFormatName);
            Element dim = getChild(metanode, "Dimension");
            float resHorz = 0.0f;
            float resVert = 0.0f;
            if (dim != null) {
                Element child;
                child = getChild(dim, "HorizontalPixelSize");
                if (child != null) {
                    resHorz = Float.parseFloat(child.getAttribute("value"));
                }
                child = getChild(dim, "VerticalPixelSize");
                if (child != null) {
                    resVert = Float.parseFloat(child.getAttribute("value"));
                }
            }
            assertEquals(xRes, resHorz, 0.000001f);
            assertEquals(yRes, resVert, 0.000001f);
        } finally {
            in.close();
        }
    }

    private static Element getChild(Element el, String name) {
        NodeList nodes = el.getElementsByTagName(name);
        if (nodes.getLength() > 0) {
            return (Element)nodes.item(0);
        } else {
            return null;
        }
    }

    private BufferedImage createTestImage() {
        BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = img.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, img.getWidth(), img.getHeight());
        g2d.setColor(Color.RED);
        g2d.fillOval(120, 80, 40, 40);
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(150, 120, 60, 50);
        g2d.dispose();
        return img;
    }

}
