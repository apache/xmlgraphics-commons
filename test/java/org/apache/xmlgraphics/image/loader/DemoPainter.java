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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

public class DemoPainter implements Graphics2DImagePainter {

    /** {@inheritDoc} */
    public Dimension getImageSize() {
        return new Dimension(10000, 10000);
    }

    public void paint(Graphics2D g2d, Rectangle2D area) {
        double w = area.getWidth();
        double h = area.getHeight();

        //Fit in paint area
        Dimension imageSize = getImageSize();
        double sx = w / imageSize.getWidth();
        double sy = h / imageSize.getHeight();
        if (sx != 1.0 || sy != 1.0) {
            g2d.scale(sx, sy);
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke());
        g2d.drawRect(0, 0, imageSize.width, imageSize.height);
        g2d.drawOval(0, 0, imageSize.width, imageSize.height);
    }

}
