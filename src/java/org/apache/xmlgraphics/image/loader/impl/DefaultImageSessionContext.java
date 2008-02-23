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
 
package org.apache.xmlgraphics.image.loader.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlgraphics.image.loader.ImageContext;

/**
 * Very simple implementation of the ImageSessionContext interface. It works for absolute URLs
 * and local filenames only. Consider writing your own implementation of the ImageSessionContext
 * if you need more sophisticated functionality.
 */
public class DefaultImageSessionContext extends AbstractImageSessionContext {

    private ImageContext context;
    private File baseDir;
    
    /**
     * Main constructor.
     * @param context the parent image context
     * @param baseDir the base directory for resolving relative filenames
     */
    public DefaultImageSessionContext(ImageContext context, File baseDir) {
        this.context = context;
        this.baseDir = baseDir;
    }
    
    /** {@inheritDoc} */
    public ImageContext getParentContext() {
        return this.context;
    }
    
    /**
     * Returns the base directory for resolving relative filenames.
     * @return the base directory
     */
    public File getBaseDir() {
        return this.baseDir;
    }

    /** {@inheritDoc} */
    protected Source resolveURI(String uri) {
        try {
            URL url = new URL(uri);
            return new StreamSource(url.openStream(), url.toExternalForm());
        } catch (MalformedURLException e) {
            File f = new File(baseDir, uri);
            if (f.isFile()) {
                return new StreamSource(f);
            } else {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }
    }

    /** {@inheritDoc} */
    public float getTargetResolution() {
        return getParentContext().getSourceResolution(); //same as source resolution
    }

}
