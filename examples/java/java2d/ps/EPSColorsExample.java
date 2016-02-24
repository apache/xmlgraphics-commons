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

/* $Id: EPSColorsExample.java 1051421 2010-12-21 08:54:25Z jeremias $ */

package java2d.ps;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;

/**
 * This example demonstrates how colors are handled when generating PostScript/EPS.
 */
public class EPSColorsExample {

    /**
     * Creates an EPS file. The contents are painted using a Graphics2D implementation that
     * generates an EPS file.
     * @param outputFile the target file
     * @throws IOException In case of an I/O error
     */
    public static void generateEPSusingJava2D(File outputFile) throws IOException {
        OutputStream out = new java.io.FileOutputStream(outputFile);
        out = new java.io.BufferedOutputStream(out);
        try {
            //Instantiate the EPSDocumentGraphics2D instance
            EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
            g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

            //Set up the document size
            g2d.setupDocument(out, 400, 200); //400pt x 200pt

            //Paint a bounding box
            g2d.drawRect(0, 0, 400, 200);

            g2d.setFont(new Font("sans-serif", Font.BOLD, 14));
            g2d.drawString("Color usage example:", 10, 20);
            g2d.setFont(new Font("sans-serif", Font.PLAIN, 12));
            g2d.drawString("RGB", 10, 84);
            g2d.drawString("CMYK", 60, 84);
            g2d.drawString("(Lab)", 110, 84);
            g2d.drawString("(Named)", 160, 84);

            //We're creating a few boxes all filled with some variant of the
            //"Postgelb" (postal yellow) color as used by Swiss Post.

            Color colRGB = new Color(255, 204, 0);
            g2d.setColor(colRGB);
            g2d.fillRect(10, 30, 40, 40);

            //Just convert RGB to CMYK and use that
            float[] compsRGB = colRGB.getColorComponents(null);
            DeviceCMYKColorSpace cmykCS = ColorSpaces.getDeviceCMYKColorSpace();
            float[] compsCMYK = cmykCS.fromRGB(compsRGB);
            Color colCMYK = DeviceCMYKColorSpace.createCMYKColor(compsCMYK);
            g2d.setColor(colCMYK);
            g2d.fillRect(60, 30, 40, 40);

            //Try CIELab (not implemented, yet)
            CIELabColorSpace d50 = ColorSpaces.getCIELabColorSpaceD50();
            Color colLab = d50.toColor(83.25f, 16.45f, 96.89f, 1.0f);
            g2d.setColor(colLab);
            g2d.fillRect(110, 30, 40, 40);

            //Try named color (Separation, not implemented, yet)
            float[] c1xyz = d50.toCIEXYZNative(83.25f, 16.45f, 96.89f);
            NamedColorSpace postgelb = new NamedColorSpace("Postgelb", c1xyz);
            Color colNamed = new Color(postgelb, new float[] {1.0f}, 1.0f);
            g2d.setColor(colNamed);
            g2d.fillRect(160, 30, 40, 40);

            //Cleanup
            g2d.finish();
        } finally {
            IOUtils.closeQuietly(out);
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
            generateEPSusingJava2D(new File(targetDir, "eps-example-colors.eps"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
