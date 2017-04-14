package com.yusheng123;

import android.app.Application;
import android.util.Log;

import com.yusheng123.fix.FixDexManager;

/**
 * Created by Monty on 2017/1/2.
 */

public class App extends Application {

    private static App app;

    private final String TAG = App.class.getName();

    public static App getInstans() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        fixBug();
    }

    /**
     * 修复bug
     */
    private void fixBug() {
        try {
            FixDexManager fixDexManager = new FixDexManager(this);
            fixDexManager.loadFixDex();
            Log.i(TAG, "修复成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "修复失败");
        }
    }
}
