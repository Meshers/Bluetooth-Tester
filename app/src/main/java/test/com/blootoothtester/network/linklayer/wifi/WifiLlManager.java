package test.com.blootoothtester.network.linklayer.wifi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.network.hotspot.WifiUtils;

public class WifiLlManager {

    public interface MessageCallback {
        void onReceiveWifiMessage(WifiMessage wifiMessage);

        void onReceiveBtMessage(BtMessage btMessage);

        void onAckedByWifi();
    }

    private final WifiLlContext mWifiLlContext;
    private final MyBluetoothAdapter mBluetoothAdapter;
    private final Context mContext;
    private final MessageCallback mMessageCallback;
    private final BroadcastReceiver mWifiBroadcastReceiver;
    private final BroadcastReceiver mBtBroadcastReceiver;

    public WifiLlManager(Context context, byte ownAddress, byte sessionId,
                         MessageCallback messageCallback, MyBluetoothAdapter bluetoothAdapter) {
        mWifiLlContext = new WifiLlContext(
                ownAddress,
                sessionId,
                new WifiLlContext.Callback() {
                    @Override
                    public void transmitWifiMessage(WifiMessage message) {
                        WifiUtils.enableHotspot(mContext, message.encode());
                    }

                    @Override
                    public void transmitBtMessage(BtMessage btMessage) {
                        mBluetoothAdapter.setName(btMessage.encode());
                    }

                    @Override
                    public void onWifiMessageReceived(WifiMessage message) {
                        mMessageCallback.onReceiveWifiMessage(message);
                    }

                    @Override
                    public void onBtMessageReceived(BtMessage btMessage) {
                        mMessageCallback.onReceiveBtMessage(btMessage);
                    }

                    @Override
                    public void onAckedByWifi() {
                        mBluetoothAdapter.makeUndiscoverable();
                        mMessageCallback.onAckedByWifi();
                        // TODO: APHILIP - we can also turn off BT, but user interaction required
                    }
                });
        mContext = context;
        mMessageCallback = messageCallback;
        mBluetoothAdapter = bluetoothAdapter;


        final WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        mWifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    for (ScanResult result : scanResults) {
                        Log.d("WifiLlManager", "Saw: " + result.SSID);

                        if (WifiMessage.isValidWifiMessage(
                                result.SSID, mWifiLlContext.getSessionId())) {
                            Log.d("WifiManager", "Valid!");
                            mWifiLlContext.receiveWifiMessage(WifiMessage.decode(result.SSID));
                        }

                    }
                }
                WifiUtils.startWifiScan(wifiManager);
            }
        };

        mBtBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                System.out.println("ACTION:" + intent.getAction());
                String action = intent.getAction();

                // if our discovery has finished, time to start again!
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    mBluetoothAdapter.find();
                }

                // If not device discovery intent, ignore it
                if (!BluetoothDevice.ACTION_FOUND.equals(action)) return;
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("DEVICE:" + device.getName() + ":" + device.getAddress());

                // if it isn't part of our clique, kick it out
                // TODO: Only WifiLlContext should know about sessionId, refactor to reflect that
                // keeping this here currently as the context currently deals only with
                // valid PDUs
                if (!BtMessage.isValid(device.getName(), mWifiLlContext.getSessionId())) {
                    return;
                }

                mWifiLlContext.receiveBtMessage(BtMessage.decode(device.getName()));
            }
        };
    }

    public void sendWifiMessage(byte[] body) {
        mWifiLlContext.sendWifiMessage(body);
        startReceivingBtMessages();
    }

    public void startReceivingBtMessages() {
        // register for BT discovery events
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // check started just for debugging purposes
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        mBluetoothAdapter.getContext().registerReceiver(mBtBroadcastReceiver, filter);
    }

    public void startReceivingWifiMessages() {
        final WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mWifiBroadcastReceiver, intentFilter);

        WifiUtils.startWifiScan(wifiManager);
    }

    public void sendBtMessage(byte toId, byte msgId, byte[] body) {
        mWifiLlContext.sendBtResponse(toId, msgId, body);
    }

    public void stopSendingWifiMessage() {
        WifiUtils.disableWifi(mContext);
    }

    public void cleanUpReceivers() {
        mContext.unregisterReceiver(mBtBroadcastReceiver);
        mContext.unregisterReceiver(mWifiBroadcastReceiver);
    }

}
