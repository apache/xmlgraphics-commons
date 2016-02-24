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

import java.net.URL;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPConstants;
import org.apache.xmlgraphics.xmp.XMPParser;
import org.apache.xmlgraphics.xmp.XMPProperty;
import org.apache.xmlgraphics.xmp.XMPSerializer;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.xml.sax.SAXException;

/**
 * This example shows how to parse an XMP metadata file.
 */
public class MergeMetadata {

    private static void mergeMetadata() throws TransformerException, SAXException {
        URL url = MergeMetadata.class.getResource("pdf-example.xmp");
        Metadata meta1 = XMPParser.parseXMP(url);

        Metadata meta2 = new Metadata();
        DublinCoreAdapter dc = new DublinCoreAdapter(meta2);
        dc.setTitle("de", "Der Herr der Ringe");
        dc.setTitle("en", "Lord of the Rings");
        dc.addCreator("J.R.R. Tolkien"); //Will replace creator from pdf-example.xmp
        dc.addDate(new Date());

        meta2.mergeInto(meta1);

        Metadata meta = meta1;
        XMPProperty prop;
        dc = new DublinCoreAdapter(meta);
        String[] creators = dc.getCreators();
        for (int i = 0, c = creators.length; i < c; i++) {
            System.out.println("Creator: " + creators[i]);
        }
        System.out.println("Title: " + dc.getTitle());
        System.out.println("Title de: " + dc.getTitle("de"));
        System.out.println("Title en: " + dc.getTitle("en"));
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreateDate");
        System.out.println("Creation Date: " + prop.getValue());
        prop = meta.getProperty(XMPConstants.XMP_BASIC_NAMESPACE, "CreatorTool");
        System.out.println("Creator Tool: " + prop.getValue());
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "Producer");
        System.out.println("Producer: " + prop.getValue());
        prop = meta.getProperty(XMPConstants.ADOBE_PDF_NAMESPACE, "PDFVersion");
        System.out.println("PDF version: " + prop.getValue());

        XMPSerializer.writeXMPPacket(meta, System.out, false);
    }

    /**
     * Command-line interface.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            mergeMetadata();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
