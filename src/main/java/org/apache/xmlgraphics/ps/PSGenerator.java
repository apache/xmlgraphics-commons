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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Stack;

import javax.xml.transform.Source;

import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.util.DoubleFormatUtil;

/**
 * This class is used to output PostScript code to an OutputStream. This class assumes that
 * the {@link PSProcSets#STD_PROCSET} has been added to the PostScript file.
 *
 * @version $Id$
 */
public class PSGenerator implements PSCommandMap {

    /**
     * Default postscript language level
     */
    public static final int DEFAULT_LANGUAGE_LEVEL = 3;

    /**
     * Indicator for the PostScript interpreter that the value is provided
     * later in the document (mostly in the %%Trailer section).
     * @deprecated Please use DSCConstants.ATEND. This constant was in the wrong place.
     */
    @Deprecated
    public static final Object ATEND = DSCConstants.ATEND;

    /** Line feed used by PostScript */
    public static final char LF = '\n';

    private static final String IDENTITY_H = "Identity-H";

    private Log log = LogFactory.getLog(getClass());
    private OutputStream out;
    private int psLevel = DEFAULT_LANGUAGE_LEVEL;
    private boolean acrobatDownsample;
    private boolean commentsEnabled = true;
    private boolean compactMode = true;
    private PSCommandMap commandMap = PSProcSets.STD_COMMAND_MAP;

    private Stack<PSState> graphicsStateStack = new Stack<PSState>();
    private PSState currentState;

    private StringBuffer doubleBuffer = new StringBuffer(16);

    private StringBuffer tempBuffer = new StringBuffer(256);

    private boolean identityHEmbedded;

    private PSResource procsetCIDInitResource;

    private PSResource identityHCMapResource;

    /**
     * Creates a new instance.
     * @param out the OutputStream to write the generated PostScript code to
     */
    public PSGenerator(OutputStream out) {
        this.out = out;
        resetGraphicsState();
    }

    /**
     * Indicates whether this instance is in compact mode. See {@link #setCompactMode(boolean)}
     * for details.
     * @return true if compact mode is enabled (the default)
     */
    public boolean isCompactMode() {
        return this.compactMode;
    }

    /**
     * Controls whether this instance shall produce compact PostScript (omitting comments and
     * using short macros). Enabling this mode requires that the standard procset
     * ({@link PSProcSets#STD_PROCSET}) is included in the PostScript file. Setting this to
     * false produces more verbose PostScript suitable for debugging.
     * @param value true to enable compact mode, false for verbose mode
     */
    public void setCompactMode(boolean value) {
        this.compactMode = value;
    }

    /**
     * Indicates whether this instance allows to write comments. See
     * {@link #setCommentsEnabled(boolean)} for details.
     * @return true if comments are enabled (the default)
     */
    public boolean isCommentsEnabled() {
        return this.commentsEnabled;
    }

    /**
     * Controls whether this instance allows to write comments using the {@link #commentln(String)}
     * method.
     * @param value true to enable comments, false to disable them
     */
    public void setCommentsEnabled(boolean value) {
        this.commentsEnabled = value;
    }

    private void resetGraphicsState() {
        if (!this.graphicsStateStack.isEmpty()) {
            throw new IllegalStateException("Graphics state stack should be empty at this point");
        }
        this.currentState = new PSState();
    }

    /**
     * Returns the OutputStream the PSGenerator writes to.
     * @return the OutputStream
     */
    public OutputStream getOutputStream() {
        return this.out;
    }

    /**
     * Returns the selected PostScript level.
     * @return the PostScript level
     */
    public int getPSLevel() {
        return this.psLevel;
    }

    /**
     * Sets the PostScript level that is used to generate the current document.
     * @param level the PostScript level (currently 1, 2 and 3 are known)
     */
    public void setPSLevel(int level) {
        this.psLevel = level;
    }

    public boolean isAcrobatDownsample() {
        return acrobatDownsample;
    }

    public void setAcrobatDownsample(boolean b) {
        acrobatDownsample = b;
    }

    /**
     * Attempts to resolve the given URI. PSGenerator should be subclasses to provide more
     * sophisticated URI resolution.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved.
     */
    public Source resolveURI(String uri) {
        return new javax.xml.transform.stream.StreamSource(uri);
    }

    /**
     * Writes a newline character to the OutputStream.
     *
     * @throws IOException In case of an I/O problem
     */
    public final void newLine() throws IOException {
        out.write(LF);
    }

    /**
     * Formats a double value for PostScript output.
     *
     * @param value value to format
     * @return the formatted value
     */
    public String formatDouble(double value) {
        doubleBuffer.setLength(0);
        DoubleFormatUtil.formatDouble(value, 3, 3, doubleBuffer);
        return doubleBuffer.toString();
    }

    /**
     * Formats a double value for PostScript output (higher resolution).
     *
     * @param value value to format
     * @return the formatted value
     */
    public String formatDouble5(double value) {
        doubleBuffer.setLength(0);
        DoubleFormatUtil.formatDouble(value, 5, 5, doubleBuffer);
        return doubleBuffer.toString();
    }

    /**
     * Writes a PostScript command to the stream.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void write(String cmd) throws IOException {
        /* TODO Check disabled until clarification.
        if (cmd.length() > 255) {
            throw new RuntimeException("PostScript command exceeded limit of 255 characters");
        } */
        out.write(cmd.getBytes("US-ASCII"));
    }

    /**
     * Writes the given number to the stream in decimal format.
     *
     * @param n a number
     * @throws IOException in case of an I/O problem
     */
    public void write(int n) throws IOException {
        write(Integer.toString(n));
    }

    /**
     * Writes a PostScript command to the stream and ends the line.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void writeln(String cmd) throws IOException {
        write(cmd);
        newLine();
    }

    /**
     * Writes a comment to the stream and ends the line. Output of comments can
     * be disabled to reduce the size of the generated file.
     *
     * @param comment          comment to write
     * @exception IOException  In case of an I/O problem
     */
    public void commentln(String comment) throws IOException {
        if (isCommentsEnabled()) {
            writeln(comment);
        }
    }

    /** {@inheritDoc} */
    public String mapCommand(String command) {
        if (isCompactMode()) {
            return this.commandMap.mapCommand(command);
        } else {
            return command;
        }
    }

    /**
     * Writes encoded data to the PostScript stream.
     *
     * @param cmd              The encoded PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void writeByteArr(byte[] cmd) throws IOException {
        out.write(cmd);
        newLine();
    }


    /**
     * Flushes the OutputStream.
     *
     * @exception IOException In case of an I/O problem
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Escapes a character conforming to the rules established in the PostScript
     * Language Reference (Search for "Literal Text Strings").
     * @param c character to escape
     * @param target target StringBuffer to write the escaped character to
     */
    public static final void escapeChar(char c, StringBuffer target) {
        switch (c) {
            case '\n':
                target.append("\\n");
                break;
            case '\r':
                target.append("\\r");
                break;
            case '\t':
                target.append("\\t");
                break;
            case '\b':
                target.append("\\b");
                break;
            case '\f':
                target.append("\\f");
                break;
            case '\\':
                target.append("\\\\");
                break;
            case '(':
                target.append("\\(");
                break;
            case ')':
                target.append("\\)");
                break;
            default:
                if (c > 255) {
                    //Ignoring non Latin-1 characters
                    target.append('?');
                } else if (c < 32 || c > 127) {
                    target.append('\\');

                    target.append((char)('0' + (c >> 6)));
                    target.append((char)('0' + ((c >> 3) % 8)));
                    target.append((char)('0' + (c % 8)));
                    //Integer.toOctalString(i)
                } else {
                    target.append(c);
                }
        }
    }


    /**
     * Converts text by applying escaping rules established in the DSC specs.
     * @param text Text to convert
     * @return String The resulting String
     */
    public static final String convertStringToDSC(String text) {
        return convertStringToDSC(text, false);
    }

    /**
     * Converts a &lt;real&gt; value for use in DSC comments.
     * @param value the value to convert
     * @return String The resulting String
     */
    public static final String convertRealToDSC(float value) {
        return Float.toString(value);
    }

    /**
     * Converts text by applying escaping rules established in the DSC specs.
     * @param text Text to convert
     * @param forceParentheses Force the use of parentheses
     * @return String The resulting String
     */
    public static final String convertStringToDSC(String text,
                                                  boolean forceParentheses) {
        if ((text == null) || (text.length() == 0)) {
            return "()";
        } else {
            int initialSize = text.length();
            initialSize += initialSize / 2;
            StringBuffer sb = new StringBuffer(initialSize);
            if ((text.indexOf(' ') >= 0) || forceParentheses) {
                sb.append('(');
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    escapeChar(c, sb);
                }
                sb.append(')');
                return sb.toString();
            } else {
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    escapeChar(c, sb);
                }
                return sb.toString();
            }
        }
    }


    /**
     * Writes a DSC comment to the output stream.
     * @param name Name of the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.xmlgraphics.ps.DSCConstants
     */
    public void writeDSCComment(String name) throws IOException {
        writeln("%%" + name);
    }


    /**
     * Writes a DSC comment to the output stream. The parameter to the DSC
     * comment can be any object. The object is converted to a String as
     * necessary.
     * @param name Name of the DSC comment
     * @param param Single parameter to the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.xmlgraphics.ps.DSCConstants
     */
    public void writeDSCComment(String name, Object param) throws IOException {
        writeDSCComment(name, new Object[] {param});
    }


    /**
     * Writes a DSC comment to the output stream. The parameters to the DSC
     * comment can be any object. The objects are converted to Strings as
     * necessary. Please see the source code to find out what parameters are
     * currently supported.
     * @param name Name of the DSC comment
     * @param params Array of parameters to the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.xmlgraphics.ps.DSCConstants
     */
    public void writeDSCComment(String name, Object[] params) throws IOException {
        tempBuffer.setLength(0);
        tempBuffer.append("%%");
        tempBuffer.append(name);
        if ((params != null) && (params.length > 0)) {
            tempBuffer.append(": ");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    tempBuffer.append(" ");
                }

                if (params[i] instanceof String) {
                    tempBuffer.append(convertStringToDSC((String)params[i]));
                } else if (params[i] == DSCConstants.ATEND) {
                    tempBuffer.append(DSCConstants.ATEND);
                } else if (params[i] instanceof Double) {
                    tempBuffer.append(formatDouble((Double) params[i]));
                } else if (params[i] instanceof Number) {
                    tempBuffer.append(params[i].toString());
                } else if (params[i] instanceof Date) {
                    DateFormat datef = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    tempBuffer.append(convertStringToDSC(datef.format((Date)params[i])));
                } else if (params[i] instanceof PSResource) {
                    tempBuffer.append(((PSResource)params[i]).getResourceSpecification());
                } else {
                    throw new IllegalArgumentException("Unsupported parameter type: "
                            + params[i].getClass().getName());
                }
            }
        }
        writeln(tempBuffer.toString());
    }


    /**
     * Saves the graphics state of the rendering engine.
     * @exception IOException In case of an I/O problem
     */
    public void saveGraphicsState() throws IOException {
        writeln(mapCommand("gsave"));

        PSState state = new PSState(this.currentState, false);
        this.graphicsStateStack.push(this.currentState);
        this.currentState = state;
    }

    /**
     * Restores the last graphics state of the rendering engine.
     * @return true if the state was restored, false if there's a stack underflow.
     * @exception IOException In case of an I/O problem
     */
    public boolean restoreGraphicsState() throws IOException {
        if (this.graphicsStateStack.size() > 0) {
            writeln(mapCommand("grestore"));
            this.currentState = this.graphicsStateStack.pop();
            return true;
        } else {
            return false;
        }
    }


    /**
     * Returns the current graphics state.
     * @return the current graphics state
     */
    public PSState getCurrentState() {
        return this.currentState;
    }

    /**
     * Issues the "showpage" command and resets the painting state accordingly.
     * @exception IOException In case of an I/O problem
     */
    public void showPage() throws IOException {
        writeln("showpage");
        resetGraphicsState();
    }

    /**
     * Concats the transformation matrix.
     * @param a A part
     * @param b B part
     * @param c C part
     * @param d D part
     * @param e E part
     * @param f F part
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(double a, double b,
            double c, double d,
            double e, double f) throws IOException {
        AffineTransform at = new AffineTransform(a, b, c, d, e, f);
        concatMatrix(at);

    }

    /**
     * Concats the transformations matrix.
     * @param matrix Matrix to use
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(double[] matrix) throws IOException {
        concatMatrix(matrix[0], matrix[1],
                     matrix[2], matrix[3],
                     matrix[4], matrix[5]);
    }

    /**
     * Formats a transformation matrix.
     * @param at the AffineTransform with the matrix
     * @return the formatted transformation matrix (example: "[1 0 0 1 0 0]")
     */
    public String formatMatrix(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        return "[" + formatDouble5(matrix[0]) + " "
            + formatDouble5(matrix[1]) + " "
            + formatDouble5(matrix[2]) + " "
            + formatDouble5(matrix[3]) + " "
            + formatDouble5(matrix[4]) + " "
            + formatDouble5(matrix[5]) + "]";
    }

    /**
     * Concats the transformations matric.
     * @param at the AffineTransform whose matrix to use
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(AffineTransform at) throws IOException {
        getCurrentState().concatMatrix(at);
        writeln(formatMatrix(at) + " " + mapCommand("concat"));
    }

    /**
     * Formats a Rectangle2D to an array.
     * @param rect the rectangle
     * @return the formatted array
     */
    public String formatRectangleToArray(Rectangle2D rect) {
        return "[" + formatDouble(rect.getX()) + " "
        + formatDouble(rect.getY()) + " "
        + formatDouble(rect.getWidth()) + " "
        + formatDouble(rect.getHeight()) + "]";
    }

    /**
     * Adds a rectangle to the current path.
     * @param x upper left corner
     * @param y upper left corner
     * @param w width
     * @param h height
     * @exception IOException In case of an I/O problem
     */
    public void defineRect(double x, double y, double w, double h)
                throws IOException {
        writeln(formatDouble(x)
            + " " + formatDouble(y)
            + " " + formatDouble(w)
            + " " + formatDouble(h)
            + " re");
    }

    /**
     * Establishes the specified line cap style.
     * @param linecap the line cap style (0, 1 or 2) as defined by the setlinecap command.
     * @exception IOException In case of an I/O problem
     */
    public void useLineCap(int linecap) throws IOException {
        if (getCurrentState().useLineCap(linecap)) {
            writeln(linecap + " " + mapCommand("setlinecap"));
        }
    }

    /**
     * Establishes the specified line join style.
     * @param linejoin the line join style (0, 1 or 2) as defined by the setlinejoin command.
     * @exception IOException In case of an I/O problem
     */
    public void useLineJoin(int linejoin) throws IOException {
        if (getCurrentState().useLineJoin(linejoin)) {
            writeln(linejoin + " " + mapCommand("setlinejoin"));
        }
    }

    /**
     * Establishes the specified miter limit.
     * @param miterlimit the miter limit as defined by the setmiterlimit command.
     * @exception IOException In case of an I/O problem
     */
    public void useMiterLimit(float miterlimit) throws IOException {
        if (getCurrentState().useMiterLimit(miterlimit)) {
            writeln(miterlimit + " " + mapCommand("setmiterlimit"));
        }
    }

    /**
     * Establishes the specified line width.
     * @param width the line width as defined by the setlinewidth command.
     * @exception IOException In case of an I/O problem
     */
    public void useLineWidth(double width) throws IOException {
        if (getCurrentState().useLineWidth(width)) {
            writeln(formatDouble(width) + " " + mapCommand("setlinewidth"));
        }
    }

    /**
     * Establishes the specified dash pattern.
     * @param pattern the dash pattern as defined by the setdash command.
     * @exception IOException In case of an I/O problem
     */
    public void useDash(String pattern) throws IOException {
        if (pattern == null) {
            pattern = PSState.DEFAULT_DASH;
        }
        if (getCurrentState().useDash(pattern)) {
            writeln(pattern + " " + mapCommand("setdash"));
        }
    }

    /**
     * Establishes the specified color (RGB).
     * @param col the color as defined by the setrgbcolor command.
     * @exception IOException In case of an I/O problem
     * @deprecated use useColor method instead
     */
    @Deprecated
    public void useRGBColor(Color col) throws IOException {
        useColor(col);
    }

    /**
     * Establishes the specified color.
     * @param col the color.
     * @exception IOException In case of an I/O problem
     */
    public void useColor(Color col) throws IOException {
        if (getCurrentState().useColor(col)) {
            writeln(convertColorToPS(col));
        }
    }

    private String convertColorToPS(Color color) {
        StringBuffer codeBuffer = new StringBuffer();

        //Important: Right now, CMYK colors are treated as device colors (DeviceCMYK) irrespective
        //of any associated color profile. All other colors are converted to sRGB (if necessary)
        //and the resulting RGB components are treated as DeviceRGB colors.
        //If all three RGB components are the same, DeviceGray is used.

        boolean established = false;
        if (color instanceof ColorWithAlternatives) {
            ColorWithAlternatives colExt = (ColorWithAlternatives)color;
            //Alternative colors have priority
            Color[] alt = colExt.getAlternativeColors();
            for (Color col : alt) {
                established = establishColorFromColor(codeBuffer, col);
                if (established) {
                    break;
                }
            }
            if (log.isDebugEnabled() && alt.length > 0) {
                log.debug("None of the alternative colors are supported. Using fallback: "
                        + color);
            }
        }

        //Fallback
        if (!established) {
            established = establishColorFromColor(codeBuffer, color);
        }
        if (!established) {
            establishFallbackRGB(codeBuffer, color);
        }

        return codeBuffer.toString();
    }

    private boolean establishColorFromColor(StringBuffer codeBuffer, Color color) {
        //Important: see above note about color handling!
        float[] comps = color.getColorComponents(null);
        if (color.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            // colorspace is CMYK
            writeSetColor(codeBuffer, comps, "setcmykcolor");
            return true;
        }
        return false;
    }

    private void writeSetColor(StringBuffer codeBuffer, float[] comps, String command) {
        for (int i = 0, c = comps.length; i < c; i++) {
            if (i > 0) {
                codeBuffer.append(" ");
            }
            codeBuffer.append(formatDouble(comps[i]));
        }
        codeBuffer.append(" ").append(mapCommand(command));
    }

    private void establishFallbackRGB(StringBuffer codeBuffer, Color color) {
        float[] comps;
        if (color.getColorSpace().isCS_sRGB()) {
            comps = color.getColorComponents(null);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Converting color to sRGB as a fallback: " + color);
            }
            ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            comps = color.getColorComponents(sRGB, null);
        }
        assert comps.length == 3;
        boolean gray = ColorUtil.isGray(color);
        if (gray) {
            comps = new float[] {comps[0]};
        }
        writeSetColor(codeBuffer, comps, gray ? "setgray" : "setrgbcolor");
    }

    /**
     * Establishes the specified font and size.
     * @param name name of the font for the "F" command (see FOP Std Proc Set)
     * @param size size of the font
     * @exception IOException In case of an I/O problem
     */
    public void useFont(String name, float size) throws IOException {
        if (getCurrentState().useFont(name, size)) {
            writeln(name + " " + formatDouble(size) + " F");
        }
    }

    private ResourceTracker resTracker = new ResourceTracker();

    /**
     * Resturns the ResourceTracker instance associated with this PSGenerator.
     * @return the ResourceTracker instance or null if none is assigned
     */
    public ResourceTracker getResourceTracker() {
        return this.resTracker;
    }

    /**
     * Sets the ResourceTracker instance to be associated with this PSGenerator.
     * @param resTracker the ResourceTracker instance
     */
    public void setResourceTracker(ResourceTracker resTracker) {
        this.resTracker = resTracker;
    }

    /**
     * Notifies the generator that a new page has been started and that the page resource
     * set can be cleared.
     * @deprecated Use the notifyStartNewPage() on ResourceTracker instead.
     */
    @Deprecated
    public void notifyStartNewPage() {
        getResourceTracker().notifyStartNewPage();
    }

    /**
     * Notifies the generator about the usage of a resource on the current page.
     * @param res the resource being used
     * @param needed true if this is a needed resource, false for a supplied resource
     * @deprecated Use the notifyResourceUsageOnPage() on ResourceTracker instead
     */
    @Deprecated
    public void notifyResourceUsage(PSResource res, boolean needed) {
        getResourceTracker().notifyResourceUsageOnPage(res);
    }

    /**
     * Writes a DSC comment for the accumulated used resources, either at page level or
     * at document level.
     * @param pageLevel true if the DSC comment for the page level should be generated,
     *                  false for the document level (in the trailer)
     * @exception IOException In case of an I/O problem
     * @deprecated Use the writeResources() on ResourceTracker instead.
     */
    @Deprecated
    public void writeResources(boolean pageLevel) throws IOException {
        getResourceTracker().writeResources(pageLevel, this);
    }

    /**
     * Indicates whether a particular resource is supplied, rather than needed.
     * @param res the resource
     * @return true if the resource is registered as being supplied.
     * @deprecated Use the isResourceSupplied() on ResourceTracker instead.
     */
    @Deprecated
    public boolean isResourceSupplied(PSResource res) {
        return getResourceTracker().isResourceSupplied(res);
    }

    /**
     * Embeds the Identity-H CMap file into the output stream, if that has not
     * already been done.
     *
     * @return true if embedding has actually been performed, false otherwise
     * (which means that a call to this method had already been made earlier)
     * @throws IOException in case of an I/O problem
     */
    public boolean embedIdentityH() throws IOException {
        if (identityHEmbedded) {
            return false;
        } else {
            resTracker.registerNeededResource(getProcsetCIDInitResource());
            writeDSCComment(DSCConstants.BEGIN_DOCUMENT, IDENTITY_H);
            InputStream cmap = PSGenerator.class.getResourceAsStream(IDENTITY_H);
            try {
                IOUtils.copyLarge(cmap, out);
            } finally {
                IOUtils.closeQuietly(cmap);
            }
            writeDSCComment(DSCConstants.END_DOCUMENT);
            resTracker.registerSuppliedResource(getIdentityHCMapResource());
            identityHEmbedded = true;
            return true;
        }
    }

    /**
     * Returns the PSResource instance corresponding to the Identity-H CMap
     * resource.
     *
     * @return the Identity-H CMap resource.
     */
    public PSResource getIdentityHCMapResource() {
        if (identityHCMapResource == null) {
            identityHCMapResource = new PSResource(PSResource.TYPE_CMAP, IDENTITY_H);
        }
        return identityHCMapResource;
    }

    /**
     * Returns the PSResource instance corresponding to the CIDInit ProcSet
     * resource.
     *
     * @return the ProcSet CIDInit resource
     */
    public PSResource getProcsetCIDInitResource() {
        if (procsetCIDInitResource == null) {
            procsetCIDInitResource = new PSResource(PSResource.TYPE_PROCSET, "CIDInit");
        }
        return procsetCIDInitResource;
    }

    /**
     * Adds a PostScript DSC comment to the output stream requiring the
     * inclusion of the CIDInit ProcSet resource.
     *
     * @throws IOException in case of an I/O problem
     */
    public void includeProcsetCIDInitResource() throws IOException {
        writeDSCComment(DSCConstants.INCLUDE_RESOURCE, getProcsetCIDInitResource());
    }

}
