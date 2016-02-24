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

package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

final class ImageInfo {

    // Default values
    private static final int DEFAULT_ROWS_PER_STRIP = 8;

    private final int numExtraSamples;
    private final ExtraSamplesType extraSampleType;
    private final ImageType imageType;
    private final int colormapSize;
    private final char[] colormap;
    private final int tileWidth;
    private final int tileHeight;
    private final int numTiles;
    private final long bytesPerRow;
    private final long bytesPerTile;

    private ImageInfo(ImageInfoBuilder builder) {
        this.numExtraSamples = builder.numExtraSamples;
        this.extraSampleType = builder.extraSampleType;
        this.imageType = builder.imageType;
        this.colormapSize = builder.colormapSize;
        this.colormap = copyColormap(builder.colormap);
        this.tileWidth = builder.tileWidth;
        this.tileHeight = builder.tileHeight;
        this.numTiles = builder.numTiles;
        this.bytesPerRow = builder.bytesPerRow;
        this.bytesPerTile = builder.bytesPerTile;
    }

    private static char[] copyColormap(char[] colorMap) {
        if (colorMap == null) {
            return null;
        }
        char[] copy = new char[colorMap.length];
        System.arraycopy(colorMap, 0, copy, 0, colorMap.length);
        return copy;
    }

    private static int getNumberOfExtraSamplesForColorSpace(ColorSpace colorSpace,
            ImageType imageType, int numBands) {
        if (imageType == ImageType.GENERIC) {
            return numBands - 1;
        } else if (numBands > 1) {
            return numBands - colorSpace.getNumComponents();
        } else {
            return 0;
        }
    }

    private static char[] createColormap(final int sizeOfColormap, byte[] r, byte[] g, byte[] b) {
        int redIndex = 0;
        int greenIndex = sizeOfColormap;
        int blueIndex = 2 * sizeOfColormap;
        char[] colormap = new char[sizeOfColormap * 3];
        for (int i = 0; i < sizeOfColormap; i++) {
            // beware of sign extended bytes
            colormap[redIndex++] = convertColorToColormapChar(0xff & r[i]);
            colormap[greenIndex++] = convertColorToColormapChar(0xff & g[i]);
            colormap[blueIndex++] = convertColorToColormapChar(0xff & b[i]);
        }
        return colormap;
    }

    private static char convertColorToColormapChar(int color) {
        return (char) (color << 8 | color);
    }

    int getNumberOfExtraSamples() {
        return numExtraSamples;
    }

    ExtraSamplesType getExtraSamplesType() {
        return extraSampleType;
    }

    ImageType getType() {
        return imageType;
    }

    int getColormapSize() {
        return colormapSize;
    }

    char[] getColormap() {
        return copyColormap(colormap);
    }

    int getTileWidth() {
        return tileWidth;
    }

    int getTileHeight() {
        return tileHeight;
    }

    int getNumTiles() {
        return numTiles;
    }

    long getBytesPerRow() {
        return bytesPerRow;
    }

    long getBytesPerTile() {
        return bytesPerTile;
    }

    static ImageInfo newInstance(RenderedImage im, int dataTypeSize, int numBands,
            ColorModel colorModel, TIFFEncodeParam params) {
        ImageInfoBuilder builder = new ImageInfoBuilder();
        if (colorModel instanceof IndexColorModel) { // Bilevel or palette
            IndexColorModel indexColorModel = (IndexColorModel) colorModel;
            int colormapSize = indexColorModel.getMapSize();
            byte[] r = new byte[colormapSize];
            indexColorModel.getReds(r);
            byte[] g = new byte[colormapSize];
            indexColorModel.getGreens(g);
            byte[] b = new byte[colormapSize];
            indexColorModel.getBlues(b);

            builder.imageType = ImageType.getTypeFromRGB(colormapSize, r, g, b, dataTypeSize,
                    numBands);
            if (builder.imageType == ImageType.PALETTE) {
                builder.colormap = createColormap(colormapSize, r, g, b);
                builder.colormapSize = colormapSize * 3;
            }
        } else if (colorModel == null) {
            if (dataTypeSize == 1 && numBands == 1) { // bilevel
                builder.imageType = ImageType.BILEVEL_BLACK_IS_ZERO;
            } else {
                builder.imageType = ImageType.GENERIC;
                builder.numExtraSamples = numBands > 1 ? numBands - 1 : 0;
            }
        } else {
            ColorSpace colorSpace = colorModel.getColorSpace();
            builder.imageType = ImageType.getTypeFromColorSpace(colorSpace, params);
            builder.numExtraSamples = getNumberOfExtraSamplesForColorSpace(colorSpace,
                    builder.imageType, numBands);
            builder.extraSampleType = ExtraSamplesType.getValue(colorModel,
                    builder.numExtraSamples);
        }

        // Initialize tile dimensions.
        final int width = im.getWidth();
        final int height = im.getHeight();
        if (params.getWriteTiled()) {
            builder.tileWidth = params.getTileWidth() > 0 ? params.getTileWidth() : width;
            builder.tileHeight = params.getTileHeight() > 0 ? params.getTileHeight() : height;
            // NB: Parentheses are used in this statement for correct rounding.
            builder.numTiles = ((width + builder.tileWidth - 1) / builder.tileWidth)
                    * ((height + builder.tileHeight - 1) / builder.tileHeight);
        } else {
            builder.tileWidth = width;
            builder.tileHeight = params.getTileHeight() > 0 ? params.getTileHeight()
                    : DEFAULT_ROWS_PER_STRIP;
            builder.numTiles = (int) Math.ceil(height / (double) builder.tileHeight);
        }
        builder.setBytesPerRow(dataTypeSize, numBands)
        .setBytesPerTile();
        return builder.build();
    }

    private static final class ImageInfoBuilder {
        private ImageType imageType = ImageType.UNSUPPORTED;
        private int numExtraSamples;
        private char[] colormap;
        private int colormapSize;
        private ExtraSamplesType extraSampleType = ExtraSamplesType.UNSPECIFIED;
        private int tileWidth;
        private int tileHeight;
        private int numTiles;
        private long bytesPerRow;
        private long bytesPerTile;

        private ImageInfoBuilder setBytesPerRow(int dataTypeSize, int numBands) {
            bytesPerRow = (long) Math.ceil((dataTypeSize / 8.0) * tileWidth * numBands);
            return this;
        }

        private ImageInfoBuilder setBytesPerTile() {
            bytesPerTile = bytesPerRow * tileHeight;
            return this;
        }

        private ImageInfo build() {
            return new ImageInfo(this);
        }
    }
}
