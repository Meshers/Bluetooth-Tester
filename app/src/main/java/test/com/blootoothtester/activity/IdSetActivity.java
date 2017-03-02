package test.com.blootoothtester.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import test.com.blootoothtester.R;

public class IdSetActivity extends Activity {

    private EditText mEtOwnAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_set);

        mEtOwnAddr = (EditText) findViewById(R.id.et_own_addr);
    }

    public void onNextClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        String addrStr = mEtOwnAddr.getText().toString();
        if (addrStr.equals("")) {
            return;
        }

        intent.putExtra(MainActivity.EXTRA_OWN_ADDRESS, Byte.parseByte(addrStr));

        startActivity(intent);
    }
}
