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

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Convenience methods around ImageWriter for the most important tasks.
 */
public class ImageWriterUtil {

    /**
     * Saves a RenderedImage as a PNG file with 96 dpi.
     * @param bitmap the bitmap to encode
     * @param outputFile the target file
     * @throws IOException in case of an I/O problem
     */
    public static void saveAsPNG(RenderedImage bitmap, File outputFile)
                throws IOException {
        saveAsPNG(bitmap, 96, outputFile);
    }

    /**
     * Saves a RenderedImage as a PNG file.
     * @param bitmap the bitmap to encode
     * @param resolution the bitmap resolution
     * @param outputFile the target file
     * @throws IOException in case of an I/O problem
     */
    public static void saveAsPNG(RenderedImage bitmap, int resolution, File outputFile)
                throws IOException {
        saveAsFile(bitmap, resolution, outputFile, "image/png");
    }

    /**
     * Saves a RenderedImage as a file. The image format is given through the MIME type
     * @param bitmap the bitmap to encode
     * @param resolution the bitmap resolution
     * @param outputFile the target file
     * @param mime the MIME type of the target file
     * @throws IOException in case of an I/O problem
     */
    public static void saveAsFile(RenderedImage bitmap,
            int resolution, File outputFile, String mime)
                throws IOException {
        OutputStream out = new java.io.FileOutputStream(outputFile);
        try {
            ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(mime);
            ImageWriterParams params = new ImageWriterParams();
            params.setResolution(resolution);
            writer.writeImage(bitmap, out, params);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

}
