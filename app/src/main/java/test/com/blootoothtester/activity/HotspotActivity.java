package test.com.blootoothtester.activity;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import test.com.blootoothtester.R;
import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;
import test.com.blootoothtester.network.hotspot.WifiUtils;
import test.com.blootoothtester.network.linklayer.wifi.BtMessage;
import test.com.blootoothtester.network.linklayer.wifi.WifiLlManager;
import test.com.blootoothtester.network.linklayer.wifi.WifiMessage;

public class HotspotActivity extends AppCompatActivity {

    private EditText mMsgEditText;

    private byte mOwnAddress;
    private byte mSessionId;

    private WifiLlManager mWifiLlManager;
    private MyBluetoothAdapter mBluetoothAdapter;

    private final static String EXTRA_OWN_ID = "own_id";
    private final static String EXTRA_SESSION_ID = "session_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);

        mOwnAddress = getIntent().getByteExtra(EXTRA_OWN_ID, (byte) -1);
        mSessionId = getIntent().getByteExtra(EXTRA_SESSION_ID, (byte) -1);

        initViews();
        initNetwork();
    }

    private void initViews() {
        mMsgEditText = (EditText) findViewById(R.id.hotspot_et_msg);

        Button sendBtn = (Button) findViewById(R.id.hotspot_btn_send_msg);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiLlManager.sendWifiMessage(mMsgEditText.getText().toString()
                        .getBytes(WifiMessage.ENCODE_CHARSET));
            }
        });

        Button serveBtn = (Button) findViewById(R.id.hotspot_btn_serve);
        serveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHotspot("lolzzz");
            }
        });

        Button fetchBtn = (Button) findViewById(R.id.hotspot_btn_fetch);
        fetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToHotspot("lolzzz");
            }
        });
    }

    public void initNetwork() {
        mBluetoothAdapter = new MyBluetoothAdapter(this);

        if (!mBluetoothAdapter.isSupported()) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        mWifiLlManager = new WifiLlManager(
                HotspotActivity.this,
                mOwnAddress,
                mSessionId,
                new WifiLlManager.MessageCallback() {
                    @Override
                    public void onReceiveWifiMessage(WifiMessage wifiMessage) {
                        Toast.makeText(
                                HotspotActivity.this,
                                new String(wifiMessage.getBody(), WifiMessage.ENCODE_CHARSET),
                                Toast.LENGTH_LONG
                        ).show();

                        mWifiLlManager.sendBtMessage(
                                wifiMessage.getFromAddress(),
                                wifiMessage.getMsgId(),
                                "I See You!".getBytes(BtMessage.ENCODE_CHARSET)
                        );
                    }

                    @Override
                    public void onReceiveBtMessage(BtMessage btMessage) {
                        Toast.makeText(
                                HotspotActivity.this,
                                new String(btMessage.getBody(), BtMessage.ENCODE_CHARSET),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onAckedByWifi() {
                        Toast.makeText(
                                HotspotActivity.this,
                                "We've just been acked!!!! :D :D :D",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                mBluetoothAdapter
        );
        mWifiLlManager.startReceivingWifiMessages();
    }

    public void startHotspot(String name) {
        if (!WifiUtils.enableHotspot(this, name)) {
            Toast.makeText(this,
                    "Error enabling hotspot!!!! :O :O :O Please contact devs immediately!",
                    Toast.LENGTH_LONG).show();
        }
        if (WifiUtils.isHotspotOn(this)) {
            Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectToHotspot(String name) {
        WifiUtils.connectToWifi(name, this);
    }

    public static void startHotspotActivity(Context context, byte ownId, byte sessionId) {
        Intent intent = new Intent(context, HotspotActivity.class);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        intent.putExtra(EXTRA_OWN_ID, ownId);
        context.startActivity(intent);
    }
}
