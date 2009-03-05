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

/**
 * PSResource subclass that represents a ProcSet resource.
 */
public class PSProcSet extends PSResource {

    private float version;
    private int revision;

    /**
     * Creates a new instance.
     * @param name name of the resource
     */
    public PSProcSet(String name) {
        this(name, 1.0f, 0);
    }

    /**
     * Creates a new instance.
     * @param name name of the resource
     * @param version version of the resource
     * @param revision revision of the resource
     */
    public PSProcSet(String name, float version, int revision) {
        super(TYPE_PROCSET, name);
        this.version = version;
        this.revision = revision;
    }

    /** @return the version */
    public float getVersion() {
        return version;
    }

    /** @return the revision */
    public int getRevision() {
        return revision;
    }

    /** @return the <resource> specification as defined in DSC v3.0 spec. */
    public String getResourceSpecification() {
        StringBuffer sb = new StringBuffer();
        sb.append(getType()).append(" ").append(PSGenerator.convertStringToDSC(getName()));
        sb.append(" ").append(PSGenerator.convertRealToDSC(getVersion()));
        sb.append(" ").append(Integer.toString(getRevision()));
        return sb.toString();
    }

}
