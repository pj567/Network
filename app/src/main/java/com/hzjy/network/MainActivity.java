package com.hzjy.network;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.hzjy.network.core.NetType;

public class MainActivity extends BaseActivity {
    private TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = findViewById(R.id.msg);
    }

    @Override
    protected void netChange(NetType netType) {
        msg.setText("" + netType);
    }
}
