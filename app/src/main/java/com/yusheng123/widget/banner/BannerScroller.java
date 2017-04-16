package com.yusheng123.widget.banner;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by 花歹 on 2017/4/16.
 * Email:   gatsbywang@126.com
 */

public class BannerScroller extends Scroller {

    //动画持续的时间
    private int mScrollDuration = 1050;

    public BannerScroller(Context context) {
        super(context);
    }

    public BannerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public BannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    /**
     * 获取切换页面动画的持续时间
     *
     * @return
     */
    public int getScrollDuration() {
        return mScrollDuration;
    }

    /**
     * 设置切换页面动画的持续时间
     *
     * @param scrollDuration 持续时间
     */
    public void setScrollDuration(int scrollDuration) {
        this.mScrollDuration = scrollDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mScrollDuration);
    }
}
