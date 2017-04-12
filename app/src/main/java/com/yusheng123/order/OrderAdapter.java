package com.yusheng123.order;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yusheng123.R;
import com.yusheng123.entity.Order;
import com.yusheng123.util.Url;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Monty on 2017/1/1.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context mContxt;
    private List<Order> mOrderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.mContxt = context;
        this.mOrderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(mContxt).inflate(R.layout.lay_order_item, parent, false));
    }


    public void notifyData(List<Order> orders){
        mOrderList = new ArrayList<>(orders);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = mOrderList.get(position);
        holder.tvOrderCreateTime.setText(order.ctime);
        holder.tvOrderId.setText(order.id);
        holder.tvOrderStatus.setText(order.status);
        Glide.with(mContxt).load(Url.getImageUrl(order.imgPath)).into(holder.ivCrdImg);
        holder.tvCrdName.setText(order.carName);
        holder.tvCrdFeatures.setText(order.carFeatures);
        holder.tvTakeStore.setText(order.takeStore);
        holder.tvYetStore.setText(order.yetStore);
        holder.tvTakeTime.setText(order.takeTime);
        holder.tvYetTime.setText(order.yetTime);
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_order_create_time)
        TextView tvOrderCreateTime;
        @BindView(R.id.tv_order_id)
        TextView tvOrderId;
        @BindView(R.id.tv_order_status)
        TextView tvOrderStatus;
        @BindView(R.id.iv_crd_img)
        ImageView ivCrdImg;
        @BindView(R.id.tv_crd_name)
        TextView tvCrdName;
        @BindView(R.id.tv_crd_features)
        TextView tvCrdFeatures;
        @BindView(R.id.tv_takeStore)
        TextView tvTakeStore;
        @BindView(R.id.tv_takeTime)
        TextView tvTakeTime;
        @BindView(R.id.tv_yetStore)
        TextView tvYetStore;
        @BindView(R.id.tv_yetTime)
        TextView tvYetTime;

        public OrderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
