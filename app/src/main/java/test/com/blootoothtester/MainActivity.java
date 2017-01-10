package test.com.blootoothtester;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    private EditText BTId;
    private EditText BTMessage;

    private MyBluetoothAdapter myBluetoothAdapter;
    private SendMessage sendMessage;

    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initialize();

        myBluetoothAdapter = new MyBluetoothAdapter(MainActivity.this, BTArrayAdapter);
        if(!myBluetoothAdapter.isSupported()) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {

            onBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    on(v);
                }
            });


            offBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    off(v);
                }
            });


            listBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);
                }
            });


            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    find(v);

                }
            });


        }
    }

    public void initialize(){

        text = (TextView) findViewById(R.id.text);
        BTId = (EditText) findViewById(R.id.bluetooth_id);
        BTMessage = (EditText) findViewById(R.id.bluetooth_message);
        onBtn = (Button)findViewById(R.id.turnOn);
        offBtn = (Button)findViewById(R.id.turnOff);
        listBtn = (Button)findViewById(R.id.paired);
        findBtn = (Button)findViewById(R.id.search);
        myListView = (ListView)findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        BTArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
    }


    public void on(View view){

        sendMessage = new SendMessage(BTId.getText().toString(), BTMessage.getText().toString(), myBluetoothAdapter);

        if (sendMessage.send()) {

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{

            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        String BTName = myBluetoothAdapter.activityResult(requestCode, resultCode, data);

            if(BTName != null) {

                text.setText(BTName);

            } else {
                text.setText("Status: Disabled");
            }

    }

    public void list(View view){
        // get paired devices

        myBluetoothAdapter.paired();

        BTArrayAdapter = myBluetoothAdapter.getBTArrayAdapter();

        Toast.makeText(getApplicationContext(),"Show Paired Devices"+BTArrayAdapter.getItem(0),
                Toast.LENGTH_SHORT).show();
    }


    public void find(View view) {

        myBluetoothAdapter.find();

        BTArrayAdapter = myBluetoothAdapter.getBTArrayAdapter();

    }

    public void off(View view){

        if(myBluetoothAdapter.off()){
            text.setText("Status: Disconnected");

            Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        //myBluetoothAdapter.destroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
