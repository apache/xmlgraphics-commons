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

package org.apache.xmlgraphics.image.loader.impl;

public interface PNGConstants {

    /*
     * First 8 bytes of any PNG file.
     */
    long PNG_SIGNATURE = 0x89504e470d0a1a0aL;

    /*
     * Color types.
     */
    int PNG_COLOR_GRAY = 0;
    int PNG_COLOR_RGB = 2;
    int PNG_COLOR_PALETTE = 3;
    int PNG_COLOR_GRAY_ALPHA = 4;
    int PNG_COLOR_RGB_ALPHA = 6;

    /*
     * Filter types.
     */
    int PNG_FILTER_NONE = 0;
    int PNG_FILTER_SUB = 1;
    int PNG_FILTER_UP = 2;
    int PNG_FILTER_AVERAGE = 3;
    int PNG_FILTER_PAETH = 4;

}
