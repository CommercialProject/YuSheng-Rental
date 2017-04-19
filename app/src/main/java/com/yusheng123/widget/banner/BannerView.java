package com.yusheng123.widget.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    //当前位置
    private int mCurrentPosition;

    //广告位描述
    private TextView mTvIndicator;

    //顶部容器
    private RelativeLayout mBottomIndicator;

    //宽高比例
    private float mWidthPropertion, mHeightPropertion;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_banner, this);

        initAttribute(attrs);

        initView();
        mIndicatorFocusDrawable = new ColorDrawable(Color.YELLOW);
        mIndicatorDefaultDrawable = new ColorDrawable(Color.WHITE);
    }

    private void initAttribute(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BannerView);

        //获取宽高比例
        mWidthPropertion = array.getFloat(R.styleable.BannerView_widthPropertion, mWidthPropertion);
        mHeightPropertion = array.getFloat(R.styleable.BannerView_heightPropertion, mHeightPropertion);
        array.recycle();
    }

    /**
     * 初始化
     */
    private void initView() {
        mBottomIndicator = (RelativeLayout) findViewById(R.id.layout_indicator);
        mBannerViewPager = (BannerViewPager) findViewById(R.id.banner_vp);
        mDotContainer = (LinearLayout) findViewById(R.id.container_dot);
        mTvIndicator = (TextView) findViewById(R.id.tv_indicator);
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
        //设置滚动监听，实现广告位描述和点的变化
        mBannerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pageSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //初始化的时候需要设置广告位描述为第一条广告位描述
        String indicatorText = mAdapter.getIndicatorText(0);
        mTvIndicator.setText(indicatorText);
        //TODO 设置宽高，post会过慢，待定
        this.post(new Runnable() {
            @Override
            public void run() {
                setHeightAndWidth();
            }
        });
    }

    private void setHeightAndWidth() {
        //动态指定高度
        if (mHeightPropertion == 0 || mWidthPropertion == 0) {
            return;
        }
        //动态指定宽高 计算高度
        int width = getMeasuredWidth();
        int height = (int) (width * mHeightPropertion / mWidthPropertion);
        //指定宽高
        getLayoutParams().height = height;
    }


    /**
     * 页面切换的回调
     *
     * @param position
     */
    private void pageSelect(int position) {
        //1、将之前的点设置为默认
        DotIndicatorView oldDotIndicatorView = (DotIndicatorView) mDotContainer.getChildAt(mCurrentPosition);
        oldDotIndicatorView.setDrawable(mIndicatorDefaultDrawable);
        //position ->  0-->2的31次方
        mCurrentPosition = position % mAdapter.getCount();
        //2、把当前位置的点选中
        DotIndicatorView currentDotIndicatorView = (DotIndicatorView) mDotContainer.getChildAt(mCurrentPosition);
        currentDotIndicatorView.setDrawable(mIndicatorFocusDrawable);


        //3、更改广告描述（租车无此要求,需要广告位时再重写）
        String indicatorText = mAdapter.getIndicatorText(mCurrentPosition);
        mTvIndicator.setText(indicatorText);
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
