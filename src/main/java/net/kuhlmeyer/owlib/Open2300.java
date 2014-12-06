package net.kuhlmeyer.owlib;

import gnu.io.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Job that communicates with an open 2300 weather station connected to serial
 * port.
 *
 * @author christof
 */
public class Open2300 {

    private static final Logger LOG = Logger.getLogger(Open2300.class);
    private final String device;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private static final int RETRY_COUNT = 50;
    private static final long ERROR_WAIT_TIMEOUT = 100;

    private InputStream is;
    private OutputStream os;

    public Open2300(String device) {
        this.device = device;
    }


    public Weather queryWeatherData() throws SocketException, IOException {

        LOG.debug("Collecting data from weather station at '" + device + "'");

        SerialPort serialPort = null;
        try {
            serialPort = openPort();

            Weather dataEntry = new Weather();
            dataEntry.setTimestamp(new Date());
            dataEntry.setTemperatureIn(getTemperature(0x0346));
            dataEntry.setTemperatureOut(getTemperature(0x0373));
            dataEntry.setRelHumidIn(getHumidity(0x03FB));
            dataEntry.setRelHumidOut(getHumidity(0x0419));
            dataEntry.setWindchill(getTemperature(0x03A0));
            dataEntry.setDewpoint(getTemperature(0x03CE));
            dataEntry.setWindspeed(getWindspeed());
            dataEntry.setWindDirection(getWindangle());
            dataEntry.setRelPressure(getRelPressure());
            dataEntry.setRainLast24Hours(getRainLast24h());
            dataEntry.setRainLast1Hour(getRainLast1h());
            dataEntry.setForecast(getForecast());
            dataEntry.setTendency(getTendency());
            return dataEntry;

        } catch (IOException e) {
            LOG.error("Error collecting data from port '" + device + "'.", e);
        } catch (NoSuchPortException e) {
            LOG.error("No port found at '" + device + "'.", e);
        } catch (PortInUseException e) {
            LOG.error("Port at '" + device + "' is already in use.", e);
        } catch (UnsupportedCommOperationException e) {
            LOG.error("Unsupported operation for port at '" + device + "'.", e);
        } finally {
            closePort(serialPort);
        }
        LOG.debug("Finished collecting data from weather station at '" + device + "'");

        return null;
    }

    /**
     * Opens the port to the weather station using the port defined in the
     * application configuration.
     */
    private SerialPort openPort() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        SerialPort serialPort = null;
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(device);
        if (portIdentifier.isCurrentlyOwned()) {
            LOG.info("Port " + device + " is already in use");
        } else {
            LOG.debug("Not owned. Now opening port " + device);
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setRTS(true);
                serialPort.setDTR(false);

                is = serialPort.getInputStream();
                os = serialPort.getOutputStream();
            }
        }

        LOG.debug("Weather station at '" + device + "' initialized.");
        return serialPort;
    }


    /**
     * Closes the serial port.
     */
    private void closePort(SerialPort serialPort) {
        try {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }

            if (serialPort != null) {
                serialPort.close();
                serialPort = null;
            }
        } catch (IOException e) {
            LOG.error("Error closing port: '" + device + "'", e);
        }
    }

    /**
     * Resets the weather station. Should be used before every communication.
     */
    private void reset() throws IOException {
        for (int i = 0; i < RETRY_COUNT; i++) {
            byte res = (byte) sendAndReceive((byte) 0x06);
            if (res != (byte) 0x02) {
                LOG.warn(String.format("Reset failed. Expected: 0x02, Received %02X", res));
            } else {
                return;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    private String getForecast() throws IOException {
        int[] data = readFromStation(0x026b, 1);

        if (data != null) {
            switch (data[0] & 0xf) {
                case 0:
                    return "rainy";
                case 1:
                    return "cloudy";
                case 2:
                    return "sunny";
            }
        }
        return null;
    }

    private String getTendency() throws IOException {
        int[] data = readFromStation(0x026c, 1);

        if (data != null) {
            switch (data[0] & 0xf) {
                case 0:
                    return "steady";
                case 1:
                    return "rising";
                case 2:
                    return "falling";
            }
        }
        return null;
    }

    private Double getRainLast1h() throws IOException {
        int[] data = readFromStation(0x04B4, 3);
        if (data == null) {
            return null;
        }
        return ((data[2] >> 4) * 1000 + (data[2] & 0xF) * 100 + (data[1] >> 4) * 10 + (data[1] & 0xF) + (data[0] >> 4) / 10.0 + (data[0] & 0xF) / 100.0);
    }

    private Double getRainLast24h() throws IOException {
        int[] data = readFromStation(0x0497, 3);
        if (data == null) {
            return null;
        }
        return ((data[2] >> 4) * 1000 + (data[2] & 0xF) * 100 + (data[1] >> 4) * 10 + (data[1] & 0xF) + (data[0] >> 4) / 10.0 + (data[0] & 0xF) / 100.0);
    }

    private Double getRelPressure() throws IOException {
        int[] data = readFromStation(0x05E2, 3);
        if (data == null) {
            return null;
        }
        return (((data[2] & 0xF) * 1000 + (data[1] >> 4) * 100 + (data[1] & 0xF) * 10 + (data[0] >> 4) + (data[0] & 0xF) / 10.0));
    }

    private Double getWindspeed() throws IOException {
        int[] data = readFromStation(0x0529, 2);
        if (data == null) {
            return null;
        }
        return (((data[1] & 0xF) << 8) + (data[0])) / 10.0 * 3.6;
    }

    private Double getWindangle() throws IOException {
        int[] data = readFromStation(0x052C, 1);
        if (data == null) {
            return null;
        }
        return (data[0] & 0xf) * 22.5;
    }

    private Integer getHumidity(int address) throws IOException {
        int[] data = readFromStation(address, 1);
        if (data == null) {
            return null;
        }
        return ((data[0] >> 4) * 10 + (data[0] & 0xF));
    }

    private Double getTemperature(int address) throws IOException {
        int[] data = readFromStation(address, 2);
        if (data == null) {
            return null;
        }
        return ((((data[1] >> 4) * 10 + (data[1] & 0xF) + (data[0] >> 4) / 10.0 + (data[0] & 0xF) / 100.0) - 30.0));
    }

    private int[] convertAddress(int address) {
        int[] result = new int[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (address >> (4 * (3 - i))) & 0x0F;
        }
        return result;
    }

    private int[] readFromStation(int addr, int len) throws IOException {
        return readFromStation(addr, len, RETRY_COUNT);
    }

    private int[] readFromStation(int addr, int len, int retry) throws IOException {
        if (retry <= 0) {
            throw new IOException("Retry failed.");
        }
        int[] address = convertAddress(addr);
        reset();

        // Send address
        for (int i = 0; i < address.length; i++) {
            int req = (0x82 + (address[i] * 0x04));
            int res = sendAndReceive((req));
            int ack = (((i * 0x10) + (req - 0x82) / 4));

            LOG.debug(String.format("Req: %02x, Res: %02x, Ack: %02x", req, res, ack));
        }

        int readReq = 0xC2 + (len * 4);
        int readRes = sendAndReceive(readReq);
        int readAck = 0x30 + len;

        LOG.debug(String.format("ReadReq: %02x, ReadRes: %02x, ReadAck: %02x", readReq, readRes, readAck));

        sleep(300);

        int checksum = 0;
        boolean readOK = true;
        int[] data = new int[len];

        for (int i = 0; i < len && readOK; i++) {
            if (!isDataAvailable()) {
                readOK = false;
            } else {
                data[i] = receiveByte();
                checksum += data[i];
            }
        }

        if (!readOK) {
            LOG.debug(String.format("No response received."));
            try {
                Thread.sleep(ERROR_WAIT_TIMEOUT);
            } catch (InterruptedException e1) {
            }
            return readFromStation(addr, len, retry - 1);
        }
        checksum &= 0xFF;

        sleep(50);

        if (isDataAvailable()) {

            int recvChecksum = receiveByte();
            if (checksum != recvChecksum) {
                LOG.debug(String.format("Checksum of data does not match: Expected: %02x, Received: %02x", checksum, recvChecksum));
                sleep(ERROR_WAIT_TIMEOUT);
                try {
                    Thread.sleep(ERROR_WAIT_TIMEOUT);
                } catch (InterruptedException e1) {
                }
                readFromStation(addr, len, retry - 1);
            }

        } else {
            LOG.debug(String.format("No checksum byte received."));
            try {
                Thread.sleep(ERROR_WAIT_TIMEOUT);
            } catch (InterruptedException e1) {
            }
            readFromStation(addr, len, retry - 1);
        }

        boolean result = false;
        for (int i : data) {
            result |= i != 0x00;
        }
        if (!result) {
            return null;
        }

        return data;
    }

    private int sendAndReceive(int req) throws IOException {
        return sendAndReceive(req, RETRY_COUNT);
    }

    private int sendAndReceive(int req, int retry) throws IOException {

        clearInputBuffer();
        writeByte(req);

        for (int i = 0; i < 20 && !isDataAvailable(); i++) {
            LOG.debug(String.format("Waiting for data (" + i + "/20)..."));
            sleep(100);
        }

        if (!isDataAvailable() && retry > 0) {
            reset();
            LOG.debug(String.format("No response received. Retry triggered."));
            return sendAndReceive(req, retry - 1);
        } else if (!isDataAvailable()) {
            throw new RuntimeException("Reading failed.");
        }

        return receiveByte();
    }

    private void clearInputBuffer() throws IOException {
        byte[] buf = new byte[is.available()];
        is.read(buf);
    }

    private boolean isDataAvailable() throws IOException {
        return is.available() > 0;
    }

    private int receiveByte() throws IOException {
        return is.read() & 0xff;
    }

    private void writeByte(int b) throws IOException {
        os.write((byte) (b & 0xff));
        os.flush();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
}
