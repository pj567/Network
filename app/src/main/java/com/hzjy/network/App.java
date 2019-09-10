package com.hzjy.network;

import android.app.Application;

/**
 * pj567
 * 2019/9/9
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NetworkListener.getInstance().init(this);
    }
}
