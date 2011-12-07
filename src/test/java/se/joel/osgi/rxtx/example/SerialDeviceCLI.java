package se.joel.osgi.rxtx.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SerialDeviceCLI implements SerialDevice.DataListener {
    SerialDeviceManager serialDeviceManager = new SerialDeviceManager();
    private SerialDevice currentDevice;
    private int baudRate = 115200;
    private Parity parity = Parity.NONE;
    private int stopBits = 1;
    private int dataBits = 8;

    private void run() throws IOException {
        serialDeviceManager.activate(null);
        InputStream in = System.in;
        LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(in));
        System.out.print("> ");
        String line;
        while ((line = lineReader.readLine()) != null && !line.toLowerCase().startsWith("q")) {
            handleLine(line.trim());
            System.out.print("> ");
        }
        if (currentDevice != null) {
            currentDevice.close();
            currentDevice.setDataListener(null);
        }
        System.out.println("Bye");
        System.exit(0);
    }

    private void handleLine(String line) {
        if (line.toLowerCase().startsWith("h")) {
            System.out.println("Available commands");
            System.out.println("Show devices    : ls");
            System.out.println("Open device     : op[en] <device name>");
            System.out.println("Close device    : cl[ose] <device name>");
            System.out.println(
                "Configure device: co[onfigure] [b[audrate]=<baud rate>] [p[arity]=<o[dd]|e[ven]|m[ark]|s[pace]|n[one]>] [s[topbits]=<stop bits>] [d[atabits]=<data bits>]");
            System.out.println("Write to device : w[rite] <data, e.g 00 0F A1>");
        } else if (line.toLowerCase().startsWith("ls")) {
            String[] deviceNames = serialDeviceManager.getDeviceNames();
            System.out
                .println("Current device: " + (currentDevice != null ? currentDevice.getName() : "No device open"));
            System.out.println("Available devices: " + Arrays.toString(deviceNames));
        } else if (line.toLowerCase().startsWith("op")) {
            String[] tokens = line.split(" ");
            if (tokens.length != 2) {
                System.out.println("Open needs an argument: port name");
                return;
            }
            String deviceName = tokens[1];
            SerialDevice device = serialDeviceManager.getDevice(deviceName);
            if (device != null) {
                if (currentDevice != null) {
                    currentDevice.close();
                    currentDevice.setDataListener(null);
                }
                currentDevice = device;
                try {
                    currentDevice.open();
                    currentDevice.setup(baudRate, parity, dataBits, stopBits);
                    currentDevice.setDataListener(this);
                    System.out.println("Opened " + currentDevice.getName());
                } catch (SerialDeviceException e) {
                    System.err.println("Failed to open device");
                    e.printStackTrace();
                    currentDevice = null;
                }
            } else {
                System.out.println("There is no device named " + deviceName);
            }
        } else if (line.toLowerCase().startsWith("cl")) {
            if (currentDevice != null) {
                currentDevice.close();
                currentDevice.setDataListener(null);
                System.out.println("Closed " + currentDevice.getName());
                currentDevice = null;
            } else {
                System.out.println("There was device open");
            }
        } else if (line.toLowerCase().startsWith("co")) {
            String[] tokens = line.split(" ");
            boolean changed = false;
            if (tokens.length >= 2) {
                Map<String, String> params = new HashMap<String, String>();
                for (int i = 1; i < tokens.length; i++) {
                    String token = tokens[i];
                    String[] subTokens = token.split("=");
                    if (subTokens.length == 2) {
                        String name = subTokens[0];
                        String value = subTokens[1];
                        params.put(name, value);
                    } else {
                        System.out.println("Bad input at '" + token + "', expected <name>=<value>");
                        return;
                    }
                }
                for (Map.Entry<String, String> param : params.entrySet()) {
                    try {
                        if (param.getKey().toLowerCase().startsWith("b")) {
                            baudRate = Integer.parseInt(param.getValue());
                            changed = true;
                        } else if (param.getKey().toLowerCase().startsWith("d")) {
                            dataBits = Integer.parseInt(param.getValue());
                            changed = true;
                        } else if (param.getKey().toLowerCase().startsWith("s")) {
                            stopBits = Integer.parseInt(param.getValue());
                            changed = true;
                        } else if (param.getKey().toLowerCase().startsWith("p")) {
                            parity = getParity(param.getValue());
                            changed = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Bad number format");
                    }
                }
            }
            System.out.println(
                "Current configuration:\nbaud rate=" + baudRate + "\nparity=" + parity + "\ndata bits=" + dataBits +
                    "\nstop bits=" + stopBits);
            if (currentDevice != null && changed) {
                try {
                    currentDevice.setup(baudRate, parity, dataBits, stopBits);
                } catch (RuntimeException e) {
                    System.out.println("Failed to configure device");
                    e.printStackTrace();
                }
            }
        } else if (line.toLowerCase().startsWith("w")) {
            if (currentDevice == null) {
                System.out.println("There is no device open for writing");
                return;
            }
            String[] tokens = line.split(" ");
            try {
                if (tokens.length >= 2) {
                    byte[] buf = new byte[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++) {
                        String token = tokens[i];
                        byte data = 0;
                        if (token.startsWith("x")) {
                            data = (byte) Integer.parseInt(token.substring(1), 16);
                        } else {
                            data = (byte) Integer.parseInt(token, 10);
                        }
                        buf[i - 1] = data;
                    }
                    try {
                        currentDevice.write(buf);
                    } catch (SerialDeviceException e) {
                        System.out.println("Error when writing to " + currentDevice.getName());
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Bad number format, only hex numbers accepted");
            }
        }
    }

    private Parity getParity(String value) {
        if (value.toLowerCase().startsWith("e")) return Parity.EVEN;
        if (value.toLowerCase().startsWith("o")) return Parity.ODD;
        if (value.toLowerCase().startsWith("s")) return Parity.SPACE;
        if (value.toLowerCase().startsWith("m")) return Parity.MARK;
        return Parity.NONE;
    }

    public static void main(String[] args) throws IOException {
        new SerialDeviceCLI().run();
    }

    public void onDataReceived(byte[] data) {
        System.out.println("\nInbound data");
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            System.out.printf("%3X ", b);
        }
        System.out.println();
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            System.out.printf("%3d ", b);
        }
        System.out.println();
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            System.out.printf("%3c ", (char) b);
        }
        System.out.println();
        try {
            currentDevice.write(new byte[]{6});
        } catch (SerialDeviceException e) {
            System.out.println("Failed to write ACK");
            e.printStackTrace();
        }
        System.out.print("> ");
    }
}
