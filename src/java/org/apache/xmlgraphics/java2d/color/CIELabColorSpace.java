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

    private static final double D = 6.0 / 29.0;
    private static final double REF_A = 1.0 / (3 * Math.pow(D, 2)); //7.787037...
    private static final double REF_B = 16.0 / 116.0;
    private static final double T0 = Math.pow(D, 3); //0.008856...

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
            return -128f;
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
            return 128f;
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
        float x = colorvalue[0];
        float y = colorvalue[1];
        float z = colorvalue[2];

        double varX = x / wpX;
        double varY = y / wpY;
        double varZ = z / wpZ;

        if (varX > T0) {
            varX = Math.pow(varX, (1 / 3.0));
        } else {
            varX = (REF_A * varX) + REF_B;
        }
        if (varY > T0) {
            varY = Math.pow(varY, 1 / 3.0);
        } else {
            varY = (REF_A * varY) + REF_B;
        }
        if (varZ > T0) {
            varZ = Math.pow(varZ, 1 / 3.0);
        } else {
            varZ = (REF_A * varZ) + REF_B;
        }

        float l = (float)((116 * varY) - 16);
        float a = (float)(500 * (varX - varY));
        float b = (float)(200 * (varY - varZ));

        //Normalize to range 0.0..1.0
        l = normalize(l, 0);
        a = normalize(a, 1);
        b = normalize(b, 2);
        return new float[] {l, a, b};
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
        //Scale to native value range
        float l = denormalize(colorvalue[0], 0);
        float a = denormalize(colorvalue[1], 1);
        float b = denormalize(colorvalue[2], 2);

        return toCIEXYZNative(l, a, b);
    }

    /**
     * Transforms a color value assumed to be in this {@link ColorSpace}
     * into the CS_CIEXYZ conversion color space. This method uses component values
     * in CIE Lab's native color ranges rather than the normalized values between 0 and 1.
     * @param l the L* component (values between 0 and 100)
     * @param a the a* component (usually between -128 and +128)
     * @param b the b* component (usually between -128 and +128)
     * @return the XYZ color values
     * @see #toCIEXYZ(float[])
     */
    public float[] toCIEXYZNative(float l, float a, float b) {
        double varY = (l + 16) / 116.0;
        double varX = a / 500 + varY;
        double varZ = varY - b / 200.0;

        if (Math.pow(varY, 3) > T0) {
            varY = Math.pow(varY, 3);
        } else {
            varY = (varY - 16 / 116.0) / REF_A;
        }
        if (Math.pow(varX, 3) > T0) {
            varX = Math.pow(varX, 3);
        } else {
            varX = (varX - 16 / 116.0) / REF_A;
        }
        if (Math.pow(varZ, 3) > T0) {
            varZ = Math.pow(varZ, 3);
        } else {
            varZ = (varZ - 16 / 116.0) / REF_A;
        }

        float x = (float)(wpX * varX / 100);
        float y = (float)(wpY * varY / 100);
        float z = (float)(wpZ * varZ / 100);

        return new float[] {x, y, z};
    }

    /** {@inheritDoc} */
    public float[] toRGB(float[] colorvalue) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] xyz = toCIEXYZ(colorvalue);
        return sRGB.fromCIEXYZ(xyz);
    }

    private float getNativeValueRange(int component) {
        return getMaxValue(component) - getMinValue(component);
    }

    private float normalize(float value, int component) {
        return (value - getMinValue(component)) / getNativeValueRange(component);
    }

    private float denormalize(float value, int component) {
        return value * getNativeValueRange(component) + getMinValue(component);
    }

    /**
     * Converts normalized (0..1) color components to CIE L*a*b*'s native value range.
     * @param comps the normalized components.
     * @return the denormalized components
     */
    public float[] toNativeComponents(float[] comps) {
        checkNumComponents(comps);
        float[] nativeComps = new float[comps.length];
        for (int i = 0, c = comps.length; i < c; i++) {
            nativeComps[i] = denormalize(comps[i], i);
        }
        return nativeComps;
    }

    /**
     * Creates a {@link Color} instance from color values usually used by the L*a*b* color space
     * by scaling them to the 0.0..1.0 range expected by Color's constructor.
     * @param colorvalue the original color values
     *                  (native value range, i.e. not normalized to 0.0..1.0)
     * @param alpha the alpha component
     * @return the requested color instance
     */
    public Color toColor(float[] colorvalue, float alpha) {
        int c = colorvalue.length;
        float[] normalized = new float[c];
        for (int i = 0; i < c; i++) {
            normalized[i] = normalize(colorvalue[i], i);
        }
        //Using ColorWithAlternatives for better equals() functionality
        return new ColorWithAlternatives(this, normalized, alpha, null);
    }

    /**
     * Creates a {@link Color} instance from color values usually used by the L*a*b* color space
     * by scaling them to the 0.0..1.0 range expected by Color's constructor.
     * @param l the L* component (values between 0 and 100)
     * @param a the a* component (usually between -128 and +127)
     * @param b the b* component (usually between -128 and +127)
     * @param alpha the alpha component (values between 0 and 1)
     * @return the requested color instance
     */
    public Color toColor(float l, float a, float b, float alpha) {
        return toColor(new float[] {l, a, b}, alpha);
    }

}
