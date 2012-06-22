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

package org.apache.xmlgraphics.image.loader.util;

/**
 * Immutable class representing a penalty value. It's valid value range is that of an
 * {@link Integer}, but giving {@link Integer#MAX_VALUE} a special meaning: it means infinite
 * penalty, i.e. a candidate with this penalty will be excluded from any choice.
 */
public final class Penalty {

    public static final Penalty ZERO_PENALTY = new Penalty(0);
    public static final Penalty INFINITE_PENALTY = new Penalty(Integer.MAX_VALUE);

    private final int value;

    /**
     * Turns a penalty value into a penaly object.
     * @param value the penalty value
     * @return the penalty object
     */
    public static Penalty toPenalty(int value) {
        switch (value) {
        case 0:
            return ZERO_PENALTY;
        case Integer.MAX_VALUE:
            return INFINITE_PENALTY;
        default:
            return new Penalty(value);
        }
    }

    private Penalty(int value) {
        this.value = value;
    }

    /**
     * Adds a penalty to this one and returns the combined penalty.
     * @param value the penalty value to add
     * @return the resulting penalty
     */
    public Penalty add(Penalty value) {
        return add(value.getValue());
    }

    /**
     * Adds a penalty to this one and returns the combined penalty.
     * @param value the penalty value to add
     * @return the resulting penalty
     */
    public Penalty add(int value) {
        long p = (long)getValue() + value;
        return toPenalty(truncate(p));
    }

    /**
     * Returns the penalty value.
     * @return the penalty value
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Indicates whether this is an infinite penalty, meaning that a solution with this penalty
     * is effectively ineligible.
     * @return true if this is an infinite penalty
     */
    public boolean isInfinitePenalty() {
        return (value == Integer.MAX_VALUE);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "Penalty: " + (isInfinitePenalty() ? "INF" : Integer.toString(getValue()));
    }

    /**
     * Truncates the long penalty value to an integer without sign side-effects.
     * @param penalty the penalty value as a long
     * @return the penalty value as an int
     */
    public static int truncate(long penalty) {
        penalty = Math.min(Integer.MAX_VALUE, penalty);
        penalty = Math.max(Integer.MIN_VALUE, penalty);
        return (int)penalty;
    }

}
