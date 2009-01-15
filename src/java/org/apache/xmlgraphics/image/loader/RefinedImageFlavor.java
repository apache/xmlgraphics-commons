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

/**
 * Special image flavor subclass which enables the refinement to specific (sub-)flavors but
 * maintaining compatibility to a parent (i.e. more general) flavor.
 */
public abstract class RefinedImageFlavor extends ImageFlavor {

    private ImageFlavor parentFlavor;

    /**
     * Constructs a new image flavor.
     * @param parentFlavor the parent image flavor
     */
    protected RefinedImageFlavor(ImageFlavor parentFlavor) {
        this(parentFlavor.getName(), parentFlavor);
    }

    /**
     * Constructs a new image flavor.
     * @param parentFlavor the parent image flavor
     * @param name the name of the flavor (must be unique)
     */
    protected RefinedImageFlavor(String name, ImageFlavor parentFlavor) {
        super(name);
        this.parentFlavor = parentFlavor;
    }

    /**
     * Returns the associated parent image flavor.
     * @return the parent image flavor
     */
    public ImageFlavor getParentFlavor() {
        return this.parentFlavor;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return this.parentFlavor.getMimeType();
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return this.parentFlavor.getNamespace();
    }

    /** {@inheritDoc} */
    public boolean isCompatible(ImageFlavor flavor) {
        return getParentFlavor().isCompatible(flavor)
            || super.isCompatible(flavor);
    }

}
