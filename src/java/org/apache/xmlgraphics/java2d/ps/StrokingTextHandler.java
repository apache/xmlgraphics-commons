/*
 * Copyright 1999-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.xmlgraphics.java2d.ps;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;

/**
 * Default TextHandler implementation which paints text using graphics primitives (shapes). 
 */
public class StrokingTextHandler implements TextHandler {

    private PSGraphics2D g2d;
    
    public StrokingTextHandler(PSGraphics2D g2d) {
        this.g2d = g2d;
    }
    
    /** @see org.apache.xmlgraphics.java2d.ps.TextHandler#writeSetup() */
    public void writeSetup() throws IOException {
        //nop
    }

    /** @see org.apache.xmlgraphics.java2d.ps.TextHandler#writePageSetup() */
    public void writePageSetup() throws IOException {
        //nop
    }

    /** @see TextHandler#drawString(java.lang.String, float, float) */
    public void drawString(String text, float x, float y) {
        java.awt.Font awtFont = g2d.getFont();
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = awtFont.createGlyphVector(frc, text);
        Shape glyphOutline = gv.getOutline(x, y);
        g2d.fill(glyphOutline);
    }

}
