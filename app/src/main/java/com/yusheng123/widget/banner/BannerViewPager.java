package com.yusheng123.widget.banner;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.yusheng123.App;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
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
    private int mDefaultIntervalTime = 3500;

    private final String TAG = BannerViewPager.class.getName();
    //实现自动轮播的handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //每隔一段时间后切换到下一页
            setCurrentItem(getCurrentItem() + 1);
            //不断轮询
            startRoll();
        }
    };

    //复用界面
    private List<View> mConverView;

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

        mConverView = new ArrayList<>();
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

        //管理activity生命周期
        Application application = ((Activity) getContext()).getApplication();
        if (application instanceof App) {
            ((App) application).registerActivityLifeCycleObserver(activityLifecycleCallbacks);
        }
    }

    /**
     * 实现自动轮播
     */
    public void startRoll() {
        //先清除消息
        mHandler.removeMessages(SCROLL_MSG);
        //再添加消息
        mHandler.sendEmptyMessageDelayed(SCROLL_MSG, mDefaultIntervalTime);

        Log.e(TAG, "startRoll");
    }

    /**
     * 销毁Handler 停止发送，解决内存泄露
     */
    @Override
    protected void onDetachedFromWindow() {
        //清除消息，handler的内存优化
        mHandler.removeMessages(SCROLL_MSG);
        mHandler = null;
        //解除绑定
        Application application = ((Activity) getContext()).getApplication();
        if (application instanceof App) {
            ((App) application).unRegisterActivityLifeCycleObserver(activityLifecycleCallbacks);
        }
        super.onDetachedFromWindow();
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
         * @param position
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            // 采取adapter设计模式，为了完全让用户自定义
            //position 0 -> 2的31
            View itemView = mAdapter.getView(position % mAdapter.getCount(), getConverView());
            container.addView(itemView);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.click(position % mAdapter.getCount());
                    }
                }
            });
            return itemView;
        }

        /**
         * 获取复用界面
         *
         * @return
         */
        private View getConverView() {
            for (int i = 0; i < mConverView.size(); i++) {
                //获取没有在viewpager中的界面
                if (mConverView.get(i).getParent() == null) {
                    return mConverView.get(i);
                }
            }
            return null;
        }


        /**
         * 销毁页面
         *
         * @param container viewpager
         * @param position
         * @param object    当前页面的view
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mConverView.add((View) object);
        }
    }


    private BannerItemClickListener mListener;

    public void setOnBannerItemClickListener(BannerItemClickListener listener) {
        this.mListener = listener;
    }

    public interface BannerItemClickListener {
        public void click(int position);
    }

    //管理Activity的生命周期
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new DefaultActivityLifecycleCallbaks() {


        @Override
        public void onActivityResumed(Activity activity) {
            //判断是不是监听了当前的activity的生命周期
            if (activity == getContext()) {
                //开启轮播
                mHandler.sendEmptyMessageDelayed(SCROLL_MSG, mDefaultIntervalTime);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (activity == getContext()) {
                //暂停轮播
                mHandler.removeMessages(SCROLL_MSG);
            }
        }

    };
}
