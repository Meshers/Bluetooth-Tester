package test.com.blootoothtester.activity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;

import test.com.blootoothtester.R;
import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.network.linklayer.DeviceDiscoveryHandler;
import test.com.blootoothtester.network.linklayer.LinkLayerManager;
import test.com.blootoothtester.network.linklayer.LinkLayerPdu;
import test.com.blootoothtester.util.Logger;

public class MainActivity extends AppCompatActivity {

    private Button mOnBtn;
    private Button mRcvBtn;
    private EditText mToId;
    private EditText mBtMessage;

    private MyBluetoothAdapter mBluetoothAdapter;
    LinkLayerManager mLinkLayerManager;

    private ArrayAdapter<String> mBtArrayAdapter;

    private Logger mLogger = new Logger();

    public final static String EXTRA_OWN_ADDRESS = "OWN_ADDRESS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();

        mBluetoothAdapter = new MyBluetoothAdapter(MainActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // this takes care of letting the user add the WRITE_SETTINGS permission
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }


        if (!mBluetoothAdapter.isSupported()) {
            mOnBtn.setEnabled(false);
            mRcvBtn.setEnabled(false);

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            mOnBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMessage(mBtMessage.getText().toString());
                }
            });


            mRcvBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Starting to receive", Toast.LENGTH_SHORT).show();
                    mLinkLayerManager.startReceiving();
                }
            });
        }


        DeviceDiscoveryHandler discoveryHandler = new DeviceDiscoveryHandler() {

            private HashMap<Byte, String> mCurrentResponseMap = new HashMap<>();

            @Override
            public void handleDiscovery(LinkLayerPdu receivedPacket) {
                mLogger.d("MainActivity", "Packet for " + receivedPacket.getToAddress()
                        + " received from " + receivedPacket.getFromAddress() + " with content"
                        + receivedPacket.getDataAsString());
                mCurrentResponseMap.put(receivedPacket.getFromAddress(),
                        receivedPacket.getDataAsString());
                mBtArrayAdapter.clear();
                for (Byte fromId : mCurrentResponseMap.keySet()) {
                    mBtArrayAdapter.add("UserId: " + fromId
                            + "\n" + "Message: " + mCurrentResponseMap.get(fromId));
                }
                mBtArrayAdapter.notifyDataSetChanged();
            }
        };

        mLinkLayerManager = new LinkLayerManager(
                getIntent().getByteExtra(EXTRA_OWN_ADDRESS, (byte) -1),
                mBluetoothAdapter,
                discoveryHandler
        );
    }

    public void initialize() {

        mToId = (EditText) findViewById(R.id.et_to_id);
        mBtMessage = (EditText) findViewById(R.id.bluetooth_message);
        mOnBtn = (Button) findViewById(R.id.send);
        mRcvBtn = (Button) findViewById(R.id.receive);
        ListView myListView = (ListView) findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        mBtArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(mBtArrayAdapter);
    }

    public void sendMessage(String msg) {
        mLinkLayerManager.sendData(msg, Byte.parseByte(mToId.getText().toString()));
        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
    }
}
