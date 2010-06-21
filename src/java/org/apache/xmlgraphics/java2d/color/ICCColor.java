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
import java.net.URI;

/**
 * This class extends AWT's {@link Color} class by fields for the profile name and the profile
 * URI in order to allow serialization of the color without having to serialize the full
 * color profile, too.
 */
public class ICCColor extends Color {

    private static final long serialVersionUID = -104707261086330666L;

    private String profileName;
    private URI profileURI;

    /**
     * Creates a new ICC-based color.
     * @param cspace the color space
     * @param profileName the color profile name
     * @param profileURI the color profile URI
     * @param components the component values
     * @param alpha the alpha value
     */
    public ICCColor(ColorSpace cspace, String profileName, URI profileURI,
            float[] components, float alpha) {
        super(cspace, components, alpha);
        this.profileName = profileName;
        this.profileURI = profileURI;
    }

    /**
     * Returns the name of the color profile as used in the SVG or XSL-FO.
     * @return the color profile name
     */
    public String getColorProfileName() {
        return this.profileName;
    }

    /**
     * Returns the URI identifying the color profile.
     * @return the URI identifying the color profile.
     */
    public URI getColorProfileURI() {
        return this.profileURI;
    }

}
