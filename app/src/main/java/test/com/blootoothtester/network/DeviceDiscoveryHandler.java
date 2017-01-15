package test.com.blootoothtester.network;


import android.bluetooth.BluetoothDevice;

public interface DeviceDiscoveryHandler {
    void handleDiscovery(LinkLayerPdu receivedPacket);
}
