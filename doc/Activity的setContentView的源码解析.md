#### 1、概述
此次源码解读为了解决titleBar的摆放问题，由于Activity的xml文件的根布局有可能是LinearLayout,RelativeLayout,FrameLayout等布局，那么titleBar直接addview(0)就可能出现布局被遮挡问题。
#### 2、源码解读
1、Activity的setContentView方法看起，里面有个 getWindow().setContentView(layoutResID)方法，那么应该去看这个Window里面的setContentView方法，发现这个Window是个抽象类，那么得去看这个gewindow()返回了什么window，可以看出这个window其实是个PhoneWindow.

```
   public void setContentView(@LayoutRes int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
```

```
 public Window getWindow() {
        return mWindow;
    }
```

```
final void attach() {
        attachBaseContext(context);

        mFragments.attachHost(null /*parent*/);

        mWindow = new PhoneWindow(this, window);
       //...省略代码
       }
```


2、PhoneWindow的setContentView方法，这里会判断mContentParent为空则调用 installDecor();

```
 public void setContentView(int layoutResID) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
         if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        //...省略代码
    }
```

3、installDecor代码，这里会创建个mDecor,这个mDecor是个FrameLayout。那么生成这个FrameLayout做了啥？这里有个generateLayout(mDecor)生成了mContentParent,那么这个mContentParent是什么？

```
 private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            mDecor = generateDecor(-1);
            mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mDecor.setIsRootNamespace(true);
            if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
                mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
            }
        } else {
            mDecor.setWindow(this);
        }
        if (mContentParent == null) {
            mContentParent = generateLayout(mDecor);
        }
        //省略代码...
    }
```

```
 protected DecorView generateDecor(int featureId) {
        //...省略代码
        return new DecorView(context, featureId, this, getAttributes());
    }
```

```
public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
//省略代码...
}
```

4、mContentParent的初始化解析，首先加载系统的资源布局，比如layoutResource = R.layout.screen_simple;然后将其加载到mDecor这个FrameLayout中（mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);），这个系统资源布局是个LinearLayout,然后从mDecor找到id为com.android.internal.R.id.content的view并返回，所以这个mContentParent是个FrameLayout.


```
protected ViewGroup generateLayout(DecorView decor) {
        // Apply data from current theme.
        //...省略代码
       else if ((features & (1 << FEATURE_ACTION_MODE_OVERLAY)) != 0) {
            layoutResource = R.layout.screen_simple_overlay_action_mode;
        } else {
            // Embedded, so no decoration is needed.
            layoutResource = R.layout.screen_simple;
            // System.out.println("Simple!");
        }

        mDecor.startChanging();
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);

        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
        //...省略代码
        return contentParent;
    }
```
从 mDecor找到com.android.internal.R.id.content的view
```
public View findViewById(@IdRes int id) {
        return getDecorView().findViewById(id);
    }
```

```
 /**
     * The ID that the main layout in the XML layout file should have.
     */
    public static final int ID_ANDROID_CONTENT = com.android.internal.R.id.content;
```
id为com.android.internal.R.id.content的view是个Fragment
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    android:orientation="vertical">
    <ViewStub android:id="@+id/action_mode_bar_stub"
              android:inflatedId="@+id/action_mode_bar"
              android:layout="@layout/action_mode_bar"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:theme="?attr/actionBarTheme" />
    <FrameLayout
         android:id="@android:id/content"
         android:layout_width="match_parent"
         android:layout_height="match_parent"
         android:foregroundInsidePadding="false"
         android:foregroundGravity="fill_horizontal|top"
         android:foreground="?android:attr/windowContentOverlay" />
</LinearLayout>
```

5、回到phonewindow的setContentView,找到mContentParent,然后将我们自己的xml布局加载到mContentParent中。

```
 mLayoutInflater.inflate(layoutResID, mContentParent);
```


#### 3、总结
setContent大致流程为:
```
graph LR
window-->mDecor
mDecor-->系统的布局linearlayout
id为content的FrameLayout-->我们的布局
```
为了解决titlebar存放位置，所以应该获取mDecor.getChildAt(0)得到系统布局的LinearLayout并放在第一个即可。






