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

import java.awt.color.ICC_Profile;

/**
 * Enumeration for rendering intents.
 */
public enum RenderingIntent {

    /** Perceptual rendering intent. Typical use: scanned images. */
    PERCEPTUAL(ICC_Profile.icPerceptual),
    /** Relative colorimetric rendering intent. Typical use: vector graphics. */
    RELATIVE_COLORIMETRIC(ICC_Profile.icRelativeColorimetric),
    /** Absolute colorimetric rendering intent. Typical use: logos and solid colors. */
    ABSOLUTE_COLORIMETRIC(ICC_Profile.icAbsoluteColorimetric),
    /** Saturation rendering intent. Typical use: business graphics. */
    SATURATION(ICC_Profile.icSaturation),
    /** Automatic rendering intent. The color profile's intent isn't overridden. */
    AUTO(4);

    private int intValue;

    private RenderingIntent(int value) {
        this.intValue = value;
    }

    /**
     * Returns an integer value identifying the rendering intent. This is the same value defined
     * by the ICC specification (0..3) plus one for "auto" (4). (See also {@link ICC_Profile}.ic*)
     * @return the integer value
     */
    public int getIntegerValue() {
        return this.intValue;
    }

    /**
     * Returns the enum value for the given integer rendering intent (as defined by the ICC
     * specification).
     * @param value the rendering intent as ICC value
     * @return the matching enum
     */
    public static RenderingIntent fromICCValue(int value) {
        switch (value) {
        case ICC_Profile.icPerceptual: return PERCEPTUAL;
        case ICC_Profile.icRelativeColorimetric: return RELATIVE_COLORIMETRIC;
        case ICC_Profile.icAbsoluteColorimetric: return ABSOLUTE_COLORIMETRIC;
        case ICC_Profile.icSaturation: return SATURATION;
        default:
            throw new IllegalArgumentException("Invalid value for rendering intent: " + value);
        }
    }

}
