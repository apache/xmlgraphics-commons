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

package org.apache.xmlgraphics.java2d.color.profile;

import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;

/**
 * Simplified in-memory representation of an ICC named color profile.
 */
public class NamedColorProfile {

    private String profileName;
    private String copyright;
    private NamedColorSpace[] namedColors;
    private RenderingIntent renderingIntent = RenderingIntent.PERCEPTUAL;

    /**
     * Creates a new named color profile.
     * @param profileName the profile name
     * @param copyright the copyright
     * @param namedColors the array of named colors
     * @param intent the rendering intent
     */
    public NamedColorProfile(String profileName, String copyright, NamedColorSpace[] namedColors,
            RenderingIntent intent) {
        this.profileName = profileName;
        this.copyright = copyright;
        this.namedColors = namedColors;
        this.renderingIntent = intent;
    }

    /**
     * Returns the color profile's rendering intent.
     * @return the rendering intent
     * (See {@link java.awt.color.ICC_Profile}.ic*)
     */
    public RenderingIntent getRenderingIntent() {
        return this.renderingIntent;
    }

    /**
     * Returns the array of named colors.
     * @return the array of named colors
     */
    public NamedColorSpace[] getNamedColors() {
        NamedColorSpace[] copy = new NamedColorSpace[this.namedColors.length];
        System.arraycopy(this.namedColors, 0, copy, 0, this.namedColors.length);
        return copy;
    }

    /**
     * Returns a named color.
     * @param name the color name
     * @return the named color (or null if it is not available)
     */
    public NamedColorSpace getNamedColor(String name) {
        if (this.namedColors != null) {
            for (NamedColorSpace namedColor : this.namedColors) {
                if (namedColor.getColorName().equals(name)) {
                    return namedColor;
                }
            }
        }
        return null;
    }

    /**
     * Returns the profile name.
     * @return the profile name
     */
    public String getProfileName() {
        return this.profileName;
    }

    /**
     * Returns the profile copyright.
     * @return the profile copyright
     */
    public String getCopyright() {
        return this.copyright;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Named color profile: ");
        sb.append(getProfileName());
        sb.append(", ").append(namedColors.length).append(" colors");
        return sb.toString();
    }

}
