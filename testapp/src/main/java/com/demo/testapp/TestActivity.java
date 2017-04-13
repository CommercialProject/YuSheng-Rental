package com.demo.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Button btn = (Button) findViewById(R.id.btn_test);
        btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, 2/0+"测试", Toast.LENGTH_LONG).show();
    }
}
