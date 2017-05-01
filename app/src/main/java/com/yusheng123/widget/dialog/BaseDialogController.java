package com.yusheng123.widget.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * Created by 花歹 on 2017/4/30.
 * Email:   gatsbywang@126.com
 */

public class BaseDialogController {


    private final BaseDialog mDialog;
    private final Window mWindow;
    private DialogViewHelper mViewHelper;

    public BaseDialogController(BaseDialog dialog, Window window) {
        mDialog = dialog;
        mWindow = window;
    }

    protected void setViewHelper(DialogViewHelper viewHelper) {
        mViewHelper = viewHelper;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     */
    protected void setText(int viewId, CharSequence text) {
        mViewHelper.setText(viewId, text);
    }

    /**
     * 获取view,解决findviewByid多次调用
     *
     * @param viewId
     * @param <T>
     * @return
     */
    protected <T extends View> T getView(int viewId) {

        return mViewHelper.getView(viewId);
    }


    /**
     * 设置点击事件
     *
     * @param viewId
     * @param onClickListenerWeakReference
     */
    protected void setOnClickListener(int viewId, WeakReference<View.OnClickListener> onClickListenerWeakReference) {
        mViewHelper.setOnClickListener(viewId, onClickListenerWeakReference);
    }


    public static class BaseDialogParams {
        public final Context mContext;
        public final int mThemeResId;
        // 点击空白是否能够取消
        public boolean mCancelable = true;

        //dialog cancel监听
        public DialogInterface.OnCancelListener mOnCancelListener;

        //dialog dismiss监听
        public DialogInterface.OnDismissListener mOnDismissListener;

        //dialog 系统按键监听
        public DialogInterface.OnKeyListener mOnKeyListener;

        //dialog contentView
        public View mView;

        //dialog contentView的资源id
        public int mViewLayoutResId;

        //存放字体
        public SparseArray<CharSequence> mTextArray = new SparseArray<>();

        //存放点击事件
        public SparseArray<WeakReference<View.OnClickListener>> mClickArray = new SparseArray<>();

        //dialog的宽
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;

        //dialog的高
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

        //是否有动画
        public int mAnimation = 0;

        //位置
        public int mGravity = Gravity.CENTER;

        public BaseDialogParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
        }

        /**
         * 绑定和设置参数
         *
         * @param dialog
         */
        public void apply(BaseDialogController dialog) {
            //流程
            //1、设置布局 DialogHelper
            DialogViewHelper dialogViewHelper = null;
            if (mViewLayoutResId != 0) {
                dialogViewHelper = new DialogViewHelper(mContext, mViewLayoutResId);
            }

            if (mView != null) {
                dialogViewHelper = new DialogViewHelper(mView);
            }

            if (dialogViewHelper == null) {
                throw new IllegalArgumentException("请设置布局setContentView");
            }

            dialog.setViewHelper(dialogViewHelper);

            dialog.mDialog.setContentView(dialogViewHelper.getContentView());

            //2、设置文本
            int textArraySize = mTextArray.size();
            for (int i = 0; i < textArraySize; i++) {
                dialog.setText(mTextArray.keyAt(i), mTextArray.valueAt(i));
            }

            //3、设置点击事件
            int clickArraySize = mClickArray.size();
            for (int i = 0; i < clickArraySize; i++) {
                dialog.setOnClickListener(mClickArray.keyAt(i), mClickArray.valueAt(i));
            }

            //4、设置动画等
            Window window = dialog.mWindow;
            //设置位置
            window.setGravity(mGravity);
            //设置动画
            if (mAnimation != 0) {
                window.setWindowAnimations(mAnimation);
            }
            //设置宽高
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = mWidth;
            params.height = mHeight;
            window.setAttributes(params);

        }
    }
}
