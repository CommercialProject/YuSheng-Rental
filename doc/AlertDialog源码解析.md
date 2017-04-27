#### 1、概述
自定义Dialog，参照AlertDialog的做法。完成我们自己的自定义Dialog，而不是简单的Dialog dialog = new Dialog();dialog.setContentView();等等
#### 2、源码
##### 2.1、先来看看AlertDialog的基本写法。创建builder，通过builder.setxxx方式设置属性，然后create ,show。这就是alertDialog的基本用法。

```
 AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
        .setTitle("AlertDialog")
        .setMessage("这是App的AlertDialog")
        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        });
        builder.create().show();
```
##### 2.2、接下来看看Builder的源码。在这里看出，Builder通过创建一个AlertController.AlertParams来存储我们设置信息。
```
 public static class Builder {
        private final AlertController.AlertParams P;
        
        public Builder(Context context, int themeResId) {
            P = new AlertController.AlertParams(new ContextThemeWrapper(
                    context, resolveDialogTheme(context, themeResId)));
        }
        
        public Builder setTitle(@StringRes int titleId) {
            P.mTitle = P.mContext.getText(titleId);
            return this;
        }
        
        public Builder setMessage(@StringRes int messageId) {
            P.mMessage = P.mContext.getText(messageId);
            return this;
        }
        
        public Builder setIcon(Drawable icon) {
            P.mIcon = icon;
            return this;
        }
        ...省略代码

    }
```
##### 2.3、接下来看看builder.create()干了啥？创建了AlertDialog,并设置一些属性。这里看到熟悉的P，还有dialog.mAlert也是AlertController，至于p.apply()是什么？

```
 public AlertDialog create() {
            // Context has already been wrapped with the appropriate theme.
            final AlertDialog dialog = new AlertDialog(P.mContext, 0, false);
            P.apply(dialog.mAlert);
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
```

```
 private AlertController mAlert;
```
##### 2.4 AlertController.AlertParams.apply()代码。这里的参数dialog是AlertDialog的AlertController，也就是将Builder里的AlertController.AlertParams存储的参数设置给了AlertDialog的AlertController。

```
public void apply(AlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId != 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                        mPositiveButtonListener, null);
            }
            if (mNegativeButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                        mNegativeButtonListener, null);
            }
            if (mNeutralButtonText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                        mNeutralButtonListener, null);
            }
            if (mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                            mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            }

            /*
            dialog.setCancelable(mCancelable);
            dialog.setOnCancelListener(mOnCancelListener);
            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener);
            }
            */
        }
```
#### 3、总结
从AlertDialog源码中看出，AlertDialog的Builder模式：

- Product 产品类 : 产品的抽象类；  （AlertDialog）
- Builder : 抽象类，   规范产品的组建，一般是由子类实现具体的组件过程；  （AlertDialog的内部类Builder）
- ConcreteBuilder : 具体的构建器；  （AlertController）
- Director : 统一组装过程(可省略)。  （builder.create()）

那么我们自定义dialog，也遵循模式来设计。



