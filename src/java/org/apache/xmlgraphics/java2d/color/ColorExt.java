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
 * Extended {@link Color} class allowing to specify a prioritized list of alternate colors.
 * An instance of this class should normally be an sRGB fallback color. The alternate colors
 * shall be the ones that are preferred if an output format supports them. This is done so because
 * it allows to specify the exact sRGB value that should be used if the alternate color spaces
 * are not supported. Colors in other color spaces will often not return the exact desired sRGB
 * value when converting native color values to sRGB through their {@link ColorSpace} instance.
 */
public final class ColorExt extends Color {

    private static final long serialVersionUID = -6125884937776779150L;

    private Color[] alternateColors;

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float, float)
     */
    public ColorExt(float r, float g, float b, float a, Color[] alternateColors) {
        super(r, g, b, a);
        initAlternateColors(alternateColors);
    }

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(float, float, float)
     */
    public ColorExt(float r, float g, float b, Color[] alternateColors) {
        super(r, g, b);
        initAlternateColors(alternateColors);
    }

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(int, boolean)
     */
    public ColorExt(int rgba, boolean hasalpha, Color[] alternateColors) {
        super(rgba, hasalpha);
        initAlternateColors(alternateColors);
    }

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int, int)
     */
    public ColorExt(int r, int g, int b, int a, Color[] alternateColors) {
        super(r, g, b, a);
        initAlternateColors(alternateColors);
    }

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(int, int, int)
     */
    public ColorExt(int r, int g, int b, Color[] alternateColors) {
        super(r, g, b);
        initAlternateColors(alternateColors);
    }

    /**
     * @param alternateColors the prioritized list of alternative colors.
     * @see Color#Color(int)
     */
    public ColorExt(int rgb, Color[] alternateColors) {
        super(rgb);
        initAlternateColors(alternateColors);
    }

    private void initAlternateColors(Color[] colors) {
        if (colors != null) {
            //Colors are immutable but array are not, so copy
            this.alternateColors = new Color[colors.length];
            System.arraycopy(colors, 0, this.alternateColors, 0, colors.length);
        }
    }

    /**
     * Returns the list of alternate colors. An empty array will be returned if no alternative
     * colors are available.
     * @return the list of alternate colors
     */
    public Color[] getAlternateColors() {
        if (this.alternateColors != null) {
            Color[] cols = new Color[this.alternateColors.length];
            System.arraycopy(this.alternateColors, 0, cols, 0, this.alternateColors.length);
            return cols;
        } else {
            return new Color[0];
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (!(obj instanceof Color)) {
            return false;
        }
        Color otherCol = (Color)obj;
        if (getRGB() != otherCol.getRGB()) {
            return false;
        }
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
        if (getClass() != obj.getClass() && this.alternateColors != null) {
            //We're quite strict here to preserve the additional functionality of ColorExt.
            //If we don't do this, a renderer may not detect the difference between a ColorExt
            //and a Color with the same sRGB fallback but with additional color alternatives
            //taking precedence in some renderers.
            return false;
        }
        if (getClass() == obj.getClass() ) {
            ColorExt other = (ColorExt)obj;
            if (this.alternateColors == null && other.alternateColors != null) {
                return false;
            } else if (this.alternateColors != null && other.alternateColors == null) {
                return false;
            }
            if (this.alternateColors != null
                    && this.alternateColors.length != other.alternateColors.length) {
                return false;
            }
            if (this.alternateColors != null) {
                for (int i = 0, c = this.alternateColors.length; i < c; i++) {
                    Color col1 = this.alternateColors[i];
                    Color col2 = other.alternateColors[i];
                    if (!col1.equals(col2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
