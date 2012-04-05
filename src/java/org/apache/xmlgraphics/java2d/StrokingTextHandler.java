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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;

/**
 * Default TextHandler implementation which paints text using graphics primitives (shapes).
 */
public class StrokingTextHandler implements TextHandler {

    /**
     * Default constructor.
     */
    public StrokingTextHandler() {
        //nop
    }

    /** {@inheritDoc} */
    public void drawString(Graphics2D g2d, String text, float x, float y) throws IOException {
        java.awt.Font awtFont = g2d.getFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = awtFont.createGlyphVector(frc, text);
        Shape glyphOutline = gv.getOutline(x, y);
        g2d.fill(glyphOutline);
    }

}
