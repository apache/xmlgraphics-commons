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

import org.apache.xmlgraphics.image.loader.impl.ImageConverterBitmap2G2D;
import org.apache.xmlgraphics.image.loader.impl.ImageConverterBuffered2Rendered;
import org.apache.xmlgraphics.image.loader.impl.ImageConverterG2D2Bitmap;
import org.apache.xmlgraphics.image.loader.impl.ImageLoaderFactoryInternalTIFF;
import org.apache.xmlgraphics.image.loader.impl.ImageLoaderFactoryRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.PreloaderEPS;
import org.apache.xmlgraphics.image.loader.impl.PreloaderJPEG;
import org.apache.xmlgraphics.image.loader.impl.PreloaderTIFF;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;

/**
 * Mock implementation for testing.
 */
public class MockImageContext implements ImageContext {

    private static MockImageContext instance;

    private ImageManager imageManager;

    /**
     * Returns a singleton instance of the mock image context.
     * @return the singleton
     */
    public static MockImageContext getInstance() {
        if (instance == null) {
            instance = new MockImageContext(true);
        }
        return instance;
    }

    /**
     * Returns an image context for testing that only contains platform- and classpath-independent
     * implementations so consistent test results can be obtained irrespective of the test
     * environment.
     * @return a new image context
     */
    public static MockImageContext newSafeInstance() {
        MockImageContext ic = new MockImageContext(false);
        ImageImplRegistry registry = ic.getImageManager().getRegistry();
        registry.registerPreloader(new PreloaderTIFF());
        registry.registerPreloader(new PreloaderJPEG());
        registry.registerPreloader(new PreloaderEPS());
        registry.registerLoaderFactory(new ImageLoaderFactoryInternalTIFF());
        registry.registerLoaderFactory(new ImageLoaderFactoryRawCCITTFax());
        registry.registerConverter(new ImageConverterBitmap2G2D());
        registry.registerConverter(new ImageConverterG2D2Bitmap());
        registry.registerConverter(new ImageConverterBuffered2Rendered());
        return ic;
    }

    /**
     * Creates a new mock image context.
     * @param discover true to enable plug-in discovery
     */
    public MockImageContext(boolean discover) {
        this.imageManager = new ImageManager(new ImageImplRegistry(discover), this);
    }

    /** {@inheritDoc} */
    public float getSourceResolution() {
        return 72;
    }

    /**
     * Returns the image manager.
     * @return the image manager
     */
    public ImageManager getImageManager() {
        return this.imageManager;
    }

    /**
     * Creates a new image session context.
     * @return the image session context
     */
    public ImageSessionContext newSessionContext() {
        return new MockImageSessionContext(this);
    }
}
