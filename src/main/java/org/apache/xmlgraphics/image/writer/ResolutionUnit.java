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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for resolution units used by images; 1 for None, 2 for Inch and 3 for Centimeter.
 */
public enum ResolutionUnit {

    /** no resolution unit */
    NONE(1, "None"),
    /** units per inch */
    INCH(2, "Inch"),
    /** units per centimeter */
    CENTIMETER(3, "Centimeter");

    //Reverse Lookup Table
    private static final Map<Integer, ResolutionUnit> LOOKUP
            = new HashMap<Integer, ResolutionUnit>();

    static {
         for (ResolutionUnit unit : EnumSet.allOf(ResolutionUnit.class)) {
              LOOKUP.put(unit.getValue(), unit);
         }
    }

    private final int value;
    private final String description;

    private ResolutionUnit(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Retrieves the numeric value of the resolution unit.
     *
     * @return 1, 2 or 3.
     */
    public int getValue() {
        return value;
    }

    /**
     * Retrieves the standard textual description of the resolution unit.
     *
     * @return None, Inch or Centimeter.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Reverse lookup by value.
     *
     * @param value the numeric value of the resolution unit
     * @return the resolution unit
     */
    public static ResolutionUnit get(int value) {
         return LOOKUP.get(value);
    }
}
