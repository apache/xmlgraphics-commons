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

package java2d.ps;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.writer.ImageWriterUtil;
import org.apache.xmlgraphics.java2d.ps.PSDocumentGraphics2D;

/**
 * This example demonstrates the usage of PostScript tiling patterns. The class also generated
 * a PNG file so the output can be compared.
 */
public class TilingPatternExample {

    private BufferedImage tile;
    private TexturePaint paint;

    /**
     * Default constructor.
     */
    public TilingPatternExample() {
        //Created TexturePaint instance
        this.tile = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D tileg2d = tile.createGraphics();
        tileg2d.setBackground(Color.WHITE);
        tileg2d.clearRect(0, 0, tile.getWidth(), tile.getHeight());
        tileg2d.setColor(Color.BLUE);
        tileg2d.fillOval(2, 2, tile.getWidth() - 2, tile.getHeight() - 2);
        tileg2d.dispose();
        Rectangle2D rect = new Rectangle2D.Double(
                2, 2,
                tile.getWidth() / 2.0, tile.getHeight() / 2.0);
        this.paint = new TexturePaint(tile, rect);
    }

    /**
     * Creates a PostScript file. The contents are painted using a Graphics2D implementation.
     * @param outputFile the target file
     * @throws IOException In case of an I/O error
     */
    public void generatePSusingJava2D(File outputFile) throws IOException {
        try (OutputStream fout = new java.io.FileOutputStream(outputFile);
             OutputStream out = new java.io.BufferedOutputStream(out)) {
            //Instantiate the PSDocumentGraphics2D instance
            PSDocumentGraphics2D g2d = new PSDocumentGraphics2D(false);
            g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

            //Set up the document size
            g2d.setupDocument(out, 400, 200); //400pt x 200pt

            paintTileAlone(g2d);
            paintShapes(g2d);
            paintText(g2d);

            g2d.nextPage();

            paintText(g2d);

            //Cleanup
            g2d.finish();
        }
    }

    private void generatePNGusingJava2D(File outputFile) throws IOException {
        BufferedImage image = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paintTileAlone(g2d);
        paintShapes(g2d);
        paintText(g2d);
        g2d.dispose();

        ImageWriterUtil.saveAsPNG(image, outputFile);
    }

    private void paintTileAlone(Graphics2D g2d) {
        AffineTransform at = new AffineTransform();
        at.translate(5, 5);
        g2d.drawRenderedImage(this.tile, at);
    }

    private void paintShapes(Graphics2D g2d) {
        g2d.setPaint(paint);
        Rectangle rect = new Rectangle(10, 50, 30, 30);
        g2d.fill(rect);
        rect = new Rectangle(10, 90, 40, 20);
        g2d.fill(rect);
        Polygon poly = new Polygon(new int[] {50, 100, 150}, new int[] {100, 20, 100}, 3);
        g2d.fill(poly);
    }

    private void paintText(Graphics2D g2d) {
        g2d.setPaint(paint);
        Font font = new Font("serif", Font.BOLD, 80);
        GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), "Java");
        g2d.translate(100, 180);
        g2d.fill(gv.getOutline());
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
            File outputFile = new File(targetDir, "tiling-example.ps");
            File pngFile = new File(targetDir, "tiling-example.png");
            TilingPatternExample app = new TilingPatternExample();
            app.generatePSusingJava2D(outputFile);
            System.out.println("File written: " + outputFile.getCanonicalPath());
            app.generatePNGusingJava2D(pngFile);
            System.out.println("File written: " + pngFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
