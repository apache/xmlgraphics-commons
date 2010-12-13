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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

/**
 * Our implementation of the class that returns information about
 * roughly what we can handle and want to see (alpha for example).
 */
class PSGraphicsConfiguration extends GraphicsConfiguration {

    // We use this to get a good colormodel..
    private static final BufferedImage BI_WITH_ALPHA
        = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    // We use this to get a good colormodel..
    private static final BufferedImage BI_WITHOUT_ALPHA
        = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    /** {@inheritDoc} */
    @Override
    public BufferedImage createCompatibleImage(int width, int height,
            int transparency) {
        if (transparency == Transparency.OPAQUE) {
            return new BufferedImage(width, height,
                                     BufferedImage.TYPE_INT_RGB);
        } else {
            return new BufferedImage(width, height,
                                     BufferedImage.TYPE_INT_ARGB);
        }
    }

    /** {@inheritDoc} */
    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        return new BufferedImage(width, height,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    /** {@inheritDoc} */
    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        throw new UnsupportedOperationException(
                "Creation of VolatileImage instances is not supported");
    }

    /** {@inheritDoc} */
    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
        throw new UnsupportedOperationException(
                "Creation of VolatileImage instances is not supported");
    }

    /** {@inheritDoc} */
    @Override
    public Rectangle getBounds() {
        //Not applicable to vector graphic devices. Up to date, it works like this.
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ColorModel getColorModel() {
        return BI_WITH_ALPHA.getColorModel();
    }

    /** {@inheritDoc} */
    @Override
    public ColorModel getColorModel(int transparency) {
        if (transparency == Transparency.OPAQUE) {
            return BI_WITHOUT_ALPHA.getColorModel();
        } else {
            return BI_WITH_ALPHA.getColorModel();
        }
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform(2, 0, 0, 2, 0, 0);
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsDevice getDevice() {
        return new PSGraphicsDevice(this);
    }

}