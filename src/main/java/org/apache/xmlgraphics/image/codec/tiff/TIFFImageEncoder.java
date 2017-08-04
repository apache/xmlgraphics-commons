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

package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.Deflater;

import org.apache.xmlgraphics.image.codec.util.ImageEncodeParam;
import org.apache.xmlgraphics.image.codec.util.ImageEncoderImpl;
import org.apache.xmlgraphics.image.codec.util.PropertyUtil;
import org.apache.xmlgraphics.image.codec.util.SeekableOutputStream;

// CSOFF: ConstantName
// CSOFF: MissingSwitchDefault
// CSOFF: MultipleVariableDeclarations
// CSOFF: OperatorWrap
// CSOFF: WhitespaceAround

/**
 * A baseline TIFF writer. The writer outputs TIFF images in either Bilevel,
 * Greyscale, Palette color or Full Color modes.
 *
 */
public class TIFFImageEncoder extends ImageEncoderImpl {

    // Incidental tags
    private static final int TIFF_JPEG_TABLES       = 347;
    private static final int TIFF_YCBCR_SUBSAMPLING = 530;
    private static final int TIFF_YCBCR_POSITIONING = 531;
    private static final int TIFF_REF_BLACK_WHITE   = 532;



    public TIFFImageEncoder(OutputStream output, ImageEncodeParam param) {
        super(output, param);
        if (this.param == null) {
            this.param = new TIFFEncodeParam();
        }
    }

    /**
     * Encodes a RenderedImage and writes the output to the
     * OutputStream associated with this ImageEncoder.
     */
    public void encode(RenderedImage im) throws IOException {
        // Write the file header (8 bytes).
        writeFileHeader();

        // Get the encoding parameters.
        TIFFEncodeParam encodeParam = (TIFFEncodeParam)param;

        Iterator iter = encodeParam.getExtraImages();
        if (iter != null) {
            int ifdOffset = 8;
            RenderedImage nextImage = im;
            TIFFEncodeParam nextParam = encodeParam;
            boolean hasNext;
            do {
                hasNext = iter.hasNext();
                ifdOffset = encode(nextImage, nextParam, ifdOffset, !hasNext);
                if (hasNext) {
                    Object obj = iter.next();
                    if (obj instanceof RenderedImage) {
                        nextImage = (RenderedImage)obj;
                        nextParam = encodeParam;
                    } else if (obj instanceof Object[]) {
                        Object[] o = (Object[])obj;
                        nextImage = (RenderedImage)o[0];
                        nextParam = (TIFFEncodeParam)o[1];
                    }
                }
            } while(hasNext);
        } else {
            encode(im, encodeParam, 8, true);
        }
    }

    /**
     * Encodes a RenderedImage as part of a multi-page file and writes the output to the
     * OutputStream associated with this ImageEncoder.
     * <p>
     * When you sent all pages, make sure you call finishMultiple() in the end. Otherwise,
     * the generated file will be corrupted.
     * @param context the context object you receive as return value to a previous call to
     *                encodeMultiple(). Set null for the first image.
     * @param img the image
     * @return a context object needed for writing multiple pages for a single image file
     * @throws IOException In case of an I/O error
     */
    public Object encodeMultiple(Object context, RenderedImage img) throws IOException {
        // Get the encoding parameters.
        TIFFEncodeParam encodeParam = (TIFFEncodeParam)param;
        if (encodeParam.getExtraImages() != null) {
            throw new IllegalStateException(PropertyUtil.getString("TIFFImageEncoder11"));
        }

        Context c = (Context)context;
        if (c == null) {
            c = new Context();
            // Write the file header (8 bytes).
            writeFileHeader();
        } else {
            //write image
            c.ifdOffset = encode(c.nextImage, encodeParam, c.ifdOffset, false);
        }
        c.nextImage = img;
        return c;
    }

    /**
     * Signals the encoder that you've finished sending pages for a multi-page image files.
     * @param context the context object you receive as return value to a previous call to
     *                encodeMultiple()
     * @throws IOException In case of an I/O error
     */
    public void finishMultiple(Object context) throws IOException {
        if (context == null) {
            throw new NullPointerException();
        }
        Context c = (Context)context;
        // Get the encoding parameters.
        TIFFEncodeParam encodeParam = (TIFFEncodeParam)param;

        //write last image
        c.ifdOffset = encode(c.nextImage, encodeParam, c.ifdOffset, true);
    }

    private static class Context {
        //TODO This approach causes always two images to be present at the same time.
        //The encoder has to be changed a little to avoid that.
        private RenderedImage nextImage;
        private int ifdOffset = 8; //Initial offset
    }

    private int encode(RenderedImage im, TIFFEncodeParam encodeParam,
                       int ifdOffset, boolean isLast) throws IOException {
        // Currently all images are stored uncompressed.
        CompressionValue compression = encodeParam.getCompression();

        if (compression == CompressionValue.JPEG_TTN2) {
            throw new IllegalArgumentException(PropertyUtil.getString("TIFFImageEncoder12"));
        }

        // Get tiled output preference.
        boolean isTiled = encodeParam.getWriteTiled();

        // Set bounds.
        int minX = im.getMinX();
        int minY = im.getMinY();
        int width = im.getWidth();
        int height = im.getHeight();

        // Get SampleModel.
        SampleModel sampleModel = im.getSampleModel();
        ColorModel colorModel = im.getColorModel();
        int[] sampleSize = sampleModel.getSampleSize();
        int dataTypeSize = sampleSize[0];
        int numBands = sampleModel.getNumBands();
        int dataType = sampleModel.getDataType();
        validateImage(dataTypeSize, sampleSize, numBands, dataType, colorModel);

        boolean dataTypeIsShort = dataType == DataBuffer.TYPE_SHORT
                || dataType == DataBuffer.TYPE_USHORT;

        // Set image type.
        ImageInfo imageInfo = ImageInfo.newInstance(im, dataTypeSize, numBands, colorModel,
                encodeParam);

        if (imageInfo.getType() == ImageType.UNSUPPORTED) {
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder8"));
        }

        final int numTiles = imageInfo.getNumTiles();
        final long bytesPerTile = imageInfo.getBytesPerTile();
        final long bytesPerRow = imageInfo.getBytesPerRow();
        final int tileHeight = imageInfo.getTileHeight();
        final int tileWidth = imageInfo.getTileWidth();

        long[] tileByteCounts = new long[numTiles];
        for (int i = 0; i < numTiles; i++) {
            tileByteCounts[i] = bytesPerTile;
        }

        if (!isTiled) {
            // Last strip may have lesser rows
            long lastStripRows = height - (tileHeight * (numTiles - 1));
            tileByteCounts[numTiles - 1] = lastStripRows * bytesPerRow;
        }
        long totalBytesOfData = bytesPerTile * (numTiles - 1) + tileByteCounts[numTiles - 1];
        long[] tileOffsets = new long[numTiles];

        // Basic fields - have to be in increasing numerical order.
        // ImageWidth                     256
        // ImageLength                    257
        // BitsPerSample                  258
        // Compression                    259
        // PhotoMetricInterpretation      262
        // StripOffsets                   273
        // RowsPerStrip                   278
        // StripByteCounts                279
        // XResolution                    282
        // YResolution                    283
        // ResolutionUnit                 296

        // Create Directory
        SortedSet<TIFFField> fields = new TreeSet<TIFFField>();

        // Image Width
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_IMAGE_WIDTH,
                                 TIFFField.TIFF_LONG, 1,
                                 new long[] {width}));

        // Image Length
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_IMAGE_LENGTH,
                                 TIFFField.TIFF_LONG, 1,
                                 new long[] {height}));

        char [] shortSampleSize = new char[numBands];
        for (int i = 0; i < numBands; i++) {
            shortSampleSize[i] = (char) dataTypeSize;
        }
        fields.add(new TIFFField(TIFFImageDecoder.TIFF_BITS_PER_SAMPLE,
                                 TIFFField.TIFF_SHORT, numBands,
                                 shortSampleSize));

        fields.add(new TIFFField(TIFFImageDecoder.TIFF_COMPRESSION,
                                 TIFFField.TIFF_SHORT, 1,
                                 new char[] {(char)compression.getValue()}));

        fields.add(
            new TIFFField(TIFFImageDecoder.TIFF_PHOTOMETRIC_INTERPRETATION,
                          TIFFField.TIFF_SHORT, 1,
                      new char[] {(char) imageInfo.getType().getPhotometricInterpretation()}));

        if (!isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_STRIP_OFFSETS,
                                     TIFFField.TIFF_LONG, numTiles,
                                     tileOffsets));
        }

        fields.add(new TIFFField(TIFFImageDecoder.TIFF_SAMPLES_PER_PIXEL,
                                 TIFFField.TIFF_SHORT, 1,
                                 new char[] {(char)numBands}));

        if (!isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_ROWS_PER_STRIP,
                                     TIFFField.TIFF_LONG, 1,
                                     new long[] {tileHeight}));

            fields.add(new TIFFField(TIFFImageDecoder.TIFF_STRIP_BYTE_COUNTS,
                                     TIFFField.TIFF_LONG, numTiles,
                                     tileByteCounts));
        }

        if (imageInfo.getColormap() != null) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_COLORMAP,
                    TIFFField.TIFF_SHORT, imageInfo.getColormapSize(),
                    imageInfo.getColormap()));
        }

        if (isTiled) {
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_WIDTH,
                                     TIFFField.TIFF_LONG, 1,
                                     new long[] {tileWidth}));

            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_LENGTH,
                                     TIFFField.TIFF_LONG, 1,
                                     new long[] {tileHeight}));

            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_OFFSETS,
                                     TIFFField.TIFF_LONG, numTiles,
                                     tileOffsets));

            fields.add(new TIFFField(TIFFImageDecoder.TIFF_TILE_BYTE_COUNTS,
                                     TIFFField.TIFF_LONG, numTiles,
                                     tileByteCounts));
        }

        if (imageInfo.getNumberOfExtraSamples() > 0) {
            char[] extraSamples = new char[imageInfo.getNumberOfExtraSamples()];
            for (int i = 0; i < imageInfo.getNumberOfExtraSamples(); i++) {
                extraSamples[i] = (char) imageInfo.getExtraSamplesType().getValue();
            }
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_EXTRA_SAMPLES,
                    TIFFField.TIFF_SHORT, imageInfo.getNumberOfExtraSamples(),
                                     extraSamples));
        }

        // Data Sample Format Extension fields.
        if (dataType != DataBuffer.TYPE_BYTE) {
            // SampleFormat
            char[] sampleFormat = new char[numBands];
            if (dataType == DataBuffer.TYPE_FLOAT) {
                sampleFormat[0] = 3;
            } else if (dataType == DataBuffer.TYPE_USHORT) {
                sampleFormat[0] = 1;
            } else {
                sampleFormat[0] = 2;
            }
            for (int b = 1; b < numBands; b++) {
                sampleFormat[b] = sampleFormat[0];
            }
            fields.add(new TIFFField(TIFFImageDecoder.TIFF_SAMPLE_FORMAT,
                                     TIFFField.TIFF_SHORT, numBands,
                                     sampleFormat));

            // NOTE: We don't bother setting the SMinSampleValue and
            // SMaxSampleValue fields as these both default to the
            // extrema of the respective data types.  Probably we should
            // check for the presence of the "extrema" property and
            // use it if available.
        }

        if (imageInfo.getType() == ImageType.YCBCR) {
            // YCbCrSubSampling: 2 is the default so we must write 1 as
            // we do not (yet) do any subsampling.
            char subsampleH = 1;
            char subsampleV = 1;

            fields.add(new TIFFField(TIFF_YCBCR_SUBSAMPLING,
                                     TIFFField.TIFF_SHORT, 2,
                                     new char[] {subsampleH, subsampleV}));


            // YCbCr positioning.
            fields.add(new TIFFField(TIFF_YCBCR_POSITIONING,
                                     TIFFField.TIFF_SHORT, 1,
                                     new char[]
                    {(char) ((compression == CompressionValue.JPEG_TTN2) ? 1 : 2)}));

            // Reference black/white.
            long[][] refbw;
            refbw = new long[][] // CCIR 601.1 headroom/footroom (presumptive)
                    {{15, 1}, {235, 1}, {128, 1}, {240, 1}, {128, 1}, {240, 1}};

            fields.add(new TIFFField(TIFF_REF_BLACK_WHITE,
                                     TIFFField.TIFF_RATIONAL, 6,
                                     refbw));
        }

        // ---- No more automatically generated fields should be added
        //      after this point. ----

        // Add extra fields specified via the encoding parameters.
        TIFFField[] extraFields = encodeParam.getExtraFields();
        List extantTags = new ArrayList(fields.size());
        for (TIFFField fld : fields) {
            extantTags.add(fld.getTag());
        }

        for (TIFFField fld : extraFields) {
            Integer tagValue = fld.getTag();
            if (!extantTags.contains(tagValue)) {
                fields.add(fld);
                extantTags.add(tagValue);
            }
        }

        // ---- No more fields of any type should be added after this. ----

        // Determine the size of the IFD which is written after the header
        // of the stream or after the data of the previous image in a
        // multi-page stream.
        int dirSize = getDirectorySize(fields);

        // The first data segment is written after the field overflow
        // following the IFD so initialize the first offset accordingly.
        tileOffsets[0] = ifdOffset + dirSize;

        // Branch here depending on whether data are being compressed.
        // If not, then the IFD is written immediately.
        // If so then there are three possibilities:
        // A) the OutputStream is a SeekableOutputStream (outCache null);
        // B) the OutputStream is not a SeekableOutputStream and a file cache
        //    is used (outCache non-null, tempFile non-null);
        // C) the OutputStream is not a SeekableOutputStream and a memory cache
        //    is used (outCache non-null, tempFile null).

        OutputStream outCache = null;
        byte[] compressBuf = null;
        File tempFile = null;

        int nextIFDOffset = 0;
        boolean skipByte = false;

        Deflater deflater = null;
        boolean jpegRGBToYCbCr = false;

        if (compression == CompressionValue.NONE) {
            // Determine the number of bytes of padding necessary between
            // the end of the IFD and the first data segment such that the
            // alignment of the data conforms to the specification (required
            // for uncompressed data only).
            int numBytesPadding = 0;
            if (dataTypeSize == 16 && tileOffsets[0] % 2 != 0) {
                numBytesPadding = 1;
                tileOffsets[0]++;
            } else if (dataTypeSize == 32 && tileOffsets[0] % 4 != 0) {
                numBytesPadding = (int)(4 - tileOffsets[0] % 4);
                tileOffsets[0] += numBytesPadding;
            }

            // Update the data offsets (which TIFFField stores by reference).
            for (int i = 1; i < numTiles; i++) {
                tileOffsets[i] = tileOffsets[i - 1] + tileByteCounts[i - 1];
            }

            if (!isLast) {
                // Determine the offset of the next IFD.
                nextIFDOffset = (int)(tileOffsets[0] + totalBytesOfData);

                // IFD offsets must be on a word boundary.
                if ((nextIFDOffset & 0x01) != 0) {
                    nextIFDOffset++;
                    skipByte = true;
                }
            }

            // Write the IFD and field overflow before the image data.
            writeDirectory(ifdOffset, fields, nextIFDOffset);

            // Write any padding bytes needed between the end of the IFD
            // and the start of the actual image data.
            if (numBytesPadding != 0) {
                for (int padding = 0; padding < numBytesPadding; padding++) {
                    output.write((byte)0);
                }
            }
        } else {
            // If compressing, the cannot be written yet as the size of the
            // data segments is unknown.

            if (output instanceof SeekableOutputStream) {
                // Simply seek to the first data segment position.
                ((SeekableOutputStream)output).seek(tileOffsets[0]);
            } else {
                // Cache the original OutputStream.
                outCache = output;

                try {
                    // Attempt to create a temporary file.
                    tempFile = File.createTempFile("jai-SOS-", ".tmp");
                    tempFile.deleteOnExit();
                    RandomAccessFile raFile = new RandomAccessFile(tempFile, "rw");
                    output = new SeekableOutputStream(raFile);

                    // this method is exited!
                } catch (IOException e) {
                    // Allocate memory for the entire image data (!).
                    output = new ByteArrayOutputStream((int)totalBytesOfData);
                }
            }

            int bufSize = 0;
            switch(compression) {
            case PACKBITS:
                bufSize = (int) (bytesPerTile + ((bytesPerRow + 127) / 128) * tileHeight);
                break;
            case DEFLATE:
                bufSize = (int) bytesPerTile;
                deflater = new Deflater(encodeParam.getDeflateLevel());
                break;
            default:
                bufSize = 0;
            }
            if (bufSize != 0) {
                compressBuf = new byte[bufSize];
            }
        }

        // ---- Writing of actual image data ----

        // Buffer for up to tileHeight rows of pixels
        int[] pixels = null;
        float[] fpixels = null;

        // Whether to test for contiguous data.
        boolean checkContiguous =
            ((dataTypeSize == 1
              && sampleModel instanceof MultiPixelPackedSampleModel
              && dataType == DataBuffer.TYPE_BYTE)
             || (dataTypeSize == 8
              && sampleModel instanceof ComponentSampleModel));

        // Also create a buffer to hold tileHeight lines of the
        // data to be written to the file, so we can use array writes.
        byte[] bpixels = null;
        if (compression != CompressionValue.JPEG_TTN2) {
            if (dataType == DataBuffer.TYPE_BYTE) {
                bpixels = new byte[tileHeight * tileWidth * numBands];
            } else if (dataTypeIsShort) {
                bpixels = new byte[2 * tileHeight * tileWidth * numBands];
            } else if (dataType == DataBuffer.TYPE_INT
                      || dataType == DataBuffer.TYPE_FLOAT) {
                bpixels = new byte[4 * tileHeight * tileWidth * numBands];
            }
        }

        // Process tileHeight rows at a time
        int lastRow = minY + height;
        int lastCol = minX + width;
        int tileNum = 0;
        for (int row = minY; row < lastRow; row += tileHeight) {
            int rows = isTiled
                ? tileHeight : Math.min(tileHeight, lastRow - row);
            int size = rows * tileWidth * numBands;

            for (int col = minX; col < lastCol; col += tileWidth) {
                // Grab the pixels
                Raster src =
                    im.getData(new Rectangle(col, row, tileWidth, rows));

                boolean useDataBuffer = false;
                if (compression != CompressionValue.JPEG_TTN2) { // JPEG access Raster
                    if (checkContiguous) {
                        if (dataTypeSize == 8) { // 8-bit
                            ComponentSampleModel csm =
                                (ComponentSampleModel)src.getSampleModel();
                            int[] bankIndices = csm.getBankIndices();
                            int[] bandOffsets = csm.getBandOffsets();
                            int pixelStride = csm.getPixelStride();
                            int lineStride = csm.getScanlineStride();

                            if (pixelStride != numBands
                               || lineStride != bytesPerRow) {
                                useDataBuffer = false;
                            } else {
                                useDataBuffer = true;
                                for (int i = 0;
                                    useDataBuffer && i < numBands;
                                    i++) {
                                    if (bankIndices[i] != 0
                                       || bandOffsets[i] != i) {
                                        useDataBuffer = false;
                                    }
                                }
                            }
                        } else { // 1-bit
                            MultiPixelPackedSampleModel mpp =
                                (MultiPixelPackedSampleModel)src.getSampleModel();
                            if (mpp.getNumBands() == 1
                               && mpp.getDataBitOffset() == 0
                               && mpp.getPixelBitStride() == 1) {
                                useDataBuffer = true;
                            }
                        }
                    }

                    if (!useDataBuffer) {
                        if (dataType == DataBuffer.TYPE_FLOAT) {
                            fpixels = src.getPixels(col, row, tileWidth, rows,
                                                    fpixels);
                        } else {
                            pixels = src.getPixels(col, row, tileWidth, rows,
                                                   pixels);
                        }
                    }
                }

                int index;

                int pixel = 0;
                int k = 0;
                switch (dataTypeSize) {

                case 1:

                    if (useDataBuffer) {
                        byte[] btmp =
                            ((DataBufferByte)src.getDataBuffer()).getData();
                        MultiPixelPackedSampleModel mpp =
                            (MultiPixelPackedSampleModel)src.getSampleModel();
                        int lineStride = mpp.getScanlineStride();
                        int inOffset =
                            mpp.getOffset(col
                                          - src.getSampleModelTranslateX(),
                                          row
                                          - src.getSampleModelTranslateY());
                        if (lineStride == bytesPerRow) {
                            System.arraycopy(btmp, inOffset,
                                             bpixels, 0,
                                             (int) bytesPerRow * rows);
                        } else {
                            int outOffset = 0;
                            for (int j = 0; j < rows; j++) {
                                System.arraycopy(btmp, inOffset,
                                                 bpixels, outOffset,
                                                 (int) bytesPerRow);
                                inOffset += lineStride;
                                outOffset += bytesPerRow;
                            }
                        }
                    } else {
                        index = 0;

                        // For each of the rows in a strip
                        for (int i = 0; i < rows; i++) {

                            // Write number of pixels exactly divisible by 8
                            for (int j = 0; j < tileWidth / 8; j++) {

                                pixel =
                                    (pixels[index++] << 7)
                                    | (pixels[index++] << 6)
                                    | (pixels[index++] << 5)
                                    | (pixels[index++] << 4)
                                    | (pixels[index++] << 3)
                                    | (pixels[index++] << 2)
                                    | (pixels[index++] << 1)
                                    | pixels[index++];
                                bpixels[k++] = (byte)pixel;
                            }

                            // Write the pixels remaining after division by 8
                            if (tileWidth % 8 > 0) {
                                pixel = 0;
                                for (int j = 0; j < tileWidth % 8; j++) {
                                    pixel |= (pixels[index++] << (7 - j));
                                }
                                bpixels[k++] = (byte)pixel;
                            }
                        }
                    }

                    if (compression == CompressionValue.NONE) {
                        output.write(bpixels, 0, rows * ((tileWidth + 7) / 8));
                    } else if (compression == CompressionValue.PACKBITS) {
                        int numCompressedBytes =
                            compressPackBits(bpixels, rows,
                                             bytesPerRow,
                                             compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    } else if (compression == CompressionValue.DEFLATE) {
                        int numCompressedBytes =
                            deflate(deflater, bpixels, compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    }

                    break;

                case 4:

                    index = 0;

                    // For each of the rows in a strip
                    for (int i = 0; i < rows; i++) {

                        // Write  the number of pixels that will fit into an
                        // even number of nibbles.
                        for (int j = 0; j < tileWidth / 2; j++) {
                            pixel = (pixels[index++] << 4) | pixels[index++];
                            bpixels[k++] = (byte)pixel;
                        }

                        // Last pixel for odd-length lines
                        if ((tileWidth & 1) == 1) {
                            pixel = pixels[index++] << 4;
                            bpixels[k++] = (byte)pixel;
                        }
                    }

                    if (compression == CompressionValue.NONE) {
                        output.write(bpixels, 0, rows * ((tileWidth + 1) / 2));
                    } else if (compression == CompressionValue.PACKBITS) {
                        int numCompressedBytes =
                            compressPackBits(bpixels, rows,
                                             bytesPerRow,
                                             compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    } else if (compression == CompressionValue.DEFLATE) {
                        int numCompressedBytes =
                            deflate(deflater, bpixels, compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    }
                    break;

                case 8:

                    if (compression != CompressionValue.JPEG_TTN2) {
                        if (useDataBuffer) {
                            byte[] btmp =
                                ((DataBufferByte)src.getDataBuffer()).getData();
                            ComponentSampleModel csm =
                                (ComponentSampleModel)src.getSampleModel();
                            int inOffset =
                                csm.getOffset(col
                                              - src.getSampleModelTranslateX(),
                                              row
                                              - src.getSampleModelTranslateY());
                            int lineStride = csm.getScanlineStride();
                            if (lineStride == bytesPerRow) {
                                System.arraycopy(btmp,
                                                 inOffset,
                                                 bpixels, 0,
                                                 (int) bytesPerRow * rows);
                            } else {
                                int outOffset = 0;
                                for (int j = 0; j < rows; j++) {
                                    System.arraycopy(btmp, inOffset,
                                                     bpixels, outOffset,
                                                     (int) bytesPerRow);
                                    inOffset += lineStride;
                                    outOffset += bytesPerRow;
                                }
                            }
                        } else {
                            for (int i = 0; i < size; i++) {
                                bpixels[i] = (byte)pixels[i];
                            }
                        }
                    }

                    if (compression == CompressionValue.NONE) {
                        output.write(bpixels, 0, size);
                    } else if (compression == CompressionValue.PACKBITS) {
                        int numCompressedBytes =
                            compressPackBits(bpixels, rows,
                                             bytesPerRow,
                                             compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    } else if (compression == CompressionValue.DEFLATE) {
                        int numCompressedBytes =
                            deflate(deflater, bpixels, compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    }
                    break;

                case 16:

                    int ls = 0;
                    for (int i = 0; i < size; i++) {
                        int value = pixels[i];
                        bpixels[ls++] = (byte)((value & 0xff00) >> 8);
                        bpixels[ls++] = (byte) (value & 0x00ff);
                    }

                    if (compression == CompressionValue.NONE) {
                        output.write(bpixels, 0, size * 2);
                    } else if (compression == CompressionValue.PACKBITS) {
                        int numCompressedBytes =
                            compressPackBits(bpixels, rows,
                                             bytesPerRow,
                                             compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    } else if (compression == CompressionValue.DEFLATE) {
                        int numCompressedBytes =
                            deflate(deflater, bpixels, compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    }
                    break;

                case 32:
                    if (dataType == DataBuffer.TYPE_INT) {
                        int li = 0;
                        for (int i = 0; i < size; i++) {
                            int value = pixels[i];
                            bpixels[li++] = (byte)((value & 0xff000000) >>> 24);
                            bpixels[li++] = (byte)((value & 0x00ff0000) >>> 16);
                            bpixels[li++] = (byte)((value & 0x0000ff00) >>> 8);
                            bpixels[li++] = (byte)(value & 0x000000ff);
                        }
                    } else { // DataBuffer.TYPE_FLOAT
                        int lf = 0;
                        for (int i = 0; i < size; i++) {
                            int value = Float.floatToIntBits(fpixels[i]);
                            bpixels[lf++] = (byte)((value & 0xff000000) >>> 24);
                            bpixels[lf++] = (byte)((value & 0x00ff0000) >>> 16);
                            bpixels[lf++] = (byte)((value & 0x0000ff00) >>> 8);
                            bpixels[lf++] = (byte)(value & 0x000000ff);
                        }
                    }
                    if (compression == CompressionValue.NONE) {
                        output.write(bpixels, 0, size * 4);
                    } else if (compression == CompressionValue.PACKBITS) {
                        int numCompressedBytes =
                            compressPackBits(bpixels, rows,
                                             bytesPerRow,
                                             compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    } else if (compression == CompressionValue.DEFLATE) {
                        int numCompressedBytes =
                            deflate(deflater, bpixels, compressBuf);
                        tileByteCounts[tileNum++] = numCompressedBytes;
                        output.write(compressBuf, 0, numCompressedBytes);
                    }
                    break;
                default:
                    break;
                }
            }
        }

        if (compression == CompressionValue.NONE) {
            // Write an extra byte for IFD word alignment if needed.
            if (skipByte) {
                output.write((byte)0);
            }
        } else {
            // Recompute the tile offsets the size of the compressed tiles.
            int totalBytes = 0;
            for (int i = 1; i < numTiles; i++) {
                int numBytes = (int)tileByteCounts[i - 1];
                totalBytes += numBytes;
                tileOffsets[i] = tileOffsets[i - 1] + numBytes;
            }
            totalBytes += (int)tileByteCounts[numTiles - 1];

            nextIFDOffset = isLast
                ? 0 : ifdOffset + dirSize + totalBytes;
            if ((nextIFDOffset & 0x01) != 0) {   // make it even
                nextIFDOffset++;
                skipByte = true;
            }

            if (outCache == null) {
                // Original OutputStream must be a SeekableOutputStream.

                // Write an extra byte for IFD word alignment if needed.
                if (skipByte) {
                    output.write((byte)0);
                }

                SeekableOutputStream sos = (SeekableOutputStream)output;

                // Save current position.
                long savePos = sos.getFilePointer();

                // Seek backward to the IFD offset and write IFD.
                sos.seek(ifdOffset);
                writeDirectory(ifdOffset, fields, nextIFDOffset);

                // Seek forward to position after data.
                sos.seek(savePos);
            } else if (tempFile != null) {

                // Using a file cache for the image data.

                // Open a FileInputStream from which to copy the data.
                FileInputStream fileStream = new FileInputStream(tempFile);
                try {
                    // Close the original SeekableOutputStream.
                    output.close();

                    // Reset variable to the original OutputStream.
                    output = outCache;

                    // Write the IFD.
                    writeDirectory(ifdOffset, fields, nextIFDOffset);

                    // Write the image data.
                    byte[] copyBuffer = new byte[8192];
                    int bytesCopied = 0;
                    while (bytesCopied < totalBytes) {
                        int bytesRead = fileStream.read(copyBuffer);
                        if (bytesRead == -1) {
                            break;
                        }
                        output.write(copyBuffer, 0, bytesRead);
                        bytesCopied += bytesRead;
                    }
                } finally {
                    // Delete the temporary file.
                    fileStream.close();
                }
                boolean isDeleted = tempFile.delete();
                assert isDeleted;

                // Write an extra byte for IFD word alignment if needed.
                if (skipByte) {
                    output.write((byte)0);
                }
            } else if (output instanceof ByteArrayOutputStream) {

                // Using a memory cache for the image data.

                ByteArrayOutputStream memoryStream = (ByteArrayOutputStream)output;

                // Reset variable to the original OutputStream.
                output = outCache;

                // Write the IFD.
                writeDirectory(ifdOffset, fields, nextIFDOffset);

                // Write the image data.
                memoryStream.writeTo(output);

                // Write an extra byte for IFD word alignment if needed.
                if (skipByte) {
                    output.write((byte)0);
                }
            } else {
                // This should never happen.
                throw new IllegalStateException(PropertyUtil.getString("TIFFImageEncoder13"));
            }
        }


        return nextIFDOffset;
    }

    private void validateImage(int dataTypeSize, int[] sampleSize, int numBands, int dataType,
            ColorModel colorModel) {
        // Retrieve and verify sample size.
        for (int i = 1; i < sampleSize.length; i++) {
            if (sampleSize[i] != dataTypeSize) {
                throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder0"));
            }
        }

        // Check low bit limits.
        if ((dataTypeSize == 1 || dataTypeSize == 4) && numBands != 1) {
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder1"));
        }

        // Retrieve and verify data type.
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            if (dataTypeSize == 4) {
                throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder2"));
            }
            break;
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
            if (dataTypeSize != 16) {
                throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder3"));
            }
            break;
        case DataBuffer.TYPE_INT:
        case DataBuffer.TYPE_FLOAT:
            if (dataTypeSize != 32) {
                throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder4"));
            }
            break;
        default:
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder5"));
        }

        if (colorModel instanceof IndexColorModel && dataType != DataBuffer.TYPE_BYTE) {
            // Don't support (unsigned) short palette-color images.
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder6"));
        }
    }

    /**
     * Calculates the size of the IFD.
     */
    private int getDirectorySize(SortedSet fields) {
        // Get the number of entries.
        int numEntries = fields.size();

        // Initialize the size excluding that of any values > 4 bytes.
        int dirSize = 2 + numEntries * 12 + 4;

        // Loop over fields adding the size of all values > 4 bytes.
        for (Object field1 : fields) {
            // Get the field.
            TIFFField field = (TIFFField) field1;

            // Determine the size of the field value.
            int valueSize = field.getCount() * SIZE_OF_TYPE[field.getType()];

            // Add any excess size.
            if (valueSize > 4) {
                dirSize += valueSize;
            }
        }

        return dirSize;
    }

    private void writeFileHeader() throws IOException {
        // 8 byte image file header

        // Byte order used within the file - Big Endian
        output.write('M');
        output.write('M');

        // Magic value
        output.write(0);
        output.write(42);

        // Offset in bytes of the first IFD.
        writeLong(8);
    }

    private void writeDirectory(int thisIFDOffset, SortedSet fields,
                                int nextIFDOffset)
        throws IOException {

        // 2 byte count of number of directory entries (fields)
        int numEntries = fields.size();

        long offsetBeyondIFD = thisIFDOffset + 12 * numEntries + 4 + 2;
        List tooBig = new ArrayList();

        // Write number of fields in the IFD
        writeUnsignedShort(numEntries);

        for (Object field1 : fields) {

            // 12 byte field entry TIFFField
            TIFFField field = (TIFFField) field1;

            // byte 0-1 Tag that identifies a field
            int tag = field.getTag();
            writeUnsignedShort(tag);

            // byte 2-3 The field type
            int type = field.getType();
            writeUnsignedShort(type);

            // bytes 4-7 the number of values of the indicated type except
            // ASCII-valued fields which require the total number of bytes.
            int count = field.getCount();
            int valueSize = getValueSize(field);
            writeLong(type == TIFFField.TIFF_ASCII ? valueSize : count);

            // bytes 8 - 11 the value or value offset
            if (valueSize > 4) {

                // We need an offset as data won't fit into 4 bytes
                writeLong(offsetBeyondIFD);
                offsetBeyondIFD += valueSize;
                tooBig.add(field);

            } else {
                writeValuesAsFourBytes(field);
            }

        }

        // Address of next IFD
        writeLong(nextIFDOffset);

        // Write the tag values that did not fit into 4 bytes
        for (Object aTooBig : tooBig) {
            writeValues((TIFFField) aTooBig);
        }
    }

    /**
     * Determine the number of bytes in the value portion of the field.
     */
    private static int getValueSize(TIFFField field) throws UnsupportedEncodingException {
        int type = field.getType();
        int count = field.getCount();
        int valueSize = 0;
        if (type == TIFFField.TIFF_ASCII) {
            for (int i = 0; i < count; i++) {
                byte[] stringBytes = field.getAsString(i).getBytes("UTF-8");   // note: default encoding @work here!
                valueSize += stringBytes.length;
                if (stringBytes[stringBytes.length - 1] != 0) {
                    valueSize++;
                }
            }
        } else {
            valueSize = count * SIZE_OF_TYPE[type];
        }
        return valueSize;
    }

    private static final int[] SIZE_OF_TYPE = {
        0, //  0 = n/a
        1, //  1 = byte
        1, //  2 = ascii
        2, //  3 = short
        4, //  4 = long
        8, //  5 = rational
        1, //  6 = sbyte
        1, //  7 = undefined
        2, //  8 = sshort
        4, //  9 = slong
        8, // 10 = srational
        4, // 11 = float
        8  // 12 = double
    };

    private void writeValuesAsFourBytes(TIFFField field) throws IOException {

        int dataType = field.getType();
        int count = field.getCount();

        switch (dataType) {

            // unsigned 8 bits
        case TIFFField.TIFF_BYTE:
            byte[] bytes = field.getAsBytes();
            if (count > 4) {
                count = 4;
            }
            for (int i = 0; i < count; i++) {
                output.write(bytes[i]);
            }

            for (int i = 0; i < (4 - count); i++) {
                output.write(0);
            }
            break;

            // unsigned 16 bits
        case TIFFField.TIFF_SHORT:
            char[] chars = field.getAsChars();
            if (count > 2) {
                count = 2;
            }
            for (int i = 0; i < count; i++) {
                writeUnsignedShort(chars[i]);
            }
            for (int i = 0; i < (2 - count); i++) {
                writeUnsignedShort(0);
            }

            break;

            // unsigned 32 bits
        case TIFFField.TIFF_LONG:
            long[] longs = field.getAsLongs();

            for (int i = 0; i < count; i++) {
                writeLong(longs[i]);
            }
            break;
        }

    }

    private void writeValues(TIFFField field) throws IOException {

        int dataType = field.getType();
        int count = field.getCount();

        switch (dataType) {

            // unsigned 8 bits
        case TIFFField.TIFF_BYTE:
        case TIFFField.TIFF_SBYTE:
        case TIFFField.TIFF_UNDEFINED:
            byte[] bytes = field.getAsBytes();
            for (int i = 0; i < count; i++) {
                output.write(bytes[i]);
            }
            break;

            // unsigned 16 bits
        case TIFFField.TIFF_SHORT:
            char[] chars = field.getAsChars();
            for (int i = 0; i < count; i++) {
                writeUnsignedShort(chars[i]);
            }
            break;
        case TIFFField.TIFF_SSHORT:
            short[] shorts = field.getAsShorts();
            for (int i = 0; i < count; i++) {
                writeUnsignedShort(shorts[i]);
            }
            break;

            // unsigned 32 bits
        case TIFFField.TIFF_LONG:
        case TIFFField.TIFF_SLONG:
            long[] longs = field.getAsLongs();
            for (int i = 0; i < count; i++) {
                writeLong(longs[i]);
            }
            break;

        case TIFFField.TIFF_FLOAT:
            float[] floats = field.getAsFloats();
            for (int i = 0; i < count; i++) {
                int intBits = Float.floatToIntBits(floats[i]);
                writeLong(intBits);
            }
            break;

        case TIFFField.TIFF_DOUBLE:
            double[] doubles = field.getAsDoubles();
            for (int i = 0; i < count; i++) {
                long longBits = Double.doubleToLongBits(doubles[i]);
                writeLong(longBits >>> 32);           // write upper 32 bits
                writeLong(longBits & 0xffffffffL);    // write lower 32 bits
            }
            break;

        case TIFFField.TIFF_RATIONAL:
        case TIFFField.TIFF_SRATIONAL:
            long[][] rationals = field.getAsRationals();
            for (int i = 0; i < count; i++) {
                writeLong(rationals[i][0]);
                writeLong(rationals[i][1]);
            }
            break;

        case TIFFField.TIFF_ASCII:
            for (int i = 0; i < count; i++) {
                byte[] stringBytes = field.getAsString(i).getBytes("UTF-8");
                output.write(stringBytes);
                if (stringBytes[stringBytes.length - 1] != (byte)0) {
                    output.write((byte)0);
                }
            }
            break;

        default:
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder10"));

        }

    }

    // Here s is never expected to have value greater than what can be
    // stored in 2 bytes.
    private void writeUnsignedShort(int s) throws IOException {
        output.write((s & 0xff00) >>> 8);
        output.write(s & 0x00ff);
    }

    /**
     * despite its name, this method writes only 4 bytes to output.
     * @param l 32bits of this are written as 4 bytes
     * @throws IOException
     */
    private void writeLong(long l) throws IOException {
        output.write((int)((l & 0xff000000) >>> 24));
        output.write((int)((l & 0x00ff0000) >>> 16));
        output.write((int)((l & 0x0000ff00) >>> 8));
        output.write((int) (l & 0x000000ff));
    }

//    /**
//     * Returns the current offset in the supplied OutputStream.
//     * This method should only be used if compressing data.
//     */
//    private long getOffset(OutputStream out) throws IOException {
//        if (out instanceof ByteArrayOutputStream) {
//            return ((ByteArrayOutputStream)out).size();
//        } else if (out instanceof SeekableOutputStream) {
//            return ((SeekableOutputStream)out).getFilePointer();
//        } else {
//            // Shouldn't happen.
//            throw new IllegalStateException(PropertyUtil.getString("TIFFImageEncoder13"));
//        }
//    }

    /**
     * Performs PackBits compression on a tile of data.
     */
    private static int compressPackBits(byte[] data, int numRows,
                                        long bytesPerRow, byte[] compData) {
        int inOffset = 0;
        int outOffset = 0;

        for (int i = 0; i < numRows; i++) {
            outOffset = packBits(data, inOffset, (int) bytesPerRow,
                                 compData, outOffset);
            inOffset += bytesPerRow;
        }

        return outOffset;
    }

    /**
     * Performs PackBits compression for a single buffer of data.
     * This should be called for each row of each tile. The returned
     * value is the offset into the output buffer after compression.
     */
    private static int packBits(byte[] input, int inOffset, int inCount,
                                byte[] output, int outOffset) {
        int inMax = inOffset + inCount - 1;
        int inMaxMinus1 = inMax - 1;

        while (inOffset <= inMax) {
            int run = 1;
            byte replicate = input[inOffset];
            while (run < 127 && inOffset < inMax
                  && input[inOffset] == input[inOffset + 1]) {
                run++;
                inOffset++;
            }
            if (run > 1) {
                inOffset++;
                output[outOffset++] = (byte)(-(run - 1));
                output[outOffset++] = replicate;
            }

            run = 0;
            int saveOffset = outOffset;
            while (run < 128
                  && ((inOffset < inMax
                    && input[inOffset] != input[inOffset + 1])
                   || (inOffset < inMaxMinus1
                    && input[inOffset] != input[inOffset + 2]))) {
                run++;
                output[++outOffset] = input[inOffset++];
            }
            if (run > 0) {
                output[saveOffset] = (byte)(run - 1);
                outOffset++;
            }

            if (inOffset == inMax) {
                if (run > 0 && run < 128) {
                    output[saveOffset]++;
                    output[outOffset++] = input[inOffset++];
                } else {
                    output[outOffset++] = (byte)0;
                    output[outOffset++] = input[inOffset++];
                }
            }
        }

        return outOffset;
    }

    private static int deflate(Deflater deflater,
                               byte[] inflated, byte[] deflated) {
        deflater.setInput(inflated);
        deflater.finish();
        int numCompressedBytes = deflater.deflate(deflated);
        deflater.reset();
        return numCompressedBytes;
    }

}
