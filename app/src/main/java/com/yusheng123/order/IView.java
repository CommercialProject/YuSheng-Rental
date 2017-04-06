package com.yusheng123.order;

import com.yusheng123.entity.Order;

import java.util.List;

/**
 * Created by Monty on 2017/1/4.
 */

public interface IView {
    void notifyListView(List<Order> orders);
    void showEmptyView();
}
