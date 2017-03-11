package test.com.blootoothtester.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import test.com.blootoothtester.R;
import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.network.linklayer.DeviceDiscoveryHandler;
import test.com.blootoothtester.network.linklayer.LinkLayerManager;
import test.com.blootoothtester.network.linklayer.LlMessage;
import test.com.blootoothtester.util.Logger;

public class MainActivity extends AppCompatActivity {

    private Button mSendBtn;
    private Button mResetBtn;

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


        if (!mBluetoothAdapter.isSupported()) {
            mSendBtn.setEnabled(false);
            mResetBtn.setEnabled(false);

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            mSendBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMessage(mBtMessage.getText().toString());
                }
            });

            mResetBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        initializeNetworkComponents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MyBluetoothAdapter.REQUEST_ENABLE_BT) {
            mBluetoothAdapter.activityResult(requestCode, resultCode);
        }
    }

    public void startReceiving() {
        Toast.makeText(MainActivity.this, "Starting to receive", Toast.LENGTH_SHORT).show();
        mLinkLayerManager.startReceiving();
    }

    public void initializeNetworkComponents() {

        DeviceDiscoveryHandler discoveryHandler = new DeviceDiscoveryHandler() {

            private HashMap<Byte, List<String>> mCurrentResponseMap = new HashMap<>();

            @Override
            public void handleDiscovery(LlMessage llMessage) {
                mLogger.d("MainActivity", "Packet for " + llMessage.getToAddress()
                        + " received from " + llMessage.getFromAddress() + " with content"
                        + llMessage.getDataAsString());

                byte fromAddress = llMessage.getFromAddress();
                if (!mCurrentResponseMap.containsKey(fromAddress)) {
                    mCurrentResponseMap.put(fromAddress, new ArrayList<String>());
                }

                mCurrentResponseMap.get(llMessage.getFromAddress())
                        .add(llMessage.getDataAsString());
                mBtArrayAdapter.clear();
                for (Byte fromId : mCurrentResponseMap.keySet()) {
                    for(String msg: mCurrentResponseMap.get(fromAddress)) {
                        mBtArrayAdapter.add("UserId: " + fromId
                                + "\n" + "Message: " + mCurrentResponseMap.get(fromId));
                    }
                }
                mBtArrayAdapter.notifyDataSetChanged();
            }
        };

        if (mLinkLayerManager != null) {
            mLinkLayerManager.cleanUp();
        }

        mLinkLayerManager = new LinkLayerManager(
                getIntent().getByteExtra(EXTRA_OWN_ADDRESS, (byte) -1),
                mBluetoothAdapter,
                discoveryHandler
        );

        startReceiving();
    }


    public void initialize() {
        mToId = (EditText) findViewById(R.id.et_to_id);
        mBtMessage = (EditText) findViewById(R.id.bluetooth_message);
        mSendBtn = (Button) findViewById(R.id.send_btn);
        mResetBtn = (Button) findViewById(R.id.reset_btn);
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
