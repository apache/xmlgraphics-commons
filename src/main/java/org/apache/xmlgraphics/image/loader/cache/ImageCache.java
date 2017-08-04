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

package org.apache.xmlgraphics.image.loader.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.SoftMapCache;


/**
 * This class provides a cache for images. The main key into the images is the original URI the
 * image was accessed with.
 * <p>
 * Don't use one ImageCache instance in the context of multiple base URIs because relative URIs
 * would not work correctly anymore.
 * <p>
 * By default, the URIs of inaccessible images are remembered but these entries are discarded
 * after 60 seconds (which causes a retry next time the same URI is requested). This allows
 * to counteract performance loss when accessing invalid or temporarily unavailable images
 * over slow connections.
 */
public class ImageCache {

    /** logger */
    protected static final Log log = LogFactory.getLog(ImageCache.class);

    //Handling of invalid URIs
    private Map invalidURIs = Collections.synchronizedMap(new java.util.HashMap());
    private ExpirationPolicy invalidURIExpirationPolicy;

    //Actual image cache
    private SoftMapCache imageInfos = new SoftMapCache(true);
    private SoftMapCache images = new SoftMapCache(true);

    private ImageCacheListener cacheListener;
    private TimeStampProvider timeStampProvider;
    private long lastHouseKeeping;

    /**
     * Default constructor with default settings.
     */
    public ImageCache() {
        this(new TimeStampProvider(), new DefaultExpirationPolicy());
    }

    /**
     * Constructor for customized behaviour and testing.
     * @param timeStampProvider the time stamp provider to use
     * @param invalidURIExpirationPolicy the expiration policy for invalid URIs
     */
    public ImageCache(TimeStampProvider timeStampProvider,
            ExpirationPolicy invalidURIExpirationPolicy) {
        this.timeStampProvider = timeStampProvider;
        this.invalidURIExpirationPolicy = invalidURIExpirationPolicy;
        this.lastHouseKeeping = this.timeStampProvider.getTimeStamp();
    }

    /**
     * Sets an ImageCacheListener instance so the events in the image cache can be observed.
     * @param listener the listener instance
     */
    public void setCacheListener(ImageCacheListener listener) {
        this.cacheListener = listener;
    }

    /**
     * Returns an ImageInfo instance for a given URI.
     * @param uri the image's URI
     * @param session the session context
     * @param manager the ImageManager handling the images
     * @return the ImageInfo instance
     * @throws ImageException if an error occurs while parsing image data
     * @throws IOException if an I/O error occurs while loading image data
     */
    public ImageInfo needImageInfo(String uri, ImageSessionContext session, ImageManager manager)
            throws ImageException, IOException {
        //Fetch unique version of the URI and use it for synchronization so we have some sort of
        //"row-level" locking instead of "table-level" locking (to use a database analogy).
        //The fine locking strategy is necessary since preloading an image is a potentially long
        //operation.
        if (isInvalidURI(uri)) {
            throw new FileNotFoundException("Image not found: " + uri);
        }
        String lockURI = uri.intern();
        synchronized (lockURI) {
            ImageInfo info = getImageInfo(uri);
            if (info == null) {
                try {
                    Source src = session.needSource(uri);
                    if (src == null) {
                        registerInvalidURI(uri);
                        throw new FileNotFoundException("Image not found: " + uri);
                    }
                    info = manager.preloadImage(uri, src);
                    session.returnSource(uri, src);
                } catch (IOException ioe) {
                    registerInvalidURI(uri);
                    throw ioe;
                } catch (ImageException e) {
                    registerInvalidURI(uri);
                    throw e;
                }
                if (info.getOriginalImage() == null || info.getOriginalImage().isCacheable()) {
                    putImageInfo(info);
                }
            }
            return info;
        }
    }

    /**
     * Indicates whether a URI has previously been identified as an invalid URI.
     * @param uri the image's URI
     * @return true if the URI is invalid
     */
    public boolean isInvalidURI(String uri) {
        boolean expired = removeInvalidURIIfExpired(uri);
        if (expired) {
            return false;
        } else {
            if (cacheListener != null) {
                cacheListener.invalidHit(uri);
            }
            return true;
        }
    }

    private boolean removeInvalidURIIfExpired(String uri) {
        Long timestamp = (Long) invalidURIs.get(uri);
        boolean expired = (timestamp == null)
                || this.invalidURIExpirationPolicy.isExpired(
                        this.timeStampProvider, timestamp);
        if (expired) {
            this.invalidURIs.remove(uri);
        }
        return expired;
    }

    /**
     * Returns an ImageInfo instance from the cache or null if none is found.
     * @param uri the image's URI
     * @return the ImageInfo instance or null if the requested information is not in the cache
     */
    protected ImageInfo getImageInfo(String uri) {
        ImageInfo info = (ImageInfo)imageInfos.get(uri);
        if (cacheListener != null) {
            if (info != null) {
                cacheListener.cacheHitImageInfo(uri);
            } else {
                if (!isInvalidURI(uri)) {
                    cacheListener.cacheMissImageInfo(uri);
                }
            }
        }
        return info;
    }

    /**
     * Registers an ImageInfo instance with the cache.
     * @param info the ImageInfo instance
     */
    protected void putImageInfo(ImageInfo info) {
        //An already existing ImageInfo is replaced.
        imageInfos.put(info.getOriginalURI(), info);
    }

    private static final long ONE_HOUR = 60 * 60 * 1000;

    /**
     * Registers a URI as invalid so getImageInfo can indicate that quickly with no I/O access.
     * @param uri the URI of the invalid image
     */
    void registerInvalidURI(String uri) {
        invalidURIs.put(uri, timeStampProvider.getTimeStamp());

        considerHouseKeeping();
    }

    /**
     * Returns an image from the cache or null if it wasn't found.
     * @param info the ImageInfo instance representing the image
     * @param flavor the requested ImageFlavor for the image
     * @return the requested image or null if the image is not in the cache
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor) {
        return getImage(info.getOriginalURI(), flavor);
    }

    /**
     * Returns an image from the cache or null if it wasn't found.
     * @param uri the image's URI
     * @param flavor the requested ImageFlavor for the image
     * @return the requested image or null if the image is not in the cache
     */
    public Image getImage(String uri, ImageFlavor flavor) {
        if (uri == null || "".equals(uri)) {
            return null;
        }
        ImageKey key = new ImageKey(uri, flavor);
        Image img = (Image)images.get(key);
        if (cacheListener != null) {
            if (img != null) {
                cacheListener.cacheHitImage(key);
            } else {
                cacheListener.cacheMissImage(key);
            }
        }
        return img;
    }

    /**
     * Registers an image with the cache.
     * @param img the image
     */
    public void putImage(Image img) {
        String originalURI = img.getInfo().getOriginalURI();
        if (originalURI == null || "".equals(originalURI)) {
            return; //Don't cache if there's no URI
        }
        //An already existing Image is replaced.
        if (!img.isCacheable()) {
            throw new IllegalArgumentException(
                    "Image is not cacheable! (Flavor: " + img.getFlavor() + ")");
        }
        ImageKey key = new ImageKey(originalURI, img.getFlavor());
        images.put(key, img);
    }

    /**
     * Clears the image cache (all ImageInfo and Image objects).
     */
    public void clearCache() {
        invalidURIs.clear();
        imageInfos.clear();
        images.clear();
        doHouseKeeping();
    }

    private void considerHouseKeeping() {
        long ts = timeStampProvider.getTimeStamp();
        if (this.lastHouseKeeping + ONE_HOUR > ts) {
            //Housekeeping is only triggered through registration of an invalid URI at the moment.
            //Depending on the environment this could be triggered next to never.
            //Doing this check for every image access could be relatively costly.
            //The only alternative is a cleanup thread which is rather heavy-weight.
            this.lastHouseKeeping = ts;
            doHouseKeeping();
        }
    }

    /**
     * Triggers some house-keeping, i.e. removes stale entries.
     */
    public void doHouseKeeping() {
        imageInfos.doHouseKeeping();
        images.doHouseKeeping();
        doInvalidURIHouseKeeping();
    }

    private void doInvalidURIHouseKeeping() {
        final Set currentEntries = new HashSet(this.invalidURIs.keySet());
        for (Object currentEntry : currentEntries) {
            final String key = (String) currentEntry;
            removeInvalidURIIfExpired(key);
        }
    }

}
