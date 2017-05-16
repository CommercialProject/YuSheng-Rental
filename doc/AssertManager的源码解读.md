#### 1、概述
AssertManager是资源加载的核心，其主要的代码在native层，接下来看下nativie如何实现在native实现资源加载。

#### 2、源码解读
国际惯例，从java开始解读。
##### 2.1 getResources.getDrawable

```
 public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme)
            throws NotFoundException {
        final TypedValue value = obtainTempTypedValue();
        try {
            final ResourcesImpl impl = mResourcesImpl;
            impl.getValue(id, value, true);
            return impl.loadDrawable(this, value, id, theme, true);
        } finally {
            releaseTempTypedValue(value);
        }
    }
```

```
 Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme,
            boolean useCache) throws NotFoundException {
      
        //省略代码...
            Drawable dr;
            if (cs != null) {
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                dr = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, null);
            }
             //省略代码...
        } catch (Exception e) {
           //省略代码...
        }
    }
```

```
  private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id,
            Resources.Theme theme) {
        //省略代码
        final Drawable dr;
        try {
            if (file.endsWith(".xml")) {
                final XmlResourceParser rp = loadXmlResourceParser(
                        file, id, value.assetCookie, "drawable");
                dr = Drawable.createFromXml(wrapper, rp, theme);
                rp.close();
            } else {
                final InputStream is = mAssets.openNonAsset(
                        value.assetCookie, file, AssetManager.ACCESS_STREAMING);
                dr = Drawable.createFromResourceStream(wrapper, value, is, file, null);
                is.close();
            }
        } catch (Exception e) {
          
        }
        return dr;
    }
```


从上面三段代码看出，getResource.getDrawable是通过AssetManager的openNonAsset或者AssetManager的xxx方法解析得到流，再通过Drawable方法生成Drawable,其主要内容在AssetManager中。

##### 2.2 AssetManager
###### 2.2.1 构造函数

```
  public AssetManager() {
        synchronized (this) {
            if (DEBUG_REFS) {
                mNumRefs = 0;
                incRefsLocked(this.hashCode());
            }
            init(false);
            if (localLOGV) Log.v(TAG, "New asset manager: " + this);
            ensureSystemAssets();
        }
    }
```

```
  private native final void init(boolean isSystem);
```
AssetManager的构造方法中调用了native层的init方法，那看看里面干了什么
###### 2.2.2 native层init()

```
static void android_content_AssetManager_init(JNIEnv* env, jobject clazz, jboolean isSystem)
{
    if (isSystem) {
        verifySystemIdmaps();
    }
    AssetManager* am = new AssetManager();
    if (am == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "");
        return;
    }

// 添加默认资源
    am->addDefaultAssets();

    ALOGV("Created AssetManager %p for Java object %p\n", am, clazz);
    env->SetLongField(clazz, gAssetManagerOffsets.mObject, reinterpret_cast<jlong>(am));
}
```
在这里有个native层的AssetManager，添加了一个默认资源

```
bool AssetManager::addDefaultAssets()
{
    const char* root = getenv("ANDROID_ROOT");
    LOG_ALWAYS_FATAL_IF(root == NULL, "ANDROID_ROOT not set");

    String8 path(root);
    path.appendPath(kSystemAssets);

    return addAssetPath(path, NULL);
}
```

```
static const char* kSystemAssets = "framework/framework-res.apk";
```
从上面添加默认资源看到，系统在加载资源的时候会默认添加系统资源，也就是为什么我们能够用@android:drawable/xxx，其实也就是从系统的默认资源apk中拿到所指定的资源。或者是不同版本下的默认背景不一致的原因。


```
bool AssetManager::addAssetPath(const String8& path, int32_t* cookie)
{
  //省略代码...
    // Check that the path has an AndroidManifest.xml
    Asset* manifestAsset = const_cast<AssetManager*>(this)->openNonAssetInPathLocked(
            kAndroidManifest, Asset::ACCESS_BUFFER, ap);
    if (manifestAsset == NULL) {
        // This asset path does not contain any resources.
        delete manifestAsset;
        return false;
    }
    delete manifestAsset;

    mAssetPaths.add(ap);

    // new paths are always added at the end
   if (mResources != NULL) {
        appendPathToResTable(ap);
    }

    return true;
}
```
资源加载会取读取AndroidManifest文件，AndroidManifest不存在会出错。

```
bool AssetManager::appendPathToResTable(const asset_path& ap) const {
    // skip those ap's that correspond to system 
     //省略代码...
     Asset* ass = NULL;
     ResTable* sharedRes = NULL;
     ass = const_cast<AssetManager*>(this)->
            openNonAssetInPathLocked("resources.arsc",sset::ACCESS_BUFFER,ap);

    return onlyEmptyResources;
}
```
resources.arsc（资源映射信息），APK解压后可看到，这个文件里面包含了与R.xx相关的映射信息，比如分辨率资源等等。

#### 3、总结

```
graph TD
AssetManager的构造方法-->native的init方法
native的init方法-->添加默认资源addDefaultAssets
添加默认资源addDefaultAssets-->framework/framework-res.apk
添加默认资源addDefaultAssets-->解析映射表resources.arsc

```
总体来说，AssetManager在native做的事情大概是这样，java层就可以通过id获取到相应的name,从而得到相应的drawable。