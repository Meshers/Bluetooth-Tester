package test.com.blootoothtester.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import test.com.blootoothtester.R;

import android.content.pm.PackageManager;

public class IdSetActivity extends Activity {

    private EditText mEtOwnAddr;
    private EditText mEtSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_set);

        mEtOwnAddr = (EditText) findViewById(R.id.et_own_addr);
        mEtSessionId = (EditText) findViewById(R.id.et_session_id);

        Button hotspotTestBtn = (Button) findViewById(R.id.id_act_hotspot_btn);
        hotspotTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHotspotActivity();
            }
        });

        makePermissionsRequest();
    }

    public void makePermissionsRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {"android.permission.ACCESS_FINE_LOCATION"};
            requestPermissions(
                    permissions, 1
            );
            // this takes care of letting the user add the WRITE_SETTINGS permission
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                makePermissionsRequest();
                break;
            }
        }
    }

    public void onNextClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        String addrStr = mEtOwnAddr.getText().toString();
        if (addrStr.equals("")) {
            return;
        }
        String sessionId = mEtSessionId.getText().toString();
        if (sessionId.equals("")) {
            return;
        }

        intent.putExtra(MainActivity.EXTRA_OWN_ADDRESS, Byte.parseByte(addrStr));
        intent.putExtra(MainActivity.EXTRA_SESSION_ID, Byte.parseByte(sessionId));

        startActivity(intent);
    }

    public void startHotspotActivity() {
        HotspotActivity.startHotspotActivity(this);
    }
}
