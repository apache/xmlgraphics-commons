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

package org.apache.xmlgraphics.java2d.ps;

import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSState;

public class PSGraphics2DTestCase {

    private PSGenerator gen;
    private PSGraphics2D gfx2d;
    private final AffineTransform transform = new AffineTransform(1, 0, 0, -1, 0, 792);

    @Before
    public void setup() {
        gen = mock(PSGenerator.class);
        createGraphics2D();
        PSState pState = new PSState();
        when(gen.getCurrentState()).thenReturn(pState);
    }

    private void createGraphics2D() {
        gfx2d = new PSGraphics2D(false, gen);
        gfx2d.setGraphicContext(new GraphicContext());
        gfx2d.setTransform(transform);
    }

    @Test
    public void draw() throws IOException {
        assertEquals(gfx2d.getTransform(), transform);
        gfx2d.draw(new Rectangle(10, 10, 100, 100));
        verify(gen, times(1)).concatMatrix(transform);
    }

    @Test
    public void testShouldBeClipped() {
        Shape line = new Line2D.Float(10, 10, 50, 50);
        Shape clipArea = new Rectangle2D.Float(20, 20, 100, 100);
        assertTrue(gfx2d.shouldBeClipped(clipArea, line));
        Shape rect = new Rectangle2D.Float(30, 30, 40, 40);
        assertFalse(gfx2d.shouldBeClipped(clipArea, rect));
    }

    @Test
    public void testFill() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PSGenerator gen = new PSGenerator(out);
        PSGraphics2D p = new PSGraphics2D(false, gen);
        p.setGraphicContext(new GraphicContext());
        p.fill(new RoundRectangle2D.Float());
        out.reset();

        p.fill(new RoundRectangle2D.Float());
        assertEquals(out.toString(),
                "GS\nN\n/f1943450110{0 0 M\n0 0 L\n0 0 0 0 0 0 C\n0 0 L\n0 0 0 0 0 0 C\n"
                        + "0 0 L\n0 0 0 0 0 0 C\n0 0 L\n0 0 0 0 0 0 C\ncp}def\nf1943450110\nf\nGR\n");
        out.reset();

        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        p.drawImage(img, 0, 0, null);

        String res = "[1 0 0 1 0 0] CT\n"
                + "GS\n"
                + "0 0 translate\n"
                + "%AXGBeginBitmap: java.awt.image.BufferedImage\n"
                + "{{\n"
                + "/RawData currentfile /ASCII85Decode filter def\n"
                + "/Data RawData /FlateDecode filter def\n"
                + "/DeviceRGB setcolorspace\n";

        assertTrue(out.toString(), out.toString().startsWith("GS\n" + res));
        out.reset();

        p.drawRenderedImage(img, new AffineTransform());
        assertTrue(out.toString(), out.toString().startsWith("GS\n[1 0 0 1 0 0] CT\n" + res));

        out.reset();

        p.writeClip(new RoundRectangle2D.Float());
        assertEquals(out.toString(), "N\n"
                + "0 0 M\n"
                + "0 0 L\n"
                + "0 0 0 0 0 0 C\n"
                + "0 0 L\n"
                + "0 0 0 0 0 0 C\n"
                + "0 0 L\n"
                + "0 0 0 0 0 0 C\n"
                + "0 0 L\n"
                + "0 0 0 0 0 0 C\n"
                + "cp\n"
                + "clip\n");
        out.reset();

        p.drawString("hi", 0f, 0f);
        assertTrue(out.toString(), out.toString().startsWith("GS\nN\n/f"));
        out.reset();

        TexturePaint tp = new TexturePaint(img, new Rectangle());
        p.setPaint(tp);
        p.fill(new Rectangle());
        assertTrue(out.toString().startsWith("GS\n<<\n/PatternType 1\n"));

        p.dispose();
    }

    @Test
    public void testAcrobatDownsample() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PSGenerator gen = new PSGenerator(out);
        PSGraphics2D p = new PSGraphics2D(false, gen);
        p.setGraphicContext(new GraphicContext());
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        TexturePaint tp = new TexturePaint(img, new Rectangle());
        p.setPaint(tp);
        p.fill(new Rectangle());
        assertTrue(out.toString().contains("1 1 8 matrix\n{<\nffffff\n>} false 3 colorimage"));
        out.reset();

        gen.setAcrobatDownsample(true);
        p.fill(new Rectangle());
        assertTrue(out.toString().contains("1 1 4 matrix\n{<\nfff\n>} false 3 colorimage"));
        p.dispose();
    }

    @Test
    public void testFillAlpha() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PSGenerator gen = new PSGenerator(out);
        PSGraphics2D p = new PSGraphics2D(false, gen);
        p.setGraphicContext(new GraphicContext());
        p.setComposite(AlphaComposite.getInstance(3, 0));
        p.fill(new Rectangle());
        assertEquals(out.toString(), "");
        p.setComposite(AlphaComposite.getInstance(3, 0.5f));
        p.fill(new Rectangle());
        assertTrue(out.toString().contains("\nN\n"));
    }
}
