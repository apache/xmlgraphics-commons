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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

import junit.framework.TestCase;

/**
 * Test class of DoubleFormatUtil
 */
public class DoubleFormatUtilTest extends TestCase {

    /**
     * Test simple values as specified in the format contract.
     * <p>
     * Note: Some of these tests will fail if formatFast is used.
     */
    public void testSimple() {
        int decimals = 4;
        int precision = 8;

        double value = 0.0;
        String expected = "0";
        String actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 0.1;
        expected = "0.1";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 1234.1;
        expected = "1234.1";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        // rounding
        value = 1234.1234567;
        expected = "1234.1235";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 1234.99995;
        expected = "1235";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = -1234.99995;
        expected = "-1235";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 1234.99994999;
        expected = "1234.9999";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        // decimals / precision switch
        value = 0.00000001;
        expected = "0.00000001";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = -0.00000001;
        expected = "-0.00000001";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 72.00001234;
        expected = "72";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        // limit precision
        value = 0.000000001;
        expected = "0";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 5.0e-9;
        expected = "0.00000001";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 4.9999999999e-9;
        expected = "0";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 2.0005e-5;
        expected = "0.00002001";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);

        value = 2.00049999999999e-5;
        expected = "0.00002";
        actual = format(value, decimals, precision);
        assertEquals(value, decimals, precision, expected, actual);
    }

    /**
     * AssertEquals with a more detailed message
     */
    private static void assertEquals(double value, int decimals, int precision, String expected, String actual) {
        assertEquals("value: " + value + ", decimals: " + decimals + ", precision: " + precision, expected, actual);
    }

    /**
     * The buffer used to format
     */
    private StringBuffer buf = new StringBuffer();

    /**
     * Formats using FormatUtil#formatDouble method
     */
    private String format(double value, int decimals, int precision) {
        buf.setLength(0);
        DoubleFormatUtil.formatDouble(value, decimals, precision, buf);
        return buf.toString();
    }

    /**
     * Formats using FormatUtil#formatDoublePrecise method
     */
    private String formatPrecise(double value, int decimals, int precision) {
        buf.setLength(0);
        DoubleFormatUtil.formatDoublePrecise(value, decimals, precision, buf);
        return buf.toString();
    }

    /**
     * Formats using FormatUtil#formatDoubleFast method
     */
    private String formatFast(double value, int decimals, int precision) {
        buf.setLength(0);
        DoubleFormatUtil.formatDoubleFast(value, decimals, precision, buf);
        return buf.toString();
    }

    /**
     * Formats using a BigDecimal. This is the reference (always returns the correct format)
     * whereas DecimalFormat may have some formating errors regarding the last digit.
     */
    private String refFormat(double value, int decimals, int precision) {
        buf.setLength(0);
        BigDecimal bg = new BigDecimal(Double.toString(value));
        int scale = Math.abs(value) < 1.0 ? precision : decimals;
        bg = bg.setScale(scale, BigDecimal.ROUND_HALF_UP);
        //buf.append(bg.toString()); // Java 1.4
        buf.append(bg.toPlainString()); // Java 1.5 and more !
        if (buf.indexOf(".") >= 0) {
            for (int i = buf.length() - 1; i > 1 && buf.charAt(i) == '0'; i--) {
                buf.setLength(i);
            }
            if (buf.charAt(buf.length() - 1) == '.') {
                buf.setLength(buf.length() - 1);
            }
        }
        return buf.toString();
    }

    /**
     * The decimal format used within formatDf method
     */
    private DecimalFormat df = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));

    /**
     * Formats using DecimalFormat#format method
     */
    private String formatDf(double value, int decimals, int precision) {
        int scale = Math.abs(value) < 1.0 ? precision : decimals;
        df.setMaximumFractionDigits(scale);
        return df.format(value);
    }

    /**
     * The maximum power of ten to use when testing high values double
     */
    private static final int maxPow = 12;

    /**
     * Tests the formatPrecise method against the reference, with random values
     */
    public void testPrecise() {
        long seed = System.currentTimeMillis();
        Random r = new Random();
        r.setSeed(seed);

        double value, highValue, lowValue;
        int nbTest = 10000;
        int maxDecimals = 12;
        
        String actual, expected;
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            expected = refFormat(value, decimals, precision);
            actual = formatPrecise(value, decimals, precision);
            assertEquals(value, decimals, precision, expected, actual);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            expected = refFormat(highValue, decimals, precision);
            actual = formatPrecise(highValue, decimals, precision);
            assertEquals(highValue, decimals, precision, expected, actual);

            lowValue = (value - 1) / 1000;
            expected = refFormat(lowValue, decimals, precision);
            actual = formatPrecise(lowValue, decimals, precision);
            assertEquals(lowValue, decimals, precision, expected, actual);
        }
    }

    /**
     * Tests the format method against the reference, with random values
     */
    public void testFormat() {
        long seed = System.currentTimeMillis();
        Random r = new Random();
        r.setSeed(seed);

        double value, highValue, lowValue;
        int nbTest = 10000;
        int maxDecimals = 12;

        String actual, expected;
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            expected = refFormat(value, decimals, precision);
            actual = format(value, decimals, precision);
            assertEquals(value, decimals, precision, expected, actual);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            expected = refFormat(highValue, decimals, precision);
            actual = format(highValue, decimals, precision);
            assertEquals(highValue, decimals, precision, expected, actual);

            lowValue = (value - 1) / 1000;
            expected = refFormat(lowValue, decimals, precision);
            actual = format(lowValue, decimals, precision);
            assertEquals(lowValue, decimals, precision, expected, actual);
        }
    }

    /**
     * Tests the formatFast method against the reference, with random values.
     * Disabled since the formatFast method is not accurate.
     */
    public void fast() {
        long seed = System.currentTimeMillis();
        Random r = new Random();
        r.setSeed(seed);

        double value, highValue, lowValue;
        int nbTest = 10000;
        int maxDecimals = 12;
        
        String actual, expected;
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            expected = refFormat(value, decimals, precision);
            actual = formatFast(value, decimals, precision);
            assertEquals(value, decimals, precision, expected, actual);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            expected = refFormat(highValue, decimals, precision);
            actual = formatFast(highValue, decimals, precision);
            assertEquals(highValue, decimals, precision, expected, actual);

            lowValue = (value - 1) / 1000;
            expected = refFormat(lowValue, decimals, precision);
            actual = formatFast(lowValue, decimals, precision);
            assertEquals(lowValue, decimals, precision, expected, actual);
        }
    }

    /**
     * Performance comparison of the differents formatXXX methods,
     * to see which one is the fastest in the same conditions.
     */
    public void performanceCompare() {
        // Rename this method in testPerformanceCompare to run it within JUnit tests
        // This method is quite long (depends of the value of nbTest).
        long seed = System.currentTimeMillis();
        Random r = new Random();
        r.setSeed(seed);

        double value, highValue, lowValue;
        long start = System.currentTimeMillis();
        int nbTest = 100000;
        int maxDecimals = 10;

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            format(value, decimals, precision);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            format(highValue, decimals, precision);

            lowValue = (value - 1) / 1000;
            format(lowValue, decimals, precision);
        }
        long formatDuration = System.currentTimeMillis() - start;
        System.out.println("Format duration: " + formatDuration + "ms to format " + (3 * nbTest) + " doubles");

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            formatPrecise(value, decimals, precision);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            formatPrecise(highValue, decimals, precision);

            lowValue = (value - 1) / 1000;
            formatPrecise(lowValue, decimals, precision);
        }
        long preciseFormatDuration = System.currentTimeMillis() - start;
        System.out.println("Format Precise duration: " + preciseFormatDuration + "ms to format " + (3 * nbTest) + " doubles");

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            formatFast(value, decimals, precision);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            formatFast(highValue, decimals, precision);

            lowValue = (value - 1) / 1000;
            formatFast(lowValue, decimals, precision);
        }
        long fastFormatDuration = System.currentTimeMillis() - start;
        System.out.println("Fast Format duration: " + fastFormatDuration + "ms to format " + (3 * nbTest) + " doubles");

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            refFormat(value, decimals, precision);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            refFormat(highValue, decimals, precision);

            lowValue = (value - 1) / 1000;
            refFormat(lowValue, decimals, precision);
        }
        long bgDuration = System.currentTimeMillis() - start;
        System.out.println("BigDecimal format duration: " + bgDuration + "ms to format " + (3 * nbTest) + " doubles");

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            value = 1 + r.nextDouble(); // Use decimals and not precision
            formatDf(value, decimals, precision);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            formatDf(highValue, decimals, precision);

            lowValue = (value - 1) / 1000;
            formatDf(lowValue, decimals, precision);
        }
        long dfDuration = System.currentTimeMillis() - start;
        System.out.println("DecimalFormat duration: " + dfDuration + "ms to format " + (3 * nbTest) + " doubles");

        r.setSeed(seed);
        start = System.currentTimeMillis();
        for (int i = nbTest; i > 0; i--) {
            int decimals = r.nextInt(maxDecimals);
            int precision = decimals + 3;
            precision++; // Avoid warning unused local variable
            value = 1 + r.nextDouble(); // Use decimals and not precision
            Double.toString(value);

            highValue = value * DoubleFormatUtil.tenPow(r.nextInt(maxPow));
            Double.toString(highValue);

            lowValue = (value - 1) / 1000;
            Double.toString(lowValue);
        }
        long toStringDuration = System.currentTimeMillis() - start;
        System.out.println("toString duration: " + toStringDuration + "ms to format " + (3 * nbTest) + " doubles");
    }
}
