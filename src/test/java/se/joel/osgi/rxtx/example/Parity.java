/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package se.joel.osgi.rxtx.example;

/**
 * Parity setup in serial port.
 */
public class Parity {
    /**
     * No parity check occurs.
     */
    public static Parity NONE = new Parity("NONE", 0);
    /**
     * Sets the parity bit so that the count of bits set is an odd number.
     */
    public static Parity ODD = new Parity("ODD", 1);
    /**
     * Sets the parity bit so that the count of bits set is an even number.
     */
    public static Parity EVEN = new Parity("EVEN", 2);
    /**
     * Leaves the parity bit set to 1.
     */
    public static Parity MARK = new Parity("MARK", 3);
    /**
     * Leaves the parity bit set to 0.
     */
    public static Parity SPACE = new Parity("SPACE", 4);

    private int ordinal;
    private String name;

    private Parity(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Gets the ordinal value for this literal.
     *
     * @return the ordinal value for this literal
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return name;
    }
}
