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
 * Implements a pseudo color space for a named color which is defined in the CIE XYZ color space.
 * At the moment, this color space always returns the fully opaque color regardless of the single
 * component value (tint) given to its conversion methods.
 */
public class NamedColorSpace extends ColorSpace implements ColorSpaceOrigin {

    private static final long serialVersionUID = -8957543225908514658L;

    private final String name;
    private final float[] xyz;

    private final String profileName;
    private final String profileURI;

    /**
     * Creates a new named color.
     * @param name the color name
     * @param xyz the CIE XYZ coordinates (valid values: 0.0f to 1.0f, although
     *                  values slightly larger than 1.0f are common)
     */
    public NamedColorSpace(String name, float[] xyz) {
        this(name, xyz, null, null);
    }

    /**
     * Creates a new named color.
     * @param name the color name
     * @param xyz the CIE XYZ coordinates (valid values: 0.0f to 1.0f, although
     *                  values slightly larger than 1.0f are common)
     * @param profileName Optional profile name associated with this color space
     * @param profileURI Optional profile URI associated with this color space
     */
    public NamedColorSpace(String name, float[] xyz, String profileName, String profileURI) {
        super(ColorSpace.TYPE_GRAY, 1);
        checkNumComponents(xyz, 3);
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("No name provided for named color space");
        }
        this.name = name.trim();
        this.xyz = new float[3];
        System.arraycopy(xyz, 0, this.xyz, 0, 3);
        this.profileName = profileName;
        this.profileURI = profileURI;
    }

    /**
     * Creates a new named color.
     * @param name the color name
     * @param color the color to use when the named color's specific color properties are not
     *                  available.
     * @param profileName Optional profile name associated with this color space
     * @param profileURI Optional profile URI associated with this color space
     */
    public NamedColorSpace(String name, Color color, String profileName, String profileURI) {
        this(name, color.getColorSpace().toCIEXYZ(color.getColorComponents(null)),
                profileName, profileURI);
    }

    /**
     * Creates a new named color.
     * @param name the color name
     * @param color the color to use when the named color's specific color properties are not
     *                  available.
     */
    public NamedColorSpace(String name, Color color) {
        this(name, color.getColorSpace().toCIEXYZ(color.getColorComponents(null)), null, null);
    }

    private void checkNumComponents(float[] colorvalue, int expected) {
        if (colorvalue == null) {
            throw new NullPointerException("color value may not be null");
        }
        if (colorvalue.length != expected) {
            throw new IllegalArgumentException("Expected " + expected
                    + " components, but got " + colorvalue.length);
        }
    }

    /**
     * Returns the color name.
     * @return the color name
     */
    public String getColorName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public String getProfileName() {
        return this.profileName;
    }

    /** {@inheritDoc} */
    public String getProfileURI() {
        return this.profileURI;
    }

    /**
     * Returns the XYZ coordinates of the named color.
     * @return the XYZ coordinates of the named color
     */
    public float[] getXYZ() {
        float[] result = new float[this.xyz.length];
        System.arraycopy(this.xyz, 0, result, 0, this.xyz.length);
        return result;
    }

    /**
     * Returns an sRGB-based color representing the full-tint color defined by this named color
     * space.
     * @return the sRGB color
     */
    public Color getRGBColor() {
        float[] comps = toRGB(this.xyz);
        return new Color(comps[0], comps[1], comps[2]);
    }

    /** {@inheritDoc} */
    public float getMinValue(int component) {
        return getMaxValue(component); //same as min, i.e. always 1.0
    }

    /** {@inheritDoc} */
    public float getMaxValue(int component) {
        switch (component) {
        case 0:
            return 1f;
        default:
            throw new IllegalArgumentException("A named color space only has 1 component!");
        }
    }

    /** {@inheritDoc} */
    public String getName(int component) {
        switch (component) {
        case 0:
            return "Tint";
        default:
            throw new IllegalArgumentException("A named color space only has 1 component!");
        }
    }

    /** {@inheritDoc} */
    public float[] fromCIEXYZ(float[] colorvalue) {
        //ignore the given color values as this is a fixed color.
        return new float[] {1.0f}; //Return value for full tint
    }

    /** {@inheritDoc} */
    public float[] fromRGB(float[] rgbvalue) {
        //ignore the given color values as this is a fixed color.
        return new float[] {1.0f}; //Return value for full tint
    }

    /** {@inheritDoc} */
    public float[] toCIEXYZ(float[] colorvalue) {
        float[] ret = new float[3];
        System.arraycopy(this.xyz, 0, ret, 0, this.xyz.length);
        return ret;
    }

    /** {@inheritDoc} */
    public float[] toRGB(float[] colorvalue) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        return sRGB.fromCIEXYZ(this.xyz);
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedColorSpace)) {
            return false;
        }
        NamedColorSpace other = (NamedColorSpace)obj;
        if (!this.name.equals(other.name)) {
            return false;
        }
        for (int i = 0, c = this.xyz.length; i < c; i++) {
            if (this.xyz[i] != other.xyz[i]) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "Named Color Space: " + getColorName();
    }

}
