首先看Activity的StartActivity的方法，其实也是调用startActivityForResult方法
    
```
 public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
```
接着来看startActivityForResult方法，其次会执行execStartActivity（）方法
    
```
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
        if (mParent == null) {
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);
        }
        //...此处省略多行
    }
```
接着看execStartActivity（），代码里看到有个startActivity()方法，通过ActivityManagerNative.getDefault()得到ActivityManager调用，接下来看看getDefault()

```
public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        //...此处省略多行
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                       intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target != null ? target.mEmbeddedID : null,
                        requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
```

在getDefault()方法中，返回的是gDefault.get()，此变量gDefault具体实例化方式如下，代码出现了IBinder字样，说明这里是通过AIDL方式获取底层服务了，在这里已经无法往下走了，往下具体逻辑待后面插件化以及NDK讲解再进行细讲。
```
private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            IBinder b = ServiceManager.getService("activity");
            if (false) {
                Log.v("ActivityManager", "default service binder = " + b);
            }
            IActivityManager am = asInterface(b);
            if (false) {
                Log.v("ActivityManager", "default service = " + am);
            }
            return am;
        }
    };
```

通过AIDL得到的结果会返回到ActivityThread类中，找到performLaunchActivity(),在这里会创建activity，这个方法里面会执行一系列生命周期方法，以后在分析

```
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        //...省略代码
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = r.packageInfo.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
             //...省略代码
        } catch (Exception e) {
            //...省略代码
        }
        
        //...省略代码
        return activity;
    }
```
接下来看newActivity()方法，在这里看的Activity的创建通过classLoader找到activity的class,利用反射实例化对象。
```
 public Activity newActivity(ClassLoader cl, String className,
            Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        return (Activity)cl.loadClass(className).newInstance();
    }

```

总结：Activity启动流程大概是这样，通过 实例化actvitiy然后调用各种生命周期完成启动，接下来将会解析类的加载机制。