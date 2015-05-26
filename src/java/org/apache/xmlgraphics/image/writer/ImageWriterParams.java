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

package org.apache.xmlgraphics.image.writer;


/**
 * Parameters for the encoder which is accessed through the
 * ImageWriter interface.
 *
 * @version $Id$
 */
public class ImageWriterParams {

    /** Forces a single strip for the whole image. */
    public static final int SINGLE_STRIP = -1;
    /** Used for generating exactly one strip for each row */
    public static final int ONE_ROW_PER_STRIP = 1;

    private Integer xResolution;
    private Integer yResolution;
    private Float jpegQuality;
    private Boolean jpegForceBaseline;
    private String compressionMethod;
    private ResolutionUnit resolutionUnit = ResolutionUnit.INCH;
    private int rowsPerStrip = ONE_ROW_PER_STRIP;
    private Endianness endianness = Endianness.DEFAULT;

    /**
     * Default constructor.
     */
    public ImageWriterParams() {
        //nop
    }

    /**
     * @return true if resolution has been set
     */
    public boolean hasResolution() {
        return getXResolution() != null && getYResolution() != null;
    }

    /**
     * @return the image resolution in dpi, or null if undefined
     */
    public Integer getResolution() {
        return getXResolution();
    }

    /**
     * @return the quality value for encoding a JPEG image
     *          (0.0-1.0), or null if undefined
     */
    public Float getJPEGQuality() {
        return this.jpegQuality;
    }

    /**
     * @return true if the baseline quantization table is forced,
     *          or null if undefined.
     */
    public Boolean getJPEGForceBaseline() {
        return this.jpegForceBaseline;
    }

    /** @return the compression method for encoding the image */
    public String getCompressionMethod() {
        return this.compressionMethod;
    }

    /**
     * Sets the target resolution of the bitmap image to be written
     * (sets both the horizontal and vertical resolution to the same value).
     * @param resolution the resolution
     */
    public void setResolution(int resolution) {
        setXResolution(resolution);
        setYResolution(resolution);
    }

    /**
     * Sets the quality setting for encoding JPEG images.
     * @param quality the quality setting (0.0-1.0)
     * @param forceBaseline force baseline quantization table
     */
    public void setJPEGQuality(float quality, boolean forceBaseline) {
        this.jpegQuality = quality;
        this.jpegForceBaseline = forceBaseline ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Set the compression method that shall be used to encode the image.
     * @param method the compression method
     */
    public void setCompressionMethod(String method) {
        this.compressionMethod = method;
    }

    /**
     * Checks if image is single strip (required by some fax processors).
     * @return true if one row per strip.
     */
    public boolean isSingleStrip() {
        return rowsPerStrip == SINGLE_STRIP;
    }

    /**
     * Convenience method to set rows per strip to single strip,
     * otherwise sets to one row per strip.
     * @param isSingle true if a single strip shall be produced, false if multiple strips are ok
     */
    public void setSingleStrip(boolean isSingle) {
        rowsPerStrip = isSingle ? SINGLE_STRIP : ONE_ROW_PER_STRIP;
    }

    /**
     * Sets the rows per strip (default is one row per strip);
     * if set to -1 (single strip), will use height of the current page,
     * required by some fax processors.
     * @param rowsPerStrip the value to set.
     */
    public void setRowsPerStrip(int rowsPerStrip) {
        this.rowsPerStrip = rowsPerStrip;
    }

    /**
     * The number of rows per strip of the TIFF image, default 1.  A value of -1
     * indicates a single strip per page will be used and RowsPerStrip will be set
     * to image height for the associated page.
     * @return the number of rows per strip, default 1.
     */
    public int getRowsPerStrip() {
        return rowsPerStrip;
    }

    /**
     * Returns the unit in which resolution values are given (ex. units per inch).
     * @return the resolution unit.
     */
    public ResolutionUnit getResolutionUnit() {
        return resolutionUnit;
    }

    /**
     * Sets the resolution unit of the image for calculating resolution.
     * @param resolutionUnit the resolution unit (inches, centimeters etc.)
     */
    public void setResolutionUnit(ResolutionUnit resolutionUnit) {
        this.resolutionUnit = resolutionUnit;
    }

    /**
     * @return the horizontal image resolution in the current resolution unit, or null if undefined
     */
    public Integer getXResolution() {
        return xResolution;
    }

    /**
     * Sets the target horizontal resolution of the bitmap image to be written.
     * @param resolution the resolution value
     */
    public void setXResolution(int resolution) {
        xResolution = resolution;
    }

    /**
     * @return the vertical image resolution in the current resolution unit, or null if undefined
     */
    public Integer getYResolution() {
        return yResolution;
    }

    /**
     * Sets the target vertical resolution of the bitmap image to be written.
     * @param resolution the resolution value
     */
    public void setYResolution(int resolution) {
        yResolution = resolution;
    }

    /**
     * Returns the endianness selected for the image.
     * @return the endianness
     */
    public Endianness getEndianness() {
        return this.endianness;
    }

    /**
     * Sets the endianness selected for the image.
     * @param endianness the endianness
     */
    public void setEndianness(Endianness endianness) {
        this.endianness = endianness;
    }

}
