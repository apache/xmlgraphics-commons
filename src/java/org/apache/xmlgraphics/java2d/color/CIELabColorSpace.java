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

/**
 * This class defines the CIE L*a*b* (CIE 1976) color space. Valid values for L* are between 0
 * and 100, for a* and b* between -127 and +127.
 * @see <a href="http://en.wikipedia.org/wiki/Lab_color_space">http://en.wikipedia.org/wiki/Lab_color_space</a>
 */
public class CIELabColorSpace extends ColorSpace {

    private static final long serialVersionUID = -1821569090707520704L;

    //CIE XYZ tristimulus values of the reference white point: Observer= 2°, Illuminant= D65
    private static final float REF_X_D65 = 95.047f;
    private static final float REF_Y_D65 = 100.000f;
    private static final float REF_Z_D65 = 108.883f;

    //CIE XYZ tristimulus values of the reference white point: Illuminant= D50
    private static final float REF_X_D50 = 96.42f;
    private static final float REF_Y_D50 = 100.00f;
    private static final float REF_Z_D50 = 82.49f;

    private static final double d = 6.0 / 29.0;
    private static final double refA = 1.0 / (3 * Math.pow(d, 2)); //7.787037...
    private static final double refB = 16.0 / 116.0;
    private static final double t0 = Math.pow(d, 3); //0.008856...

    private float wpX;
    private float wpY;
    private float wpZ;

    /**
     * Default constructor using the D65 white point.
     */
    public CIELabColorSpace() {
        this(getD65WhitePoint());
    }

    /**
     * CIE Lab space constructor which allows to give an arbitrary white point.
     * @param whitePoint the white point in XYZ coordinates (valid values: 0.0f to 1.0f, although
     * values slightly larger than 1.0f are common)
     */
    public CIELabColorSpace(float[] whitePoint) {
        super(ColorSpace.TYPE_Lab, 3);
        checkNumComponents(whitePoint, 3);
        this.wpX = whitePoint[0];
        this.wpY = whitePoint[1];
        this.wpZ = whitePoint[2];
    }

    /**
     * Returns the D65 white point.
     * @return the D65 white point.
     */
    public static float[] getD65WhitePoint() {
        return new float[] {REF_X_D65, REF_Y_D65, REF_Z_D65};
    }

    /**
     * Returns the D50 white point.
     * @return the D50 white point.
     */
    public static float[] getD50WhitePoint() {
        return new float[] {REF_X_D50, REF_Y_D50, REF_Z_D50};
    }

    private void checkNumComponents(float[] colorvalue) {
        checkNumComponents(colorvalue, getNumComponents());
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
     * Returns the configured white point.
     * @return the white point in CIE XYZ coordinates
     */
    public float[] getWhitePoint() {
        return new float[] {wpX, wpY, wpZ};
    }

    private static final String CIE_LAB_ONLY_HAS_3_COMPONENTS = "CIE Lab only has 3 components!";

    /** {@inheritDoc} */
    public float getMinValue(int component) {
        switch (component) {
        case 0: //L*
            return 0f;
        case 1: //a*
        case 2: //b*
            return -127f;
        default:
            throw new IllegalArgumentException(CIE_LAB_ONLY_HAS_3_COMPONENTS);
        }
    }

    /** {@inheritDoc} */
    public float getMaxValue(int component) {
        switch (component) {
        case 0: //L*
            return 100f;
        case 1: //a*
        case 2: //b*
            return 127f;
        default:
            throw new IllegalArgumentException(CIE_LAB_ONLY_HAS_3_COMPONENTS);
        }
    }

    /** {@inheritDoc} */
    public String getName(int component) {
        switch (component) {
        case 0:
            return "L*";
        case 1:
            return "a*";
        case 2:
            return "b*";
        default:
            throw new IllegalArgumentException(CIE_LAB_ONLY_HAS_3_COMPONENTS);
        }
    }

    //Note: the conversion functions used here were mostly borrowed from Apache Commons Sanselan
    //and adjusted to the local requirements.

    /** {@inheritDoc} */
    public float[] fromCIEXYZ(float[] colorvalue) {
        checkNumComponents(colorvalue, 3);
        float X = colorvalue[0];
        float Y = colorvalue[1];
        float Z = colorvalue[2];

        double var_X = X / wpX;
        double var_Y = Y / wpY;
        double var_Z = Z / wpZ;

        if (var_X > t0) {
            var_X = Math.pow(var_X, (1 / 3.0));
        } else {
            var_X = (refA * var_X) + refB;
        }
        if (var_Y > t0) {
            var_Y = Math.pow(var_Y, 1 / 3.0);
        } else {
            var_Y = (refA * var_Y) + refB;
        }
        if (var_Z > t0) {
            var_Z = Math.pow(var_Z, 1 / 3.0);
        } else {
            var_Z = (refA * var_Z) + refB;
        }

        float L = (float)((116 * var_Y) - 16);
        float a = (float)(500 * (var_X - var_Y));
        float b = (float)(200 * (var_Y - var_Z));
        return new float[] {L, a, b};
    }

    /** {@inheritDoc} */
    public float[] fromRGB(float[] rgbvalue) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] xyz = sRGB.toCIEXYZ(rgbvalue);
        return fromCIEXYZ(xyz);
    }

    /** {@inheritDoc} */
    public float[] toCIEXYZ(float[] colorvalue) {
        checkNumComponents(colorvalue);
        float L = colorvalue[0];
        float a = colorvalue[1];
        float b = colorvalue[2];

        double var_Y = (L + 16) / 116.0;
        double var_X = a / 500 + var_Y;
        double var_Z = var_Y - b / 200.0;


        if (Math.pow(var_Y, 3) > t0) {
            var_Y = Math.pow(var_Y, 3);
        } else {
            var_Y = (var_Y - 16 / 116.0) / refA;
        }
        if (Math.pow(var_X, 3) > t0) {
            var_X = Math.pow(var_X, 3);
        } else {
            var_X = (var_X - 16 / 116.0) / refA;
        }
        if (Math.pow(var_Z, 3) > t0) {
            var_Z = Math.pow(var_Z, 3);
        } else {
            var_Z = (var_Z - 16 / 116.0) / refA;
        }

        float X = (float)(wpX * var_X / 100);
        float Y = (float)(wpY * var_Y / 100);
        float Z = (float)(wpZ * var_Z / 100);

        return new float[] {X, Y, Z};
    }

    /** {@inheritDoc} */
    public float[] toRGB(float[] colorvalue) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] xyz = toCIEXYZ(colorvalue);
        return sRGB.fromCIEXYZ(xyz);
    }

}
