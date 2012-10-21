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

// Original author: Matthias Reichenbacher

package org.apache.xmlgraphics.image.loader.impl;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.io.InputStream;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;

public class ImageRawPNG extends ImageRawStream {

    private ColorModel cm;
    private ICC_Profile iccProfile;
    private int bitDepth;
    private boolean isTransparent;
    private int grayTransparentAlpha;
    private int redTransparentAlpha;
    private int greenTransparentAlpha;
    private int blueTransparentAlpha;
    private int renderingIntent = -1;

    /**
     * Main constructor.
     * @param info the image info object
     * @param in the ImageInputStream with the raw content
     * @param colorModel the color model
     * @param bitDepth the bit depth
     * @param iccProfile an ICC color profile or null if no profile is associated
     */
    public ImageRawPNG(ImageInfo info, InputStream in, ColorModel colorModel, int bitDepth, ICC_Profile iccProfile) {
        super(info, ImageFlavor.RAW_PNG, in);
        this.iccProfile = iccProfile;
        this.cm = colorModel;
        this.bitDepth = bitDepth;
    }

    /**
     * The bit depth of each color channel.
     * @return the bit depth of one channel (same for all)
     */
    public int getBitDepth() {
        return bitDepth;
    }

    /**
     * Returns the ICC color profile if one is associated with the PNG image.
     * @return the ICC color profile or null if there's no profile
     */
    public ICC_Profile getICCProfile() {
        return this.iccProfile;
    }

    /**
     * Returns the image's color model.
     * @return the color model
     */
    public ColorModel getColorModel() {
        return this.cm;
    }

    /**
     * Returns the image's color space.
     * @return the color space
     */
    public ColorSpace getColorSpace() {
        return this.cm.getColorSpace();
    }

    /**
     * Sets the gray transparent pixel value.
     * @param gray the transparent pixel gray value (0...255)
     */
    protected void setGrayTransparentAlpha(int gray) {
        this.isTransparent = true;
        this.grayTransparentAlpha = gray;
    }

    /**
     * Sets the RGB transparent pixel values.
     * @param red the transparent pixel red value (0...255)
     * @param green the transparent pixel green value (0...255)
     * @param blue the transparent pixel blue value (0...255)
     */
    protected void setRGBTransparentAlpha(int red, int green, int blue) {
        this.isTransparent = true;
        this.redTransparentAlpha = red;
        this.greenTransparentAlpha = green;
        this.blueTransparentAlpha = blue;
    }

    /**
     * Used to flag image as transparent when the image is of pallete type.
     */
    protected void setTransparent() {
        this.isTransparent = true;
    }

    /**
     * Whether the image is transparent (meaning there is a transparent pixel)
     * @return true if transparent pixel exists
     */
    public boolean isTransparent() {
        return this.isTransparent;
    }

    /**
     * The color of the transparent pixel.
     * @return the color of the transparent pixel.
     */
    public Color getTransparentColor() {
        Color color = null;
        if (!this.isTransparent) {
            return color;
        }
        if (cm.getNumColorComponents() == 3) {
            color = new Color(this.redTransparentAlpha, this.greenTransparentAlpha, this.blueTransparentAlpha);
        } else {
            color = new Color(this.grayTransparentAlpha, 0, 0);
        }
        return color;
    }

    /**
     * Used to set the rendering intent when the color space is sRGB.
     * @param intent the rendering intent of the sRGB color space
     */
    public void setRenderingIntent(int intent) {
        renderingIntent = intent;
    }

    /**
     * Returns the rendering intent of the sRGB color space.
     * @return the rendering intent
     */
    public int getRenderingIntent() {
      return this.renderingIntent;
    }

}
