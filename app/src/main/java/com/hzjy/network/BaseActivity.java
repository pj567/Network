package com.hzjy.network;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.hzjy.network.core.NetType;
import com.hzjy.network.core.Network;

/**
 * pj567
 * 2019/9/9
 */

public abstract class BaseActivity extends FragmentActivity {
    private boolean isPause = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkListener.getInstance().registerObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Network(netType = NetType.AUTO)
    public void onNetChanged(final NetType netType) {
        Log.e("onNetChange",netType+"");
        if (!isPause) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    netChange(netType);
                }
            });
        }
    }

    protected abstract void netChange(NetType netType);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkListener.getInstance().unRegisterObserver(this);
    }
}
