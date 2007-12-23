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
import java.io.IOException;

/**
 * Interface which allows writing multiple images into one image file if the output format
 * supports this feature. 
 *
 * @version $Id$
 */
public interface MultiImageWriter {

    /**
     * Encodes an image and writes it to the image file.
     * @param image the image to be encoded
     * @param params a parameters object to customize the encoding.
     * @throws IOException In case of an /IO problem
     */
    public void writeImage(RenderedImage image, ImageWriterParams params) 
            throws IOException;
    
    public void close() throws IOException;
    
}
