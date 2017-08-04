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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlgraphics.util.Service;

/**
 * Registry for {@link ImageWriter} implementations.
 */
public final class ImageWriterRegistry {

    private static volatile ImageWriterRegistry instance;

    private Map<String, List<ImageWriter>> imageWriterMap
            = new java.util.HashMap<String, List<ImageWriter>>();
    private Map<String, Integer> preferredOrder;

    /**
     * Default constructor. The default preferred order for the image writers is loaded from the
     * resources.
     */
    public ImageWriterRegistry() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("default-preferred-order.properties");
        if (in != null) {
            try {
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            } catch (IOException ioe) {
                throw new RuntimeException(
                        "Could not load default preferred order due to I/O error: "
                            + ioe.getMessage());
            }
        }
        setPreferredOrder(props);
        setup();
    }

    /**
     * Special constructor. The preferred order for the image writers can be specified as a
     * Map (for example a Properties file). The entries of the Map consists of fully qualified
     * class or package names as keys and integer numbers as values. Zero (0) is the default
     * priority.
     * @param preferredOrder the map of order properties used to order the plug-ins
     */
    public ImageWriterRegistry(Properties preferredOrder) {
        setPreferredOrder(preferredOrder);
        setup();
    }

    private void setPreferredOrder(Properties preferredOrder) {
        Map<String, Integer> order = new java.util.HashMap<String, Integer>();
        for (Map.Entry<Object, Object> entry : preferredOrder.entrySet()) {
            order.put(entry.getKey().toString(),
                    Integer.parseInt(entry.getValue().toString()));
        }
        this.preferredOrder = order;
    }

    /** @return a singleton instance of the ImageWriterRegistry. */
    public static ImageWriterRegistry getInstance() {
        if (instance == null) {
            instance = new ImageWriterRegistry();
        }
        return instance;
    }

    private void setup() {
        Iterator iter = Service.providers(ImageWriter.class);
        while (iter.hasNext()) {
            ImageWriter writer = (ImageWriter)iter.next();
            register(writer);
        }
    }

    private int getPriority(ImageWriter writer) {
        String key = writer.getClass().getName();
        Integer value = preferredOrder.get(key);
        while (value == null) {
            int pos = key.lastIndexOf(".");
            if (pos < 0) {
                break;
            }
            key = key.substring(0, pos);
            value = preferredOrder.get(key);
        }
        return (value != null) ? value : 0;
    }

    /**
     * Registers a new ImageWriter implementation with the associated priority in the registry.
     * Higher priorities get preference over lower priorities.
     * @param writer the ImageWriter instance to register.
     * @param priority the priority of the writer in the registry.
     * @see #register(ImageWriter)
     */
    public void register(ImageWriter writer, int priority) {

        String key = writer.getClass().getName();
        // Register the priority to preferredOrder; overwrite original priority if exists
        preferredOrder.put(key, priority);

        register(writer);
    }

    /**
     * Registers a new ImageWriter implementation in the registry. If an ImageWriter for the same
     * target MIME type has already been registered, it is placed in an array based on priority.
     * @param writer the ImageWriter instance to register.
     */
    public synchronized void register(ImageWriter writer) {
        List<ImageWriter> entries = imageWriterMap.get(writer.getMIMEType());
        if (entries == null) {
            entries = new java.util.ArrayList<ImageWriter>();
            imageWriterMap.put(writer.getMIMEType(), entries);
        }

        int priority = getPriority(writer);
        ListIterator<ImageWriter> li = entries.listIterator();
        while (li.hasNext()) {
            ImageWriter w = li.next();
            if (getPriority(w) < priority) {
                li.previous();
                break;
            }
        }
        li.add(writer);
    }

    /**
     * Returns an ImageWriter that can be used to encode an image to the requested MIME type.
     * @param mime the MIME type of the desired output format
     * @return a functional ImageWriter instance handling the desired output format or
     *         null if none can be found.
     */
    public synchronized ImageWriter getWriterFor(String mime) {
        List<ImageWriter> entries = imageWriterMap.get(mime);
        if (entries == null) {
            return null;
        }
        for (ImageWriter writer : entries) {
            if (writer.isFunctional()) {
                return writer;
            }
        }
        return null;
    }

}
