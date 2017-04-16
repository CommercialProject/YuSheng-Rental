package com.yusheng123.widget.banner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;


/**
 * 使用之前最后先看下viewpager的setadapter 和 setcurrentItem源码
 * Created by 花歹 on 2017/4/16.
 * Email:   gatsbywang@126.com
 */

public class BannerViewPager extends ViewPager {

    // 页面切换的scroller
    private BannerScroller mBannerScroller;

    //自定义的adapter
    private BannerAdapter mAdapter;

    //发送显示的msg
    private final int SCROLL_MSG = 0x0011;

    //页面切换间隔时间
    private int mDefaultIntervalTime = 2050;

    private final String TAG = BannerViewPager.class.getName();
    //实现自动轮播的handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //每隔一段时间后切换到下一页
            setCurrentItem(getCurrentItem() + 1);
            //不断轮询
            startRoll();
            Log.e(TAG, "handleMessage");
        }
    };

    public BannerViewPager(Context context) {
        this(context, null);
    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        //通过反射设置viewpager内部的scroller,改变动画速率
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            mBannerScroller = new BannerScroller(context);
            scrollerField.set(this, mBannerScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置切换页面动画的持续时间
     *
     * @param scrollDuration 持续时间
     */
    public void setScrollDuration(int scrollDuration) {
        mBannerScroller.setScrollDuration(scrollDuration);
    }

    public void setAdapter(BannerAdapter adapter) {
        mAdapter = adapter;
        setAdapter(new BannerPagerAdapter());
    }

    /**
     * 实现自动轮播
     */
    public void startRoll() {
        //先清除消息
        mHandler.removeMessages(SCROLL_MSG);
        //再添加消息
        mHandler.sendEmptyMessageDelayed(SCROLL_MSG, mDefaultIntervalTime);
    }

    /**
     * 销毁Handler 停止发送，解决内存泄露
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(SCROLL_MSG);
        mHandler = null;
    }

    /**
     * viewpager适配器
     */
    private class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            //为了实现无限循环
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            //官方推荐此写法
            return view == object;
        }

        /**
         * 创建页面
         *
         * @param container viewpager
         * @param position  并不是当前的页面所处的位置，由于创建页面还包括缓存页面，所以此position还可能是缓存页面
         *                  的position
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 采取adapter设计模式，为了完全让用户自定义
            View itemView = mAdapter.getView(position);
            container.addView(itemView);
            return itemView;
        }


        /**
         * 销毁页面
         *
         * @param container viewpager
         * @param position  并不是当前的页面所处的位置，由于销毁页面还包括缓存页面，所以此position还可能是缓存页面
         *                  的position
         * @param object    当前页面的view
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            object = null;
        }
    }
}
