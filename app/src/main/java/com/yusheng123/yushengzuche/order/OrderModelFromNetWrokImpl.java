package com.yusheng123.yushengzuche.order;

import android.util.Log;

import com.yusheng123.yushengzuche.util.Url;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Created by Monty on 2017/1/6.
 */

public class OrderModelFromNetWrokImpl extends AbsModel {
    private final String TAG = OrderModelFromNetWrokImpl.this.getClass().getSimpleName();

    @Override
    public void getOrderList(int page, final OnOrderListListener onOrderListListener) {

        if (page == 0 || page == 1) {
            page = 1;
        }
        OkHttpUtils
                .get()
                .url(Url.getOrderPath())
                .addParams("pageNumber", page + "")
                .addHeader("Cookie", "JSESSIONID=B728E7735FEE5E94E2D78886014FAD7A")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, e.getMessage());
                        onOrderListListener.onError(e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, response);
                        onOrderListListener.onSuccess(parseOrderList(response));
                    }
                });
    }


}
