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

package org.apache.xmlgraphics.java2d.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * This class extends the ICCColorSpace class by providing
 * convenience methods to convert to sRGB using various
 * methods, forcing a given intent, such as perceptual or
 * relative colorimetric. It also additionally holds the name
 * and source URI of the color profile.
 */
public class ICCColorSpaceWithIntent extends ICC_ColorSpace implements ColorSpaceOrigin {

    private static final long serialVersionUID = -3338065900662625221L;

    static final ColorSpace SRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    private RenderingIntent intent;
    private String profileName;
    private String profileURI;

    /**
     * Creates a new ICC-based color space.
     * @param p the color profile
     * @param intent the overriding rendering intent (use {@link RenderingIntent#AUTO}
     *          to preserve the profile's)
     * @param profileName the color profile name
     * @param profileURI the source URI of the color profile
     */
    public ICCColorSpaceWithIntent(ICC_Profile p, RenderingIntent intent,
            String profileName, String profileURI) {
        super(p);

        this.intent = intent;

        /**
         * Apply the requested intent into the profile
         */
        if (intent != RenderingIntent.AUTO) {
            byte[] hdr = p.getData(ICC_Profile.icSigHead);
            hdr[ICC_Profile.icHdrRenderingIntent] = (byte)intent.getIntegerValue();
        }

        this.profileName = profileName;
        this.profileURI = profileURI;
    }

    /**
     * Returns the sRGB value obtained by forcing the
     * conversion method to the intent passed to the
     * constructor.
     * @param values the color values in the local color space
     * @return the sRGB values
     */
    public float[] intendedToRGB(float[] values) {
        switch(intent) {
        case ABSOLUTE_COLORIMETRIC:
            return absoluteColorimetricToRGB(values);
        case PERCEPTUAL:
        case AUTO:
            return perceptualToRGB(values);
        case RELATIVE_COLORIMETRIC:
            return relativeColorimetricToRGB(values);
        case SATURATION:
            return saturationToRGB(values);
        default:
            throw new Error("invalid intent:" + intent );
        }
    }

    /**
     * Perceptual conversion is the method implemented by the
     * base class's toRGB method
     * @param values the color values in the local color space
     * @return the sRGB values
     */
    private float[] perceptualToRGB(float[] values) {
        return toRGB(values);
    }

    /**
     * Relative colorimetric needs to happen through CIEXYZ
     * conversion.
     * @param values the color values in the local color space
     * @return the sRGB values
     */
    private float[] relativeColorimetricToRGB(float[] values) {
        float[] ciexyz = toCIEXYZ(values);
        return SRGB.fromCIEXYZ(ciexyz);
    }

    /**
     * Absolute colorimetric. NOT IMPLEMENTED.
     * Temporarily returns same as perceptual.
     * @param values the color values in the local color space
     * @return the sRGB values
     */
    private float[] absoluteColorimetricToRGB(float[] values) {
        return perceptualToRGB(values);
    }

    /**
     * Saturation. NOT IMPLEMENTED. Temporarily returns same
     * as perceptual.
     * @param values the color values in the local color space
     * @return the sRGB values
     */
    private float[] saturationToRGB(float[] values) {
        return perceptualToRGB(values);
    }

    /** {@inheritDoc} */
    public String getProfileName() {
        return this.profileName;
    }

    /** {@inheritDoc} */
    public String getProfileURI() {
        return this.profileURI;
    }

}
