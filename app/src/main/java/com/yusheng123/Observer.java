package com.yusheng123;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

import static android.app.Application.ActivityLifecycleCallbacks;

/**
 * Created by 花歹 on 2017/4/26.
 * Email:   gatsbywang@126.com
 */

public class Observer{

    protected ArrayList<ActivityLifecycleCallbacks> mObservers = new ArrayList<>();

    public void registerObserver(ActivityLifecycleCallbacks observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                throw new IllegalStateException("Observer " + observer + " is already registered.");
            }
            mObservers.add(observer);
        }
    }

    public void unregisterObserver(ActivityLifecycleCallbacks observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
                throw new IllegalStateException("Observer " + observer + " was not registered.");
            }
            mObservers.remove(index);
        }
    }

}
