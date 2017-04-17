package com.yusheng123.widget.banner;

import android.view.View;

/**
 * Created by 花歹 on 2017/4/16.
 * Email:   gatsbywang@126.com
 */

public abstract class BannerAdapter {
    /**
     * 根据位置获取view
     *
     * @param position
     * @return
     */
    public abstract View getView(int position);

    /**
     * 获取轮播的数量
     *
     * @return
     */
    public abstract int getCount();
}
