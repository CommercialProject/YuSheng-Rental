package com.yusheng123.order;

import com.yusheng123.entity.Order;

import java.util.List;

/**
 * Created by Monty on 2017/1/4.
 */

public interface IModel {
    void getOrderList(int page, OnOrderListListener onOrderListListener);

    interface OnOrderListListener{
        void onSuccess(List<Order> orders);
        void onError(String errorMsg);
    }
}
