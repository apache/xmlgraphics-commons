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

package org.apache.xmlgraphics.image.loader.pipeline;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.CompositeImageLoader;
import org.apache.xmlgraphics.image.loader.spi.ImageConverter;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.util.dijkstra.DefaultEdgeDirectory;
import org.apache.xmlgraphics.util.dijkstra.DijkstraAlgorithm;
import org.apache.xmlgraphics.util.dijkstra.Vertex;

/**
 * Factory class for image processing pipelines.
 */
public class PipelineFactory {

    /** logger */
    protected static final Log log = LogFactory.getLog(PipelineFactory.class);

    private ImageManager manager;

    private int converterEdgeDirectoryVersion = -1;

    /** Holds the EdgeDirectory for all image conversions */
    private DefaultEdgeDirectory converterEdgeDirectory;

    /**
     * Main constructor.
     * @param manager the ImageManager instance
     */
    public PipelineFactory(ImageManager manager) {
        this.manager = manager;
    }

    private DefaultEdgeDirectory getEdgeDirectory() {
        ImageImplRegistry registry = manager.getRegistry();
        if (registry.getImageConverterModifications() != converterEdgeDirectoryVersion) {
            Collection converters = registry.getImageConverters();

            //Rebuild edge directory
            DefaultEdgeDirectory dir = new DefaultEdgeDirectory();
            for (Object converter1 : converters) {
                ImageConverter converter = (ImageConverter) converter1;
                Penalty penalty = Penalty.toPenalty(converter.getConversionPenalty());
                penalty = penalty.add(
                        registry.getAdditionalPenalty(converter.getClass().getName()));
                dir.addEdge(new ImageConversionEdge(converter, penalty));
            }

            converterEdgeDirectoryVersion = registry.getImageConverterModifications();
            this.converterEdgeDirectory = dir; //Replace (thread-safe)
        }
        return this.converterEdgeDirectory;
    }

    /**
     * Creates and returns an {@link ImageProviderPipeline} that allows to load an image of the
     * given MIME type and present it in the requested image flavor.
     * @param originalImage the original image that serves as the origin point of the conversion
     * @param targetFlavor the requested image flavor
     * @return an {@link ImageProviderPipeline} or null if no suitable pipeline could be assembled
     */
    public ImageProviderPipeline newImageConverterPipeline(
                Image originalImage, ImageFlavor targetFlavor) {
        //Get snapshot to avoid concurrent modification problems (thread-safety)
        DefaultEdgeDirectory dir = getEdgeDirectory();
        ImageRepresentation destination = new ImageRepresentation(targetFlavor);
        ImageProviderPipeline pipeline = findPipeline(dir, originalImage.getFlavor(), destination);
        return pipeline;
    }

    /**
     * Creates and returns an {@link ImageProviderPipeline} that allows to load an image of the
     * given MIME type and present it in the requested image flavor.
     * @param imageInfo the image info object of the original image
     * @param targetFlavor the requested image flavor
     * @return an {@link ImageProviderPipeline} or null if no suitable pipeline could be assembled
     */
    public ImageProviderPipeline newImageConverterPipeline(
                ImageInfo imageInfo, ImageFlavor targetFlavor) {
        ImageProviderPipeline[] candidates = determineCandidatePipelines(imageInfo, targetFlavor);

        //Choose best pipeline
        if (candidates.length > 0) {
            Arrays.sort(candidates, new PipelineComparator());
            ImageProviderPipeline pipeline = (ImageProviderPipeline)candidates[0];
            if (pipeline != null && log.isDebugEnabled()) {
                log.debug("Pipeline: " + pipeline
                        + " with penalty " + pipeline.getConversionPenalty());
            }
            return pipeline;
        } else {
            return null;
        }
    }

    /**
     * Determines all possible pipelines for the given image that can produce the requested
     * target flavor.
     * @param imageInfo the image information
     * @param targetFlavor the target flavor
     * @return the candidate pipelines
     */
    public ImageProviderPipeline[] determineCandidatePipelines(
                ImageInfo imageInfo, ImageFlavor targetFlavor) {
        String originalMime = imageInfo.getMimeType();
        ImageImplRegistry registry = manager.getRegistry();
        List candidates = new java.util.ArrayList();

        //Get snapshot to avoid concurrent modification problems (thread-safety)
        DefaultEdgeDirectory dir = getEdgeDirectory();

        ImageLoaderFactory[] loaderFactories = registry.getImageLoaderFactories(
                imageInfo, targetFlavor);
        if (loaderFactories != null) {
            //Directly load image and return it
            ImageLoader loader;
            if (loaderFactories.length == 1) {
                 loader = loaderFactories[0].newImageLoader(targetFlavor);
            } else {
                int count = loaderFactories.length;
                ImageLoader[] loaders = new ImageLoader[count];
                for (int i = 0; i < count; i++) {
                    loaders[i] = loaderFactories[i].newImageLoader(targetFlavor);
                }
                loader = new CompositeImageLoader(loaders);
            }
            ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), loader);
            candidates.add(pipeline);
        } else {
            //Need to use ImageConverters
            if (log.isTraceEnabled()) {
                log.trace("No ImageLoaderFactory found that can load this format ("
                        + targetFlavor + ") directly. Trying ImageConverters instead...");
            }

            ImageRepresentation destination = new ImageRepresentation(targetFlavor);
            //Get Loader for originalMIME
            // --> List of resulting flavors, possibly multiple loaders
            loaderFactories = registry.getImageLoaderFactories(originalMime);
            if (loaderFactories != null) {

                //Find best pipeline -> best loader
                for (ImageLoaderFactory loaderFactory : loaderFactories) {
                    ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(originalMime);
                    for (ImageFlavor flavor : flavors) {
                        ImageProviderPipeline pipeline = findPipeline(dir, flavor, destination);
                        if (pipeline != null) {
                            ImageLoader loader = loaderFactory.newImageLoader(flavor);
                            pipeline.setImageLoader(loader);
                            candidates.add(pipeline);
                        }
                    }
                }
            }
        }
        return (ImageProviderPipeline[])candidates.toArray(
                new ImageProviderPipeline[candidates.size()]);
    }

    /** Compares two pipelines based on their conversion penalty. */
    private static class PipelineComparator implements Comparator, Serializable {

        private static final long serialVersionUID = 1161513617996198090L;

        public int compare(Object o1, Object o2) {
            ImageProviderPipeline p1 = (ImageProviderPipeline)o1;
            ImageProviderPipeline p2 = (ImageProviderPipeline)o2;
            //Lowest penalty first
            return p1.getConversionPenalty() - p2.getConversionPenalty();
        }

    }

    private ImageProviderPipeline findPipeline(DefaultEdgeDirectory dir,
            ImageFlavor originFlavor, ImageRepresentation destination) {
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(
                dir);
        ImageRepresentation origin = new ImageRepresentation(originFlavor);
        dijkstra.execute(origin, destination);
        if (log.isTraceEnabled()) {
            log.trace("Lowest penalty: " + dijkstra.getLowestPenalty(destination));
        }

        Vertex prev = destination;
        Vertex pred = dijkstra.getPredecessor(destination);
        if (pred == null) {
            if (log.isTraceEnabled()) {
                log.trace("No route found!");
            }
            return null;
        } else {
            LinkedList stops = new LinkedList();
            while ((pred = dijkstra.getPredecessor(prev)) != null) {
                ImageConversionEdge edge = (ImageConversionEdge)
                        dir.getBestEdge(pred, prev);
                stops.addFirst(edge);
                prev = pred;
            }
            ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), null);
            for (Object stop : stops) {
                ImageConversionEdge edge = (ImageConversionEdge) stop;
                pipeline.addConverter(edge.getImageConverter());
            }
            return pipeline;
        }
    }

    /**
     * Finds and returns an array of {@link ImageProviderPipeline} instances which can handle
     * the given MIME type and return one of the given {@link ImageFlavor}s.
     * @param imageInfo the image info object
     * @param flavors the possible target flavors
     * @return an array of pipelines
     */
    public ImageProviderPipeline[] determineCandidatePipelines(ImageInfo imageInfo,
            ImageFlavor[] flavors) {
        List candidates = new java.util.ArrayList();
        for (ImageFlavor flavor : flavors) {
            //Find the best pipeline for each flavor
            ImageProviderPipeline pipeline = newImageConverterPipeline(imageInfo, flavor);
            if (pipeline == null) {
                continue; //No suitable pipeline found for flavor
            }
            Penalty p = pipeline.getConversionPenalty(this.manager.getRegistry());
            if (!p.isInfinitePenalty()) {
                candidates.add(pipeline);
            }
        }
        return (ImageProviderPipeline[])candidates.toArray(
                new ImageProviderPipeline[candidates.size()]);
    }

    /**
     * Finds and returns an array of {@link ImageProviderPipeline} instances which can handle
     * the convert the given {@link Image} and return one of the given {@link ImageFlavor}s.
     * @param sourceImage the image to be converted
     * @param flavors the possible target flavors
     * @return an array of pipelines
     */
    public ImageProviderPipeline[] determineCandidatePipelines(Image sourceImage,
            ImageFlavor[] flavors) {
        List candidates = new java.util.ArrayList();
        for (ImageFlavor flavor : flavors) {
            //Find the best pipeline for each flavor
            ImageProviderPipeline pipeline = newImageConverterPipeline(sourceImage, flavor);
            if (pipeline != null) {
                candidates.add(pipeline);
            }
        }
        return (ImageProviderPipeline[])candidates.toArray(
                new ImageProviderPipeline[candidates.size()]);
    }


}
