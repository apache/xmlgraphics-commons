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

package org.apache.xmlgraphics.ps;

import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

/**
 * Abstract helper class for generating PostScript forms.
 */
public abstract class FormGenerator {

    private String formName;
    private String title;
    private Dimension2D dimensions;

    /**
     * Main constructor.
     * @param formName the form's name
     * @param title the form's title or null
     * @param dimensions the form's dimensions
     */
    public FormGenerator(String formName, String title, Dimension2D dimensions) {
        this.formName = formName;
        this.title = title;
        this.dimensions = dimensions;
    }

    /**
     * Returns the form's name.
     * @return the form's name
     */
    public String getFormName() {
        return this.formName;
    }

    /**
     * Returns the form's title.
     * @return the form's title or null if there's no title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * returns the form's dimensions.
     * @return the form's dimensions
     */
    public Dimension2D getDimensions() {
        return this.dimensions;
    }

    /**
     * Generates the PostScript code for the PaintProc of the form.
     * @param gen the PostScript generator
     * @throws IOException if an I/O error occurs
     */
    protected abstract void generatePaintProc(PSGenerator gen) throws IOException;

    /**
     * Generates some PostScript code right after the form definition (used primarily for
     * bitmap data).
     * @param gen the PostScript generator
     * @throws IOException if an I/O error occurs
     */
    protected void generateAdditionalDataStream(PSGenerator gen) throws IOException {
        //nop
    }

    /**
     * Returns the matrix for use in the form.
     * @return the matrix
     */
    protected AffineTransform getMatrix() {
        return new AffineTransform();
    }

    /**
     * Returns the form's bounding box.
     * @return the form's bounding box
     */
    protected Rectangle2D getBBox() {
        return new Rectangle2D.Double(0, 0, dimensions.getWidth(), dimensions.getHeight());
    }

    /**
     * Generates the PostScript form.
     * @param gen the PostScript generator
     * @return a PSResource instance representing the form
     * @throws IOException if an I/O error occurs
     */
    public PSResource generate(PSGenerator gen) throws IOException {
        if (gen.getPSLevel() < 2) {
            throw new UnsupportedOperationException(
                    "Forms require at least Level 2 PostScript");
        }
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE,
                new Object[] {PSResource.TYPE_FORM, getFormName()});
        if (title != null) {
            gen.writeDSCComment(DSCConstants.TITLE, title);
        }
        gen.writeln("/" + formName);
        gen.writeln("<< /FormType 1");
        gen.writeln("  /BBox " + gen.formatRectangleToArray(getBBox()));
        gen.writeln("  /Matrix " + gen.formatMatrix(getMatrix()));
        gen.writeln("  /PaintProc {");
        gen.writeln("    pop");
        gen.writeln("    gsave");
        generatePaintProc(gen);
        gen.writeln("    grestore");
        gen.writeln("  } bind");
        gen.writeln(">> def");
        PSResource res = new PSResource(PSResource.TYPE_FORM, formName);
        try {
            generateAdditionalDataStream(gen);
        } finally {
            gen.writeDSCComment(DSCConstants.END_RESOURCE);
            gen.getResourceTracker().registerSuppliedResource(res);
        }
        return res;
    }
}
