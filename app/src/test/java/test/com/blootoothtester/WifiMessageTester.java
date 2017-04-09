package test.com.blootoothtester;

import static org.junit.Assert.*;
import org.junit.Test;

import test.com.blootoothtester.network.linklayer.wifi.WifiMessage;

public class WifiMessageTester {
    @Test
    public void testEncodeDecode() {
        WifiMessage wifiMessage = new WifiMessage(
                (byte) 1,
                (byte) 1,
                new byte[]{
                (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1,
                (byte) 1},
                "Hello!".getBytes()
        );

        WifiMessage wifiMessageReturned = WifiMessage.decode(wifiMessage.encode());

        assertTrue(wifiMessageReturned.equals(wifiMessage));
        assertTrue(new String(wifiMessageReturned.getBody()).equals("Hello!"));
        assertTrue(WifiMessage.isValidWifiMessage(wifiMessageReturned.encode()));
        assertTrue(WifiMessage.isValidWifiMessage(wifiMessage.encode()));
    }

    @Test
    public void testValidWifiMessage() {
        byte[] encoded = new byte[]{(byte)21,(byte)21,(byte)21};
        assertFalse(WifiMessage.isValidWifiMessage(new String(encoded, WifiMessage.ENCODE_CHARSET)));
        encoded = new byte[]{(byte)21,(byte)20,(byte)19};
        assertTrue(WifiMessage.isValidWifiMessage(new String(encoded, WifiMessage.ENCODE_CHARSET)));
    }
}
