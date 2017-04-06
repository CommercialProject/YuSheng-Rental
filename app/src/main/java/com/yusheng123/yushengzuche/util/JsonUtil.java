package com.yusheng123.yushengzuche.util;

import android.content.res.AssetManager;

import com.yusheng123.yushengzuche.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Monty on 2017/1/2.
 */

public class JsonUtil {
    public static String getOrderJson() {
        StringBuffer stringBuffer = new StringBuffer();
        AssetManager assetManager = App.getInstans().getApplicationContext().getAssets();
        try {
            InputStream is = assetManager.open("order.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String str = null;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }
}
