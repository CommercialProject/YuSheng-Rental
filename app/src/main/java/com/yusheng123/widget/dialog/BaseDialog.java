package com.yusheng123.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.yusheng123.R;

import java.lang.ref.WeakReference;

/**
 * Created by 花歹 on 2017/4/30.
 * Email:   gatsbywang@126.com
 */

public class BaseDialog extends Dialog {
    private BaseDialogController mController;

    private BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        mController = new BaseDialogController(this, getWindow());
    }


    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     */
    public void setText(int viewId, CharSequence text) {
        mController.setText(viewId, text);
    }

    /**
     * 获取view
     *
     * @param viewId
     * @param <T>
     * @return
     */
    private <T extends View> T getView(int viewId) {
        return mController.getView(viewId);
    }


    /**
     * 设置点击事件
     *
     * @param viewId
     * @param onClickListener
     */
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        mController.setOnClickListener(viewId, new WeakReference<View.OnClickListener>(onClickListener));
    }


    public static class Builder {
        private final BaseDialogController.BaseDialogParams P;


        public Builder(Context context) {
            this(context, R.style.BaseDialogTheme);
        }

        public Builder(Context context, int themeResId) {
            P = new BaseDialogController.BaseDialogParams(context, themeResId);
        }

        /**
         * 设置布局
         *
         * @param layoutResId 资源id
         * @return
         */
        public Builder setContentView(int layoutResId) {
            P.mView = null;
            P.mViewLayoutResId = layoutResId;
            return this;
        }

        /**
         * 设置布局
         *
         * @param view
         * @return
         */
        public Builder setContentView(View view) {
            P.mView = view;
            P.mViewLayoutResId = 0;
            return this;
        }

        /**
         * 设置是否能外部隐藏dialog
         *
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        /**
         * 设置 当dialog被cancel的回调
         *
         * @return 返回Builder
         */
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        /**
         * 设置 当dialog被dismiss的回调
         *
         * @return 返回Builder
         */
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        /**
         * 设置 dialog的按钮事件
         *
         * @return 返回Builder
         */
        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        /**
         * 设置文本
         *
         * @param viewId
         * @param charSequence
         * @return
         */
        public Builder setText(int viewId, CharSequence charSequence) {
            P.mTextArray.put(viewId, charSequence);
            return this;
        }

        /**
         * 设置点击事件
         *
         * @param viewId
         * @param onClickListener
         * @return
         */
        public Builder setOnClickListener(int viewId, View.OnClickListener onClickListener) {
            P.mClickArray.put(viewId, new WeakReference<View.OnClickListener>(onClickListener));
            return this;
        }

        /**
         * 全屏
         *
         * @return
         */
        public Builder fullWidth() {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        /**
         * 底部弹出
         *
         * @param isAnimation 是否有动画
         * @return
         */
        public Builder fromBottom(boolean isAnimation) {
            if (isAnimation) {
                P.mAnimation = R.style.animation_dialog_bottom;
            }
            P.mGravity = Gravity.BOTTOM;
            return this;
        }

        /**
         * 设置dialog的宽高
         *
         * @param width
         * @param height
         * @return
         */
        public Builder setWidthAndHeight(int width, int height) {

            P.mWidth = width;
            P.mHeight = height;
            return this;
        }

        /**
         * 设置默认动画效果
         *
         * @return
         */
        public Builder addDefaultAnimation() {
            P.mAnimation = R.style.animation_dialog_bottom;
            return this;
        }

        /**
         * 设置自定义动画
         *
         * @return
         */
        public Builder setAnimation(int styleAnimation) {
            P.mAnimation = styleAnimation;
            return this;
        }


        /**
         * 创建Dialog实例
         *
         * @return
         */
        public BaseDialog create() {
            // Context has already been wrapped with the appropriate theme.
            final BaseDialog dialog = new BaseDialog(P.mContext, P.mThemeResId);
            P.apply(dialog.mController);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        /**
         * 显示
         *
         * @return
         */
        public BaseDialog show() {
            final BaseDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
