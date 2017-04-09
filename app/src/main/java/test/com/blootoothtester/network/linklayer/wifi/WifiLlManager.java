package test.com.blootoothtester.network.linklayer.wifi;

import android.content.Context;

import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;

public class WifiLlManager {

    public interface MessageCallback {
        void receiveWifiMessage(WifiMessage wifiMessage);
        void receiveBtMessage();
        void receiveAcknowledgement();
    }

    private final WifiLlContext mWifiLlContext;

    public WifiLlManager(Context context, byte ownAddress, MyBluetoothAdapter bluetoothAdapter) {
        mWifiLlContext = new WifiLlContext(ownAddress);
    }

    public void sendWifiMessage(byte[] body) {
        mWifiLlContext.sendMessage(body);
    }

    public void sendBtMessage(byte[] body) {

    }

    public void startReceivingBtMessages() {

    }

    public void startReceivingWifiMessages() {

    }

}
