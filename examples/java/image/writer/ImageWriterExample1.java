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

package image.writer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedString;

import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;

public class ImageWriterExample1 {

    /**
     * Paints a few things on a Graphics2D instance.
     * @param g2d the Graphics2D instance
     * @param pageNum a page number
     */
    protected void paintSome(Graphics2D g2d, int pageNum) {
        //Paint a bounding box
        g2d.drawRect(0, 0, 400, 200);

        //A few rectangles rotated and with different color
        Graphics2D copy = (Graphics2D)g2d.create();
        int c = 12;
        for (int i = 0; i < c; i++) {
            float f = ((i + 1) / (float)c);
            Color col = new Color(0.0f, 1 - f, 0.0f);
            copy.setColor(col);
            copy.fillRect(70, 90, 50, 50);
            copy.rotate(-2 * Math.PI / (double)c, 70, 90);
        }
        copy.dispose();

        //Some text
        copy = (Graphics2D)g2d.create();
        copy.rotate(-0.25);
        copy.setColor(Color.RED);
        copy.setFont(new Font("sans-serif", Font.PLAIN, 36));
        copy.drawString("Hello world!", 140, 140);
        copy.setColor(Color.RED.darker());
        copy.setFont(new Font("serif", Font.PLAIN, 36));
        copy.drawString("Hello world!", 140, 180);
        copy.dispose();

        //Try attributed text
        AttributedString aString = new AttributedString("This is attributed text.");
        aString.addAttribute(TextAttribute.FAMILY, "SansSerif");
        aString.addAttribute(TextAttribute.FAMILY, "Serif", 8, 18);
        aString.addAttribute(TextAttribute.FOREGROUND, Color.orange, 8, 18);
        g2d.drawString(aString.getIterator(), 250, 170);

        g2d.drawString("Page: " + pageNum, 250, 190);
    }

    /**
     * Creates a bitmap file. We paint a few things on a bitmap and then save the bitmap using
     * an ImageWriter.
     * @param outputFile the target file
     * @param format the target format (a MIME type, ex. "image/png")
     * @throws IOException In case of an I/O error
     */
    public void generateBitmapUsingJava2D(File outputFile, String format)
                throws IOException {
        //String compression = "CCITT T.6";
        String compression = "PackBits";
        boolean monochrome = compression.startsWith("CCITT"); //CCITT is for 1bit b/w only

        BufferedImage bimg;
        if (monochrome) {
            bimg = new BufferedImage(400, 200, BufferedImage.TYPE_BYTE_BINARY);
        } else {
            bimg = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = bimg.createGraphics();
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, 400, 200);
        g2d.setColor(Color.black);

        //Paint something
        paintSome(g2d, 1);

        try (OutputStream fout = new java.io.FileOutputStream(outputFile);
             OutputStream out = new java.io.BufferedOutputStream(fout)) {

            ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(format);
            ImageWriterParams params = new ImageWriterParams();
            params.setCompressionMethod(compression);
            params.setResolution(72);
            writer.writeImage(bimg, out, params);

        }
    }

    /**
     * Command-line interface
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            File targetDir;
            if (args.length >= 1) {
                targetDir = new File(args[0]);
            } else {
                targetDir = new File(".");
            }
            if (!targetDir.exists()) {
                System.err.println("Target Directory does not exist: " + targetDir);
            }
            File outputFile = new File(targetDir, "eps-example1.tif");
            ImageWriterExample1 app = new ImageWriterExample1();
            app.generateBitmapUsingJava2D(outputFile, "image/tiff");
            System.out.println("File written: " + outputFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
