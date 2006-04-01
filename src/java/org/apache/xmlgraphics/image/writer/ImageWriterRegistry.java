/*

   Copyright 2006  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.xmlgraphics.image.writer;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.apache.xmlgraphics.util.Service;

/**
 * Registry for ImageWriter implementations. They are primarily registered through the "Service
 * Provider" mechanism.
 * @see org.apache.xmlgraphics.util.Service
 */
public class ImageWriterRegistry {

    private static ImageWriterRegistry instance;
    
    private Map imageWriterMap = new HashMap();
    
    private ImageWriterRegistry() {
        setup();
    }
    
    /** @return a singleton instance of the ImageWriterRegistry. */
    public static ImageWriterRegistry getInstance() {
        if (instance == null) {
            instance = new ImageWriterRegistry();
        }
        return instance;
    }
    
    private void setup() {
        Iterator iter = Service.providers(ImageWriter.class);
        while (iter.hasNext()) {
            ImageWriter writer = (ImageWriter)iter.next();
            register(writer);
        }
    }
    
    /**
     * Registers a new ImageWriter implementation in the registry. If an ImageWriter for the same
     * target MIME type has already been registered, it is overwritten with the new one.
     * @param writer the ImageWriter instance to register.
     */
    public void register(ImageWriter writer) {
        imageWriterMap.put(writer.getMIMEType(), writer);
    }
    
    /**
     * Returns an ImageWriter that can be used to encode an image to the requested MIME type.
     * @param mime the MIME type of the desired output format
     * @return an ImageWriter instance handling the desired output format or null if none can be
     *         found.
     */
    public ImageWriter getWriterFor(String mime) {
        return (ImageWriter)imageWriterMap.get(mime);
    }
    
}
