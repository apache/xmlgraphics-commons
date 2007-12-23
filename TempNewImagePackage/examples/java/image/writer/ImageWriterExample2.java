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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

public class ImageWriterExample2 extends ImageWriterExample1 {

    private BufferedImage createAnImage(String compression, int pageNum) {
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
        paintSome(g2d, pageNum);
        
        return bimg;
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

        OutputStream out = new java.io.FileOutputStream(outputFile);
        out = new java.io.BufferedOutputStream(out);
        try {

            ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(format);
            ImageWriterParams params = new ImageWriterParams();
            params.setCompressionMethod(compression);
            params.setResolution(72);
            
            if (writer.supportsMultiImageWriter()) {
                MultiImageWriter multiWriter = writer.createMultiImageWriter(out);
                multiWriter.writeImage(createAnImage(compression, 1), params);
                multiWriter.writeImage(createAnImage(compression, 2), params);
                multiWriter.close();
            } else {
                throw new UnsupportedOperationException("multi-page images not supported for " 
                        + format);
            }
            
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
            File outputFile = new File(targetDir, "eps-example2.tif");
            ImageWriterExample2 app = new ImageWriterExample2();
            app.generateBitmapUsingJava2D(outputFile, "image/tiff");
            System.out.println("File written: " + outputFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
