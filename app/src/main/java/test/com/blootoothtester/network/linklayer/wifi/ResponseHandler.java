package test.com.blootoothtester.network.linklayer.wifi;

public interface ResponseHandler {
    void onNewWifiMessageDetected(WifiMessage message);
    void onBtAcknowledged();
}
