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

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * This class represents an uncalibrated CMYK color space.
 */
public class DeviceCMYKColorSpace extends AbstractDeviceSpecificColorSpace
            implements ColorSpaceOrigin {

    private static final long serialVersionUID = 2925508946083542974L;

    /** The name for the uncalibrated CMYK pseudo-profile */
    public static final String PSEUDO_PROFILE_NAME = "#CMYK";

    /**
     * Constructs an uncalibrated CMYK ColorSpace object with {@link ColorSpace#TYPE_CMYK} and
     * 4 components.
     * @see java.awt.color.ColorSpace#ColorSpace(int, int)
     */
    public DeviceCMYKColorSpace() {
        super(TYPE_CMYK, 4);
    }

    /** {@inheritDoc} */
    @Override
    public float[] toRGB(float[] colorvalue) {
        return new float [] {
            (1 - colorvalue[0]) * (1 - colorvalue[3]),
            (1 - colorvalue[1]) * (1 - colorvalue[3]),
            (1 - colorvalue[2]) * (1 - colorvalue[3])};
    }

    /** {@inheritDoc} */
    @Override
    public float[] fromRGB(float[] rgbvalue) {
        assert rgbvalue.length == 3;
        //Note: this is an arbitrary conversion, not a color-managed one!
        float r = rgbvalue[0];
        float g = rgbvalue[1];
        float b = rgbvalue[2];
        if (r == g && r == b) {
            return new float[] {0, 0, 0, 1 - r};
        } else {
            float c = 1 - r;
            float m = 1 - g;
            float y = 1 - b;
            float k = Math.min(c, Math.min(m, y));
            return new float[] {c, m, y, k};
        }
    }

    /** {@inheritDoc} */
    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        throw new UnsupportedOperationException("NYI");
    }

    /** {@inheritDoc} */
    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        throw new UnsupportedOperationException("NYI");
    }

    /**
     * Creates a color instance representing a device-specific CMYK color. An sRGB value
     * is calculated from the CMYK colors but it may not correctly represent the given CMYK
     * values.
     * @param cmykComponents the CMYK components
     * @return the device-specific color
     */
    public static Color createCMYKColor(float[] cmykComponents) {
        DeviceCMYKColorSpace cmykCs = ColorSpaces.getDeviceCMYKColorSpace();
        Color cmykColor = new ColorWithAlternatives(cmykCs, cmykComponents, 1.0f, null);
        return cmykColor;
    }

    /** {@inheritDoc} */
    public String getProfileName() {
        return PSEUDO_PROFILE_NAME;
    }

    /** {@inheritDoc} */
    public String getProfileURI() {
        return null; //No URI
    }

}
