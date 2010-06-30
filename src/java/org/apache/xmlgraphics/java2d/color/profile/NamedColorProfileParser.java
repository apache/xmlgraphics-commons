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
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;

/**
 * This class is a parser for ICC named color profiles. It uses Java's {@link ICC_Profile} class
 * for parsing the basic structure but adds functionality to parse certain profile tags.
 */
public class NamedColorProfileParser {

    private static final int MLUC = 0x6D6C7563; //'mluc'
    private static final int NCL2 = 0x6E636C32; //'ncl2'

    /**
     * Indicates whether the profile is a named color profile.
     * @param profile the color profile
     * @return true if the profile is a named color profile, false otherwise
     */
    public static boolean isNamedColorProfile(ICC_Profile profile) {
        return profile.getProfileClass() == ICC_Profile.CLASS_NAMEDCOLOR;
    }

    /**
     * Parses a named color profile (NCP).
     * @param profile the profile to analyze
     * @return an object representing the parsed NCP
     * @throws IOException if an I/O error occurs
     */
    public NamedColorProfile parseProfile(ICC_Profile profile) throws IOException {
        if (!isNamedColorProfile(profile)) {
            throw new IllegalArgumentException("Given profile is not a named color profile (NCP)");
        }
        String profileDescription = getProfileDescription(profile);
        String copyright = getCopyright(profile);
        NamedColorSpace[] ncs = readNamedColors(profile);
        return new NamedColorProfile(profileDescription, copyright, ncs);
    }

    private String getProfileDescription(ICC_Profile profile) throws IOException {
        byte[] tag = profile.getData(ICC_Profile.icSigProfileDescriptionTag);
        return readSimpleString(tag);
    }

    private String getCopyright(ICC_Profile profile) throws IOException {
        byte[] tag = profile.getData(ICC_Profile.icSigCopyrightTag);
        return readSimpleString(tag);
    }

    private NamedColorSpace[] readNamedColors(ICC_Profile profile) throws IOException {
        byte[] tag = profile.getData(ICC_Profile.icSigNamedColor2Tag);
        DataInput din = new DataInputStream(new ByteArrayInputStream(tag));
        int sig = din.readInt();
        if (sig != NCL2) {
            throw new UnsupportedOperationException("Unsupported structure type: "
                    + toSignatureString(sig) + ". Expected " + toSignatureString(NCL2));
        }
        din.skipBytes(8);
        int numColors = din.readInt();
        NamedColorSpace[] result = new NamedColorSpace[numColors];
        int numDeviceCoord = din.readInt();
        String prefix = readAscii(din, 32);
        String suffix = readAscii(din, 32);
        for (int i = 0; i < numColors; i++) {
            String name = prefix + readAscii(din, 32) + suffix;
            int[] pcs = readUInt16Array(din, 3);
            float[] colorvalue = new float[3];
            for (int j = 0; j < pcs.length; j++) {
                colorvalue[j] = ((float)pcs[j]) / 0x8000;
            }

            //device coordinates are ignored for now
            /*int[] deviceCoord =*/ readUInt16Array(din, numDeviceCoord);

            switch (profile.getPCSType()) {
            case ColorSpace.TYPE_XYZ:
                result[i] = new NamedColorSpace(name, colorvalue);
                break;
            case ColorSpace.TYPE_Lab:
                //Not sure if this always D50 here,
                //but the illuminant in the header is fixed to D50.
                CIELabColorSpace labCS = ColorSpaces.getCIELabColorSpaceD50();
                result[i] = new NamedColorSpace(name, labCS.toColor(colorvalue, 1.0f));
                break;
            default:
                throw new UnsupportedOperationException(
                        "PCS type is not supported: " + profile.getPCSType());
            }
        }
        return result;
    }

    private int[] readUInt16Array(DataInput din, int count) throws IOException {
        if (count == 0) {
            return null;
        }
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            int v = din.readUnsignedShort();
            result[i] = v;
        }
        return result;
    }

    private String readAscii(DataInput in, int maxLength) throws IOException {
        byte[] data = new byte[maxLength];
        in.readFully(data);
        String result = new String(data, "US-ASCII");
        int idx = result.indexOf('\0');
        if (idx >= 0) {
            result = result.substring(0, idx);
        }
        return result;
    }

    private String readSimpleString(byte[] tag) throws IOException {
        DataInput din = new DataInputStream(new ByteArrayInputStream(tag));
        int sig = din.readInt();
        if (sig == MLUC) {
            return readMLUC(din);
        } else {
            return null; //Unsupported tag structure type
        }
    }

    private String readMLUC(DataInput din) throws IOException {
        din.skipBytes(16);
        int firstLength = din.readInt();
        int firstOffset = din.readInt();
        int offset = 28;
        din.skipBytes(firstOffset - offset);
        byte[] utf16 = new byte[firstLength];
        din.readFully(utf16);
        return new String(utf16, "UTF-16BE");
    }

    private String toSignatureString(int sig) {
        StringBuffer sb = new StringBuffer();
        sb.append((char)(sig >> 24 & 0xFF));
        sb.append((char)(sig >> 16 & 0xFF));
        sb.append((char)(sig >> 8 & 0xFF));
        sb.append((char)(sig & 0xFF));
        return sb.toString();
    }

}
