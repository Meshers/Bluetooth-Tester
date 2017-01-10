package test.com.blootoothtester;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by sarahchristina on 1/10/17.
 */
public class MyBluetoothAdapter {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter myBluetoothAdapter;

    private Activity activity;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> BTArrayAdapter;
    private String BTName;

    public MyBluetoothAdapter(Activity activity, ArrayAdapter<String>BTArrayAdapter){

        this.activity = activity;
        this.myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.BTArrayAdapter = BTArrayAdapter;
    }

    public boolean isSupported(){

        if(myBluetoothAdapter != null){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean on(String BTName){

        this.BTName = BTName;

        if (!myBluetoothAdapter.isEnabled()) {

            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            activity.startActivityForResult(discoverIntent, 0);

            return true;
        }
        else{

            return false;
        }

    }


    public boolean off(){

        return myBluetoothAdapter.disable();
    }


    protected String activityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {

                myBluetoothAdapter.setName(BTName);

                return myBluetoothAdapter.getName();

            } else {
                return null;
            }
        }
        return null;
    }

    public void paired(){

        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices){

            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        }

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(){

        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            activity.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }

    }

    public ArrayAdapter<String> getBTArrayAdapter(){

        return BTArrayAdapter;
    }

    protected void destroy() {
        activity.unregisterReceiver(bReceiver);
    }
}
