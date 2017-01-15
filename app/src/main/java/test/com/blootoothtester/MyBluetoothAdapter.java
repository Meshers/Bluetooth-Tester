package test.com.blootoothtester;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by sarahchristina on 1/10/17.
 */
public class MyBluetoothAdapter {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter myBluetoothAdapter;

    private Activity activity;

    private ArrayAdapter<String> BTArrayAdapter;
    private String BTName;

    public MyBluetoothAdapter(Activity activity, ArrayAdapter<String> BTArrayAdapter) {

        this.activity = activity;
        this.myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.BTArrayAdapter = BTArrayAdapter;

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(bReceiver, filter);


    }

    public boolean isSupported() {
        return myBluetoothAdapter != null;
    }

    public boolean on(String BTName) {
        this.BTName = BTName;

        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            return true;
        } else {
            return false;
        }

    }

    public void setName(String name) {
        BTName = name;
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        } else {
//            if (myBluetoothAdapter.getScanMode() !=
//                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3000);
//                activity.startActivity(discoverableIntent);
//            }
            myBluetoothAdapter.setName(name);
            makeDiscoverable(3000);
        }
    }

    public void makeDiscoverable(int timeOut) {
        Class<?> baClass = BluetoothAdapter.class;
        Method[] methods = baClass.getDeclaredMethods();
        Method mSetScanMode = null;
        for (Method method : methods) {
            if (method.getName().equals("setScanMode") && method.getParameterTypes().length == 2
                    && method.getParameterTypes()[0].equals(int.class)
                    && method.getParameterTypes()[1].equals(int.class)) {
                mSetScanMode = method;
                break;
            }
        }
        try {
            mSetScanMode.invoke(myBluetoothAdapter,
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeOut);
        } catch (Exception e) {
            Log.e("discoverable", e.getMessage());
            for (Class parameter : mSetScanMode.getParameterTypes()) {
                System.out.println("PARAM:" + parameter);
            }
        }
    }

    public boolean off() {
        return myBluetoothAdapter.disable();
    }

    protected String activityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            setName(BTName);
//            Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            activity.startActivityForResult(discoverIntent, 0);
        } else if (resultCode == REQUEST_ENABLE_BT) {
            Toast.makeText(activity, "Bluetooth failed to be enabled", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("ACTION:" + intent.getAction());
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("DEVICE:" + device.getName() + ":" + device.getAddress());
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find() {

        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        BTArrayAdapter.clear();
        myBluetoothAdapter.startDiscovery();
    }

    public ArrayAdapter<String> getBTArrayAdapter() {

        return BTArrayAdapter;
    }

    protected void destroy() {
        activity.unregisterReceiver(bReceiver);
    }
}
