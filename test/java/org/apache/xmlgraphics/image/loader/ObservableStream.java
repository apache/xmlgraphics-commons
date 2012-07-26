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

package org.apache.xmlgraphics.image.loader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implemented by observable streams.
 */
public interface ObservableStream {

    /**
     * Indicates whether the stream has been closed.
     * @return true if the stream is closed
     */
    boolean isClosed();

    /**
     * Returns the system ID for the stream being observed.
     * @return the system ID
     */
    String getSystemID();

    public static class Factory {

        public static ImageInputStream observe(ImageInputStream iin, String systemID) {
            return (ImageInputStream) Proxy.newProxyInstance(
                    Factory.class.getClassLoader(),
                    new Class[] {ImageInputStream.class, ObservableStream.class},
                    new ObservingImageInputStreamInvocationHandler(iin, systemID));
        }

    }

    public static class ObservingImageInputStreamInvocationHandler
            implements InvocationHandler, ObservableStream {

        /** logger */
        protected static Log log = LogFactory.getLog(ObservableInputStream.class);

        private ImageInputStream iin;
        private boolean closed;
        private String systemID;

        public ObservingImageInputStreamInvocationHandler(ImageInputStream iin, String systemID) {
            this.iin = iin;
            this.systemID = systemID;
        }

        /** {@inheritDoc} */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(ObservableStream.class)) {
                return method.invoke(this, args);
            } else if ("close".equals(method.getName())) {
                if (!closed) {
                    log.debug("Stream is being closed: " + getSystemID());
                    closed = true;
                    try {
                        return method.invoke(iin, args);
                    } catch (InvocationTargetException ite) {
                        log.error("Error while closing underlying stream: " + ite.getMessage());
                        throw ite;
                    }
                } else {
                    throw new IllegalStateException("Stream is already closed!");
                }
            } else {
                return method.invoke(iin, args);
            }
        }

        /** {@inheritDoc} */
        public String getSystemID() {
            return this.systemID;
        }

        /** {@inheritDoc} */
        public boolean isClosed() {
            return this.closed;
        }

    }

}
