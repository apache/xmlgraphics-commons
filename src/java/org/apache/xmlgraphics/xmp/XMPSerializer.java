/*
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.xmlgraphics.xmp;

import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

/**
 * Serializes an XMP tree to XML or to an XMP packet.
 */
public class XMPSerializer {

    private static final String DEFAULT_ENCODING = "UTF-8";
    
    /**
     * Writes the in-memory representation of the XMP metadata to a JAXP Result.
     * @param meta the metadata
     * @param res the JAXP Result to write to
     * @throws TransformerConfigurationException if an error occurs setting up the XML 
     *              infrastructure.
     * @throws SAXException if a SAX-related problem occurs while writing the XML
     */
    public static void writeXML(Metadata meta, Result res) 
            throws TransformerConfigurationException, SAXException {
        writeXML(meta, res, false, false);
    }
    
    /**
     * Writes the in-memory representation of the XMP metadata to an OutputStream as an XMP packet.
     * @param meta the metadata
     * @param out the stream to write to
     * @param readOnlyXMP true if the generated XMP packet should be read-only
     * @throws TransformerConfigurationException if an error occurs setting up the XML 
     *              infrastructure.
     * @throws SAXException if a SAX-related problem occurs while writing the XML
     */
    public static void writeXMPPacket(Metadata meta, OutputStream out, boolean readOnlyXMP) 
            throws TransformerConfigurationException, SAXException {
        StreamResult res = new StreamResult(out);
        writeXML(meta, res, true, readOnlyXMP);
        
    }
    
    private static void writeXML(Metadata meta, Result res, 
                    boolean asXMPPacket, boolean readOnlyXMP) 
                            throws TransformerConfigurationException, SAXException {
        SAXTransformerFactory tFactory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        TransformerHandler handler = tFactory.newTransformerHandler();
        Transformer transformer = handler.getTransformer();
        if (asXMPPacket) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, DEFAULT_ENCODING);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        handler.setResult(res);
        handler.startDocument();
        if (asXMPPacket) {
            handler.processingInstruction("xpacket", 
                    "begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"");
        }
        meta.toSAX(handler);
        if (asXMPPacket) {
            if (readOnlyXMP) {
                handler.processingInstruction("xpacket", "end=\"r\"");
            } else {
                //Create padding string (40 * 101 characters is more or less the recommended 4KB)
                StringBuffer sb = new StringBuffer(101);
                sb.append('\n');
                for (int i = 0; i < 100; i++) {
                    sb.append(" ");
                }
                char[] padding = sb.toString().toCharArray();
                for (int i = 0; i < 40; i++) {
                    handler.characters(padding, 0, padding.length);
                }
                handler.characters(new char[] {'\n'}, 0, 1);
                handler.processingInstruction("xpacket", "end=\"w\"");
            }

        }
        handler.endDocument();
    }
    
}
