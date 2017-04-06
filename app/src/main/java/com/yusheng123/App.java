package com.yusheng123;

import android.app.Application;

/**
 * Created by Monty on 2017/1/2.
 */

public class App extends Application {

    private static App app;

    public static App getInstans(){
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
