package test.com.blootoothtester.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import test.com.blootoothtester.R;
import test.com.blootoothtester.network.hotspot.HotspotHelper;

public class HotspotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);

        initButtons();
    }

    private void initButtons() {
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

    public void startHotspot(String name) {
        if (!HotspotHelper.enableHotspot(this, name)) {
            Toast.makeText(this,
                    "Error enabling hotspot!!!! :O :O :O Please contact devs immediately!",
                    Toast.LENGTH_LONG).show();
        }
        if (HotspotHelper.isHotspotOn(this)) {
            Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectToHotspot(String name) {
        HotspotHelper.connectToWifi(name, this);
    }

    public static void startHotspotActivity(Context context) {
        Intent intent = new Intent(context, HotspotActivity.class);
        context.startActivity(intent);
    }
}
