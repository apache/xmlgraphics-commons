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

package org.apache.xmlgraphics.image.loader;

/**
 * This interface defines some standard hints to be used for image processing in this package.
 * They are provided for convenience. You can define your own hints as you like.
 * Generally, consumers should not rely on the presence of any hint!
 */
public interface ImageProcessingHints {

    /** Used to send a hint about the source resolution for pixel to unit conversions. */
    String SOURCE_RESOLUTION = "SOURCE_RESOLUTION"; //Value: Number (unit dpi)
    /** Used to send a hint about the target resolution (of the final output format). */
    String TARGET_RESOLUTION = "TARGET_RESOLUTION"; //Value: Number (unit dpi)

    /**
     * Used to pass in the {@link ImageSessionContext}. A consumer can use this to load embedded
     * images over the same mechanism as the main image (ex. JPEG images referenced in an
     * SVG image).
     * @since 1.4
     */
    String IMAGE_SESSION_CONTEXT = "IMAGE_SESSION_CONTEXT"; //Value: ImageSessionContext instance

    /**
     * Used to pass in the {@link ImageManager}. A consumer can use this to load embedded
     * images over the same mechanism as the main image (ex. JPEG images referenced in an
     * SVG image).
     * @since 1.4
     */
    String IMAGE_MANAGER = "IMAGE_MANAGER"; //Value: ImageManager instance

    /** Used to tell the image loader to ignore any color profile in the image. */
    String IGNORE_COLOR_PROFILE = "IGNORE_COLOR_PROFILE"; //Value: Boolean

    /** Used to tell a bitmap producer to generate a certain type of bitmap. */
    String BITMAP_TYPE_INTENT = "BITMAP_TYPE_INTENT";

    /**
     * Used with BITMAP_TYPE_INTENT to indicate that the generated bitmap should be a
     * grayscale image.
     */
    String BITMAP_TYPE_INTENT_GRAY = "gray";

    /**
     * Used with BITMAP_TYPE_INTENT to indicate that the generated bitmap should be a
     * 1 bit black and white image.
     */
    String BITMAP_TYPE_INTENT_MONO = "mono";

    /**
     * Used to indicate how existing transparency information (for example, an alpha channel)
     * shall be treated. */
    String TRANSPARENCY_INTENT = "TRANSPARENCY_INTENT";

    /**
     * Used with TRANSPARENCY_INTENT to indicate that any transparency information shall be
     * preserved (the default).
     */
    String TRANSPARENCY_INTENT_PRESERVE = "preserve";

    /**
     * Used with TRANSPARENCY_INTENT to indicate that any transparency information shall be
     * ignored.
     */
    String TRANSPARENCY_INTENT_IGNORE = "ignore";


}
