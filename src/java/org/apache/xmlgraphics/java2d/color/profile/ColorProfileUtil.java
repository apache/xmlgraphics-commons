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

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

// CSOFF: MethodName

/**
 * Helper methods for handling color profiles.
 */
public final class ColorProfileUtil {

    private ColorProfileUtil() {
    }

    /**
     * Returns the profile description of an ICC profile
     * @param profile the profile
     * @return the description
     */
    public static String getICCProfileDescription(ICC_Profile profile) {
        byte[] data = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        if (data == null) {
            return null;
        } else {
            //Info on the data format: http://www.color.org/ICC-1_1998-09.PDF
            int length = (data[8] << 3 * 8) | (data[9] << 2 * 8) | (data[10] << 8) | data[11];
            length--; //Remove trailing NUL character
            try {
                return new String(data, 12, length, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException("Incompatible VM");
            }
        }
    }

    /**
     * Indicates whether a given color profile is identical to the default sRGB profile
     * provided by the Java class library.
     * @param profile the color profile to check
     * @return true if it is the default sRGB profile
     */
    public static boolean isDefaultsRGB(ICC_Profile profile) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ICC_Profile sRGBProfile = null;
        if (sRGB instanceof ICC_ColorSpace) {
            sRGBProfile = ((ICC_ColorSpace)sRGB).getProfile();
        }
        return profile == sRGBProfile;
    }

    /**
     * Proxy method for {@link ICC_Profile#getInstance(byte[])}
     * that properly synchronizes the call to avoid a potential race condition.
     * @param data    the specified ICC Profile data
     * @return  an {@link ICC_Profile} instance corresponding to the data in the
     *          specified byte array
     */
    public static ICC_Profile getICC_Profile(byte[] data) {
        synchronized (ICC_Profile.class) {
            return ICC_Profile.getInstance(data);
        }
    }

    /**
     * Proxy method for {@link ICC_Profile#getInstance(int)}
     * that properly synchronizes the call to avoid a potential race condition.
     * @param colorSpace    the type of color space to create a profile for. The specified type is
     *                      one of the color space constants defined in the {@link ColorSpace}
     *                      class.
     * @return  an {@link ICC_Profile} instance corresponding to the specified {@code ColorSpace}
     * @throws IllegalArgumentException if {@code colorSpace} is not one of the predefined types
     */
    public static ICC_Profile getICC_Profile(int colorSpace) {
        synchronized (ICC_Profile.class) {
            return ICC_Profile.getInstance(colorSpace);
        }
    }

    /**
     * Proxy method for {@link ICC_Profile#getInstance(java.io.InputStream)}
     * that properly synchronizes the call to avoid a potential race condition.
     * @param in    the input stream from which to read the profile data
     * @return  an {@link ICC_Profile} instance corresponding to the data in the
     *          specified {@link InputStream}
     * @throws IOException  if an I/O error occurs while reading the stream
     * @throws IllegalArgumentException if the stream does not contain valid ICC Profile data
     */
    public static ICC_Profile getICC_Profile(InputStream in) throws IOException {
        synchronized (ICC_Profile.class) {
            return ICC_Profile.getInstance(in);
        }
    }

    /**
     * Proxy method for {@link ICC_Profile#getInstance(java.lang.String)}
     * that properly synchronizes the call to avoid a potential race condition.
     * @param fileName    the name of the file that contains the profile data
     * @return  an {@link ICC_Profile} instance corresponding to the data in the specified file
     * @throws IOException  if the file cannot be opened, or an I/O error occurs while reading
     *          the stream
     * @throws IllegalArgumentException if the stream does not contain valid ICC Profile data
     * @throws SecurityException if a security manager is installed and it does not permit read
     *          access to the given file.
     */
    public static ICC_Profile getICC_Profile(String fileName) throws IOException {
        synchronized (ICC_Profile.class) {
            return ICC_Profile.getInstance(fileName);
        }
    }

}
