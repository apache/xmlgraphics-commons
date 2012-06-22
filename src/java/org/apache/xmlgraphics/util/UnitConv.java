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

package org.apache.xmlgraphics.util;

import java.awt.geom.AffineTransform;

/**
 * Utility class for unit conversions.
 */
public final class UnitConv {

    private UnitConv() {
    }

    /** conversion factory from millimeters to inches. */
    public static final float IN2MM = 25.4f;

    /** conversion factory from centimeters to inches. */
    public static final float IN2CM = 2.54f;

    /** conversion factory from inches to points. */
    public static final int IN2PT = 72;

    /** Describes the unit pica. */
    public static final String PICA = "pc";

    /** Describes the unit point. */
    public static final String POINT = "pt";

    /** Describes the unit millimeter. */
    public static final String MM = "mm";

    /** Describes the unit centimeter. */
    public static final String CM = "cm";

    /** Describes the unit inch. */
    public static final String INCH = "in";

    /** Describes the unit millipoint. */
    public static final String MPT = "mpt";

        /** Describes the unit pixel. */
    public static final String PX = "px";

    /**
     * Converts millimeters (mm) to points (pt)
     * @param mm the value in mm
     * @return the value in pt
     */
    public static double mm2pt(double mm) {
        return mm * IN2PT / IN2MM;
    }

    /**
     * Converts millimeters (mm) to millipoints (mpt)
     * @param mm the value in mm
     * @return the value in mpt
     */
    public static double mm2mpt(double mm) {
        return mm * 1000 * IN2PT / IN2MM;
    }

    /**
     * Converts points (pt) to millimeters (mm)
     * @param pt the value in pt
     * @return the value in mm
     */
    public static double pt2mm(double pt) {
        return pt * IN2MM / IN2PT;
    }

    /**
     * Converts millimeters (mm) to inches (in)
     * @param mm the value in mm
     * @return the value in inches
     */
    public static double mm2in(double mm) {
        return mm / IN2MM;
    }

    /**
     * Converts inches (in) to millimeters (mm)
     * @param in the value in inches
     * @return the value in mm
     */
    public static double in2mm(double in) {
        return in * IN2MM;
    }

    /**
     * Converts inches (in) to millipoints (mpt)
     * @param in the value in inches
     * @return the value in mpt
     */
    public static double in2mpt(double in) {
        return in * IN2PT * 1000;
    }

    /**
     * Converts inches (in) to points (pt)
     * @param in the value in inches
     * @return the value in pt
     */
    public static double in2pt(double in) {
        return in * IN2PT;
    }

    /**
     * Converts millipoints (mpt) to inches (in)
     * @param mpt the value in mpt
     * @return the value in inches
     */
    public static double mpt2in(double mpt) {
        return mpt / IN2PT / 1000;
    }

    /**
     * Converts millimeters (mm) to pixels (px)
     * @param mm the value in mm
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     */
    public static double mm2px(double mm, int resolution) {
        return mm2in(mm) * resolution;
    }

    /**
     * Converts millipoints (mpt) to pixels (px)
     * @param mpt the value in mpt
     * @param resolution the resolution in dpi (dots per inch)
     * @return the value in pixels
     */
    public static double mpt2px(double mpt, int resolution) {
        return mpt2in(mpt) * resolution;
    }

    /**
     * Converts a millipoint-based transformation matrix to points.
     * @param at a millipoint-based transformation matrix
     * @return a point-based transformation matrix
     */
    public static AffineTransform mptToPt(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        //Convert to points
        matrix[4] = matrix[4] / 1000;
        matrix[5] = matrix[5] / 1000;
        return new AffineTransform(matrix);
    }

    /**
     * Converts a point-based transformation matrix to millipoints.
     * @param at a point-based transformation matrix
     * @return a millipoint-based transformation matrix
     */
    public static AffineTransform ptToMpt(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        //Convert to millipoints
        matrix[4] = matrix[4] * 1000;
        matrix[5] = matrix[5] * 1000;
        return new AffineTransform(matrix);
    }

    /**
     * Convert the given unit length to a dimensionless integer representing
     * a whole number of base units (milli-points).
     *
     * @param value input unit value
     * @return int millipoints
     */
    public static int convert(String value) {
        double retValue = 0;
        if (value != null) {
            if (value.toLowerCase().indexOf(PX) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 1000;
            } else if (value.toLowerCase().indexOf(INCH) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 72000;
            } else if (value.toLowerCase().indexOf(CM) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 28346.4567;
            } else if (value.toLowerCase().indexOf(MM) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 2834.64567;
            } else if (value.toLowerCase().indexOf(MPT) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 3));
            } else if (value.toLowerCase().indexOf(POINT) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 1000;
            } else if (value.toLowerCase().indexOf(PICA) > 0) {
                retValue = Double.parseDouble(value.substring(0, value.length() - 2));
                retValue *= 12000;
            }
        }
        return (int)retValue;
    }
}
