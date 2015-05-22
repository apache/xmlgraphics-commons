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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;
import org.apache.xmlgraphics.image.loader.impl.ImageLoaderRawCCITTFax;
import org.apache.xmlgraphics.image.loader.mocks.MockImageLoaderFactoryTIFF;
import org.apache.xmlgraphics.image.loader.pipeline.ImageProviderPipeline;
import org.apache.xmlgraphics.image.loader.pipeline.PipelineFactory;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.util.Penalty;

/**
 * Tests the pipeline factory.
 */
public class PipelineFactoryTestCase {

    /**
     * Tests the pipeline factory by checking to load a TIFF image.
     * @throws Exception if an error occurs
     */
    @Test
    public void testPipelineFactoryPlain() throws Exception {
        MockImageContext imageContext = MockImageContext.newSafeInstance();
        ImageManager manager = imageContext.getImageManager();
        PipelineFactory pFactory = new PipelineFactory(manager);

        //Input is some TIFF file
        ImageInfo imageInfo = new ImageInfo("test:tiff", "image/tiff");

        //We want a G2D image
        ImageFlavor targetFlavor = ImageFlavor.GRAPHICS2D;

        ImageProviderPipeline pipeline = pFactory.newImageConverterPipeline(
                imageInfo, targetFlavor);
        assertNotNull(pipeline);
        assertEquals(pipeline.getTargetFlavor(), targetFlavor);

        //penalty for internal TIFF implementation (fallback role) is 1000 + 10 for the conversion
        assertEquals(1010, pipeline.getConversionPenalty());
        assertEquals(ImageFlavor.GRAPHICS2D, pipeline.getTargetFlavor());
        if (pipeline.toString().indexOf("LoaderInternalTIFF") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
        if (pipeline.toString().indexOf("ImageConverterBitmap2G2D") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }

        ImageProviderPipeline[] candidates = pFactory.determineCandidatePipelines(
                imageInfo, new ImageFlavor[] {targetFlavor});
        assertEquals(1, candidates.length);

        //Now add another implementation that poses as TIFF loader
        imageContext.getImageManager().getRegistry().registerLoaderFactory(
                new MockImageLoaderFactoryTIFF());

        candidates = pFactory.determineCandidatePipelines(
                imageInfo, targetFlavor);
        assertEquals(3, candidates.length);
        //3 because the mock impl provides Buffered- and RenderedImage capabilities

        pipeline = pFactory.newImageConverterPipeline(imageInfo, targetFlavor);
        assertNotNull(pipeline);
        assertEquals(pipeline.getTargetFlavor(), targetFlavor);

        //Assuming mock impl without penalty + 10 for the conversion
        assertEquals(10, pipeline.getConversionPenalty());
        assertEquals(ImageFlavor.GRAPHICS2D, pipeline.getTargetFlavor());
        if (pipeline.toString().indexOf(MockImageLoaderFactoryTIFF.class.getName()) < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
        if (pipeline.toString().indexOf("ImageConverterBitmap2G2D") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
    }

    /**
     * Similar test as above but here we take raw CCITT loading into consideration, too.
     * @throws Exception if an error occurs
     */
    @Test
    public void testPipelineFactoryImageInfoDependency() throws Exception {
        MockImageContext imageContext = MockImageContext.newSafeInstance();
        ImageManager manager = imageContext.getImageManager();
        PipelineFactory pFactory = new PipelineFactory(manager);

        //Input is some TIFF file with CCITT Group 4 compression
        ImageInfo imageInfo = new ImageInfo("test:tiff", "image/tiff");
        imageInfo.getCustomObjects().put("TIFF_STRIP_COUNT", 1);
        imageInfo.getCustomObjects().put("TIFF_COMPRESSION", TIFFImage.COMP_FAX_G4_2D);

        //We want either a G2D image or a raw CCITT image
        ImageFlavor[] targetFlavors = new ImageFlavor[] {
                ImageFlavor.GRAPHICS2D, ImageFlavor.RAW_CCITTFAX};

        ImageProviderPipeline[] candidates = pFactory.determineCandidatePipelines(
                imageInfo, targetFlavors);
        assertNotNull(candidates);
        assertEquals(2, candidates.length);

        ImageProviderPipeline pipeline = manager.choosePipeline(candidates);

        //0 penalty because the raw loader is the most efficient choice here
        assertEquals(0, pipeline.getConversionPenalty());
        assertEquals(ImageFlavor.RAW_CCITTFAX, pipeline.getTargetFlavor());
        if (pipeline.toString().indexOf("LoaderRawCCITTFax") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }

        //Now, we set this to a multi-strip TIFF which should disable the raw loader
        imageInfo.getCustomObjects().put("TIFF_STRIP_COUNT", 7);

        candidates = pFactory.determineCandidatePipelines(
                imageInfo, targetFlavors);
        assertNotNull(candidates);
        assertEquals(1, candidates.length);

        pipeline = manager.choosePipeline(candidates);

        //penalty for internal TIFF implementation (fallback role) is 1000 + 10 for the conversion
        assertEquals(1010, pipeline.getConversionPenalty());
        assertEquals(ImageFlavor.GRAPHICS2D, pipeline.getTargetFlavor());
        if (pipeline.toString().indexOf("LoaderInternalTIFF") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
        if (pipeline.toString().indexOf("ImageConverterBitmap2G2D") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
    }

    /**
     * Similar test as above but now we're playing with additional penalties in the registry.
     * @throws Exception if an error occurs
     */
    @Test
    public void testPipelineFactoryAdditionalPenalty() throws Exception {
        MockImageContext imageContext = MockImageContext.newSafeInstance();
        ImageManager manager = imageContext.getImageManager();
        PipelineFactory pFactory = new PipelineFactory(manager);

        //Adding additional penalty for CCITT loading
        ImageImplRegistry registry = imageContext.getImageManager().getRegistry();
        registry.setAdditionalPenalty(ImageLoaderRawCCITTFax.class.getName(),
                Penalty.toPenalty(10000));

        //Input is some TIFF file with CCITT Group 4 compression
        ImageInfo imageInfo = new ImageInfo("test:tiff", "image/tiff");
        imageInfo.getCustomObjects().put("TIFF_STRIP_COUNT", 1);
        imageInfo.getCustomObjects().put("TIFF_COMPRESSION", TIFFImage.COMP_FAX_G4_2D);

        //We want either a G2D image or a raw CCITT image
        ImageFlavor[] targetFlavors = new ImageFlavor[] {
                ImageFlavor.GRAPHICS2D, ImageFlavor.RAW_CCITTFAX};

        ImageProviderPipeline[] candidates = pFactory.determineCandidatePipelines(
                imageInfo, targetFlavors);
        assertNotNull(candidates);
        assertEquals(2, candidates.length);

        ImageProviderPipeline pipeline = manager.choosePipeline(candidates);

        //penalty for internal TIFF implementation (fallback role) is 1000 + 10 for the conversion
        assertEquals(1010, pipeline.getConversionPenalty());
        assertEquals(ImageFlavor.GRAPHICS2D, pipeline.getTargetFlavor());
        if (pipeline.toString().indexOf("LoaderInternalTIFF") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }
        if (pipeline.toString().indexOf("ImageConverterBitmap2G2D") < 0) {
            fail("Chose the wrong pipeline: " + pipeline.toString());
        }

        //Now set an infinite penalty making the solution ineligible
        registry.setAdditionalPenalty(ImageLoaderRawCCITTFax.class.getName(),
                Penalty.INFINITE_PENALTY);

        candidates = pFactory.determineCandidatePipelines(imageInfo, targetFlavors);
        assertNotNull(candidates);
        assertEquals(1, candidates.length);
        //While earlier 2 candidates were returned, here we only get 1 because of the infinite
        //penalty.
    }

}
