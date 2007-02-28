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

package org.apache.xmlgraphics.ps.dsc.events;

import java.util.List;

/**
 * Abstract base class for DSC comments.
 */
public abstract class AbstractDSCComment extends AbstractEvent implements DSCComment {

    private final boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }
    
    private int parseNextParam(String value, int pos, List lst) {
        int startPos = pos;
        pos++;
        while (pos < value.length() && !isWhitespace(value.charAt(pos))) {
            pos++;
        }
        String param = value.substring(startPos, pos);
        lst.add(param);
        return pos;
    }
    
    private int parseNextParentheseString(String value, int pos, List lst) {
        int nestLevel = 1;
        pos++;
        StringBuffer sb = new StringBuffer();
        while (pos < value.length() && nestLevel > 0) {
            final char c = value.charAt(pos);
            switch (c) {
            case '(':
                nestLevel++;
                if (nestLevel > 1) {
                    sb.append(c);
                }
                break;
            case ')':
                if (nestLevel > 1) {
                    sb.append(c);
                }
                nestLevel--;
                break;
            case '\\':
                pos++;
                char cnext = value.charAt(pos);
                switch (cnext) {
                case '\\':
                    sb.append(cnext);
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case '(':
                    sb.append('(');
                    break;
                case ')':
                    sb.append(')');
                    break;
                default:
                    int code = Integer.parseInt(value.substring(pos, pos + 3), 8);
                    sb.append((char)code);
                    pos += 2;
                }
                break;
            default:
                sb.append(c);
            }
            pos++;
        }
        lst.add(sb.toString());
        pos++;
        return pos;
    }
    
    /**
     * Splits the params of the DSC comment value in to a List.
     * @param value the DSC comment value
     * @return the List of values
     */
    protected List splitParams(String value) {
        List lst = new java.util.ArrayList();
        int pos = 0;
        value = value.trim();
        while (pos < value.length()) {
            if (isWhitespace(value.charAt(pos))) {
                pos++;
                continue;
            }
            if (value.charAt(pos) == '(') {
                pos = parseNextParentheseString(value, pos, lst);
            } else {
                pos = parseNextParam(value, pos, lst);
            }
        }
        return lst;
    }
    
    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCComment#isAtend()
     */
    public boolean isAtend() {
        return false;
    }
    
    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#asDSCComment()
     */
    public DSCComment asDSCComment() {
        return this;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.AbstractEvent#isDSCComment()
     */
    public boolean isDSCComment() {
        return true;
    }

    /**
     * @see org.apache.xmlgraphics.ps.dsc.events.DSCEvent#getEventType()
     */
    public int getEventType() {
        return DSC_COMMENT;
    }
}
