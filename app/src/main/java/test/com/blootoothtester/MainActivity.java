package test.com.blootoothtester;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;

import test.com.blootoothtester.network.LinkLayerManager;
import test.com.blootoothtester.network.LinkLayerPdu;

public class MainActivity extends AppCompatActivity {

    private Button onBtn;
    private Button offBtn;
    private Button rcvBtn;
    private TextView text;
    private EditText BTId;
    private EditText BTMessage;

    private MyBluetoothAdapter myBluetoothAdapter;
    private Message messageObj;

    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initialize();

        myBluetoothAdapter = new MyBluetoothAdapter(MainActivity.this, BTArrayAdapter);
        if (!myBluetoothAdapter.isSupported()) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            rcvBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            onBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMessage(BTMessage.getText().toString());
                }
            });


            offBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    off(v);
                }
            });


            rcvBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    receiveMessage(v);
                }
            });


            messageObj = new Message(myBluetoothAdapter);
        }

        try {
            LinkLayerPdu message = new LinkLayerPdu((byte) 1, (byte) 2, "Hello".getBytes("UTF-8"));

            byte encoded[] = message.encode();

            LinkLayerPdu received = new LinkLayerPdu(encoded);

            System.out.println(new String(received.getData()));
            System.out.println(new String(encoded));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                // Do stuff here
            }
            else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void initialize() {

        text = (TextView) findViewById(R.id.text);
        BTId = (EditText) findViewById(R.id.bluetooth_id);
        BTMessage = (EditText) findViewById(R.id.bluetooth_message);
        onBtn = (Button) findViewById(R.id.send);
        offBtn = (Button) findViewById(R.id.turnOff);
        rcvBtn = (Button) findViewById(R.id.receive);
        myListView = (ListView) findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        BTArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
    }


    public void sendMessage(String msg) {
        LinkLayerManager linkLayerManager = new LinkLayerManager(
                Byte.parseByte(BTId.getText().toString()),
                myBluetoothAdapter
        );
        linkLayerManager.sendData(msg, (byte)5);
//        if (messageObj.send(BTId.getText().toString(), BTMessage.getText().toString())) {
//
//            Toast.makeText(getApplicationContext(), "Bluetooth turned on",
//                    Toast.LENGTH_LONG).show();
//        } else {
//
//            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
//                    Toast.LENGTH_LONG).show();
//        }
    }

    public void receiveMessage(View view) {

        BTArrayAdapter = messageObj.recieve();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String BTName = myBluetoothAdapter.activityResult(requestCode, resultCode, data);

        if (BTName != null) {

            text.setText(BTName);

        } else {
            text.setText("Status: Disabled");
        }

    }

    public void off(View view) {

        if (myBluetoothAdapter.off()) {
            text.setText("Status: Disconnected");

            Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                    Toast.LENGTH_LONG).show();
        }

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
