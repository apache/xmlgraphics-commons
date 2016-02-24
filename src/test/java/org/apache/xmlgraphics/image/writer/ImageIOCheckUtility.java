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

import java.util.Iterator;

import javax.imageio.ImageIO;

public final class ImageIOCheckUtility {

    private ImageIOCheckUtility() {
    }

    /**
     * Determines whether the JAI ImageIO library is present to run tests
     * @return Returns true if the library is present.
     */
    public static boolean isSunTIFFImageWriterAvailable() {
        Iterator<javax.imageio.ImageWriter> tiffWriters
            = ImageIO.getImageWritersByMIMEType("image/tiff");
        boolean found = false;
        while (tiffWriters.hasNext()) {
            javax.imageio.ImageWriter writer = tiffWriters.next();
            if ("com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter".equals(
                    writer.getClass().getName())) {
                //JAI ImageIO implementation present
                found = true;
                break;
            }
        }
        return found;
    }
}
