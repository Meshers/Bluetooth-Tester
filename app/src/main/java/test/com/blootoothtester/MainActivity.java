package test.com.blootoothtester;

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
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.network.DeviceDiscoveryHandler;
import test.com.blootoothtester.network.LinkLayerManager;
import test.com.blootoothtester.network.LinkLayerPdu;

public class MainActivity extends AppCompatActivity {

    private Button mOnBtn;
    private Button mOffBtn;
    private Button mRcvBtn;
    private TextView mText;
    private EditText mFromId;
    private EditText mToId;
    private EditText mBtMessage;

    private MyBluetoothAdapter mBluetoothAdapter;
    LinkLayerManager mLinkLayerManager;

    private ArrayAdapter<String> mBtArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();

        mBluetoothAdapter = new MyBluetoothAdapter(MainActivity.this);

        DeviceDiscoveryHandler discoveryHandler = new DeviceDiscoveryHandler() {

            private HashMap<Byte, String> mCurrentResponseMap = new HashMap<>();

            @Override
            public void handleDiscovery(LinkLayerPdu receivedPacket) {
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
                Byte.parseByte(mFromId.getText().toString()),
                mBluetoothAdapter,
                discoveryHandler
        );

        if (!mBluetoothAdapter.isSupported()) {
            mOnBtn.setEnabled(false);
            mOffBtn.setEnabled(false);
            mRcvBtn.setEnabled(false);
            mText.setText("Status: not supported");

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            mOnBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMessage(mBtMessage.getText().toString());
                }
            });


            mOffBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    off(v);
                }
            });


            mRcvBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mLinkLayerManager.startReceiving();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // this takes care of letting the user add the WRITE_SETTINGS permission
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public void initialize() {

        mText = (TextView) findViewById(R.id.text);
        mFromId = (EditText) findViewById(R.id.et_from_id);
        mToId = (EditText) findViewById(R.id.et_to_id);
        mBtMessage = (EditText) findViewById(R.id.bluetooth_message);
        mOnBtn = (Button) findViewById(R.id.send);
        mOffBtn = (Button) findViewById(R.id.turn_off);
        mRcvBtn = (Button) findViewById(R.id.receive);
        ListView myListView = (ListView) findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        mBtArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(mBtArrayAdapter);
    }


    public void sendMessage(String msg) {
        mLinkLayerManager.setFromAddr(Byte.parseByte(mFromId.getText().toString()));
        mLinkLayerManager.sendData(msg, Byte.parseByte(mToId.getText().toString()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String BTName = mBluetoothAdapter.activityResult(requestCode, resultCode);

        if (BTName != null) {

            mText.setText(BTName);

        } else {
            mText.setText("Status: Disabled");
        }

    }

    public void off(View view) {

        if (mBluetoothAdapter.off()) {
            mText.setText("Status: Disconnected");

            Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                    Toast.LENGTH_LONG).show();
        }

    }
}
