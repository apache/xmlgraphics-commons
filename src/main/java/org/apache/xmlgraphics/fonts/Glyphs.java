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

package org.apache.xmlgraphics.fonts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

/**
 * This class provides a number of constants for glyph management.
 */
public final class Glyphs {

    private Glyphs() {
    }

    /**
     * Glyph name for the "notdef" glyph
     */
    public static final String NOTDEF = ".notdef";

    /**
     * Glyph names for Mac encoding
     * @deprecated That array was supposed to represent the standard Macintosh ordering
     *      of glyphs in a TrueType font (it does NOT correspond to the MacRoman encoding).
     *      In addition some entries are incorrect.
     */
    @Deprecated
    public static final String[] MAC_GLYPH_NAMES = {
        /* 0x00 */
        Glyphs.NOTDEF, ".null", "CR", "space", "exclam", "quotedbl", "numbersign",
                "dollar", "percent", "ampersand", "quotesingle", "parenleft",
                "parenright", "asterisk", "plus", "comma", /* 0x10 */
        "hyphen", "period", "slash", "zero", "one", "two", "three", "four",
                  "five", "six", "seven", "eight", "nine", "colon",
                  "semicolon", "less", /* 0x20 */
        "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F",
                 "G", "H", "I", "J", "K", "L", /* 0x30 */
        "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
             "bracketleft", "backslash", /* 0x40 */
        "bracketright", "asciicircum", "underscore", "grave", "a", "b", "c",
                        "d", "e", "f", "g", "h", "i", "j", "k", "l",
                        /* 0x50 */
        "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
             "braceleft", "bar", /* 0x60 */
        "braceright", "asciitilde", "Adieresis", "Aring", "Ccedilla",
                      "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute",
                      "agrave", "acircumflex", "adieresis", "atilde",
                      "aring", "ccedilla", /* 0x70 */
        "eacute", "egrave", "ecircumflex", "edieresis", "iacute", "igrave",
                  "icircumflex", "idieresis", "ntilde", "oacute", "ograve",
                  "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
                  /* 0x80 */
        "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling",
                       "section", "bullet", "paragraph", "germandbls",
                       "registered", "copyright", "trademark", "acute",
                       "dieresis", "notequal", /* 0x90 */
        "AE", "Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
              "yen", "mu", "partialdiff", "Sigma", "Pi", "pi", "integral",
              "ordfeminine", "ordmasculine", "Omega", /* 0xa0 */
        "ae", "oslash", "questiondown", "exclamdown", "logicalnot",
              "radical", "florin", "approxequal", "Delta", "guillemotleft",
              "guillemotright", "ellipsis", "nbspace", "Agrave", "Atilde",
              "Otilde", /* 0xb0 */
        "OE", "oe", "endash", "emdash", "quotedblleft", "quotedblright",
              "quoteleft", "quoteright", "divide", "lozenge", "ydieresis",
              "Ydieresis", "fraction", "currency", "guilsinglleft",
              "guilsinglright", /* 0xc0 */
        "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase",
              "quotedblbase", "perthousand", "Acircumflex", "Ecircumflex",
              "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex",
              "Idieresis", "Igrave", /* 0xd0 */
        "Oacute", "Ocircumflex", "applelogo", "Ograve", "Uacute",
                  "Ucircumflex", "Ugrave", "dotlessi", "circumflex", "tilde",
                  "macron", "breve", "dotaccent", "ring", "cedilla",
                  "hungarumlaut", /* 0xe0 */
        "ogonek", "caron", "Lslash", "lslash", "Scaron", "scaron", "Zcaron",
                  "zcaron", "brokenbar", "Eth", "eth", "Yacute", "yacute",
                  "Thorn", "thorn", "minus", /* 0xf0 */
        "multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf",
                    "onequarter", "threequarters", "franc", "Gbreve",
                    "gbreve", "Idot", "Scedilla", "scedilla", "Cacute",
                    "cacute", "Ccaron", /* 0x100 */
        "ccaron", "dmacron"
    };

    /**
     * Glyph names for tex8r encoding
     */
    public static final String[] TEX8R_GLYPH_NAMES = {
        // 0x00
        NOTDEF, "dotaccent", "fi", "fl", "fraction", "hungarumlaut",
                   "Lslash", "lslash", "ogonek", "ring", ".notdef", "breve",
                   "minus", ".notdef", "Zcaron", "zcaron", // 0x10
        "caron", "dotlessi", "dotlessj", "ff", "ffi", "ffl", ".notdef",
                 ".notdef", NOTDEF, NOTDEF, NOTDEF, NOTDEF,
                 NOTDEF, NOTDEF, "grave", "quotesingle", // 0x20
        "space", "exclam", "quotedbl", "numbersign", "dollar", "percent",
                 "ampersand", "quoteright", "parenleft", "parenright",
                 "asterisk", "plus", "comma", "hyphen", "period", "slash",
                 // 0x30
        "zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "colon", "semicolon", "less", "equal",
                "greater", "question", // 0x40
        "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
              "M", "N", "O", // 0x50
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft",
             "backslash", "bracketright", "asciicircum", "underscore", // 0x60
        "quoteleft", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
                     "l", "m", "n", "o", // 0x70
        "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft",
             "bar", "braceright", "asciitilde", NOTDEF, // 0x80
        "Euro", NOTDEF, "quotesinglbase", "florin", "quotedblbase",
                "ellipsis", "dagger", "daggerdbl", "circumflex",
                "perthousand", "Scaron", "guilsinglleft", "OE", NOTDEF,
                NOTDEF, NOTDEF, // 0x90
        NOTDEF, NOTDEF, NOTDEF, "quotedblleft", "quotedblright",
                   "bullet", "endash", "emdash", "tilde", "trademark",
                   "scaron", "guilsinglright", "oe", NOTDEF, NOTDEF,
                   "Ydieresis", // 0xA0
        NOTDEF, "exclamdown", "cent", "sterling", "currency", "yen",
                   "brokenbar", "section", "dieresis", "copyright",
                   "ordfeminine", "guillemotleft", "logicalnot", "hyphen",
                   "registered", "macron", // 0xB0
        "degree", "plusminus", "twosuperior", "threesuperior", "acute", "mu",
                  "paragraph", "periodcentered", "cedilla", "onesuperior",
                  "ordmasculine", "guillemotright", "onequarter", "onehalf",
                  "threequarters", "questiondown", // 0xC0
        "Agrave", "Aacute", "Acircumflex", "Atilde", "Adieresis", "Aring",
                  "AE", "Ccedilla", "Egrave", "Eacute", "Ecircumflex",
                  "Edieresis", "Igrave", "Iacute", "Icircumflex",
                  "Idieresis", // 0xD0
        "Eth", "Ntilde", "Ograve", "Oacute", "Ocircumflex", "Otilde",
               "Odieresis", "multiply", "Oslash", "Ugrave", "Uacute",
               "Ucircumflex", "Udieresis", "Yacute", "Thorn", "germandbls",
               // 0xE0
        "agrave", "aacute", "acircumflex", "atilde", "adieresis", "aring",
                  "ae", "ccedilla", "egrave", "eacute", "ecircumflex",
                  "edieresis", "igrave", "iacute", "icircumflex",
                  "idieresis", // 0xF0
        "eth", "ntilde", "ograve", "oacute", "ocircumflex", "otilde",
               "odieresis", "divide", "oslash", "ugrave", "uacute",
               "ucircumflex", "udieresis", "yacute", "thorn", "ydieresis"
    };

    /**
     * The characters in WinAnsiEncoding
     */
    public static final char[] WINANSI_ENCODING = {
        // not used until char 32
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0, 0, // 0x20
        ' ', '\u0021', '\"', '\u0023', '$', '%', '&', '\'', '(', ')', '*', '+', ',',
             '\u002d', '\u002e', '/', // 0x30
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=',
             '>', '?', '@', // 0x40
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
             'O', // 0x50
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\u005b', '\\',
             '\u005d', '^', '_', // 0x60
        '\u2018', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
             'n', 'o', // 0x70
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '\u007b', '\u007c', '\u007d',
             '\u007e', '\u2022', // 0x80
        '\u20ac', '\u2022', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020',
             '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u2022',
             '\u017d', '\u2022', // 0x90
        '\u2022', '\u2018',                             // quoteleft
        '\u2019',                                       // quoteright
        '\u201c',                                       // quotedblleft
        '\u201d',                                       // quotedblright
        '\u2022',                                       // bullet
        '\u2013',                                       // endash
        '\u2014',                                       // emdash
        '~',
        '\u2122',                                       // trademark
        '\u0161', '\u203a', '\u0153', '\u2022', '\u017e', '\u0178', // 0xA0
             ' ', '\u00a1', '\u00a2', '\u00a3', '\u00a4', '\u00a5',
             '\u00a6', '\u00a7', '\u00a8', '\u00a9', '\u00aa', '\u00ab',
             '\u00ac', '\u00ad',  '\u00ae', '\u00af', // 0xb0
        '\u00b0', '\u00b1', '\u00b2', '\u00b3', '\u00b4',
             '\u00b5',                      // This is hand-coded, the rest is assumption
        '\u00b6',                           // and *might* not be correct...
        '\u00b7', '\u00b8', '\u00b9', '\u00ba', '\u00bb', '\u00bc', '\u00bd',
             '\u00be', '\u00bf', // 0xc0
        '\u00c0', '\u00c1', '\u00c2', '\u00c3', '\u00c4', '\u00c5', // Aring
        '\u00c6',                                            // AE
        '\u00c7', '\u00c8', '\u00c9', '\u00ca', '\u00cb', '\u00cc',
             '\u00cd', '\u00ce', '\u00cf', // 0xd0
        '\u00d0', '\u00d1', '\u00d2', '\u00d3', '\u00d4', '\u00d5',
             '\u00d6', '\u00d7', '\u00d8',    // Oslash
        '\u00d9', '\u00da', '\u00db', '\u00dc', '\u00dd', '\u00de',
             '\u00df', // 0xe0
        '\u00e0', '\u00e1', '\u00e2', '\u00e3', '\u00e4', '\u00e5', // aring
        '\u00e6',                                            // ae
        '\u00e7', '\u00e8', '\u00e9', '\u00ea', '\u00eb', '\u00ec',
             '\u00ed', '\u00ee', '\u00ef', // 0xf0
        '\u00f0', '\u00f1', '\u00f2', '\u00f3', '\u00f4', '\u00f5',
             '\u00f6', '\u00f7', '\u00f8', '\u00f9', '\u00fa', '\u00fb',
             '\u00fc', '\u00fd', '\u00fe', '\u00ff'
    };

    /**
     * The characters in AdobeStandardCyrillicEncoding
     */
    public static final char[] ADOBECYRILLIC_ENCODING = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        ' ',        //space
        '\u0021',    //exclam
        '\"',        //quotedbl
        '\u0023',    //numbersign
        '$',        //dollar
        '%',        //percent
        '&',        //ampersand
        '\'',        //quotesingle
        '(',        //parenleft
        ')',        //parenright
        '*',        //asterisk
        '+',        //plus
        ',',        //comma
        '\u002d',    //hyphen
        '\u002e',    //period
        '/',        //slash

        '0',        //zero
        '1',        //one
        '2',        //two
        '3',        //three
        '4',        //four
        '5',        //five
        '6',        //six
        '7',        //seven
        '8',        //eight
        '9',        //nine
        ':',        //colon
        ';',        //semicolon
        '<',        //less
        '=',        //equal
        '>',        //greater
        '?',        //question

        '@',        //at
        'A',        //A
        'B',        //B
        'C',        //C
        'D',        //D
        'E',        //E
        'F',        //F
        'G',        //G
        'H',        //H
        'I',        //I
        'J',        //J
        'K',        //K
        'L',        //L
        'M',        //M
        'N',        //N
        'O',        //O

        'P',        //P
        'Q',        //Q
        'R',        //R
        'S',        //S
        'T',        //T
        'U',        //U
        'V',        //V
        'W',        //W
        'X',        //X
        'Y',        //Y
        'Z',        //Z
        '\u005b',    //bracketleft
        '\\',        //backslash
        '\u005d',    //bracketright
        '^',        //asciicircum
        '_',        //underscore

        '\u0060',    //grave
        'a',        //a
        'b',        //b
        'c',        //c
        'd',        //d
        'e',        //e
        'f',        //f
        'g',        //g
        'h',        //h
        'i',        //i
        'j',        //j
        'k',        //k
        'l',        //l
        'm',        //m
        'n',        //n
        'o',        //o

        'p',        //p
        'q',        //q
        'r',        //r
        's',        //s
        't',        //t
        'u',        //u
        'v',        //v
        'w',        //w
        'x',        //x
        'y',        //y
        'z',        //z
        '\u007b',    //braceleft
        '\u007c',    //bar
        '\u007d',    //braceright
        '\u007e',    //asciitilde
        0,

        '\u0402',    //afii10051
        '\u0403',    //afii10052
        '\u201a',    //quotesinglebase
        '\u0453',    //afii10100
        '\u201e',    //quotedblbase
        '\u2026',    //ellipsis
        '\u2020',    //dagger
        '\u2021',    //daggerdbl
        '\u20ac',    //euro
        '\u2030',    //perthousand
        '\u0409',    //afii10058
        '\u2039',    //guilsignlleft
        '\u040a',    //afii10059
        '\u040c',    //afii10061
        '\u040b',    //afii10060
        '\u040f',    //afii10045

        '\u0452',    //afii10099
        '\u2018',    //quoteleft
        '\u2019',    //quoteright
        '\u201c',    //quotedblleft
        '\u201d',    //quotedblright
        '\u2022',    //bullet
        '\u2013',    //endash
        '\u2014',    //emdash
        0,
        '\u2122',    //trademark
        '\u0459',    //afii10106
        '\u203a',    //guilsinglright
        '\u045a',    //afii10107
        '\u045c',    //afii10109
        '\u045b',    //afii10108
        '\u045f',    //afii10193

        '\u00a0',    //nbspace
        '\u040e',    //afii10062
        '\u045e',    //afii10110
        '\u0408',    //afii10057
        '\u00a4',    //currency
        '\u0490',    //afii10050
        '\u00a6',    //brokenbar
        '\u00a7',    //section
        '\u0401',    //afii10023
        '\u00a9',    //copyright
        '\u0404',    //afii10053
        '\u00ab',    //guillemotleft
        '\u00ac',    //logicalnot
        '\u00ad',    //softhyphen
        '\u00ae',    //registered
        '\u0407',    //afii10056

        '\u00b0',    //degree
        '\u00b1',    //plusminus
        '\u0406',    //afii10055
        '\u0456',    //afii10103
        '\u0491',    //afii10098
        '\u00b5',    //mu
        '\u00b6',    //paragraph
        '\u00b7',    //periodcentered
        '\u0451',    //afii10071
        '\u2116',    //afii61352
        '\u0454',    //afii10101
        '\u00bb',    //guillemotright
        '\u0458',    //afii10105
        '\u0405',    //afii10054
        '\u0455',    //afii10102
        '\u0457',    //afii10104

        '\u0410',    //afii10017
        '\u0411',    //afii10018
        '\u0412',    //afii10019
        '\u0413',    //afii10020
        '\u0414',    //afii10021
        '\u0415',    //afii10022
        '\u0416',    //afii10024
        '\u0417',    //afii10025
        '\u0418',    //afii10026
        '\u0419',    //afii10027
        '\u041a',    //afii10028
        '\u041b',    //afii10029
        '\u041c',    //afii10030
        '\u041d',    //afii10031
        '\u041e',    //afii10032
        '\u041f',    //afii10033

        '\u0420',    //afii10034
        '\u0421',    //afii10035
        '\u0422',    //afii10036
        '\u0423',    //afii10037
        '\u0424',    //afii10038
        '\u0425',    //afii10039
        '\u0426',    //afii10040
        '\u0427',    //afii10041
        '\u0428',    //afii10042
        '\u0429',    //afii10043
        '\u042a',    //afii10044
        '\u042b',    //afii10045
        '\u042c',    //afii10046
        '\u042d',    //afii10047
        '\u042e',    //afii10048
        '\u042f',    //afii10049

        '\u0430',    //afii10065
        '\u0431',    //afii10066
        '\u0432',    //afii10067
        '\u0433',    //afii10068
        '\u0434',    //afii10069
        '\u0435',    //afii10070
        '\u0436',    //afii10072
        '\u0437',    //afii10073
        '\u0438',    //afii10074
        '\u0439',    //afii10075
        '\u043a',    //afii10076
        '\u043b',    //afii10077
        '\u043c',    //afii10078
        '\u043d',    //afii10079
        '\u043e',    //afii10080
        '\u043f',    //afii10081

        '\u0440',    //afii10082
        '\u0441',    //afii10083
        '\u0442',    //afii10084
        '\u0443',    //afii10085
        '\u0444',    //afii10086
        '\u0445',    //afii10087
        '\u0446',    //afii10088
        '\u0447',    //afii10089
        '\u0448',    //afii10090
        '\u0449',    //afii10091
        '\u044a',    //afii10092
        '\u044b',    //afii10093
        '\u044c',    //afii10094
        '\u044d',    //afii10095
        '\u044e',    //afii10096
        '\u044f',    //afii10097
    };

    /**
     * List of unicode glyphs
     */
    private static final String[] UNICODE_GLYPHS;
    private static final String[] DINGBATS_GLYPHS;

    private static final Map CHARNAME_ALTERNATIVES;

    private static final Map CHARNAMES_TO_UNICODE;

    static {
        Map map = new java.util.TreeMap();
        UNICODE_GLYPHS = loadGlyphList("glyphlist.txt", map);
        DINGBATS_GLYPHS = loadGlyphList("zapfdingbats.txt", map);
        CHARNAMES_TO_UNICODE = Collections.unmodifiableMap(map);

        map = new java.util.TreeMap();
        addAlternatives(map, new String[] {"Omega", "Omegagreek"});
        addAlternatives(map, new String[] {"Delta", "Deltagreek"});
        //fraction maps to 2044 (FRACTION SLASH) and 2215 (DIVISION SLASH)
        addAlternatives(map, new String[] {"fraction", "divisionslash"});
        //hyphen maps to 002D (HYPHEN-MINUS) and 00AD (SOFT HYPHEN)
        addAlternatives(map, new String[] {"hyphen", "sfthyphen", "softhyphen", "minus"});
        //macron maps to 00AF (MACRON) and 02C9 (MODIFIER LETTER MACRON)
        addAlternatives(map, new String[] {"macron", "overscore"});
        //mu maps to 00B5 (MICRO SIGN) and 03BC (GREEK SMALL LETTER MU)
        addAlternatives(map, new String[] {"mu", "mu1", "mugreek"});
        //periodcentered maps to 00B7 (MIDDLE DOT) and 2219 (BULLET OPERATOR)
        addAlternatives(map, new String[]
                                    {"periodcentered", "middot", "bulletoperator", "anoteleia"});
        //space maps to 0020 (SPACE) and 00A0 (NO-BREAK SPACE)
        addAlternatives(map, new String[] {"space", "nonbreakingspace", "nbspace"});

        //Scedilla maps to 015E (and F6C1 in private use area)
        //Tcommaaccent maps to 0162 (LATIN CAPITAL LETTER T WITH CEDILLA)
        //  and 021a (LATIN CAPITAL LETTER T WITH COMMA BELOW)
        //scedilla maps to 015f (LATIN SMALL LETTER S WITH CEDILLA) (and F6C2 in private use area)
        //tcommaaccent maps to 0163 and 021b

        //map numbers from and to their respective "oldstyle" variant
        addAlternatives(map, new String[] {"zero", "zerooldstyle"});
        addAlternatives(map, new String[] {"one", "oneoldstyle"});
        addAlternatives(map, new String[] {"two", "twooldstyle"});
        addAlternatives(map, new String[] {"three", "threeoldstyle"});
        addAlternatives(map, new String[] {"four", "fouroldstyle"});
        addAlternatives(map, new String[] {"five", "fiveoldstyle"});
        addAlternatives(map, new String[] {"six", "sixoldstyle"});
        addAlternatives(map, new String[] {"seven", "sevenoldstyle"});
        addAlternatives(map, new String[] {"eight", "eightoldstyle"});
        addAlternatives(map, new String[] {"nine", "nineoldstyle"});

        //map currency signs from and to their respective "oldstyle" variant
        addAlternatives(map, new String[] {"cent", "centoldstyle"});
        addAlternatives(map, new String[] {"dollar", "dollaroldstyle"});

        //Cyrillic names according Adobe Techninal Note #5013 aka Adobe Standard Cyrillic Font Specification
        addAlternatives(map, new String[] {"Acyrillic",        "afii10017"});
        addAlternatives(map, new String[] {"Becyrillic",    "afii10018"});
        addAlternatives(map, new String[] {"Vecyrillic",    "afii10019"});
        addAlternatives(map, new String[] {"Gecyrillic",    "afii10020"});
        addAlternatives(map, new String[] {"Decyrillic",    "afii10021"});
        addAlternatives(map, new String[] {"Iecyrillic",    "afii10022"});
        addAlternatives(map, new String[] {"Iocyrillic",    "afii10023"});
        addAlternatives(map, new String[] {"Zhecyrillic",    "afii10024"});
        addAlternatives(map, new String[] {"Zecyrillic",    "afii10025"});
        addAlternatives(map, new String[] {"Iicyrillic",    "afii10026"});
        addAlternatives(map, new String[] {"Iishortcyrillic",    "afii10027"});
        addAlternatives(map, new String[] {"Kacyrillic",    "afii10028"});
        addAlternatives(map, new String[] {"Elcyrillic",    "afii10029"});
        addAlternatives(map, new String[] {"Emcyrillic",    "afii10030"});
        addAlternatives(map, new String[] {"Encyrillic",    "afii10031"});
        addAlternatives(map, new String[] {"Ocyrillic",        "afii10032"});
        addAlternatives(map, new String[] {"Pecyrillic",    "afii10033"});
        addAlternatives(map, new String[] {"Ercyrillic",    "afii10034"});
        addAlternatives(map, new String[] {"Escyrillic",    "afii10035"});
        addAlternatives(map, new String[] {"Tecyrillic",    "afii10036"});
        addAlternatives(map, new String[] {"Ucyrillic",        "afii10037"});
        addAlternatives(map, new String[] {"Efcyrillic",    "afii10038"});
        addAlternatives(map, new String[] {"Khacyrillic",    "afii10039"});
        addAlternatives(map, new String[] {"Tsecyrillic",    "afii10040"});
        addAlternatives(map, new String[] {"Checyrillic",    "afii10041"});
        addAlternatives(map, new String[] {"Shacyrillic",    "afii10042"});
        addAlternatives(map, new String[] {"Shchacyrillic",    "afii10043"});
        addAlternatives(map, new String[] {"Hardsigncyrillic",    "afii10044"});
        addAlternatives(map, new String[] {"Yericyrillic",    "afii10045"});
        addAlternatives(map, new String[] {"Softsigncyrillic",    "afii10046"});
        addAlternatives(map, new String[] {"Ereversedcyrillic",    "afii10047"});
        addAlternatives(map, new String[] {"IUcyrillic",    "afii10048"});
        addAlternatives(map, new String[] {"IAcyrillic",    "afii10049"});

        addAlternatives(map, new String[] {"acyrillic",        "afii10065"});
        addAlternatives(map, new String[] {"becyrillic",    "afii10066"});
        addAlternatives(map, new String[] {"vecyrillic",    "afii10067"});
        addAlternatives(map, new String[] {"gecyrillic",    "afii10068"});
        addAlternatives(map, new String[] {"decyrillic",    "afii10069"});
        addAlternatives(map, new String[] {"iecyrillic",    "afii10070"});
        addAlternatives(map, new String[] {"iocyrillic",    "afii10071"});
        addAlternatives(map, new String[] {"zhecyrillic",    "afii10072"});
        addAlternatives(map, new String[] {"zecyrillic",    "afii10073"});
        addAlternatives(map, new String[] {"iicyrillic",    "afii10074"});
        addAlternatives(map, new String[] {"iishortcyrillic",    "afii10075"});
        addAlternatives(map, new String[] {"kacyrillic",    "afii10076"});
        addAlternatives(map, new String[] {"elcyrillic",    "afii10077"});
        addAlternatives(map, new String[] {"emcyrillic",    "afii10078"});
        addAlternatives(map, new String[] {"encyrillic",    "afii10079"});
        addAlternatives(map, new String[] {"ocyrillic",        "afii10080"});
        addAlternatives(map, new String[] {"pecyrillic",    "afii10081"});
        addAlternatives(map, new String[] {"ercyrillic",    "afii10082"});
        addAlternatives(map, new String[] {"escyrillic",    "afii10083"});
        addAlternatives(map, new String[] {"tecyrillic",    "afii10084"});
        addAlternatives(map, new String[] {"ucyrillic",        "afii10085"});
        addAlternatives(map, new String[] {"efcyrillic",    "afii10086"});
        addAlternatives(map, new String[] {"khacyrillic",    "afii10087"});
        addAlternatives(map, new String[] {"tsecyrillic",    "afii10088"});
        addAlternatives(map, new String[] {"checyrillic",    "afii10089"});
        addAlternatives(map, new String[] {"shacyrillic",    "afii10090"});
        addAlternatives(map, new String[] {"shchacyrillic",    "afii10091"});
        addAlternatives(map, new String[] {"hardsigncyrillic",    "afii10092"});
        addAlternatives(map, new String[] {"yericyrillic",    "afii10093"});
        addAlternatives(map, new String[] {"softsigncyrillic",    "afii10094"});
        addAlternatives(map, new String[] {"ereversedcyrillic",    "afii10095"});
        addAlternatives(map, new String[] {"iucyrillic",    "afii10096"});
        addAlternatives(map, new String[] {"iacyrillic",    "afii10097"});

        addAlternatives(map, new String[] {"Gheupturncyrillic",    "afii10050"});
        addAlternatives(map, new String[] {"Djecyrillic",    "afii10051"});
        addAlternatives(map, new String[] {"Gjecyrillic",    "afii10052"});
        addAlternatives(map, new String[] {"Ecyrillic",        "afii10053"});
        addAlternatives(map, new String[] {"Dzecyrillic",    "afii10054"});
        addAlternatives(map, new String[] {"Icyrillic",        "afii10055"});
        addAlternatives(map, new String[] {"Yicyrillic",    "afii10056"});
        addAlternatives(map, new String[] {"Jecyrillic",    "afii10057"});
        addAlternatives(map, new String[] {"Ljecyrillic",    "afii10058"});
        addAlternatives(map, new String[] {"Njecyrillic",    "afii10059"});
        addAlternatives(map, new String[] {"Tshecyrillic",    "afii10060"});
        addAlternatives(map, new String[] {"Kjecyrillic",    "afii10061"});
        addAlternatives(map, new String[] {"Ushortcyrillic",    "afii10062"});

        addAlternatives(map, new String[] {"Dzhecyrillic",    "afii10145"});
        addAlternatives(map, new String[] {"Yatcyrillic",    "afii10146"});
        addAlternatives(map, new String[] {"Fitacyrillic",    "afii10147"});
        addAlternatives(map, new String[] {"Izhitsacyrillic",    "afii10148"});

        addAlternatives(map, new String[] {"gheupturncyrillic",    "afii10098"});
        addAlternatives(map, new String[] {"djecyrillic",    "afii10099"});
        addAlternatives(map, new String[] {"gjecyrillic",    "afii10100"});
        addAlternatives(map, new String[] {"ecyrillic",        "afii10101"});
        addAlternatives(map, new String[] {"dzecyrillic",    "afii10102"});
        addAlternatives(map, new String[] {"icyrillic",        "afii10103"});
        addAlternatives(map, new String[] {"yicyrillic",    "afii10104"});
        addAlternatives(map, new String[] {"jecyrillic",    "afii10105"});
        addAlternatives(map, new String[] {"ljecyrillic",    "afii10106"});
        addAlternatives(map, new String[] {"njecyrillic",    "afii10107"});
        addAlternatives(map, new String[] {"tshecyrillic",    "afii10108"});
        addAlternatives(map, new String[] {"kjecyrillic",    "afii10109"});
        addAlternatives(map, new String[] {"ushortcyrillic",    "afii10110"});

        addAlternatives(map, new String[] {"dzhecyrillic",    "afii10193"});
        addAlternatives(map, new String[] {"yatcyrillic",    "afii10194"});
        addAlternatives(map, new String[] {"fitacyrillic",    "afii10195"});
        addAlternatives(map, new String[] {"izhitsacyrillic",    "afii10196"});

        CHARNAME_ALTERNATIVES = Collections.unmodifiableMap(map);
    }

    private static void addAlternatives(Map map, String[] alternatives) {
        for (int i = 0, c = alternatives.length; i < c; i++) {
            String[] alt = new String[c - 1];
            int idx = 0;
            for (int j = 0; j < c; j++) {
                if (i != j) {
                    alt[idx] = alternatives[j];
                    idx++;
                }
            }
            map.put(alternatives[i], alt);
        }
    }

    private static String[] loadGlyphList(String filename, Map charNameToUnicodeMap) {
        List lines = new java.util.ArrayList();
        InputStream in = Glyphs.class.getResourceAsStream(filename);
        if (in == null) {
            throw new RuntimeException("Cannot load " + filename
                    + ". The Glyphs class cannot properly be initialized!");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "US-ASCII"));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        lines.add(line);
                    }
                }
            } finally {
                reader.close();
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Incompatible JVM! US-ASCII encoding is not supported."
                    + " The Glyphs class cannot properly be initialized!");
        } catch (IOException ioe) {
            throw new RuntimeException("I/O error while loading " + filename
                    + ". The Glyphs class cannot properly be initialized!");
        } finally {
            IOUtils.closeQuietly(in);
        }
        String[] arr = new String[lines.size() * 2];
        int pos = 0;
        StringBuffer buf = new StringBuffer();
        for (Object line1 : lines) {
            String line = (String) line1;
            int semicolon = line.indexOf(';');
            if (semicolon <= 0) {
                continue;
            }
            String charName = line.substring(0, semicolon);
            String rawUnicode = line.substring(semicolon + 1);
            buf.setLength(0);

            StringTokenizer tokenizer = new StringTokenizer(rawUnicode, " ", false);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                assert token.length() == 4;
                buf.append(hexToChar(token));
            }

            String unicode = buf.toString();
            arr[pos] = unicode;
            pos++;
            arr[pos] = charName;
            pos++;
            assert !charNameToUnicodeMap.containsKey(charName);
            charNameToUnicodeMap.put(charName, unicode);
        }
        return arr;
    }

    private static char hexToChar(String hex) {
        return (char)Integer.parseInt(hex, 16);
    }

    /**
     * Return the glyphname from a character,
     * eg, charToGlyphName('\\') returns "backslash"
     *
     * @param ch glyph to evaluate
     * @return the name of the glyph
     */
    public static String charToGlyphName(char ch) {
        return stringToGlyph(Character.toString(ch));
    }

    /**
     * Returns a String containing the Unicode sequence the given glyph name represents.
     * @param glyphName the glyph name
     * @return the Unicode sequence of the glyph (or null if the glyph name is unknown)
     */
    public static String getUnicodeSequenceForGlyphName(String glyphName) {
        //Mapping: see http://www.adobe.com/devnet/opentype/archives/glyph.html
        //Step 1
        int period = glyphName.indexOf('.');
        if (period >= 0) {
            glyphName = glyphName.substring(0, period);
        }

        //Step 2
        StringBuffer sb = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(glyphName, "_", false);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            //Step 3
            String sequence = (String)CHARNAMES_TO_UNICODE.get(token);
            if (sequence == null) {
                if (token.startsWith("uni")) {
                    int len = token.length();
                    int pos = 3;
                    while (pos + 4 <= len) {
                        try {
                            sb.append(hexToChar(token.substring(pos, pos + 4)));
                        } catch (NumberFormatException nfe) {
                            return null;
                        }
                        pos += 4;
                    }
                } else if (token.startsWith("u")) {
                    if (token.length() > 5) {
                        //TODO: Unicode scalar values greater than FFFF are currently not supported
                        return null;
                    }
                    if (token.length() < 5) {
                        /*
                         * This is not in the form of 'u1234' --probably a
                         * non-official glyph name that isn't listed in the
                         * unicode map.
                         */
                        return null;
                    }
                    try {
                        sb.append(hexToChar(token.substring(1, 5)));
                    } catch (NumberFormatException nfe) {
                        return null;
                    }
                } else {
                    //ignore (empty string)
                }
            } else {
                sb.append(sequence);
            }
        }

        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    /**
     * Return the glyphname from a string,
     * eg, glyphToString("\\") returns "backslash"
     *
     * @param name glyph to evaluate
     * @return the name of the glyph
     * TODO: javadocs for glyphToString and stringToGlyph are confused
     * @deprecated User getUnicodeCodePointsForGlyphName instead. This method only returns the
     *          first Unicode code point it finds.
     */
    @Deprecated
    public static String glyphToString(String name) {
        for (int i = 0; i < UNICODE_GLYPHS.length; i += 2) {
            if (UNICODE_GLYPHS[i + 1].equals(name)) {
                return UNICODE_GLYPHS[i];
            }
        }
        return "";
    }

    /**
     * Return the string representation of a glyphname,
     * eg stringToGlyph("backslash") returns "\\"
     *
     * @param name name of the glyph
     * @return the string representation (or an empty String if no match was found)
     */
    public static String stringToGlyph(String name) {
        for (int i = 0; i < UNICODE_GLYPHS.length; i += 2) {
            if (UNICODE_GLYPHS[i].equals(name)) {
                return UNICODE_GLYPHS[i + 1];
            }
        }
        for (int i = 0; i < DINGBATS_GLYPHS.length; i += 2) {
            if (DINGBATS_GLYPHS[i].equals(name)) {
                return DINGBATS_GLYPHS[i + 1];
            }
        }
        return "";
    }

    /**
     * Returns an array of char names which can serve as alternatives for the given one.
     * @param charName the character name to search alternatives for
     * @return an array of char names or null if no alternatives are available
     */
    public static String[] getCharNameAlternativesFor(String charName) {
        return (String[])CHARNAME_ALTERNATIVES.get(charName);
    }

}
