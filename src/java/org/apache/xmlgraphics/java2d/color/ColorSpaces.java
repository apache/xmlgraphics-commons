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
 * Provides access to various color spaces.
 */
public final class ColorSpaces {

    private static DeviceCMYKColorSpace deviceCMYK;
    private static CIELabColorSpace cieLabD50;
    private static CIELabColorSpace cieLabD65;

    private ColorSpaces() {
        //Don't instantiate this class
    }

    /**
     * Returns an instance of the device-specific CMYK color space.
     * @return an instance of the device-specific CMYK color space
     */
    public static synchronized DeviceCMYKColorSpace getDeviceCMYKColorSpace() {
        if (deviceCMYK == null) {
            deviceCMYK = new DeviceCMYKColorSpace();
        }
        return deviceCMYK;
    }

    /**
     * Indicates whether the given color space is device-specific (i.e. uncalibrated).
     * @param cs the color space to check
     * @return true if the color space is device-specific
     */
    public static boolean isDeviceColorSpace(ColorSpace cs) {
        return (cs instanceof AbstractDeviceSpecificColorSpace);
    }

    /**
     * Returns an instance of the CIE L*a*b* color space using the D50 white point.
     * @return an instance of the requested CIE L*a*b* color space
     */
    public static synchronized CIELabColorSpace getCIELabColorSpaceD50() {
        if (cieLabD50 == null) {
            cieLabD50 = new CIELabColorSpace(CIELabColorSpace.getD50WhitePoint());
        }
        return cieLabD50;
    }

    /**
     * Returns an instance of the CIE L*a*b* color space using the D65 white point.
     * @return an instance of the requested CIE L*a*b* color space
     */
    public static synchronized CIELabColorSpace getCIELabColorSpaceD65() {
        if (cieLabD65 == null) {
            cieLabD65 = new CIELabColorSpace(CIELabColorSpace.getD65WhitePoint());
        }
        return cieLabD65;
    }

    private static final ColorSpaceOrigin UNKNOWN_ORIGIN = new ColorSpaceOrigin() {

        public String getProfileURI() {
            return null;
        }

        public String getProfileName() {
            return null;
        }
    };

    /**
     * Returns information about the origin of a color space.
     * @param cs the color space
     * @return the origin information
     */
    public static ColorSpaceOrigin getColorSpaceOrigin(ColorSpace cs) {
        if (cs instanceof ColorSpaceOrigin) {
            return (ColorSpaceOrigin)cs;
        } else {
            return UNKNOWN_ORIGIN;
        }
    }

}
