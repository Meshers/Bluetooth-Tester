package test.com.blootoothtester.network;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.util.Constants;

public class LinkLayerManager {
    private byte mFromAddr;
    private MyBluetoothAdapter mBluetoothAdapter;
    private DeviceDiscoveryHandler mDiscoveryHandler;

    public LinkLayerManager(byte fromAddr, MyBluetoothAdapter bluetoothAdapter,
                            DeviceDiscoveryHandler discoveryHandler) {
        mFromAddr = fromAddr;
        mBluetoothAdapter = bluetoothAdapter;
        mDiscoveryHandler = discoveryHandler;

        // register for BT discovery events

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // check started just for debugging purposes
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        BroadcastReceiver bReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                System.out.println("ACTION:" + intent.getAction());
                String action = intent.getAction();

                // if our discovery has finished, time to start again!
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    startReceiving();
                }

                // When discovery finds a device
                if (!BluetoothDevice.ACTION_FOUND.equals(action)) return;
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("DEVICE:" + device.getName() + ":" + device.getAddress());
                // add the name and the MAC address of the object to the arrayAdapter
                if (!LinkLayerPdu.isValidPdu(device.getName())) return;
                try {
                    LinkLayerPdu pdu = new LinkLayerPdu(device.getName());
                    if (pdu.getFromAddress() != Constants.LINK_LAYER_PDU_TEACHER_ID
                            && pdu.getToAddress() != mFromAddr) {
                        return;
                    }
                    mDiscoveryHandler.handleDiscovery(pdu);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };

        bluetoothAdapter.getContext().registerReceiver(bReceiver, filter);
    }

    public void startReceiving() {
        // this means we got to start our discovery loop
        mBluetoothAdapter.find();
    }

    public void sendData(byte[] packet, byte toAddr) {
        LinkLayerPdu frame = new LinkLayerPdu(mFromAddr, toAddr, packet);
        mBluetoothAdapter.setName(new String(frame.encode()));
    }

    public void sendData(String msg, byte toAddr) {
        try {
            sendData(msg.getBytes("UTF-8"), toAddr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setFromAddr(byte fromAddr) {
        mFromAddr = fromAddr;
    }

}
