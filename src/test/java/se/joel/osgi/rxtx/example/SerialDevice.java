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
 * Generic interface of a serial device.
 */
public interface SerialDevice {
    /**
     * Initializes the device.
     *
     * @param baudRate
     * @param parity
     * @param dataBits
     * @param stopBits
     */
    void setup(int baudRate, Parity parity, int dataBits, int stopBits);

    /**
     * Opens the device.
     */
    public void open() throws SerialDeviceException;

    /**
     * Closes the device.
     */
    public void close();

    /**
     * Gets the name of the device.
     *
     * @return the name of the device
     */
    public String getName();

    /**
     * Sets a listener for data received on the device.
     *
     * @param dataListener
     */
    public void setDataListener(DataListener dataListener);

    /**
     * Writes to the device.
     *
     * @param data
     */
    public void write(byte[] data) throws SerialDeviceException;

    /**
     * Checks if the device is open.
     *
     * @return
     */
    boolean isOpen();

    /**
     * Listener for data received from a serial device.
     */
    interface DataListener {
        /**
         * Handles the reception of some bytes of data.
         *
         * @param data the data
         */
        public void onDataReceived(byte[] data);
    }
}