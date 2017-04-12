package com.yusheng123.fix;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yusheng123.R;
public class FixTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_test);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FixTestActivity.this, "Bug修复测试", Toast.LENGTH_LONG).show();
            }
        });
    }

//    private void fixBug() {
//        File fixFile = new File(Environment.getExternalStorageDirectory(), "fix.dex");
//        if (fixFile.exists()) {
//            FixDexManager fixDexManager = new FixDexManager(this);
//            try {
//                fixDexManager.fixDex(fixFile.getAbsolutePath());
//                Toast.makeText(this, "修复成功", Toast.LENGTH_LONG).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(this, "修复失败", Toast.LENGTH_LONG).show();
//            }
//
//        }
//    }
}
