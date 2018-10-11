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

package org.apache.xmlgraphics.image.loader.impl;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.xmlgraphics.image.codec.png.PNGChunk;
import org.apache.xmlgraphics.image.codec.util.PropertyUtil;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;

// CSOFF: MethodName

/**
 * Provides methods useful for processing PNG files.
 */
class PNGFile implements PNGConstants {

    private ColorModel colorModel;
    private ICC_Profile iccProfile;
    private int sRGBRenderingIntent = -1;
    private int bitDepth;
    private int colorType;
    private boolean isTransparent;
    private int grayTransparentAlpha;
    private int redTransparentAlpha;
    private int greenTransparentAlpha;
    private int blueTransparentAlpha;
    private List<InputStream> streamVec = new ArrayList<InputStream>();
    private int paletteEntries;
    private byte[] redPalette;
    private byte[] greenPalette;
    private byte[] bluePalette;
    private byte[] alphaPalette;
    private boolean hasPalette;
    private boolean hasAlphaPalette;

    public PNGFile(InputStream stream, String uri) throws IOException, ImageException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        DataInputStream distream = new DataInputStream(stream);
        long magic = distream.readLong();
        if (magic != PNG_SIGNATURE) {
            String msg = PropertyUtil.getString("PNGImageDecoder0");
            throw new ImageException(msg);
        }
        // only some chunks are worth parsing in the current implementation
        do {
            try {
                PNGChunk chunk;
                String chunkType = PNGChunk.getChunkType(distream);
                if (chunkType.equals(PNGChunk.ChunkType.IHDR.name())) {
                    chunk = PNGChunk.readChunk(distream);
                    parse_IHDR_chunk(chunk);
                } else if (chunkType.equals(PNGChunk.ChunkType.PLTE.name())) {
                    chunk = PNGChunk.readChunk(distream);
                    parse_PLTE_chunk(chunk);
                } else if (chunkType.equals(PNGChunk.ChunkType.IDAT.name())) {
                    chunk = PNGChunk.readChunk(distream);
                    streamVec.add(new ByteArrayInputStream(chunk.getData()));
                } else if (chunkType.equals(PNGChunk.ChunkType.IEND.name())) {
                    // chunk = PNGChunk.readChunk(distream);
                    PNGChunk.skipChunk(distream);
                    break; // fall through to the bottom
                } else if (chunkType.equals(PNGChunk.ChunkType.tRNS.name())) {
                    chunk = PNGChunk.readChunk(distream);
                    parse_tRNS_chunk(chunk);
                } else if (chunkType.equals(PNGChunk.ChunkType.iCCP.name())) {
                    chunk = PNGChunk.readChunk(distream);
                    parse_iCCP_chunk(chunk);
                } else if (chunkType.equals(PNGChunk.ChunkType.sRGB.name())) {
                  chunk = PNGChunk.readChunk(distream);
                  parse_sRGB_chunk(chunk);
                } else {
                    if (Character.isUpperCase(chunkType.charAt(0))) {
                        throw new ImageException("PNG unknown critical chunk: " + chunkType);
                    }
                    PNGChunk.skipChunk(distream);
                }
            } catch (Exception e) {
                String msg = PropertyUtil.getString("PNGImageDecoder2");
                throw new RuntimeException(msg + " " + uri, e);
            }
        } while (true);
    }

    public ImageRawPNG getImageRawPNG(ImageInfo info) throws ImageException {
        InputStream seqStream = new SequenceInputStream(Collections.enumeration(streamVec));
        ColorSpace rgbCS = null;
        switch (colorType) {
        case PNG_COLOR_GRAY:
            if (hasPalette) {
                throw new ImageException("Corrupt PNG: color palette is not allowed!");
            }
            colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
                    ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            break;
        case PNG_COLOR_RGB:
            if (iccProfile != null) {
                rgbCS = new ICC_ColorSpace(iccProfile);
            } else if (sRGBRenderingIntent != -1) {
                rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            } else {
                rgbCS = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            }
            colorModel = new ComponentColorModel(rgbCS, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            break;
        case PNG_COLOR_PALETTE:
            if (hasAlphaPalette) {
                colorModel = new IndexColorModel(bitDepth, paletteEntries, redPalette, greenPalette,
                        bluePalette, alphaPalette);
            } else {
                colorModel = new IndexColorModel(bitDepth, paletteEntries, redPalette, greenPalette,
                        bluePalette);
            }
            break;
        case PNG_COLOR_GRAY_ALPHA:
            if (hasPalette) {
                throw new ImageException("Corrupt PNG: color palette is not allowed!");
            }
            colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), true, false,
                    ColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
            break;
        case PNG_COLOR_RGB_ALPHA:
            if (iccProfile != null) {
                rgbCS = new ICC_ColorSpace(iccProfile);
            } else if (sRGBRenderingIntent != -1) {
                rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            } else {
                rgbCS = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            }
            colorModel = new ComponentColorModel(rgbCS, true, false, ColorModel.TRANSLUCENT,
                    DataBuffer.TYPE_BYTE);
            break;
        default:
            throw new ImageException("Unsupported color type: " + colorType);
        }
        // the iccProfile is still null for now
        ImageRawPNG rawImage = new ImageRawPNG(info, seqStream, colorModel, bitDepth, iccProfile);
        if (isTransparent) {
            if (colorType == PNG_COLOR_GRAY) {
                rawImage.setGrayTransparentAlpha(grayTransparentAlpha);
            } else if (colorType == PNG_COLOR_RGB) {
                rawImage.setRGBTransparentAlpha(redTransparentAlpha, greenTransparentAlpha,
                        blueTransparentAlpha);
            } else if (colorType == PNG_COLOR_PALETTE) {
                rawImage.setTransparent();
            } else {
                //
            }
        }
        if (sRGBRenderingIntent != -1) {
          rawImage.setRenderingIntent(sRGBRenderingIntent);
        }
        return rawImage;
    }

    private void parse_IHDR_chunk(PNGChunk chunk) {
        bitDepth = chunk.getInt1(8);
        colorType = chunk.getInt1(9);
        int compressionMethod = chunk.getInt1(10);
        if (compressionMethod != 0) {
            throw new RuntimeException("Unsupported PNG compression method: " + compressionMethod);
        }
        int filterMethod = chunk.getInt1(11);
        if (filterMethod != 0) {
            throw new RuntimeException("Unsupported PNG filter method: " + filterMethod);
        }
        int interlaceMethod = chunk.getInt1(12);
        if (interlaceMethod != 0) {
            // this is a limitation of the current implementation
            throw new RuntimeException("Unsupported PNG interlace method: " + interlaceMethod);
        }
    }

    private void parse_PLTE_chunk(PNGChunk chunk) {
        paletteEntries = chunk.getLength() / 3;
        redPalette = new byte[paletteEntries];
        greenPalette = new byte[paletteEntries];
        bluePalette = new byte[paletteEntries];
        hasPalette = true;

        int pltIndex = 0;
        for (int i = 0; i < paletteEntries; i++) {
            redPalette[i] = chunk.getByte(pltIndex++);
            greenPalette[i] = chunk.getByte(pltIndex++);
            bluePalette[i] = chunk.getByte(pltIndex++);
        }
    }

    private void parse_tRNS_chunk(PNGChunk chunk) {
        if (colorType == PNG_COLOR_PALETTE) {
            int entries = chunk.getLength();
            if (entries > paletteEntries) {
                // Error -- mustn't have more alpha than RGB palette entries
                String msg = PropertyUtil.getString("PNGImageDecoder14");
                throw new RuntimeException(msg);
            }
            // Load beginning of palette from the chunk
            alphaPalette = new byte[paletteEntries];
            for (int i = 0; i < entries; i++) {
                alphaPalette[i] = chunk.getByte(i);
            }
            // Fill rest of palette with 255
            for (int i = entries; i < paletteEntries; i++) {
                alphaPalette[i] = (byte) 255;
            }
            hasAlphaPalette = true;
        } else if (colorType == PNG_COLOR_GRAY) {
            grayTransparentAlpha = chunk.getInt2(0);
        } else if (colorType == PNG_COLOR_RGB) {
            redTransparentAlpha = chunk.getInt2(0);
            greenTransparentAlpha = chunk.getInt2(2);
            blueTransparentAlpha = chunk.getInt2(4);
        } else if (colorType == PNG_COLOR_GRAY_ALPHA || colorType == PNG_COLOR_RGB_ALPHA) {
            // Error -- GA or RGBA image can't have a tRNS chunk.
            String msg = PropertyUtil.getString("PNGImageDecoder15");
            throw new RuntimeException(msg);
        }
        isTransparent = true;
    }

    private void parse_iCCP_chunk(PNGChunk chunk) {
        int length = chunk.getLength();
        int textIndex = 0;
        while (chunk.getByte(textIndex++) != 0) {
            //NOP
        }
        textIndex++;
        byte[] profile = new byte[length - textIndex];
        System.arraycopy(chunk.getData(), textIndex, profile, 0, length - textIndex);
        ByteArrayInputStream bais = new ByteArrayInputStream(profile);
        InflaterInputStream iis = new InflaterInputStream(bais, new Inflater());
        try {
            iccProfile = ICC_Profile.getInstance(iis);
        } catch (IOException ioe) {
            // this is OK; the profile will be null
        }
    }

    private void parse_sRGB_chunk(PNGChunk chunk) {
      sRGBRenderingIntent = chunk.getByte(0);
    }

}
