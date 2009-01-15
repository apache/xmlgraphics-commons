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

package org.apache.xmlgraphics.image;

public final class GraphicsConstants {

    /**
     * The default DPI used when they cannot be determined by the graphics
     * environment or loaded from the image.
     */
    public static final int DEFAULT_DPI = 72;

    /**
     * Graphics may be sampled at this resolution.
     */
    public static final int DEFAULT_SAMPLE_DPI = 300;

    /**
     * This class should not be instantiated.
     */
    private GraphicsConstants() {
    }
}
