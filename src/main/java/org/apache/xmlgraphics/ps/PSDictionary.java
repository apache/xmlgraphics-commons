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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

// CSOFF: InnerAssignment

/**
 * This class is used to encapsulate postscript dictionary objects.
 */
public class PSDictionary extends java.util.HashMap {

    private static final long serialVersionUID = 815367222496219197L;

    /**
     * This class is used to parse dictionary strings.
     */
    private static class Maker {

        /**
         * Simple token holding class
         */
        private static class Token {
            /**
             * start index in string
             */
            private int startIndex = -1;

            /**
             * end index in string
             */
            private int endIndex = -1;

            /**
             * token string value
             */
            private String value;
        }

        private static final String[][] BRACES = {
            {"<<", ">>"},
            {"[", "]"},
            {"{", "}"},
            {"(", ")"}
        };

        private static final int OPENING = 0;
        private static final int CLOSING = 1;
        private static final int DICTIONARY = 0;
        private static final int ARRAY = 1;
        private static final int PROCEDURE = 2;
        private static final int STRING = 3;

        /**
         * Returns a Token containing the start, end index and value of the next token
         * found in a given string
         *
         * @param str
         *            string to search
         * @param fromIndex
         *            search from index
         * @return Token containing the start, end index and value of the next token
         */
        protected Token nextToken(String str, int fromIndex) {
            Token t = null;
            for (int i = fromIndex; i < str.length(); i++) {
                boolean isWhitespace = Character.isWhitespace(str.charAt(i));
                // start index found
                if (t == null && !isWhitespace) {
                    t = new Token();
                    t.startIndex = i;
                // end index found
                } else if (t != null && isWhitespace) {
                    t.endIndex = i;
                    break;
                }
            }
            // start index found
            if (t != null) {
                // end index not found so take end of string
                if (t.endIndex == -1) {
                    t.endIndex = str.length();
                }
                t.value = str.substring(t.startIndex, t.endIndex);
            }
            return t;
        }

        /**
         * Returns the closing brace index from a given string searches from a
         * given index
         *
         * @param str
         *            string to search
         * @param braces
         *            string array of opening and closing brace
         * @param fromIndex
         *            searches from index
         * @return matching brace index
         * @throws org.apache.xmlgraphics.ps.PSDictionaryFormatException
         *            thrown in the event that a parsing error occurred
         */
        private int indexOfMatchingBrace(String str, String[] braces,
                int fromIndex) throws PSDictionaryFormatException {
            final int len = str.length();
            if (braces.length != 2) {
                throw new PSDictionaryFormatException("Wrong number of braces");
            }
            for (int openCnt = 0, closeCnt = 0; fromIndex < len; fromIndex++) {
                if (str.startsWith(braces[OPENING], fromIndex)) {
                    openCnt++;
                } else if (str.startsWith(braces[CLOSING], fromIndex)) {
                    closeCnt++;
                    if (openCnt > 0 && openCnt == closeCnt) {
                        return fromIndex; // found
                    }
                }
            }
            return -1; // not found
        }

        /**
         * Strips braces from complex object string
         *
         * @param str
         *            String to parse
         * @param braces
         *            String array containing opening and closing braces
         * @return String with braces stripped
         * @throws org.apache.xmlgraphics.ps.PSDictionaryFormatException
         *            thrown in the event that a parsing error occurred
         */
        private String stripBraces(String str, String[] braces) throws PSDictionaryFormatException {
            // find first opening brace
            int firstIndex = str.indexOf(braces[OPENING]);
            if (firstIndex == -1) {
                throw new PSDictionaryFormatException(
                        "Failed to find opening parameter '" + braces[OPENING]
                                + "");
            }

            // find last matching brace
            int lastIndex = indexOfMatchingBrace(str, braces, firstIndex);
            if (lastIndex == -1) {
                throw new PSDictionaryFormatException(
                        "Failed to find matching closing parameter '"
                                + braces[CLOSING] + "'");
            }

            // strip brace and trim
            int braceLen = braces[OPENING].length();
            str = str.substring(firstIndex + braceLen, lastIndex).trim();
            return str;
        }

        /**
         * Parses a dictionary string and provides a dictionary object
         *
         * @param str a dictionary string
         * @return A postscript dictionary object
         * @throws org.apache.xmlgraphics.ps.PSDictionaryFormatException
         *            thrown in the event that a parsing error occurred
         */
        public PSDictionary parseDictionary(String str) throws PSDictionaryFormatException {
            PSDictionary dictionary = new PSDictionary();
            str = stripBraces(str.trim(), BRACES[DICTIONARY]);
            // length of dictionary string
            final int len = str.length();

            Token keyToken;
            for (int currIndex = 0; (keyToken = nextToken(str, currIndex)) != null
                    && currIndex <= len;) {
                if (keyToken.value == null) {
                    throw new PSDictionaryFormatException("Failed to parse object key");
                }
                Token valueToken = nextToken(str, keyToken.endIndex + 1);
                String[] braces = null;
                for (String[] brace : BRACES) {
                    if (valueToken.value.startsWith(brace[OPENING])) {
                        braces = brace;
                        break;
                    }
                }
                Object obj = null;
                if (braces != null) {
                    // find closing brace
                    valueToken.endIndex = indexOfMatchingBrace(str, braces,
                        valueToken.startIndex)
                        + braces[OPENING].length();
                    if (valueToken.endIndex < 0) {
                        throw new PSDictionaryFormatException("Closing value brace '"
                            + braces[CLOSING] + "' not found for key '"
                            + keyToken.value + "'");
                    }
                    valueToken.value = str.substring(valueToken.startIndex, valueToken.endIndex);
                }
                if (braces == null || braces == BRACES[PROCEDURE] || braces == BRACES[STRING]) {
                    obj = valueToken.value;
                } else if (BRACES[ARRAY] == braces) {
                    List objList = new java.util.ArrayList();
                    String objString = stripBraces(valueToken.value, braces);
                    StringTokenizer tokenizer = new StringTokenizer(objString, ",");
                    while (tokenizer.hasMoreTokens()) {
                        objList.add(tokenizer.nextToken());
                    }
                    obj = objList;
                } else if (BRACES[DICTIONARY] == braces) {
                    obj = parseDictionary(valueToken.value);
                }
                dictionary.put(keyToken.value, obj);
                currIndex = valueToken.endIndex + 1;
            }
            return dictionary;
        }
    }

    /**
     * Parses a given a dictionary string and returns an object
     *
     * @param str dictionary string
     * @return dictionary object
     * @throws org.apache.xmlgraphics.ps.PSDictionaryFormatException
     *            thrown in the event that a parsing error occurred
     */
    public static PSDictionary valueOf(String str) throws PSDictionaryFormatException {
        return (new Maker()).parseDictionary(str);
    }

    /**
     * @param obj object to test equality against
     * @return whether a given object is equal to this dictionary object
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PSDictionary)) {
            return false;
        }
        PSDictionary dictionaryObj = (PSDictionary) obj;
        if (dictionaryObj.size() != size()) {
            return false;
        }
        for (Object e : entrySet()) {
            Map.Entry entry = (Map.Entry) e;
            String key = (String) entry.getKey();
            if (!dictionaryObj.containsKey(key)) {
                return false;
            }
            if (!dictionaryObj.get(key).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int hashCode = 7;
        for (Object value : values()) {
            hashCode += value.hashCode();
        }
        return hashCode;
    }

    /** {@inheritDoc} */
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer("<<\n");
        for (Object o : super.keySet()) {
            String key = (String) o;
            sb.append("  " + key + " ");
            Object obj = super.get(key);
            if (obj instanceof ArrayList) {
                List array = (List) obj;
                StringBuilder str = new StringBuilder("[");
                for (Object element : array) {
                    str.append(element + " ");
                }
                String str2 = str.toString().trim();
                str2 += "]";
                sb.append(str2 + "\n");
            } else {
                sb.append(obj.toString() + "\n");
            }
        }
        sb.append(">>");
        return sb.toString();
    }
}
