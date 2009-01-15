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

import java.io.IOException;
import java.util.Map;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;

/**
 * Simple abstract base class for ImageLoaders.
 */
public abstract class AbstractImageLoader implements ImageLoader {

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, ImageSessionContext session)
                throws ImageException, IOException {
        return loadImage(info, null, session);
    }

    /** {@inheritDoc} */
    public int getUsagePenalty() {
        return MEDIUM_LOADING_PENALTY;
    }

    /**
     * Indicates whether an embedded color profile should be ignored.
     * @param hints a Map of hints that can be used by implementations to customize the loading
     *                  process (may be null).
     * @return true if any color profile should be ignored
     */
    protected boolean ignoreColorProfile(Map hints) {
        if (hints == null) {
            return false;
        }
        Boolean b = (Boolean)hints.get(ImageProcessingHints.IGNORE_COLOR_PROFILE);
        return (b != null) && b.booleanValue();
    }

}
