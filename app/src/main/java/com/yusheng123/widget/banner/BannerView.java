package com.yusheng123.widget.banner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yusheng123.R;

/**
 * Created by 花歹 on 2017/4/16.
 * Email:   gatsbywang@126.com
 */

public class BannerView extends RelativeLayout {

    //轮播的viewpager
    private BannerViewPager mBannerViewPager;

    //点的容器
    private LinearLayout mDotContainer;

    //自定义的adapter
    private BannerAdapter mAdapter;

    //选中的drawable指示器
    private Drawable mIndicatorFocusDrawable;

    //默认的drawable指示器
    private Drawable mIndicatorDefaultDrawable;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_banner, this);
        initView();

        mIndicatorFocusDrawable = new ColorDrawable(Color.RED);
        mIndicatorDefaultDrawable = new ColorDrawable(Color.WHITE);
    }

    /**
     * 初始化
     */
    private void initView() {
        mBannerViewPager = (BannerViewPager) findViewById(R.id.banner_vp);
        mDotContainer = (LinearLayout) findViewById(R.id.layout_dot);
    }

    /**
     * 设置适配器
     *
     * @param adapter
     */
    public void setAdapter(BannerAdapter adapter) {
        mBannerViewPager.setAdapter(adapter);
        mAdapter = adapter;
        //初始化点的指示器
        initDotIndicator();
    }

    /**
     * 初始化点的指示器
     */
    private void initDotIndicator() {

        //获取广告位的数量
        int count = mAdapter.getCount();

        for (int i = 0; i < count; i++) {
            DotIndicatorView dotIndicatorView = new DotIndicatorView(getContext());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp2px(8), dp2px(8));

            layoutParams.leftMargin = layoutParams.rightMargin = dp2px(2);

            dotIndicatorView.setLayoutParams(layoutParams);

            dotIndicatorView.setBackgroundColor(Color.RED);
            if (i == 0) {
                dotIndicatorView.setDrawable(mIndicatorFocusDrawable);
            } else {
                dotIndicatorView.setDrawable(mIndicatorDefaultDrawable);
            }

            mDotContainer.addView(dotIndicatorView);
        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * 开始滚动
     */
    public void startRoll() {
        mBannerViewPager.startRoll();
    }
}
