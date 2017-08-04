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
import java.util.Arrays;

/**
 * Extended {@link Color} class allowing to specify a prioritized list of alternative colors.
 * The alternative colors shall be the ones that are preferred if an output format supports them.
 * This is normally used for passing device-specific colors through to the output format.
 * <p>
 * This class only adds a single reference to a color array which should not increase memory
 * consumption by much if no alternative colors are specified.
 * <p>
 * <b>Important:</b> Due to a flaw in {@link Color#equals(Object)}, the <code>equals()</code>
 * method should not be used to compare two colors, especially when used to update the current
 * color for some output format. {@link Color} only takes the sRGB values into account but not
 * more the advanced facets of this class. Use {@link ColorUtil#isSameColor(Color, Color)} for
 * such a check.
 */
public class ColorWithAlternatives extends Color {

    private static final long serialVersionUID = -6125884937776779150L;

    private Color[] alternativeColors;

    /**
     * Constructor for RGBA colors.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float, float)
     */
    public ColorWithAlternatives(float r, float g, float b, float a, Color[] alternativeColors) {
        super(r, g, b, a);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for RGB colors.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float)
     */
    public ColorWithAlternatives(float r, float g, float b, Color[] alternativeColors) {
        super(r, g, b);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for RGBA colors.
     * @param rgba the combined RGBA value
     * @param hasalpha true if the alpha bits are valid, false otherwise
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, boolean)
     */
    public ColorWithAlternatives(int rgba, boolean hasalpha, Color[] alternativeColors) {
        super(rgba, hasalpha);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for RGBA colors.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int, int)
     */
    public ColorWithAlternatives(int r, int g, int b, int a, Color[] alternativeColors) {
        super(r, g, b, a);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for RGB colors.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int)
     */
    public ColorWithAlternatives(int r, int g, int b, Color[] alternativeColors) {
        super(r, g, b);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for RGB colors.
     * @param rgb the combined RGB components
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int)
     */
    public ColorWithAlternatives(int rgb, Color[] alternativeColors) {
        super(rgb);
        initAlternativeColors(alternativeColors);
    }

    /**
     * Constructor for colors with an arbitrary color space.
     * @param cspace the color space
     * @param components the color components
     * @param alpha the alpha component
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(ColorSpace, float[], float)
     */
    public ColorWithAlternatives(ColorSpace cspace, float[] components, float alpha,
            Color[] alternativeColors) {
        super(cspace, components, alpha);
        initAlternativeColors(alternativeColors);
    }

    private void initAlternativeColors(Color[] colors) {
        if (colors != null) {
            //Colors are immutable but array are not, so copy
            this.alternativeColors = new Color[colors.length];
            System.arraycopy(colors, 0, this.alternativeColors, 0, colors.length);
        }
    }

    /**
     * Returns the list of alternative colors. An empty array will be returned if no alternative
     * colors are available.
     * @return the list of alternative colors
     */
    public Color[] getAlternativeColors() {
        if (this.alternativeColors != null) {
            Color[] cols = new Color[this.alternativeColors.length];
            System.arraycopy(this.alternativeColors, 0, cols, 0, this.alternativeColors.length);
            return cols;
        } else {
            return new Color[0];
        }
    }

    /**
     * Indicates whether alternative colors are available.
     * @return true if alternative colors are available.
     */
    public boolean hasAlternativeColors() {
        return this.alternativeColors != null && this.alternativeColors.length > 0;
    }

    /**
     * Indicates whether another instance has the same alternative colors.
     * @param col the color to compare the alternatives to
     * @return true if the same alternative colors are present
     */
    public boolean hasSameAlternativeColors(ColorWithAlternatives col) {
        if (!hasAlternativeColors()) {
            return !col.hasAlternativeColors();
        }
        // this.hasAlternativeColors()
        if (!col.hasAlternativeColors()) {
            return false;
        }
        // this.hasAlternativeColors() && col.hasAlternativeColors()
        Color[] alt1 = getAlternativeColors();
        Color[] alt2 = col.getAlternativeColors();
        if (alt1.length != alt2.length) {
            return false;
        }
        for (int i = 0, c = alt1.length; i < c; i++) {
            Color c1 = alt1[i];
            Color c2 = alt2[i];
            if (!ColorUtil.isSameColor(c1, c2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the first alternative color found with the given color space type.
     * @param colorSpaceType the color space type ({@link ColorSpace}.TYPE_*).
     * @return the requested alternative color or null, if no match was found
     */
    public Color getFirstAlternativeOfType(int colorSpaceType) {
        if (hasAlternativeColors()) {
            for (Color alternativeColor : this.alternativeColors) {
                if (alternativeColor.getColorSpace().getType() == colorSpaceType) {
                    return alternativeColor;
                }
            }
        }
        return null;
    }

    public int hashCode() {
        int hash = super.hashCode();
        if (alternativeColors != null) {
            hash = 37 * hash + Arrays.hashCode(alternativeColors);
        }
        return hash;
    }
}
