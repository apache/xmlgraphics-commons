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

package org.apache.xmlgraphics.java2d.ps;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

/**
 * This implements the GraphicsDevice interface as appropriate for
 * a PSGraphics2D. This is quite simple since we only have one
 * GraphicsConfiguration for now.
 */
class PSGraphicsDevice extends GraphicsDevice {

    /** The Graphics Config that created us... */
    protected GraphicsConfiguration gc;

    /**
     * Create a new PostScript graphics device.
     *
     * @param gc The graphics configuration we should reference
     */
    PSGraphicsDevice(GraphicsConfiguration gc) {
        this.gc = gc;
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
        return gc;
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration[] getConfigurations() {
        return new GraphicsConfiguration[] {gc};
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        return gc;
    }

    /** {@inheritDoc} */
    @Override
    public String getIDstring() {
        return toString();
    }

    /** {@inheritDoc} */
    @Override
    public int getType() {
        return GraphicsDevice.TYPE_PRINTER;
    }

}

