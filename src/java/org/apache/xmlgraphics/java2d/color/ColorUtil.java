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


/**
 * Generic Color helper class.
 * <p>
 * This class supports parsing string values into color values and creating
 * color values for strings. It provides a list of standard color names.
 */
public final class ColorUtil {

    /**
     * Private constructor since this is an utility class.
     */
    private ColorUtil() {
    }


    /**
     * Lightens up a color for groove, ridge, inset and outset border effects.
     * @param col the color to lighten up
     * @param factor factor by which to lighten up (negative values darken the color)
     * @return the modified color
     */
    public static Color lightenColor(Color col, float factor) {
        // TODO: This function converts the color into the sRGB namespace.
        // This should be avoided if possible.
        float[] cols = new float[4];
        cols = col.getRGBComponents(cols);
        if (factor > 0) {
            cols[0] += (1.0 - cols[0]) * factor;
            cols[1] += (1.0 - cols[1]) * factor;
            cols[2] += (1.0 - cols[2]) * factor;
        } else {
            cols[0] -= cols[0] * -factor;
            cols[1] -= cols[1] * -factor;
            cols[2] -= cols[2] * -factor;
        }
        return new ColorWithAlternatives(cols[0], cols[1], cols[2], cols[3], null);
    }



    /**
     * Indicates whether the color is a gray value.
     * @param col the color
     * @return true if it is a gray value
     */
    public static boolean isGray(Color col) {
        return (col.getRed() == col.getBlue() && col.getRed() == col.getGreen());
    }

    /**
     * Creates an uncalibrated CMYK color with the given gray value.
     * @param black the gray component (0 - 1)
     * @return the CMYK color
     */
    public static Color toCMYKGrayColor(float black) {
        //Calculated color components
        float[] cmyk = new float[] {0f, 0f, 0f, 1.0f - black};
        //Create native color
        return DeviceCMYKColorSpace.createCMYKColor(cmyk);
    }

    /**
     * Converts an arbitrary {@link Color} to a plain sRGB color doing the conversion at the
     * best possible conversion quality.
     * @param col the original color
     * @return the sRGB equivalent
     */
    public static Color toSRGBColor(Color col) {
        if (col.getColorSpace().isCS_sRGB()) {
            return col; //Don't convert if already sRGB to avoid conversion differences
        }
        float[] comps = col.getColorComponents(null);
        float[] srgb = col.getColorSpace().toRGB(comps);
        comps = col.getComponents(null);
        float alpha = comps[comps.length - 1];
        return new Color(srgb[0], srgb[1], srgb[2], alpha);
    }

    /**
     * Checks if two colors are the same color. This check is much more restrictive than
     * {@link Color#equals(Object)} in that it doesn't only check if both colors result in the
     * same sRGB value. For example, if two colors not of the same exact class are compared,
     * they are treated as not the same.
     * <p>
     * Note: At the moment, this method only supports {@link Color} and
     * {@link ColorWithAlternatives} only. Other subclasses of {@link Color} are checked only using
     * the {@link Color#equals(Object)} method.
     * @param col1 the first color
     * @param col2 the second color
     * @return true if both colors are the same color
     */
    public static boolean isSameColor(Color col1, Color col2) {
        //Check fallback sRGB values first, then go into details
        if (!col1.equals(col2)) {
            return false;
        }

        //Consider same-ness only between colors of the same class (not subclasses)
        //but consider a ColorWithAlternatives without alternatives to be the same as a Color.
        Class<?> cl1 = col1.getClass();
        if (col1 instanceof ColorWithAlternatives
                && !((ColorWithAlternatives) col1).hasAlternativeColors()) {
            cl1 = Color.class;
        }
        Class<?> cl2 = col2.getClass();
        if (col2 instanceof ColorWithAlternatives
                && !((ColorWithAlternatives) col2).hasAlternativeColors()) {
            cl2 = Color.class;
        }
        if (cl1 != cl2) {
            return false;
        }

        //Check color space
        if (!col1.getColorSpace().equals(col2.getColorSpace())) {
            return false;
        }

        //Check native components
        float[] comps1 = col1.getComponents(null);
        float[] comps2 = col2.getComponents(null);
        if (comps1.length != comps2.length) {
            return false;
        }
        for (int i = 0, c = comps1.length; i < c; i++) {
            if (comps1[i] != comps2[i]) {
                return false;
            }
        }

        //Compare alternative colors, order is relevant
        if (col1 instanceof ColorWithAlternatives && col2 instanceof ColorWithAlternatives) {
            ColorWithAlternatives ca1 = (ColorWithAlternatives) col1;
            ColorWithAlternatives ca2 = (ColorWithAlternatives) col2;
            return ca1.hasSameAlternativeColors(ca2);
        }

        return true;
    }

}
