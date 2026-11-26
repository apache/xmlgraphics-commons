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
 * The row filter a PNG writer applies before compressing a row. Filters work on whole bytes, so
 * they rarely help images with packed pixels (palettes with 1, 2 or 4 bits per pixel).
 */
public enum PNGFilter {
    /**
     * The bytes of a row as they are: the default, and what the PNG specification recommends for
     * palette images and images with less than 8 bits per pixel.
     */
    NONE,
    /** Every byte as the difference to the same byte of the pixel on the left. */
    SUB,
    /**
     * Every byte as the difference to the byte above it: nearly free, and it can help images with
     * 8 bits per sample whose rows mostly repeat the row above, such as labels with an 8 bit palette.
     */
    UP,
    /** Every byte as the difference to the average of the bytes left of and above it. */
    AVERAGE,
    /** Every byte as the difference to the Paeth predictor of the bytes left, above and above left. */
    PAETH,
    /**
     * For every row the filter giving the smallest sum of absolute differences (the libpng
     * heuristic recommended by the PNG specification). It can give truecolor images with many
     * colors slightly smaller files, but takes about twice the time.
     */
    ADAPTIVE
}
