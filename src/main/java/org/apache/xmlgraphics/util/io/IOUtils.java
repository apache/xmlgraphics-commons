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

package org.apache.xmlgraphics.util.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Utility class providing convenience methods for I/O operations.
 */
public final class IOUtils {
    private IOUtils() {
    }

    /**
     * Copies all bytes from the provided input stream to the given output stream.
     * The method operates using a buffer and is useful for transferring data
     * between streams without modifying the contents.
     *
     * @param input  the {@code InputStream} to read data from, must not be {@code null}
     * @param output the {@code OutputStream} to write data to, must not be {@code null}
     * @return the total number of bytes copied
     * @throws IOException if an I/O error occurs during reading or writing
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        // TODO replace with input.transferTo(output) when Java 9+ is acceptable
        byte[] buffer = new byte[4096];
        long count = 0;
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

     /**
      * Converts the contents of the provided InputStream into a byte array.
      * This method reads all the data from the InputStream and writes it into
      * a ByteArrayOutputStream, which is then converted to a byte array.
      *
      * @param input the InputStream to read data from, must not be {@code null}
      * @return a byte array containing all the data read from the InputStream
      * @throws IOException if an I/O error occurs while reading from the InputStream
      */
     public static byte[] toByteArray(InputStream input) throws IOException {
         // TODO replace with InputStream.readAllBytes when Java 9+ is acceptable
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         copy(input, output);
         return output.toByteArray();
    }

    /**
     * Converts the contents of the provided Reader into a String.
     * This method reads characters from the Reader into a buffer and then
     * appends them to a StringBuilder, which is returned as a String.
     *
     * @param reader the Reader to read data from, must not be null
     * @return a String containing all the characters read from the Reader
     * @throws IOException if an I/O error occurs while reading from the Reader
     */
    public static String toString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char [] buf = new char[128];
        int n;
        while ((n = reader.read(buf)) > 0) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    /**
     * Closes a {@code Closeable} object quietly, suppressing any exceptions
     * that might occur during the close operation. If the provided {@code Closeable}
     * is {@code null}, this method does nothing.
     *
     * @param closeable the {@code Closeable} object to close; may be {@code null}.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
            //Ignore
        }
    }
}
