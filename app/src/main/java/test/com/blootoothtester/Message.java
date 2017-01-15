package test.com.blootoothtester;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.ArrayAdapter;

import java.io.UnsupportedEncodingException;

import test.com.blootoothtester.network.LinkLayerPdu;


/**
 * Created by sarahchristina on 1/10/17.
 */
public class Message {

    private MyBluetoothAdapter myBluetoothAdapter;

    private String id;
    private String message;

    private String BTBeaconData;

    public Message(MyBluetoothAdapter myBluetoothAdapter) {

        this.myBluetoothAdapter = myBluetoothAdapter;

        this.id = null;
        this.message = null;

    }

    public String createPacket() {

        byte[] bytes = new byte[256];

        //add sender data to packet
        byte[] sender = id.getBytes();
        bytes[0] = sender[0];
        bytes[1] = sender[1];

        //add receiver data to packet;
        bytes[2] = 0;
        bytes[3] = 0;

        //add 1 byte for type of message;
        bytes[4] = 0;

        //add 1 byte for length
        bytes[5] = (byte) message.length();

        //add data to packet
        byte[] payload = message.getBytes();

        for (int i = 0; i < payload.length; i++) {
            if (i + 6 <= bytes.length)
                bytes[i + 6] = payload[i];
        }

        return bytes.toString();

    }

    public boolean send(String id, String message) {

        this.id = id;
        this.message = message;

        //BTBeaconData = createPacket();
        BTBeaconData = id + ":" + message;

        return myBluetoothAdapter.on(BTBeaconData);

    }

    public ArrayAdapter<String> recieve() {

        myBluetoothAdapter.find();

        ArrayAdapter<String> BTArrayAdapter;

        BTArrayAdapter = myBluetoothAdapter.getBTArrayAdapter();

        for (int i = 0; i < BTArrayAdapter.getCount(); i++) {

            try {

                byte[] name = BTArrayAdapter.getItem(i).getBytes("UTF-8");

                if (LinkLayerPdu.isValidPdu(name)) {
                    System.out.println("VALID! " + new String(name));
                } else {
                    System.out.println("INVALID! " + new String(name));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return BTArrayAdapter;
    }
}
