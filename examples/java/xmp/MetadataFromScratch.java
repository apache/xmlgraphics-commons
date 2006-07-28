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

import java.util.Date;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSerializer;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.xml.sax.SAXException;

/**
 * This example shows how to build an XMP metadata file from scratch in Java.
 */
public class MetadataFromScratch {

    private static void buildAndPrintMetadata() 
                throws TransformerConfigurationException, SAXException {
        Metadata meta = new Metadata();
        DublinCoreAdapter dc = new DublinCoreAdapter(meta);
        dc.setTitle("de", "Der Herr der Ringe");
        dc.setTitle("en", "Lord of the Rings");
        dc.addDate(new Date());
        
        StreamResult res = new StreamResult(System.out);
        XMPSerializer.writeXML(meta, res);
        
    }

    /**
     * Command-line interface.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            buildAndPrintMetadata();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
