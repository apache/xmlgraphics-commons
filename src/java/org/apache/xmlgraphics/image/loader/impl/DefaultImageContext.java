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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import org.apache.xmlgraphics.image.GraphicsConstants;
import org.apache.xmlgraphics.image.loader.ImageContext;

/**
 * Very simple ImageContext implementation that uses the <code>Toolkit</code>'s screen resolution.
 */
public class DefaultImageContext implements ImageContext {

    private final float sourceResolution;

    /**
     * Main constructor.
     */
    public DefaultImageContext() {
        if (GraphicsEnvironment.isHeadless()) {
            this.sourceResolution = GraphicsConstants.DEFAULT_DPI;
        } else {
            this.sourceResolution = Toolkit.getDefaultToolkit()
                    .getScreenResolution();
        }
    }

    /** {@inheritDoc} */
    public float getSourceResolution() {
        return this.sourceResolution;
    }

}
