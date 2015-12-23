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

package org.apache.xmlgraphics.image.writer;


/**
 * Enumeration for specifying the endianness of the image.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Endianness">Wikipedia on Endianness</a>
 */
public enum Endianness {

    /**
     * Default endianness. This can be different depending on the output format or is used
     * when the image format doesn't allow to specify the endianness.
     */
    DEFAULT,

    /** Little endian, least significant byte first, LSB, Intel Format. */
    LITTLE_ENDIAN,

    /** Big endian, most significant byte first, MSB, Motorola Format. */
    BIG_ENDIAN;

    /**
     * Translates an endian type specified in the configuration file into the
     * equivalent type should one exist.
     * @param value The value specified in the configration file
     * @return Returns the Endianess object of the found type. If no type matches
     * it returns null.
     */
    public static Endianness getEndianType(String value) {
        if (value != null) {
            for (Endianness endianValue : Endianness.values()) {
                if (endianValue.toString().equalsIgnoreCase(value)) {
                    return endianValue;
                }
            }
        }
        return null;
    }

}
