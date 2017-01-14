package test.com.blootoothtester.network;


import android.util.Log;

import java.io.UnsupportedEncodingException;

public class NetworkLayerManager {
    private final static String TAG = "NetworkLayerManager";

    public static void sendMessage(String msg) {
        byte[] encoded_msg;
        try {
            encoded_msg = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed encoding message provided by user", e);
            throw new RuntimeException("Unsupported encoding", e);
        }

        NetworkLayerHeader header = new NetworkLayerHeader(
                (byte) (System.nanoTime() % 256),
                false,
                (byte) 0);
        byte[] packet = header.writeHeaderIntoPacket(
                new byte[NetworkLayerHeader.HEADER_SIZE_BYTES + encoded_msg.length]);
        System.arraycopy(encoded_msg, 0, packet, 3, encoded_msg.length);

        LinkLayerManager.sendData(packet);
    }

    public void receiveMessage() {

    }
}
