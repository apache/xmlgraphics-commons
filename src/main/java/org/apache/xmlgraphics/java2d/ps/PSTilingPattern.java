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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * This class is implementation of PostScript tiling pattern. It allows to make a pattern
 * with defined PaintProc or texture.
 *
 * Originally authored by Jiri Kunhart.
 */
public class PSTilingPattern {

    /**
     * A code identifying the pattern type that this dictionary describes;
     * must be 1 for a tiling pattern
     */
    public static final int PATTERN_TYPE_TILING = 1;

    /** PostScript constant for a shading pattern (unsupported) */
    public static final int PATTERN_TYPE_SHADING = 2;

    /** the pattern type of this pattern */
    protected int patternType = PATTERN_TYPE_TILING;
    //TODO To be moved to a super class once shading patterns are implemented.

    /**
     * The name of the pattern (for example: "Pattern1" )
     */
    protected String patternName;

    /**
     * The XUID is an extended unique ID -- an array of integers that provides for
     * distributed, hierarchical management of the space of unique ID numbers
     * (optional)
     */
    protected List xUID;

    /**
     * A PostScript procedure for painting the pattern cell
     */
    protected StringBuffer paintProc;

    /**
     * An array of four numbers in the pattern coordinate system, giving
     * the coordinates of the left, bottom, right, and top edges, respectively, of the
     * pattern cell's bounding box
     */
    protected Rectangle2D bBox;

    /**
     * The desired horizontal spacing between pattern cells, measured in
     * the pattern coordinate system
     */
    protected double xStep;

    /**
     * The desired vertical spacing between pattern cells, measured in
     * the pattern coordinate system
     */
    protected double yStep;

    /**
     * A code that determines how the color of the pattern cell is to be
     * specified: 1 for colored pattern, 2 for uncolored pattern
     */
    protected int paintType = 2;

    /**
     * A code that controls adjustments to the spacing of tiles relative to
     * the device pixel grid:
     * 1 for constant spacing,
     * 2 for no distortion
     * 3 for constant spacing and faster tiling.
     */
    protected int tilingType = 1;

    /**
     *  A texture is used for filling shapes
     */
    protected TexturePaint texture;

    /**
     * Constructor for the creation of pattern with defined PaintProc
     *
     * @param patternName the name of the pattern (for example: "Pattern1" ), if
     * the name is null, the pattern should be stored in PSPatternStorage, where the pattern
     * gets a name (the pattern without name cannot be use in PS file)
     * @param paintProc a postscript procedure for painting the pattern cell
     * @param bBox a pattern cell's bounding box
     * @param xStep the desired horizontal spacing between pattern cells
     * @param yStep the desired vertical spacing between pattern cells
     * @param paintType 1 for colored pattern, 2 for uncolored pattern
     * @param tilingType adjustments to the spacing of tiles relative to
     * the device pixel grid (1,2 or 3)
     * @param xUID an extended unique ID (optional)
     */
    public PSTilingPattern(String patternName, StringBuffer paintProc, Rectangle bBox,
                           double xStep, double yStep,
                           int paintType, int tilingType, List xUID) {

        // check the parameters
        this.patternName = patternName;
        this.paintProc = paintProc;
        setBoundingBox(bBox);
        setXStep(xStep);
        setYStep(yStep);
        setPaintType(paintType);
        setTilingType(tilingType);
        this.xUID = xUID;
    }

    /**
     * Constructor for the creation of pattern with defined texture
     *
     * @param patternName the name of the pattern (for example: "Pattern1" ), if
     * the name is null, the pattern should be stored in PSPatternStorage, where the pattern
     * gets a name (a pattern without name cannot be use in PS file)
     * @param texture a texture is used for filling a shape
     * @param xStep the desired horizontal spacing between pattern cells
     * @param yStep yStep the desired vertical spacing between pattern cells
     * @param tilingType adjustments to the spacing of tiles relative to
     * the device pixel grid (1,2 or 3)
     * @param xUID xUID an extended unique ID (optional)
     */
    public PSTilingPattern(String patternName, TexturePaint texture, double xStep, double yStep,
                           int tilingType, List xUID) {

        this(patternName, null, new Rectangle(), 1, 1, 1, tilingType, xUID);

        this.texture = texture;

        Rectangle2D anchor = texture.getAnchorRect();
        bBox = new Rectangle2D.Double(
                anchor.getX(), anchor.getY(),
                anchor.getX() + anchor.getWidth(), anchor.getY() + anchor.getHeight());

        // xStep and yStep may be either positive or negative, but not zero => if it is zero,
        // we set xStep and yStep in this way that the pattern will be without spaces
        this.xStep = (xStep == 0) ? anchor.getWidth() : xStep;
        this.yStep = (yStep == 0) ? anchor.getHeight() : yStep;
    }

    /**
     * Gets the name of the pattern
     *
     * @return String representing the name of the pattern.
     */
    public String getName() {
        return (this.patternName);
    }

    /**
     * Sets the name of the pattern.
     * @param name the name of the pattern. Can be anything without spaces (for example "Pattern1").
     */
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException("Parameter patternName must not be null");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Parameter patternName must not be empty");
        }
        if (name.indexOf(" ") >= 0) {
            throw new IllegalArgumentException(
                    "Pattern name must not contain any spaces");
        }
        this.patternName = name;
    }

    /**
     * Returns the bounding box.
     *
     * @return a pattern cell's bounding box
     */
    public Rectangle2D getBoundingBox() {
        return (this.bBox);
    }

    /**
     * Sets the bounding box.
     *
     * @param bBox a pattern cell's bounding box
     */
    public void setBoundingBox(Rectangle2D bBox) {
        if (bBox == null) {
            throw new NullPointerException("Parameter bBox must not be null");
        }
        this.bBox = bBox;
    }

    /**
     * Gets the postscript procedure PaintProc
     *
     * @return the postscript procedure PaintProc
     */
    public StringBuffer getPaintProc() {
        return (this.paintProc);
    }

    /**
     * Sets the postscript procedure PaintProc
     *
     * @param paintProc the postscript procedure PaintProc
     */
    public void setPaintProc(StringBuffer paintProc) {
        this.paintProc = paintProc;
    }

    /**
     * Gets the horizontal spacing between pattern cells
     *
     * @return the horizontal spacing between pattern cells
     */
    public double getXStep() {
        return (this.xStep);
    }

    /**
     * Sets the horizontal spacing between pattern cells
     *
     * @param xStep the horizontal spacing between pattern cells
     */
    public void setXStep(double xStep) {
        if (xStep == 0) {
            throw new IllegalArgumentException("Parameter xStep must not be 0");
        }
        this.xStep = xStep;
    }

    /**
     * Gets the vertical spacing between pattern cells
     *
     * @return the vertical spacing between pattern cells
     */
    public double getYStep() {
        return (this.yStep);
    }

    /**
     * Sets the vertical spacing between pattern cells
     *
     * @param yStep the vertical spacing between pattern cells
     */
    public void setYStep(double yStep) {
        if (yStep == 0) {
            throw new IllegalArgumentException("Parameter yStep must not be 0");
        }
        this.yStep = yStep;
    }

    /**
     * Gets the code that determines how the color of the pattern cell is to be
     * specified: 1 for colored pattern, 2 for uncolored pattern
     *
     * @return the paint type
     */
    public int getPaintType() {
        return (this.paintType);
    }

    /**
     * Sets the code that determines how the color of the pattern cell is to be
     * specified: 1 for colored pattern, 2 for uncolored pattern
     *
     * @param paintType the paint type
     */
    public void setPaintType(int paintType) {
        if ((paintType != 1) && (paintType != 2)) {
            throw new IllegalArgumentException("Parameter paintType must not be "
                    + paintType + " (only 1 or 2)");
        }
        this.paintType = paintType;
    }

    /**
     * Gets a code that controls adjustments to the spacing of tiles relative to
     * the device pixel grid: 1 for constant spacing, 2 for no distortion
     * 3 for constant spacing and faster tiling
     *
     * @return the tiling type
     */
    public int getTilingType() {
        return (this.tilingType);
    }

    /**
     * Sets a code that controls adjustments to the spacing of tiles relative to
     * the device pixel grid: 1 for constant spacing, 2 for no distortion
     * 3 for constant spacing and faster tiling
     *
     * @param tilingType the tiling type
     */
    public void setTilingType(int tilingType) {
        if (!((tilingType <= 3) && (tilingType >= 1))) {
            throw new IllegalArgumentException("Parameter tilingType must not be "
                    + tilingType + " (only 1, 2 or 3)");
        }
        this.tilingType = tilingType;
    }

    /**
     * Gets a texture which is used for filling shapes
     *
     * @return the texture
     */
    public TexturePaint getTexturePaint() {
        return (this.texture);
    }

    /**
     * Sets a texture which is used for filling shapes
     *
     * @param texturePaint the texture
     */
    public void setTexturePaint(TexturePaint texturePaint) {
        this.texture = texturePaint;
    }

    /**
     * Gets an extended unique ID that uniquely identifies the pattern
     *
     * @return xUID the unique ID
     */
    public List getXUID() {
        return (this.xUID);
    }

    /**
     * Sets an extended unique ID that uniquely identifies the pattern
     *
     * @param xUID the unique ID
     */
    public void setXUID(List xUID) {
        this.xUID = xUID;
    }

    /**
     * Generates postscript code for a pattern
     *
     * @return The string which contains postscript code of pattern definition
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("<<\n");
        sb.append("/PatternType " + this.patternType + "\n");
        sb.append("/PaintType " + paintType + "\n");
        sb.append("/TilingType " + tilingType + "\n");
        sb.append("/XStep " + xStep + "\n");
        sb.append("/YStep " + yStep + "\n");
        sb.append("/BBox " + "[" + bBox.getX() + " " + bBox.getY() + " "
                        + bBox.getWidth() + " " + bBox.getHeight() + "]" + "\n");
        sb.append("/PaintProc\n" + "{\n");

        // the PaintProc procedure is expected to consume its dictionary operand !
        if ((paintProc == null) || (paintProc.indexOf("pop") != 0)) {
            sb.append("pop\n");
        }

        if (texture != null) {
            int width = texture.getImage().getWidth();
            int height = texture.getImage().getHeight();

            Rectangle2D anchor = texture.getAnchorRect();
            if (anchor.getX() != 0 || anchor.getY() != 0) {
                sb.append(anchor.getX() + " " + anchor.getY() + " translate\n");
            }
            double scaleX = anchor.getWidth() / width;
            double scaleY = anchor.getHeight() / height;
            if (scaleX != 1 || scaleY != 1) {
                sb.append(scaleX + " " + scaleY + " scale\n");
            }

            // define color image: width height bits/comp matrix
            //                        datasrc0 datasrcncomp-1 multi ncomp colorimage
            sb.append(width + " " + height + " 8 " + "matrix\n");   // width height bits/comp matrix
            int [] argb = new int[width * height];                  // datasrc0 datasrcncomp-1
            sb.append("{<");
            getAsRGB().getRGB(0, 0, width, height, argb, 0, width);
            int count = 0;
            for (int i = 0; i < argb.length; i++) {
                if ((i % width == 0) || (count > 249)) {
                    sb.append("\n");
                    count = 0;  // line should not be longer than 255 characters
                }
                // delete alpha canal and write to output
                StringBuffer sRGB = new StringBuffer(Integer.toHexString(argb[i] & 0x00ffffff));
                if (sRGB.length() != 6) {
                    sRGB.insert(0, "000000");   // zero padding
                    sRGB = new StringBuffer(sRGB.substring(sRGB.length() - 6));
                }
                sb.append(sRGB);
                count += 6;
            }
            sb.append("\n>} false 3 colorimage");                   //  multi ncomp colorimage
        } else {
            sb.append(paintProc);
        }
        sb.append("\n} bind \n");  // the end of PaintProc
        sb.append(">>\n");

        // create pattern instance from prototype
        sb.append("matrix\n");
        sb.append("makepattern\n");

        // save pattern to current dictionary
        sb.append("/" + patternName + " exch def\n");

        return sb.toString();
    }

    private BufferedImage getAsRGB() {
        BufferedImage img = texture.getImage();
        if (img.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage buf = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = buf.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setBackground(Color.white);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return buf;
        }
        return img;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return
            0
            ^ patternType
            ^ ((xUID != null) ? xUID.hashCode() : 0)
            ^ ((paintProc != null) ? paintProc.hashCode() : 0)
            ^ ((bBox != null) ? bBox.hashCode() : 0)
            ^ Double.valueOf(xStep).hashCode()
            ^ Double.valueOf(yStep).hashCode()
            ^ paintType
            ^ tilingType
            ^ ((texture != null) ? texture.hashCode() : 0);
    }

    /**
     * Compares two patterns data (except their names).
     * {@inheritDoc}
     */
    public boolean equals(Object pattern) {
        if (pattern == null) {
            return false;
        }
        if (!(pattern instanceof PSTilingPattern)) {
            return false;
        }
        if (this == pattern) {
            return true;
        }

        PSTilingPattern patternObj = (PSTilingPattern) pattern;
        if (this.patternType != patternObj.patternType) {
            return false;
        }

        TexturePaint patternTexture = patternObj.getTexturePaint();

        if (((patternTexture == null) && (texture != null))
             || ((patternTexture != null) && (texture == null))) {
            return false;
        }

        if ((patternTexture != null) && (texture != null)) {
            // compare textures data
            int width = texture.getImage().getWidth();
            int height = texture.getImage().getHeight();

            int widthPattern = patternTexture.getImage().getWidth();
            int heightPattern = patternTexture.getImage().getHeight();

            if (width != widthPattern) {
                return false;
            }
            if (height != heightPattern) {
                return false;
            }
            int [] rgbData = new int[width * height];
            int [] rgbDataPattern = new int[widthPattern * heightPattern];

            texture.getImage().getRGB(0, 0, width, height, rgbData, 0, width);
            patternTexture.getImage().getRGB(0, 0, widthPattern, heightPattern,
                    rgbDataPattern, 0, widthPattern);

            for (int i = 0; i < rgbData.length; i++) {
                if (rgbData[i] != rgbDataPattern[i]) {
                    return false;
                }
            }
        } else {
            // compare PaintProc
            if (!paintProc.toString().equals(patternObj.getPaintProc().toString())) {
                return false;
            }
        }

        // compare other parameters
        if (xStep != patternObj.getXStep()) {
            return false;
        }
        if (yStep != patternObj.getYStep()) {
            return false;
        }
        if (paintType != patternObj.getPaintType()) {
            return false;
        }
        if (tilingType != patternObj.getTilingType()) {
            return false;
        }
        if (!bBox.equals(patternObj.getBoundingBox())) {
            return false;
        }
        if ((xUID != null) && (patternObj.getXUID() != null)) {
            if (!xUID.equals(patternObj.getXUID())) {
                return false;
            }
        }
        return true;
    }
}
