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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Very simple image viewer application that demonstrates the use of the image loader framework.
 */
public class ImageViewer {

    private ImageContext imageContext;
    private ImageManager imageManager;
    
    public ImageViewer() {
        //These two are set up for the whole application
        this.imageContext = new DefaultImageContext();
        this.imageManager = new ImageManager(this.imageContext);
    }
    
    public void display(File f) throws IOException {
        //The ImageSessionContext might for each processing run
        ImageSessionContext sessionContext = new DefaultImageSessionContext(
                this.imageContext, null);
        
        //Construct URI from filename
        String uri = f.toURI().toASCIIString();
        
        ImageGraphics2D g2dImage = null;
        try {
            //Preload image
            ImageInfo info = this.imageManager.getImageInfo(uri, sessionContext);
            
            //Load image and request Graphics2D image
            g2dImage = (ImageGraphics2D)this.imageManager.getImage(
                    info, ImageFlavor.GRAPHICS2D, sessionContext);
            
        } catch (ImageException e) {
            e.printStackTrace();
            
            //Create "error image" if the image cannot be displayed
            g2dImage = createErrorImage();
        }
        
        //Display frame with image
        ViewerFrame frame = new ViewerFrame(g2dImage);
        frame.setVisible(true);
    }

    private ImageGraphics2D createErrorImage() {
        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public Dimension getImageSize() {
                return new Dimension(10, 10);
            }

            public void paint(Graphics2D g2d, Rectangle2D area) {
                g2d.translate(area.getX(), area.getY());
                double w = area.getWidth();
                double h = area.getHeight();

                //Fit in paint area
                Dimension imageSize = getImageSize();
                double sx = w / imageSize.getWidth();
                double sy = h / imageSize.getHeight();
                if (sx != 1.0 || sy != 1.0) {
                    g2d.scale(sx, sy);
                }

                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(0));
                g2d.drawRect(0, 0, imageSize.width, imageSize.height);
                g2d.drawLine(0, 0, imageSize.width, imageSize.height);
                g2d.drawLine(0, imageSize.height, imageSize.width, 0);
            }
            
        };
        Dimension dim = painter.getImageSize();
        
        ImageSize size = new ImageSize();
        size.setSizeInMillipoints(dim.width, dim.height);
        size.setResolution(imageContext.getSourceResolution());
        size.calcPixelsFromSize();
        
        ImageInfo info = new ImageInfo(null, null);
        info.setSize(size);
        return new ImageGraphics2D(info, painter);
    }
    
    /**
     * The application's main method.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            ImageViewer app = new ImageViewer();
            if (args.length < 1) {
                throw new IllegalArgumentException("No filename given as application argument");
            }
            app.display(new File(args[0]));
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

}
