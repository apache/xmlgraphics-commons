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

package org.apache.xmlgraphics.java2d;

import java.io.IOException;

/**
 * Interface which the Graphics2D class delegates text painting to.
 */
public interface TextHandler {

    /**
     * Draw some text.
     * @param text the text to paint
     * @param x the x-coordinate where the <code>String</code> should be rendered
     * @param y the y-coordinate where the <code>String</code> should be rendered
     * @throws IOException In case of an I/O error
     */
    void drawString(String text, float x, float y) throws IOException;
}
