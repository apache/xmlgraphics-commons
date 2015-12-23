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

package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.image.ColorModel;

enum ExtraSamplesType {
    UNSPECIFIED(0),
    ASSOCIATED_ALPHA(1),
    UNASSOCIATED_ALPHA(2);

    private final int typeValue;

    private ExtraSamplesType(int value) {
        this.typeValue = value;
    }

    static ExtraSamplesType getValue(ColorModel colorModel, int numExtraSamples) {
        if (numExtraSamples == 1 && colorModel.hasAlpha()) {
            return colorModel.isAlphaPremultiplied() ? ASSOCIATED_ALPHA : UNASSOCIATED_ALPHA;
        }
        return UNSPECIFIED;
    }

    int getValue() {
        return typeValue;
    }
}
