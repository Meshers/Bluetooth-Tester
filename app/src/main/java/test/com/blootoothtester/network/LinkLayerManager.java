package test.com.blootoothtester.network;


import java.io.UnsupportedEncodingException;

import test.com.blootoothtester.MyBluetoothAdapter;

public class LinkLayerManager {
    private byte mFromAddr;
    private MyBluetoothAdapter myBluetoothAdapter;

    public LinkLayerManager(byte fromAddr, MyBluetoothAdapter bluetoothAdapter) {
        mFromAddr = fromAddr;
        myBluetoothAdapter = bluetoothAdapter;
    }

    public void sendData(byte[] packet, byte toAddr) {
        LinkLayerPdu frame = new LinkLayerPdu(mFromAddr, toAddr, packet);
        myBluetoothAdapter.setName(new String(frame.encode()));
    }
    public void sendData(String msg, byte toAddr) {
        try {
            sendData(msg.getBytes("UTF-8"), toAddr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
