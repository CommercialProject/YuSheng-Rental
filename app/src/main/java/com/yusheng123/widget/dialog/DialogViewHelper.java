package com.yusheng123.widget.dialog;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by 花歹 on 2017/5/1.
 * Email:   gatsbywang@126.com
 */

public class DialogViewHelper {

    private View mContentView;
    private final String TAG = DialogViewHelper.class.getName();
    private SparseArray<WeakReference<View>> mViews;

    public DialogViewHelper(Context context, int viewLayoutResId) {
        this(LayoutInflater.from(context).inflate(viewLayoutResId, null));
    }

    public DialogViewHelper(View view) {
        mContentView = view;
        mViews = new SparseArray<>();
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     */
    void setText(int viewId, CharSequence text) {
        TextView textView = getView(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * 获取view,解决findviewByid多次调用
     *
     * @param viewId
     * @param <T>
     * @return
     */
    <T extends View> T getView(int viewId) {
        WeakReference<View> viewWeakReference = mViews.get(viewId);
        View view = null;
        if (viewWeakReference != null) {
            view = viewWeakReference.get();
        }
        if (view == null) {
            view = mContentView.findViewById(viewId);
            if (view != null) {
                mViews.put(viewId, new WeakReference<View>(view));
            }
        }
        return (T) view;
    }


    /**
     * 设置点击事件
     *
     * @param viewId
     * @param onClickListenerWeakReference
     */
    void setOnClickListener(int viewId, WeakReference<View.OnClickListener> onClickListenerWeakReference) {
        View.OnClickListener onClickListener = onClickListenerWeakReference.get();
        if (onClickListener == null) {
            Log.e(TAG, "OnClickListener is null! Maybe not initialize or recycle of Low memory. ");
            return;
        }
        View view = getView(viewId);
        if (view != null) {
            view.setOnClickListener(onClickListener);
        }
    }

    /**
     * 获取contentView
     *
     * @return
     */
    View getContentView() {
        return mContentView;
    }
}
