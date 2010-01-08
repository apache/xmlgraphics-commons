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

/* $Id: ColorUtil.java 815938 2009-09-16 19:38:13Z jeremias $ */

package org.apache.xmlgraphics.java2d;

import java.awt.Color;

/**
 * Generic Color helper class.
 * <p>
 * This class supports parsing string values into color values and creating
 * color values for strings. It provides a list of standard color names.
 */
public final class ColorUtil {

    /** The name for the uncalibrated CMYK pseudo-profile */
    public static final String CMYK_PSEUDO_PROFILE = "#CMYK";

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
        return new Color(cols[0], cols[1], cols[2], cols[3]);
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
     * Creates an uncalibrary CMYK color with the given gray value.
     * @param black the gray component (0 - 1)
     * @return the CMYK color
     */
    public static Color toCMYKGrayColor(float black) {
        float[] cmyk = new float[] {0f, 0f, 0f, 1.0f - black};
        CMYKColorSpace cmykCs = CMYKColorSpace.getInstance();
        float[] rgb = cmykCs.toRGB(cmyk);
        return ColorExt.createFromFoRgbIcc(rgb[0], rgb[1], rgb[2],
                CMYK_PSEUDO_PROFILE, null, cmykCs, cmyk);
    }
}
