package com.yusheng123;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.yusheng123.fix.FixDexManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;


/**
 * Created by Monty on 2017/1/2.
 * update by Monty on 2017/4/18
 * 增加Activity全局管理
 */

public class App extends Application {

    private static App app;

    private final String TAG = App.class.getName();

    public static App getInstans() {
        return app;
    }

    private ActivityLifecycleObserver mObserver = new ActivityLifecycleObserver();

    /**
     * 维护Activity 的list
     */
    private static List<Activity> mActivitys = Collections
            .synchronizedList(new LinkedList<Activity>());

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //热修复写在这比较耗时，加载控制在0.4s内是合理的，待优化
        fixBug();
        registerActivityListener();
        initBmob();
    }

    private void initBmob() {
        BmobConfig config = new BmobConfig.Builder(this)
                ////设置appkey
                .setApplicationId(BmobConfigs.Application_Id)
                ////请求超时时间（单位为秒）：默认15s
                .setConnectTimeout(30)
                ////文件分片上传时每片的大小（单位字节），默认512*1024
                .setUploadBlockSize(1024 * 1024)
                ////文件的过期时间(单位为秒)：默认1800s
                .setFileExpiration(2500)
                .build();
        Bmob.initialize(config);
    }

    /**
     * 修复bug
     */
    private void fixBug() {
        try {
            FixDexManager fixDexManager = new FixDexManager(this);
            fixDexManager.loadFixDex();
            Log.i(TAG, "修复成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "修复失败");
        }
    }

    /**
     * 添加Activity到列表中
     *
     * @param activity
     */
    public void pushActivity(Activity activity) {
        mActivitys.add(activity);
    }

    /**
     * 从Activity列表中删除一个Activity
     *
     * @param activity
     */
    public void popActivity(Activity activity) {
        mActivitys.remove(activity);
    }

    /**
     * 获取当前Activity（栈中最后一个压入的）
     */
    public static Activity currentActivity() {
        if (mActivitys == null || mActivitys.isEmpty()) {
            return null;
        }
        Activity activity = mActivitys.get(mActivitys.size() - 1);
        return activity;
    }

    /**
     * 结束当前Activity（最后一个入栈的）
     */
    public static void finishCurrentActivity() {
        if (mActivitys == null || mActivitys.isEmpty()) {
            return;
        }
        finishActivity(getTopActivity());
    }

    /**
     * 结束一个Activity
     *
     * @param activity 要结束的Activity
     */
    public static void finishActivity(Activity activity) {
        if (mActivitys == null || mActivitys.isEmpty()) {
            return;
        }
        if (activity != null) {
            mActivitys.remove(activity);
            activity.finish();
        }
    }

    /**
     * 根据指定类名结束Activity
     *
     * @param cls 要结束的Activity类名
     */
    public static void finishActivity(Class<?> cls) {
        if (mActivitys == null || mActivitys.isEmpty()) {
            return;
        }
        for (Activity activity : mActivitys) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 按照指定类名找到activity
     *
     * @param cls
     * @return
     */
    public static Activity findActivity(Class<?> cls) {
        Activity targetActivity = null;
        if (mActivitys != null) {
            for (Activity activity : mActivitys) {
                if (activity.getClass().equals(cls)) {
                    targetActivity = activity;
                    break;
                }
            }
        }
        return targetActivity;
    }

    /**
     * 返回栈顶的Activity
     *
     * @return
     */
    public static Activity getTopActivity() {
        Activity topActivity = null;
        synchronized (mActivitys) {
            final int index = mActivitys.size() - 1;
            if (index < 0) {
                return null;
            }
            topActivity = mActivitys.get(index);
        }
        return topActivity;

    }

    /**
     * 结束所有Activity
     */
    public static void finishAllActivity() {
        if (mActivitys == null) {
            return;
        }
        for (Activity activity : mActivitys) {
            activity.finish();
        }
        mActivitys.clear();
    }

    /**
     * 退出应用程序
     */
    public static void appExit() {
        try {
            finishAllActivity();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ActivityLifecycleObserver extends Observer implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            /**
             *  监听到 Activity创建事件 将该 Activity 加入list
             */
            pushActivity(activity);

            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityCreated(activity, savedInstanceState);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityStarted(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityResumed(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityPaused(activity);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityStopped(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivitySaveInstanceState(activity, outState);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (null == mActivitys || mActivitys.isEmpty()) {
                return;
            }
            if (mActivitys.contains(activity)) {
                /**
                 *  监听到 Activity销毁事件 将该Activity 从list中移除
                 */
                popActivity(activity);
            }

            //其他观察者同时调用
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onActivityDestroyed(activity);
            }
        }
    }

    /**
     * 注册全局Activity生命周期监听
     */
    public void registerActivityListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(mObserver);
        }
    }


    public void registerActivityLifeCycleObserver(ActivityLifecycleCallbacks observer) {
        if (mObserver != null) {
            mObserver.registerObserver(observer);
        }
    }

    public void unRegisterActivityLifeCycleObserver(ActivityLifecycleCallbacks observer) {
        if (mObserver != null) {
            mObserver.unregisterObserver(observer);
        }
    }
}
