package com.yusheng123;

import android.app.Application;
import android.os.Environment;
import android.widget.Toast;

import com.yusheng123.fix.FixDexManager;

import java.io.File;

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
//        fixBug();
    }

    private void fixBug() {
        File fixFile = new File(Environment.getExternalStorageDirectory(), "fix.dex");
        if (fixFile.exists()) {
            FixDexManager fixDexManager = new FixDexManager(this);
            try {
                fixDexManager.fixDex(fixFile.getAbsolutePath());
                Toast.makeText(this, "修复成功", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "修复失败", Toast.LENGTH_LONG).show();
            }

        }
    }
}
