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

/* $Id: ColorExt.java 884117 2009-11-25 14:42:48Z jeremias $ */

package org.apache.xmlgraphics.java2d.color;

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * Extended {@link Color} class allowing to specify a prioritized list of alternative colors.
 * The alternative colors shall be the ones that are preferred if an output format supports them.
 * This is normally used for passing device-specific colors through to the output format.
 * <p>
 * An additional benefit of this class is a better {@link ColorWithAlternatives#equals(Object)}
 * method than the one from {@link Color} which only takes the sRGB values into account.
 * <p>
 * This class only adds a single reference to a color array which should not increase memory
 * consumption by much if no alternative colors are specified.
 */
public class ColorWithAlternatives extends Color {

    private static final long serialVersionUID = -6125884937776779150L;

    private Color[] alternativeColors;

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float, float)
     */
    public ColorWithAlternatives(float r, float g, float b, float a, Color[] alternativeColors) {
        super(r, g, b, a);
        initAlternativeColors(alternativeColors);
    }

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float)
     */
    public ColorWithAlternatives(float r, float g, float b, Color[] alternativeColors) {
        super(r, g, b);
        initAlternativeColors(alternativeColors);
    }

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, boolean)
     */
    public ColorWithAlternatives(int rgba, boolean hasalpha, Color[] alternativeColors) {
        super(rgba, hasalpha);
        initAlternativeColors(alternativeColors);
    }

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int, int)
     */
    public ColorWithAlternatives(int r, int g, int b, int a, Color[] alternativeColors) {
        super(r, g, b, a);
        initAlternativeColors(alternativeColors);
    }

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int)
     */
    public ColorWithAlternatives(int r, int g, int b, Color[] alternativeColors) {
        super(r, g, b);
        initAlternativeColors(alternativeColors);
    }

    /**
     * @param alternativeColors the prioritized list of alternative colors.
     * @see Color#Color(int)
     */
    public ColorWithAlternatives(int rgb, Color[] alternativeColors) {
        super(rgb);
        initAlternativeColors(alternativeColors);
    }

    /**
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
     * Returns the first alternative color found with the given color space type.
     * @param colorSpaceType the color space type ({@link ColorSpace}.TYPE_*).
     * @return the requested alternative color or null, if no match was found
     */
    public Color getFirstAlternativeOfType(int colorSpaceType) {
        if (hasAlternativeColors()) {
            for (int i = 0, c = this.alternativeColors.length; i < c; i++) {
                if (this.alternativeColors[i].getColorSpace().getType() == colorSpaceType) {
                    return this.alternativeColors[i];
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (!(obj instanceof Color)) {
            return false;
        }
        Color otherCol = (Color)obj;
        /* java.awt.Color from Sun does not consistenly convert floats to [0..255]
        if (getRGB() != otherCol.getRGB()) {
            return false;
        }*/
        if (!getColorSpace().equals(otherCol.getColorSpace())) {
            return false;
        }
        float[] comps = getComponents(null);
        float[] otherComps = otherCol.getComponents(null);
        if (comps.length != otherComps.length) {
            return false;
        }
        for (int i = 0, c = comps.length; i < c; i++) {
            if (comps[i] != otherComps[i]) {
                return false;
            }
        }
        if (getClass() != obj.getClass() && this.alternativeColors != null) {
            //We're quite strict here to preserve the additional functionality of this class.
            //If we don't do this, a renderer may not detect the difference between this class
            //and a Color with the same sRGB value but with additional color alternatives
            //taking precedence in some renderers.
            return false;
        }
        if (getClass() == obj.getClass() ) {
            ColorWithAlternatives other = (ColorWithAlternatives)obj;
            if (this.alternativeColors == null && other.alternativeColors != null) {
                return false;
            } else if (this.alternativeColors != null && other.alternativeColors == null) {
                return false;
            }
            if (this.alternativeColors != null
                    && this.alternativeColors.length != other.alternativeColors.length) {
                return false;
            }
            if (this.alternativeColors != null) {
                for (int i = 0, c = this.alternativeColors.length; i < c; i++) {
                    Color col1 = this.alternativeColors[i];
                    Color col2 = other.alternativeColors[i];
                    if (!col1.equals(col2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
