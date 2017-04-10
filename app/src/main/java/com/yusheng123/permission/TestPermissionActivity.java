package com.yusheng123.permission;

import android.app.Activity;
import android.os.Bundle;

/**
 * Todo 测试activity 待删除
 * Created by wang on 2017/4/10.
 */
public class TestPermissionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.requestPermissionResult(this, requestCode, permissions);

    }
}
