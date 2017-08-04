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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * This class defines the basic resources (procsets) used by the Apache XML Graphics project.
 *
 * @version $Id$
 */
public final class PSProcSets {

    private PSProcSets() {
    }

    /** the standard procset for the XML Graphics project */
    public static final PSResource STD_PROCSET;
    /** the EPS procset for the XML Graphics project */
    public static final PSResource EPS_PROCSET = new EPSProcSet();

    /** the standard command map matching the {@link #STD_PROCSET}. */
    public static final PSCommandMap STD_COMMAND_MAP;

    static {
        StdProcSet stdProcSet = new StdProcSet();
        STD_PROCSET = stdProcSet;
        STD_COMMAND_MAP = stdProcSet;
    }

    /**
     * The standard procset used by XML Graphics Commons.
     */
    private static class StdProcSet extends PSProcSet implements PSCommandMap {

        /** A Map<String, String> of standard shorthand macros defined in the {@link StdProcSet}. */
        private static final Map STANDARD_MACROS;

        static {
            Map macros = new java.util.HashMap();
            macros.put("moveto", "M");
            macros.put("rmoveto", "RM");
            macros.put("curveto", "C");
            macros.put("lineto", "L");
            macros.put("show", "t");
            macros.put("ashow", "A");
            macros.put("closepath", "cp");
            macros.put("setrgbcolor", "RC");
            macros.put("setgray", "GC");
            macros.put("setcmykcolor", "CC");
            macros.put("newpath", "N");
            macros.put("setmiterlimit", "ML");
            macros.put("setlinewidth", "LC");
            macros.put("setlinewidth", "LW");
            macros.put("setlinejoin", "LJ");
            macros.put("grestore", "GR");
            macros.put("gsave", "GS");
            macros.put("fill", "f");
            macros.put("stroke", "S");
            macros.put("concat", "CT");
            STANDARD_MACROS = Collections.unmodifiableMap(macros);
        }

        public StdProcSet() {
            super("Apache XML Graphics Std ProcSet", 1.2f, 0);
        }

        public void writeTo(PSGenerator gen) throws IOException {
            gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE,
                    new Object[] {TYPE_PROCSET, getName(),
                        Float.toString(getVersion()), Integer.toString(getRevision())});
            gen.writeDSCComment(DSCConstants.VERSION,
                    new Object[] {Float.toString(getVersion()), Integer.toString(getRevision())});
            gen.writeDSCComment(DSCConstants.COPYRIGHT, "Copyright 2001-2003,2010 "
                        + "The Apache Software Foundation. "
                        + "License terms: http://www.apache.org/licenses/LICENSE-2.0");
            gen.writeDSCComment(DSCConstants.TITLE,
                    "Basic set of procedures used by the XML Graphics project (Batik and FOP)");

            gen.writeln("/bd{bind def}bind def");
            gen.writeln("/ld{load def}bd");
            for (Object o : STANDARD_MACROS.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                gen.writeln("/" + entry.getValue() + "/" + entry.getKey() + " ld");
            }

            gen.writeln("/re {4 2 roll M"); //define rectangle
            gen.writeln("1 index 0 rlineto");
            gen.writeln("0 exch rlineto");
            gen.writeln("neg 0 rlineto");
            gen.writeln("cp } bd");

            gen.writeln("/_ctm matrix def"); //Holds the current matrix
            gen.writeln("/_tm matrix def");
            //BT: save currentmatrix, set _tm to identitymatrix and move to 0/0
            gen.writeln("/BT { _ctm currentmatrix pop matrix _tm copy pop 0 0 moveto } bd");
            //ET: restore last currentmatrix
            gen.writeln("/ET { _ctm setmatrix } bd");
            gen.writeln("/iTm { _ctm setmatrix _tm concat } bd");
            gen.writeln("/Tm { _tm astore pop iTm 0 0 moveto } bd");

            gen.writeln("/ux 0.0 def");
            gen.writeln("/uy 0.0 def");

            // <font> <size> F
            gen.writeln("/F {");
            gen.writeln("  /Tp exch def");
            // gen.writeln("  currentdict exch get");
            gen.writeln("  /Tf exch def");
            gen.writeln("  Tf findfont Tp scalefont setfont");
            gen.writeln("  /cf Tf def  /cs Tp def");
            gen.writeln("} bd");

            gen.writeln("/ULS {currentpoint /uy exch def /ux exch def} bd");
            gen.writeln("/ULE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add moveto  Tcx uy To add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");

            gen.writeln("/OLE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add cs add moveto Tcx uy To add cs add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");

            gen.writeln("/SOE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add cs 10 mul 26 idiv add moveto "
                        + "Tcx uy To add cs 10 mul 26 idiv add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");

            gen.writeln("/QT {");
            gen.writeln("/Y22 exch store");
            gen.writeln("/X22 exch store");
            gen.writeln("/Y21 exch store");
            gen.writeln("/X21 exch store");
            gen.writeln("currentpoint");
            gen.writeln("/Y21 load 2 mul add 3 div exch");
            gen.writeln("/X21 load 2 mul add 3 div exch");
            gen.writeln("/X21 load 2 mul /X22 load add 3 div");
            gen.writeln("/Y21 load 2 mul /Y22 load add 3 div");
            gen.writeln("/X22 load /Y22 load curveto");
            gen.writeln("} bd");

            gen.writeln("/SSPD {");
            gen.writeln("dup length /d exch dict def");
            gen.writeln("{");
            gen.writeln("/v exch def");
            gen.writeln("/k exch def");
            gen.writeln("currentpagedevice k known {");
            gen.writeln("/cpdv currentpagedevice k get def");
            gen.writeln("v cpdv ne {");
            gen.writeln("/upd false def");
            gen.writeln("/nullv v type /nulltype eq def");
            gen.writeln("/nullcpdv cpdv type /nulltype eq def");
            gen.writeln("nullv nullcpdv or");
            gen.writeln("{");
            gen.writeln("/upd true def");
            gen.writeln("} {");
            gen.writeln("/sametype v type cpdv type eq def");
            gen.writeln("sametype {");
            gen.writeln("v type /arraytype eq {");
            gen.writeln("/vlen v length def");
            gen.writeln("/cpdvlen cpdv length def");
            gen.writeln("vlen cpdvlen eq {");
            gen.writeln("0 1 vlen 1 sub {");
            gen.writeln("/i exch def");
            gen.writeln("/obj v i get def");
            gen.writeln("/cpdobj cpdv i get def");
            gen.writeln("obj cpdobj ne {");
            gen.writeln("/upd true def");
            gen.writeln("exit");
            gen.writeln("} if");
            gen.writeln("} for");
            gen.writeln("} {");
            gen.writeln("/upd true def");
            gen.writeln("} ifelse");
            gen.writeln("} {");
            gen.writeln("v type /dicttype eq {");
            gen.writeln("v {");
            gen.writeln("/dv exch def");
            gen.writeln("/dk exch def");
            gen.writeln("/cpddv cpdv dk get def");
            gen.writeln("dv cpddv ne {");
            gen.writeln("/upd true def");
            gen.writeln("exit");
            gen.writeln("} if");
            gen.writeln("} forall");
            gen.writeln("} {");
            gen.writeln("/upd true def");
            gen.writeln("} ifelse");
            gen.writeln("} ifelse");
            gen.writeln("} if");
            gen.writeln("} ifelse");
            gen.writeln("upd true eq {");
            gen.writeln("d k v put");
            gen.writeln("} if");
            gen.writeln("} if");
            gen.writeln("} if");
            gen.writeln("} forall");
            gen.writeln("d length 0 gt {");
            gen.writeln("d setpagedevice");
            gen.writeln("} if");
            gen.writeln("} bd");

            gen.writeln("/RE { % /NewFontName [NewEncodingArray] /FontName RE -");
            gen.writeln("  findfont dup length dict begin");
            gen.writeln("  {");
            gen.writeln("    1 index /FID ne");
            gen.writeln("    {def} {pop pop} ifelse");
            gen.writeln("  } forall");
            gen.writeln("  /Encoding exch def");
            gen.writeln("  /FontName 1 index def");
            gen.writeln("  currentdict definefont pop");
            gen.writeln("  end");
            gen.writeln("} bind def");

            gen.writeDSCComment(DSCConstants.END_RESOURCE);
            gen.getResourceTracker().registerSuppliedResource(this);
        }

        /** {@inheritDoc} */
        public String mapCommand(String command) {
            String mapped = (String)STANDARD_MACROS.get(command);
            return (mapped != null ? mapped : command);
        }

    }

    private static class EPSProcSet extends PSProcSet {

        public EPSProcSet() {
            super("Apache XML Graphics EPS ProcSet", 1.0f, 0);
        }

        public void writeTo(PSGenerator gen) throws IOException {
            gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE,
                    new Object[] {TYPE_PROCSET, getName(),
                        Float.toString(getVersion()), Integer.toString(getRevision())});
            gen.writeDSCComment(DSCConstants.VERSION,
                    new Object[] {Float.toString(getVersion()), Integer.toString(getRevision())});
            gen.writeDSCComment(DSCConstants.COPYRIGHT, "Copyright 2002-2003 "
                    + "The Apache Software Foundation. "
                    + "License terms: http://www.apache.org/licenses/LICENSE-2.0");
            gen.writeDSCComment(DSCConstants.TITLE,
                    "EPS procedures used by the Apache XML Graphics project (Batik and FOP)");

            gen.writeln("/BeginEPSF { %def");
            gen.writeln("/b4_Inc_state save def         % Save state for cleanup");
            gen.writeln("/dict_count countdictstack def % Count objects on dict stack");
            gen.writeln("/op_count count 1 sub def      % Count objects on operand stack");
            gen.writeln("userdict begin                 % Push userdict on dict stack");
            gen.writeln("/showpage { } def              % Redefine showpage, { } = null proc");
            gen.writeln("0 setgray 0 setlinecap         % Prepare graphics state");
            gen.writeln("1 setlinewidth 0 setlinejoin");
            gen.writeln("10 setmiterlimit [ ] 0 setdash newpath");
            gen.writeln("/languagelevel where           % If level not equal to 1 then");
            gen.writeln("{pop languagelevel             % set strokeadjust and");
            gen.writeln("1 ne                           % overprint to their defaults.");
            gen.writeln("{false setstrokeadjust false setoverprint");
            gen.writeln("} if");
            gen.writeln("} if");
            gen.writeln("} bd");

            gen.writeln("/EndEPSF { %def");
            gen.writeln("count op_count sub {pop} repeat            % Clean up stacks");
            gen.writeln("countdictstack dict_count sub {end} repeat");
            gen.writeln("b4_Inc_state restore");
            gen.writeln("} bd");

            gen.writeDSCComment(DSCConstants.END_RESOURCE);
            gen.getResourceTracker().registerSuppliedResource(this);
        }

    }

    /**
     * Generates a resource defining standard procset with operations used by the XML Graphics
     * project.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static void writeStdProcSet(PSGenerator gen) throws IOException {
        ((StdProcSet)STD_PROCSET).writeTo(gen);
    }

    /**
     * Generates a resource defining standard procset with operations used by the XML Graphics
     * project.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     * @deprecated Use writeStdProcSet() instead.
     */
    public static void writeFOPStdProcSet(PSGenerator gen) throws IOException {
        writeStdProcSet(gen);
    }


    /**
     * Generates a resource defining a procset for including EPS graphics.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static void writeEPSProcSet(PSGenerator gen) throws IOException {
        ((EPSProcSet)EPS_PROCSET).writeTo(gen);
    }

    /**
     * Generates a resource defining a procset for including EPS graphics.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     * @deprecated Use writeEPSProcSet() instead.
     */
    public static void writeFOPEPSProcSet(PSGenerator gen) throws IOException {
        writeEPSProcSet(gen);
    }


}
