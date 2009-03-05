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
import java.io.OutputStream;

/**
 * The interface is implemented by classes that can generate the raw bitmap field for an image
 * that might be further encoded/compressed by the image handler class.
 */
public interface ImageEncoder {

    /**
     * Writes the whole raw bitmap field to the given OutputStream. The implementation must not
     * close the OutputStream when it is finished!
     * @param out the OutputStream to write to
     * @throws IOException if an I/O error occurs
     */
    void writeTo(OutputStream out) throws IOException;

    String getImplicitFilter();

}
