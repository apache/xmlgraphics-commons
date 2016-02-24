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

/* $Id: DSCProcessingExample1.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package ps;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.tools.PageExtractor;

/**
 * Demonstrates how the DSC parser can be used to extract a series of pages from a DSC-compliant
 * PostScript file. For details how this works, please look into the PageExtractor class
 * where the actual functionality is located. This sample class only calls the code there.
 */
public class DSCProcessingExample1 {

    /**
     * Extracts a series of pages from a DSC-compliant PostScript file.
     * @param srcFile the source PostScript file
     * @param tgtFile the target file to write the extracted pages to
     * @param from the starting page number
     * @param to the ending page number
     * @throws IOException In case of an I/O error
     */
    public void extractPages(File srcFile, File tgtFile, int from, int to) throws IOException {
        InputStream in = new java.io.FileInputStream(srcFile);
        in = new java.io.BufferedInputStream(in);
        try {
            OutputStream out = new java.io.FileOutputStream(tgtFile);
            out = new java.io.BufferedOutputStream(out);
            try {
                PageExtractor.extractPages(in, out, from, to);
            } catch (DSCException e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                IOUtils.closeQuietly(out);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private static void showInfo() {
        System.out.println(
                "Call: DSCProcessingExample1 <source-file> <target-file> <from-page> <to-page>");
    }

    /**
     * Command-line interface
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            File srcFile , tgtFile;
            int from, to;
            if (args.length >= 4) {
                srcFile = new File(args[0]);
                tgtFile = new File(args[1]);
                from = Integer.parseInt(args[2]);
                to = Integer.parseInt(args[3]);
            } else {
                throw new IllegalArgumentException("Invalid number of parameters!");
            }
            if (!srcFile.exists()) {
                throw new IllegalArgumentException("Source file not found: " + srcFile);
            }
            DSCProcessingExample1 app = new DSCProcessingExample1();
            app.extractPages(srcFile, tgtFile, from, to);
            System.out.println("File written: " + tgtFile.getCanonicalPath());
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            showInfo();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
