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

package xmp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.TransformerException;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPArray;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPPacketParser;
import org.apache.xmlgraphics.xmp.XMPProperty;

/**
 * This example shows how to parse an XMP packet from an arbitrary file.
 */
public class ExtractMetadataPacket {

    private static void parseMetadata() throws IOException, TransformerException {
        URL url = ExtractMetadataPacket.class.getResource("xmp-sandbox.fop.trunk.pdf");
        InputStream in = url.openStream();
        try {
            Metadata meta = XMPPacketParser.parse(in);
            if (meta == null) {
                System.err.println("No XMP packet found!");
            } else {
                dumpSomeMetadata(meta);
            }
        } finally {
            in.close();
        }
    }

    private static void dumpSomeMetadata(Metadata meta) {
        XMPProperty prop;
        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "creator");
        if (prop != null) {
            XMPArray array;
            array = prop.getArrayValue();
            for (int i = 0, c = array.getSize(); i < c; i++) {
                System.out.println("Creator: " + array.getValue(i));
            }
        }
        prop = meta.getProperty(XMPConstants.DUBLIN_CORE_NAMESPACE, "title");
        if (prop != null) {
            System.out.println("Title: " + prop.getValue());
        }
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreateDate");
        if (prop != null) {
            System.out.println("Creation Date: " + prop.getValue());
        }
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreatorTool");
        if (prop != null) {
            System.out.println("Creator Tool: " + prop.getValue());
        }
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "Producer");
        if (prop != null) {
            System.out.println("Producer: " + prop.getValue());
        }
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "PDFVersion");
        if (prop != null) {
            System.out.println("PDF version: " + prop.getValue());
        }
    }

    /**
     * Command-line interface.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            parseMetadata();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
