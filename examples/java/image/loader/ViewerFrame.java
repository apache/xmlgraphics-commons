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

package image.loader;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Viewer frame for the image viewer.
 */
public class ViewerFrame extends Frame {

    public static final String TITLE = "Very Simple Image Viewer";
    
    public ViewerFrame(ImageGraphics2D g2dImage) {
        super(TITLE);
        addWindowListener(new WindowHandler());
        buildGUI(g2dImage);
        setSize(500, 400);
    }
    
    private void buildGUI(final ImageGraphics2D g2dImage) {
        JPanel imagePanel = new JPanel() {
            /** {@inheritDoc} */
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2d = (Graphics2D)graphics.create();
                try {
                    Rectangle paintRect = new Rectangle(
                            30, 30,
                            getWidth() - 60, getHeight() - 60);
                    //g2d.translate(paintRect.getX(), paintRect.getY());

                    Graphics2DImagePainter painter = g2dImage.getGraphics2DImagePainter();
                    Dimension dim = painter.getImageSize();
                    double sx = paintRect.getWidth() / dim.getWidth();
                    double sy = paintRect.getHeight() / dim.getHeight();
                    //g2d.scale(sx, sy);
                    
                    /*
                    Rectangle2D targetRect = new Rectangle2D.Double(
                            paintRect.x * sx, paintRect.y * sy,
                            dim.width, dim.height);
                            */
                    Rectangle2D targetRect = new Rectangle2D.Double(
                            paintRect.x, paintRect.y,
                            paintRect.width, paintRect.height);
                    
                    
                    g2d.draw(targetRect);
                    painter.paint(g2d, targetRect);
                } finally {
                    g2d.dispose();
                }
            }
        };
        add("Center", imagePanel);
    }
    
    private class WindowHandler extends WindowAdapter {
        public void windowClosing(WindowEvent we) {
            System.exit(0);
        }
    }

}
