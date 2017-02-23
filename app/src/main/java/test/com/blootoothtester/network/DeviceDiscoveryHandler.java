package test.com.blootoothtester.network;


import test.com.blootoothtester.network.linklayer.LinkLayerPdu;

public interface DeviceDiscoveryHandler {
    void handleDiscovery(LinkLayerPdu receivedPacket);
}
