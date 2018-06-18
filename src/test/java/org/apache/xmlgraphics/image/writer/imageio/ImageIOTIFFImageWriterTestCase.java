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

package org.apache.xmlgraphics.image.writer.imageio;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Node;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.writer.Endianness;
import org.apache.xmlgraphics.image.writer.ImageIOCheckUtility;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;
import org.apache.xmlgraphics.image.writer.ResolutionUnit;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * Tests for {@link ImageIOTIFFImageWriter}.
 */
public class ImageIOTIFFImageWriterTestCase {

    /**
     * Checks endianness when writing multi-page TIFF.
     * @throws Exception if an error occurs
     */
    @Test
    public void testEndianess() throws Exception {
        runEndiannessTest(new TestImageWriter(), 300);
        runEndiannessTest(new TestMultiImageWriter(), 96);
    }

    private void runEndiannessTest(ImageWriterHelper imageWriterHelper, int resolution) throws Exception {
        if (!ImageIOCheckUtility.isSunTIFFImageWriterAvailable()) {
            System.out.println("Skipping endianness test for ImageIO-based TIFF writer"
                    + " because JAI ImageIO Tools is not available!");
            return; //No JAI ImageIO TIFF codec available: skipping test
        }

        BufferedImage image = createTestImage(resolution);

        ImageWriterParams params = new ImageWriterParams();
        params.setCompressionMethod("CCITT T.6");
        params.setResolution(resolution);
        params.setSingleStrip(true);
        params.setEndianness(Endianness.LITTLE_ENDIAN);
        params.setResolutionUnit(ResolutionUnit.INCH);

        ImageWriter writer = new ImageIOTIFFImageWriter();
        imageWriterHelper.createImageWriter(writer);
        imageWriterHelper.writeImage(image, params);
        byte[] tiffData = imageWriterHelper.getByteArrayOutput().toByteArray();
        Assert.assertEquals('I', tiffData[0]);
        Assert.assertEquals('I', tiffData[1]);

        //Switch to big endian
        params.setEndianness(Endianness.BIG_ENDIAN);
        imageWriterHelper.createImageWriter(writer);
        imageWriterHelper.writeImage(image, params);
        tiffData = imageWriterHelper.getByteArrayOutput().toByteArray();
        Assert.assertEquals('M', tiffData[0]);
        Assert.assertEquals('M', tiffData[1]);

        //Test with no params (TIFF codec defaults to big endian)
        imageWriterHelper.createImageWriter(writer);
        imageWriterHelper.writeImageNoParams(image);
        if (imageWriterHelper.getByteArrayOutput().size() > 0) {
            tiffData = imageWriterHelper.getByteArrayOutput().toByteArray();
            Assert.assertEquals('M', tiffData[0]);
            Assert.assertEquals('M', tiffData[1]);
        }
    }

    private BufferedImage createTestImage(int dpi) {
        ImageSize size = new ImageSize();
        size.setSizeInMillipoints((int)UnitConv.mm2mpt(210), (int)UnitConv.mm2mpt(297));
        size.setResolution(dpi);
        size.calcPixelsFromSize();
        int w = size.getWidthPx();
        int h = size.getHeightPx();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.setColor(Color.BLACK);
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setFont(new Font("sans-serif", Font.PLAIN, 15));
        g2d.drawString("This is some test text!", 20, 50);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Float(200, 200, 50, 50));
        g2d.dispose();

        return image;
    }

    private interface ImageWriterHelper {
        void createImageWriter(ImageWriter imageWriter) throws IOException;
        void writeImage(BufferedImage image, ImageWriterParams params) throws IOException;
        void writeImageNoParams(BufferedImage image) throws IOException;
        ByteArrayOutputStream getByteArrayOutput();
    }

    private static class TestImageWriter implements ImageWriterHelper {
        private ImageWriter writer;
        private ByteArrayOutputStream baout;

        public void createImageWriter(ImageWriter imageWriter) throws IOException {
            baout = new ByteArrayOutputStream();
            writer = new ImageIOTIFFImageWriter();
        }

        public void writeImage(BufferedImage image, ImageWriterParams params)
                throws IOException {
            writer.writeImage(image, baout, params);
        }

        public void writeImageNoParams(BufferedImage image) throws IOException {
            writer.writeImage(image, baout);
        }

        public ByteArrayOutputStream getByteArrayOutput() {
            return baout;
        }
    }

    private static class TestMultiImageWriter implements ImageWriterHelper {
        private MultiImageWriter writer;
        private ByteArrayOutputStream baout;

        public void createImageWriter(ImageWriter imageWriter)
                throws IOException {
            baout = new ByteArrayOutputStream();
            writer = imageWriter.createMultiImageWriter(baout);
        }

        public void writeImage(BufferedImage image, ImageWriterParams params)
                throws IOException {
            //Writer the same image twice (producing 2 pages)
            writer.writeImage(image, params);
            writer.writeImage(image, params);
            writer.close();
        }

        public void writeImageNoParams(BufferedImage image) throws IOException {
            //Not needed on a multi-image writer
        }

        public ByteArrayOutputStream getByteArrayOutput() {
            return baout;
        }
    }

    @Test
    public void testNewMetadataFormat() {
        ImageWriterParams params = new ImageWriterParams();
        params.setResolution(92);
        MyIIOMetadata metadata = new MyIIOMetadata();
        new ImageIOTIFFImageWriter().updateMetadata(null, metadata, params);
        Assert.assertEquals(metadata.mergeNode, "javax_imageio_tiff_image_1.0");
    }

    static class MyIIOMetadata extends IIOMetadata {
        String mergeNode;
        MyIIOMetadata() {
            super(true, "javax_imageio_tiff_image_1.0", null, null, null);
        }
        public boolean isReadOnly() {
            return false;
        }
        public Node getAsTree(String formatName) {
            IIOMetadataNode node = new IIOMetadataNode();
            node.appendChild(new IIOMetadataNode("Dimension"));
            return node;
        }
        public void mergeTree(String formatName, Node root) {
            mergeNode = root.getNodeName();
        }
        public void reset() {
        }
    };
}
