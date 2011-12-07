package se.joel.osgi.rxtx.example;

import com.ericsson.research.common.testutil.ReflectionTestUtil;
import gnu.io.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import static org.junit.Assert.*;


/**
 * RxtxCommSerialDevice Tester.
 */
public class RxtxCommSerialDeviceTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private CommPortIdentifier portId;
    private RxtxCommSerialDevice serialDevice;
    private SerialPort serialPort;

    @Before
    public void setUp() throws Exception {
        portId = context.mock(CommPortIdentifier.class);
        serialPort = context.mock(SerialPort.class);
        serialDevice = new RxtxCommSerialDevice(portId);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Method: setup(int baudRate, Parity parity, int dataBits, int stopBits)
     */
    @Test
    public void testSetup()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        performOpen(null, null);

        context.checking(new Expectations() {{
            oneOf(serialPort).setSerialPortParams(115200, 1, 2, Parity.NONE.getOrdinal());
        }});

        serialDevice.setup(115200, Parity.NONE, 1, 2);

        context.assertIsSatisfied();
    }

    /**
     * Method: close()
     */
    @Test
    public void testClose_open()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        performOpen(null, null);

        context.checking(new Expectations() {{
            oneOf(serialPort).removeEventListener();
            oneOf(serialPort).close();
        }});

        serialDevice.close();

        context.assertIsSatisfied();
        assertNull(ReflectionTestUtil.<String>getField(serialDevice, "serialPort"));
    }

    /**
     * Method: close()
     */
    @Test
    public void testClose_alreadyClosed() {
        serialDevice.close();

        context.assertIsSatisfied();
        assertNull(ReflectionTestUtil.<String>getField(serialDevice, "serialPort"));
    }

    /**
     * Method: getName()
     */
    @Test
    public void testGetName()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        performOpen(null, null);

        context.checking(new Expectations() {{
            oneOf(portId).getName();
            will(returnValue("Test port"));
        }});

        String name = serialDevice.getName();

        context.assertIsSatisfied();
        assertEquals("Test port", name);
    }

    /**
     * Method: setDataListener(DataListener dataListener)
     */
    @Test
    public void testSetDataListener() {
        SerialDevice.DataListener dataListener = new SerialDevice.DataListener() {
            public void onDataReceived(byte[] data) {

            }
        };

        serialDevice.setDataListener(dataListener);

        assertEquals(dataListener, ReflectionTestUtil.getField(serialDevice, "listener"));
    }

    /**
     * Method: write(byte[] data)
     */
    @Test
    public void testWrite_success()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        final OutputStream out = context.mock(OutputStream.class);

        performOpen(out, null);
        context.checking(new Expectations() {{
            oneOf(serialPort).setSerialPortParams(325, 8, 1, Parity.EVEN.getOrdinal());
            oneOf(out).write(new byte[]{1, 2, 3, 4, 5, 6});
            oneOf(out).flush();
        }});

        serialDevice.setup(325, Parity.EVEN, 8, 1);

        try {
            serialDevice.write(new byte[]{1, 2, 3, 4, 5, 6});
        } catch (RuntimeException e) {
            // OK
        }

        context.assertIsSatisfied();
    }

    /**
     * Method: write(byte[] data)
     */
    @Test
    public void testWrite_noOut()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        performOpen(null, null);

        try {
            serialDevice.write(new byte[]{1, 2, 3, 4, 5, 6});
            fail("Exception was expected");
        } catch (RuntimeException e) {
            // OK
        }

        context.assertIsSatisfied();
    }

    /**
     * Method: write(byte[] data)
     */
    @Test
    public void testWrite_closed() throws PortInUseException, SerialDeviceException {
        try {
            serialDevice.write(new byte[]{1, 2, 3, 4, 5, 6});
            fail("Exception was expected");
        } catch (RuntimeException e) {
            // OK
        }

        context.assertIsSatisfied();
    }

    /**
     * Method: isOpen()
     */
    @Test
    public void testIsOpen()
        throws PortInUseException, SerialDeviceException, UnsupportedCommOperationException, IOException,
        TooManyListenersException {
        assertFalse(serialDevice.isOpen());

        performOpen(null, null);

        context.assertIsSatisfied();
        assertTrue(serialDevice.isOpen());
    }

    /**
     * Method: serialEvent(SerialPortEvent serialPortEvent)
     */
    @Test
    public void testSerialEvent_listenerNotNull()
        throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException,
        SerialDeviceException {
        final InputStream in = context.mock(InputStream.class);
        final byte[] contents = {2, 3, 4, 5, 6, 7};
        final SerialDevice.DataListener listener = context.mock(
            SerialDevice.DataListener.class);

        performOpen(null, in);

        context.checking(new Expectations() {{
            oneOf(serialPort).setSerialPortParams(325, 8, 1, Parity.EVEN.getOrdinal());
            oneOf(in).available();
            will(returnValue(contents.length));
            oneOf(in).read(with(byteArrayThatGetsUpdatedWith(contents)));
            will(returnValue(contents.length));
            oneOf(listener).onDataReceived(contents);
            oneOf(in).available();
            will(returnValue(0));
        }});

        serialDevice.setup(325, Parity.EVEN, 8, 1);
        serialDevice.setDataListener(listener);

        serialDevice.serialEvent(new SerialPortEvent(serialPort, SerialPortEvent.DATA_AVAILABLE, true, true));

        context.assertIsSatisfied();
    }

    /**
     * Method: serialEvent(SerialPortEvent serialPortEvent)
     */
    @Test
    public void testSerialEvent_listenerNull()
        throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException,
        SerialDeviceException {
        final InputStream in = context.mock(InputStream.class);
        final byte[] contents = {2, 3, 4, 5, 6, 7};

        performOpen(null, in);
        context.checking(new Expectations() {{
            oneOf(serialPort).setSerialPortParams(325, 8, 1, Parity.EVEN.getOrdinal());
            oneOf(in).available();
            will(returnValue(contents.length));
            oneOf(in).read(with(byteArrayThatGetsUpdatedWith(contents)));
            will(returnValue(contents.length));
            oneOf(in).available();
            will(returnValue(0));
        }});

        serialDevice.setup(325, Parity.EVEN, 8, 1);

        serialDevice.serialEvent(new SerialPortEvent(serialPort, SerialPortEvent.DATA_AVAILABLE, true, true));

        context.assertIsSatisfied();
    }

    private void performOpen(final OutputStream out, final InputStream in)
        throws SerialDeviceException, PortInUseException, UnsupportedCommOperationException, TooManyListenersException,
        IOException {
        context.checking(new Expectations() {{
            oneOf(portId).open(RxtxCommSerialDevice.class.getName(), 30000);
            will(returnValue(serialPort));
            oneOf(serialPort).setSerialPortParams(with(aNonNull(Integer.class)), with(aNonNull(Integer.class)),
                with(aNonNull(Integer.class)), with(aNonNull(Integer.class)));
            oneOf(serialPort).setDTR(true);
            oneOf(serialPort).setRTS(true);
            oneOf(serialPort).setFlowControlMode(0);
            oneOf(serialPort).notifyOnDataAvailable(true);
            oneOf(serialPort).getInputStream();
            will(returnValue(in));
            oneOf(serialPort).getOutputStream();
            will(returnValue(out));
            oneOf(serialPort).addEventListener(serialDevice);
        }});

        serialDevice.open();
    }

    private static Matcher<byte[]> byteArrayThatGetsUpdatedWith(final byte[] contents) {
        return new TypeSafeMatcher<byte[]>() {

            @Override
            public boolean matchesSafely(byte[] actual) {
                int length = Math.min(contents.length, actual.length);
                System.arraycopy(contents, 0, actual, 0, length);
                return true;
            }

            public void describeTo(Description description) {
                description.appendText("Return bytes: ").appendText(contents.toString());
            }
        };
    }

}
