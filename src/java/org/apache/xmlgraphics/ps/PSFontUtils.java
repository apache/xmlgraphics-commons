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

package org.apache.xmlgraphics.ps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.fonts.Glyphs;
import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;
import org.apache.xmlgraphics.util.io.SubInputStream;

/**
 * Utility code for font handling in PostScript.
 */
public final class PSFontUtils {

    private PSFontUtils() {
    }

    /**
     * This method reads a Type 1 font from a stream and embeds it into a PostScript stream.
     * Note: Only the IBM PC Format as described in section 3.3 of the Adobe Technical Note #5040
     * is supported.
     * @param gen The PostScript generator
     * @param in the InputStream from which to read the Type 1 font
     * @throws IOException in case an I/O problem occurs
     */
    public static void embedType1Font(PSGenerator gen, InputStream in) throws IOException {
        boolean finished = false;
        while (!finished) {
            int segIndicator = in.read();
            if (segIndicator < 0) {
                throw new IOException("Unexpected end-of-file while reading segment indicator");
            } else if (segIndicator != 128) {
                throw new IOException("Expected ASCII 128, found: " + segIndicator);
            }
            int segType = in.read();
            if (segType < 0) {
                throw new IOException("Unexpected end-of-file while reading segment type");
            }
            int dataSegLen = 0;
            switch (segType) {
                case 1: //ASCII
                    dataSegLen = EndianUtils.readSwappedInteger(in);

                    BufferedReader reader = new BufferedReader(
                            new java.io.InputStreamReader(
                                    new SubInputStream(in, dataSegLen), "US-ASCII"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        gen.writeln(line);
                        }
                    break;
                case 2: //binary
                    dataSegLen = EndianUtils.readSwappedInteger(in);

                    SubInputStream sin = new SubInputStream(in, dataSegLen);
                    ASCIIHexOutputStream hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
                    IOUtils.copy(sin, hexOut);
                    gen.newLine();
                    break;
                case 3: //EOF
                    finished = true;
                    break;
                default: throw new IOException("Unsupported segment type: " + segType);
            }
        }
    }

    /** the PSResource representing the WinAnsiEncoding. */
    public static final PSResource WINANSI_ENCODING_RESOURCE
            = new PSResource(PSResource.TYPE_ENCODING, "WinAnsiEncoding");

    /**
     * Defines the WinAnsi encoding for use in PostScript files.
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O problem
     */
    public static void defineWinAnsiEncoding(PSGenerator gen) throws IOException {
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, WINANSI_ENCODING_RESOURCE);
        gen.writeln("/WinAnsiEncoding [");
        for (int i = 0; i < Glyphs.WINANSI_ENCODING.length; i++) {
            if (i > 0) {
                if ((i % 5) == 0) {
                    gen.newLine();
                } else {
                    gen.write(" ");
                }
            }
            final char ch = Glyphs.WINANSI_ENCODING[i];
            final String glyphname = Glyphs.charToGlyphName(ch);
            if ("".equals(glyphname)) {
                gen.write("/" + Glyphs.NOTDEF);
            } else {
                gen.write("/");
                gen.write(glyphname);
            }
        }
        gen.newLine();
        gen.writeln("] def");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(WINANSI_ENCODING_RESOURCE);
    }

    /** the PSResource representing the AdobeStandardCyrillicEncoding. */
    public static final PSResource ADOBECYRILLIC_ENCODING_RESOURCE
            = new PSResource(PSResource.TYPE_ENCODING, "AdobeStandardCyrillicEncoding");

    /**
     * Defines the AdobeStandardCyrillic encoding for use in PostScript files.
     * @param gen the PostScript generator
     * @throws IOException In case of an I/O problem
     */
    public static void defineAdobeCyrillicEncoding(PSGenerator gen) throws IOException {
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, ADOBECYRILLIC_ENCODING_RESOURCE);
        gen.writeln("/AdobeStandardCyrillicEncoding [");
        for (int i = 0; i < Glyphs.ADOBECYRILLIC_ENCODING.length; i++) {
            if (i > 0) {
                if ((i % 5) == 0) {
                    gen.newLine();
                } else {
                    gen.write(" ");
                }
            }
            final char ch = Glyphs.ADOBECYRILLIC_ENCODING[i];
            final String glyphname = Glyphs.charToGlyphName(ch);
            if ("".equals(glyphname)) {
                gen.write("/" + Glyphs.NOTDEF);
            } else {
                gen.write("/");
                gen.write(glyphname);
            }
        }
        gen.newLine();
        gen.writeln("] def");
        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(ADOBECYRILLIC_ENCODING_RESOURCE);
    }


    /**
     * Redefines the encoding of a font.
     * @param gen the PostScript generator
     * @param fontName the font name
     * @param encoding the new encoding (must be predefined in the PS file)
     * @throws IOException In case of an I/O problem
     */
    public static void redefineFontEncoding(PSGenerator gen, String fontName, String encoding)
                throws IOException {
        gen.writeln("/" + fontName + " findfont");
        gen.writeln("dup length dict begin");
        gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
        gen.writeln("  /Encoding " + encoding + " def");
        gen.writeln("  currentdict");
        gen.writeln("end");
        gen.writeln("/" + fontName + " exch definefont pop");
    }

}
