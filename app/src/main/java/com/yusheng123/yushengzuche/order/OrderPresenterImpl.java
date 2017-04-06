package com.yusheng123.yushengzuche.order;

import android.util.Log;

import com.yusheng123.yushengzuche.entity.Order;

import java.util.List;

/**
 * Created by Monty on 2017/1/4.
 */
public class OrderPresenterImpl implements IPresenter {
    private IView mView ;
    private IModel mModel;
    public OrderPresenterImpl(IView view){
        this.mView = view;
        this.mModel = new OrderModelFromNetWrokImpl();
    }
    @Override
    public void showOrderList() {
        mModel.getOrderList(0, new IModel.OnOrderListListener() {
            @Override
            public void onSuccess(List<Order> orders) {
                mView.notifyListView(orders);
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("OrderPresenterImpl","加载失败");
            }
        });

    }

    @Override
    public void showMoreOrderList() {

    }
}
