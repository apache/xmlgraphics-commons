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
 
package org.apache.xmlgraphics.ps;

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * Abstract helper class for generating PostScript forms.
 */
public class ImageFormGenerator extends FormGenerator {

    //Mode 1 (RenderedImage)
    private RenderedImage image;
    
    //Mode 2 (ImageEncoder)
    private ImageEncoder encoder;
    private ColorSpace colorSpace;
    
    private boolean invertImage;
    private Dimension pixelDimensions;
    
    /**
     * Main constructor.
     * @param formName the form's name
     * @param title the form's title or null
     * @param dimensions the form's dimensions in units (usually points)
     * @param image the image
     * @param invertImage true if the image shall be inverted
     */
    public ImageFormGenerator(String formName, String title,
            Dimension2D dimensions,
            RenderedImage image, boolean invertImage) {
        super(formName, title, dimensions);
        this.image = image;
        this.encoder = ImageEncodingHelper.createRenderedImageEncoder(image);
        this.invertImage = invertImage;
        this.pixelDimensions = new Dimension(image.getWidth(), image.getHeight());
    }
    
    /**
     * Main constructor.
     * @param formName the form's name
     * @param title the form's title or null
     * @param dimensions the form's dimensions in units (usually points)
     * @param dimensionsPx the form's dimensions in pixels
     * @param encoder the image encoder
     * @param colorSpace the target color space
     * @param invertImage true if the image shall be inverted
     */
    public ImageFormGenerator(String formName, String title,
            Dimension2D dimensions, Dimension dimensionsPx,
            ImageEncoder encoder, ColorSpace colorSpace, boolean invertImage) {
        super(formName, title, dimensions);
        this.encoder = encoder;
        this.colorSpace = colorSpace;
        this.invertImage = invertImage;
        this.pixelDimensions = dimensionsPx;
    }
    
    /**
     * Returns the name of the data segment associated with this image form.
     * @return the data segment name
     */
    protected String getDataName() {
        return getFormName() + ":Data";
    }
    
    private String getAdditionalFilters(PSGenerator gen) {
        String implicitFilter = encoder.getImplicitFilter(); 
        if (implicitFilter != null) {
            return "/ASCII85Decode filter " + implicitFilter + " filter";
        } else {
            if (gen.getPSLevel() >= 3) {
                return "/ASCII85Decode filter /FlateDecode filter";
            } else {
                return "/ASCII85Decode filter /RunLengthDecode filter";
            }
        }
    }
    
    /** {@inheritDoc} */
    protected void generatePaintProc(PSGenerator gen) throws IOException {
        if (gen.getPSLevel() == 2) {
            gen.writeln("    userdict /i 0 put"); //rewind image data
        } else {
            gen.writeln("    " + getDataName() + " 0 setfileposition"); //rewind image data
        }
        String dataSource;
        if (gen.getPSLevel() == 2) {
            dataSource = "{ " + getDataName() + " i get /i i 1 add store } bind";
        } else {
            dataSource = getDataName();
        }
        AffineTransform at = new AffineTransform();
        at.scale(getDimensions().getWidth(), getDimensions().getHeight());
        gen.concatMatrix(at);
        PSDictionary imageDict = new PSDictionary();
        imageDict.put("/DataSource", dataSource);
        if (this.image != null) {
            PSImageUtils.writeImageCommand(this.image, imageDict, gen);
        } else {
            imageDict.put("/BitsPerComponent", Integer.toString(8));
            PSImageUtils.writeImageCommand(imageDict,
                    this.pixelDimensions, this.colorSpace, this.invertImage,
                    gen);
        }
    }

    /** {@inheritDoc} */
    protected void generateAdditionalDataStream(PSGenerator gen) throws IOException {
        gen.writeln("/" + getDataName() + " currentfile");
        gen.writeln(getAdditionalFilters(gen));
        if (gen.getPSLevel() == 2) {
            //Creates a data array from the inline file
            gen.writeln("{ /temp exch def ["
                    + " { temp 16384 string readstring not {exit } if } loop ] } exec");
        } else {
            gen.writeln("/ReusableStreamDecode filter");
        }
        PSImageUtils.compressAndWriteBitmap(encoder, gen);
        gen.writeln("def");
    }
    
}
