package se.joel.osgi.rxtx.example;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * A serial device wrapping a {@link SerialPort}.
 */
public class RxtxCommSerialDevice implements SerialDevice, SerialPortEventListener {
    private CommPortIdentifier portId;
    private DataListener listener;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;
    private int baudRate = 115200;
    private Parity parity = Parity.NONE;
    private int dataBits = 8;
    private int stopBits = 1;

    /**
     * Creates a device for the specified port ID.
     *
     * @param portId
     */
    public RxtxCommSerialDevice(CommPortIdentifier portId) {
        this.portId = portId;
    }

    /**
     * {@inheritDoc}
     */
    public void open() throws SerialDeviceException {
        try {
            serialPort = (SerialPort) portId.open(getClass().getName(), 30000);
            serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity.getOrdinal());
            serialPort.setDTR(true);
            serialPort.setRTS(true);
            serialPort.setFlowControlMode(0);
            serialPort.notifyOnDataAvailable(true);
            this.in = serialPort.getInputStream();
            this.out = serialPort.getOutputStream();
            serialPort.addEventListener(this);
        } catch (UnsupportedCommOperationException e) {
            throw new RuntimeException("Unsupported feature", e);
        } catch (TooManyListenersException e) {
            throw new RuntimeException("Too many listeners", e);
        } catch (IOException e) {
            throw new RuntimeException("IO exception", e);
        } catch (PortInUseException e) {
            throw new SerialDeviceException("Exception when opening serial device: " + getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setup(int baudRate, Parity parity, int dataBits, int stopBits) {
        this.baudRate = baudRate;
        this.parity = parity;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        if (serialPort != null) {
            try {
                serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity.getOrdinal());
            } catch (UnsupportedCommOperationException e) {
                throw new RuntimeException("Unsupported feature", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
        serialPort = null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return portId.getName();
    }

    /**
     * {@inheritDoc}
     */
    public void setDataListener(DataListener dataListener) {
        this.listener = dataListener;
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] data) throws SerialDeviceException {
        if (!isOpen()) {
            throw new RuntimeException("Tried to write to closed device");
        }

        if (out == null) {
            throw new RuntimeException("Tried to write to device without output stream");
        }

        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            throw new SerialDeviceException("Exception when writing", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOpen() {
        return serialPort != null;
    }

    /**
     * {@inheritDoc}
     */
    public void serialEvent(SerialPortEvent serialPortEvent) {
        switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;

            case SerialPortEvent.DATA_AVAILABLE:
                byte[] readBuffer = new byte[1024];
                try {
                    while (in.available() > 0) {
                        int n = in.read(readBuffer);
                        byte[] buf = new byte[n];
                        System.arraycopy(readBuffer, 0, buf, 0, n);
                        if (listener != null) {
                            listener.onDataReceived(buf);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }
}
