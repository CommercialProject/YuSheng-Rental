package com.yusheng123.order;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.yusheng123.R;
import com.yusheng123.entity.Order;
import com.yusheng123.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderListActivity extends AppCompatActivity implements IView {

    @BindView(R.id.order_RecyclerView)
    XRecyclerView orderRecyclerView;

    private OrderAdapter orderAdapter;

    private IPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        orderRecyclerView.setLayoutManager(layoutManager);
        orderRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));

        mPresenter = new OrderPresenterImpl(this);

        orderAdapter = new OrderAdapter(this, new ArrayList<Order>());

        orderRecyclerView.setAdapter(orderAdapter);

        orderRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                mPresenter.showOrderList();
            }

            @Override
            public void onLoadMore() {
                mPresenter.showOrderList();
            }
        });
//        orderRecyclerView.setAdapter();


    }

    @Override
    public void notifyListView(List<Order> orders) {
        orderAdapter.notifyData(orders);
    }

    @Override
    public void showEmptyView() {

    }
}
