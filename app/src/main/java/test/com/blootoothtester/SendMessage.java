package test.com.blootoothtester;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;


/**
 * Created by sarahchristina on 1/10/17.
 */
public class SendMessage {

    private MyBluetoothAdapter myBluetoothAdapter;

    private String id;
    private String message;

    private String BTBeaconData;

    public SendMessage(String id, String message, MyBluetoothAdapter myBluetoothAdapter){

        this.myBluetoothAdapter = myBluetoothAdapter;

        this.id = id;
        this.message = message;

    }

    public String createPacket(){

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
        bytes[5] = (byte)message.length();

        //add data to packet
        byte[] payload = message.getBytes();

        for(int i = 0; i < payload.length; i++){
            if(i+6<=bytes.length)
                bytes[i+6] = payload[i];
        }

        return bytes.toString();

    }

    public boolean send(){
        // turn on bluetooth and send put becon message containing the id and data

        BTBeaconData = createPacket();

        return myBluetoothAdapter.on(BTBeaconData);

    }
}
