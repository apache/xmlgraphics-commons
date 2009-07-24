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

package org.apache.xmlgraphics.image.loader.impl;

import java.awt.Color;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;

/**
 * This class is an implementation of the Image interface exposing a BufferedImage.
 */
public class ImageBuffered extends ImageRendered {

    /**
     * Main constructor.
     * @param info the image info object
     * @param buffered the BufferedImage instance
     * @param transparentColor the transparent color or null
     * @param iccProfile an ICC color profile or null if no profile is associated
     */
    public ImageBuffered(ImageInfo info, BufferedImage buffered, Color transparentColor, ICC_Profile iccProfile) {
        super(info, buffered, transparentColor, iccProfile);
    }

    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return ImageFlavor.BUFFERED_IMAGE;
    }

    /**
     * Returns the contained BufferedImage instance.
     * @return the BufferedImage instance
     */
    public java.awt.image.BufferedImage getBufferedImage() {
        return (BufferedImage)getRenderedImage();
    }
}
