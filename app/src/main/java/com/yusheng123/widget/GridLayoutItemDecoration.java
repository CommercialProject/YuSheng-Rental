package com.yusheng123.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 花歹 on 2017/4/21.
 * Email:   gatsbywang@126.com
 */

public class GridLayoutItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDrivider;
    private final int mDefaultOffsetHeight;
    private final int mDefaultOffsetWidth;

    public GridLayoutItemDecoration(Context context, int drawableResourceId) {
        mDrivider = ContextCompat.getDrawable(context, drawableResourceId);
        mDefaultOffsetHeight = mDrivider.getIntrinsicHeight();
        mDefaultOffsetWidth = mDrivider.getIntrinsicWidth() > 0 ? mDrivider.getIntrinsicWidth() : mDefaultOffsetHeight;
    }

    //留出位置
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        outRect.bottom = mDrivider.getIntrinsicHeight();
//        outRect.right = mDrivider.getIntrinsicWidth() == 0 ? mDrivider.getIntrinsicHeight() : mDrivider.getIntrinsicWidth();

        int bottom = mDrivider.getIntrinsicHeight();
        int right = mDrivider.getIntrinsicWidth() > 0 ? mDrivider.getIntrinsicWidth() : mDrivider.getIntrinsicHeight();
        if (isLastColumn(view, parent)) {
            right = 0;
        }

        if (isLastRaw(view, parent)) {
            bottom = 0;
        }

        outRect.bottom = bottom;
        outRect.right = right;
    }

    //绘制 分割线
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    private boolean isLastColumn(View view, RecyclerView parent) {
        //获取当前位置
        int currentPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        int spanCount = getSpanCount(parent);
        return (currentPosition + 1) % spanCount == 0;
    }

    private int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int spanCount = 1;
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    private boolean isLastRaw(View view, RecyclerView parent) {

        //获取当前位置
        int currentPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        //当前位置 > (行数-1)*列数

        //列数
        int spanCount = getSpanCount(parent);

        //行数
        int raw = parent.getAdapter().getItemCount() % spanCount == 0 ? parent.getAdapter().getItemCount() / spanCount :
                (parent.getAdapter().getItemCount() / spanCount) + 1;


        return currentPosition > (raw - 1) * spanCount;
    }

    /**
     * 绘制水平方向分割线
     *
     * @param c
     * @param parent
     */
    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
            int top = childView.getTop() - layoutParams.topMargin;
            int left = childView.getRight() + layoutParams.rightMargin;
            int right = left + mDefaultOffsetWidth;
            int bottom = childView.getBottom() + layoutParams.bottomMargin;
            mDrivider.setBounds(left, top, right, bottom);
            mDrivider.draw(c);
        }
    }

    /**
     * 绘制垂直方向分割线
     *
     * @param c
     * @param parent
     */
    private void drawVertical(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
            int left = childView.getLeft() - layoutParams.leftMargin;
            int right = childView.getRight() + mDefaultOffsetWidth + layoutParams.rightMargin;
            int top = childView.getBottom() + layoutParams.bottomMargin;
            int bottom = top + mDrivider.getIntrinsicHeight();
            mDrivider.setBounds(left, top, right, bottom);
            mDrivider.draw(c);
        }
    }
}
