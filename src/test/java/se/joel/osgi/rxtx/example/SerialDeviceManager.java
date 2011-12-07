package se.joel.osgi.rxtx.example;

import gnu.io.CommPortIdentifier;
import org.osgi.service.component.ComponentContext;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SerialDeviceManager {
    private Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
    private Map devices = new HashMap();


    /**
     * Activate method for this service. This method will be invoked by the OSGi
     * framework.
     *
     * @param context The context for this Component.
     */
    protected void activate(ComponentContext context) {
        System.out.println("Deactivated serial device manager");

        for (Enumeration portList = portIdentifiers; portList.hasMoreElements(); ) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            String deviceName = portId.getName();
            System.out.println("Found port " + deviceName);
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                RxtxCommSerialDevice serialDevice = new RxtxCommSerialDevice(portId);
                devices.put(deviceName, serialDevice);
                System.out.println("Found serial port " + deviceName);
                if (context != null) {
                    context.getBundleContext().registerService(SerialDevice.class.getName(), serialDevice, null);
                }
            }
        }
    }

    /**
     * Deactivate method for this service. This method will be invoked by the
     * OSGi framework.
     *
     * @param context The context for this Component.
     */
    protected void deactivate(ComponentContext context) {
        System.out.println("Deactivated serial device manager");
        devices.clear();
    }

    /**
     * Gets the serial device with the specified name.
     *
     * @param name
     * @return the serial device with the specified name
     */
    public SerialDevice getDevice(String name) {
        return (SerialDevice) devices.get(name);
    }

    public String[] getDeviceNames() {
        String[] names = new String[devices.size()];
        int i = 0;
        for (Iterator iterator = devices.keySet().iterator(); iterator.hasNext(); ) {
            String name = (String) iterator.next();
            names[i++] = name;
        }
        return names;
    }
}
