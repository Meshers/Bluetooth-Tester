package test.com.blootoothtester;

import android.util.Base64;

import static org.junit.Assert.*;

import org.junit.Test;

import test.com.blootoothtester.network.linklayer.wifi.WifiMessage;

public class WifiMessageTester {
    @Test
    public void testEncodeDecode() {
        WifiMessage wifiMessage = new WifiMessage(
                (byte) 21,
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
        assertTrue(WifiMessage.isValidWifiMessage(wifiMessageReturned.encode(), (byte) 21));
        assertTrue(WifiMessage.isValidWifiMessage(wifiMessage.encode(), (byte) 21));
    }

    @Test
    public void testValidWifiMessage() {
        byte[] encoded = new byte[]{(byte) 21, (byte) 21, (byte) 21, (byte) 11};
        assertFalse(WifiMessage.isValidWifiMessage(
                Base64.encodeToString(encoded, WifiMessage.BASE64_FLAGS),
                (byte) 11
                )
        );

        encoded = new byte[]{(byte) 21, (byte) 20, (byte) 19, (byte) 10};
        assertFalse(WifiMessage.isValidWifiMessage(
                Base64.encodeToString(encoded, WifiMessage.BASE64_FLAGS),
                (byte) 11
                )
        );

        assertTrue(WifiMessage.isValidWifiMessage(
                Base64.encodeToString(encoded, WifiMessage.BASE64_FLAGS),
                (byte) 10
                )
        );
    }
}
