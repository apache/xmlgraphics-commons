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

package org.apache.xmlgraphics.java2d;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of {@link java.awt.GraphicsConfiguration} that does not support transparencies
 * (alpha channels).
 */
public class GraphicsConfigurationWithoutTransparency extends AbstractGraphicsConfiguration {

    private static final Log LOG = LogFactory.getLog(GraphicsConfigurationWithoutTransparency.class);

    // We use this to get a good colormodel..
    private static final BufferedImage BI_WITHOUT_ALPHA =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    private final GraphicsConfigurationWithTransparency defaultDelegate = new GraphicsConfigurationWithTransparency();

    @Override
    public GraphicsDevice getDevice() {
        return new GenericGraphicsDevice(this);
    }

    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        return defaultDelegate.createCompatibleImage(width, height, Transparency.OPAQUE);
    }

    @Override
    public BufferedImage createCompatibleImage(int width, int height, int transparency) {
        if (transparency != Transparency.OPAQUE) {
            LOG.warn("Does not support transparencies (alpha channels) in images");
        }
        return defaultDelegate.createCompatibleImage(width, height, Transparency.OPAQUE);
    }

    @Override
    public ColorModel getColorModel() {
        return BI_WITHOUT_ALPHA.getColorModel();
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        if (transparency == Transparency.OPAQUE) {
            LOG.warn("Does not support transparencies (alpha channels) in images");
        }
        return getColorModel();
    }

    @Override
    public AffineTransform getDefaultTransform() {
        return defaultDelegate.getDefaultTransform();
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        return defaultDelegate.getNormalizingTransform();
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }
}
