#### 1、概述
此次resource源码解读，主要是是插件换肤的核心点。通过加载resource资源完成换肤功能。
#### 2、源码解读
 imageview可以通过设置setImageDrawable方法设置图片，那么drawable的获取可以通过getResource.getDrawable方法。那么应该来看看getResource是怎么得到resource。
#####  2.1 getReource方法

```
 Context mBase;

public ContextWrapper(Context base) {
    mBase = base;
}

public Context createConfigurationContext(Configuration overrideConfiguration) {
        return mBase.createConfigurationContext(overrideConfiguration);
}

 private Resources getResourcesInternal() {
        if (mResources == null) {
            if (mOverrideConfiguration == null) {
                mResources = super.getResources();
            } else {
                final Context resContext = createConfigurationContext(mOverrideConfiguration);
                mResources = resContext.getResources();
            }
        }
        return mResources;
}
```
从上面几段代码可以看出，从context的getResources方法得到resource,可是Context是个抽象类，这需要找到mBase的实现类是什么才可以得到具体的getReources方法逻辑。

##### 2.2 ContextImpl的getResources

```
public Resources getResources() {
        return mResources;
}

 private ContextImpl(ContextImpl container, ActivityThread mainThread,
            LoadedApk packageInfo, IBinder activityToken, UserHandle user, boolean restricted,
            Display display, Configuration overrideConfiguration, int createDisplayWithId) {
        mOuterContext = this;

    //省略代码...
        Resources resources = packageInfo.getResources(mainThread);
        if (resources != null) {
            if (displayId != Display.DEFAULT_DISPLAY
                    || overrideConfiguration != null
                    || (compatInfo != null && compatInfo.applicationScale
                            != resources.getCompatibilityInfo().applicationScale)) {
                resources = mResourcesManager.getTopLevelResources(packageInfo.getResDir(),
                        packageInfo.getSplitResDirs(), packageInfo.getOverlayDirs(),
                        packageInfo.getApplicationInfo().sharedLibraryFiles, displayId,
                        overrideConfiguration, compatInfo);
            }
        }
        mResources = resources;
        //省略代码...
}
```
contextImpl的getResources返回的mResources，是通过LoadedApk的getResources方法。
##### 2.3 多重跳转

```
graph LR
LoadedApk的getResources-->ActivityThread的getTopLevelResources  

ActivityThread.getTopLevelResources-->ResourcesManager的getTopLevelResources

```
##### 2.4 ResourcesManager的getTopLevelResources

```
 Resources getTopLevelResources(String resDir, String[] splitResDirs,
            String[] overlayDirs, String[] libDirs, int displayId,
            Configuration overrideConfiguration, CompatibilityInfo compatInfo) {
     //省略代码...
      AssetManager assets = new AssetManager();
        // resDir can be null if the 'android' package is creating a new Resources object.
        // This is fine, since each AssetManager automatically loads the 'android' package
        // already.
        if (resDir != null) {
            if (assets.addAssetPath(resDir) == 0) {
                return null;
            }
        }
        //省略代码...
        DisplayMetrics dm = getDisplayMetricsLocked(displayId);
        Configuration config;
        final boolean isDefaultDisplay = (displayId == Display.DEFAULT_DISPLAY);
        final boolean hasOverrideConfig = key.hasOverrideConfiguration();
        if (!isDefaultDisplay || hasOverrideConfig) {
            config = new Configuration(getConfiguration());
            if (!isDefaultDisplay) {
                applyNonDefaultDisplayMetricsToConfigurationLocked(dm, config);
            }
            if (hasOverrideConfig) {
                config.updateFrom(key.mOverrideConfiguration);
                if (DEBUG) Slog.v(TAG, "Applied overrideConfig=" + key.mOverrideConfiguration);
            }
        } else {
            config = getConfiguration();
        }
        r = new Resources(assets, dm, config, compatInfo);
        //省略代码...
    }
```
从最终的Resourcesmanager得出,getResource其实就说new了一个。
那么Resources的构造函数中的核心参数为AssetManager，这个AssetManager会加载资源文件路径，我们最终要将这个路径替换成新的皮肤路径，完成换肤功能。

