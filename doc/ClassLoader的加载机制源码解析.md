##### 1、概述
从上次的activity的启动分析中====，我们知道performLaunchActivity()方法创建了Activity并执行相应的生命周期方法，那么里面的ClassLoader是个什么东东呢？
##### 2、源码解读
###### 1、mClassLoader
接着往下看，getClassLoader（）返回的是mClassLoader，那去找找mClassLoader，mClassLoader最终是PathClassLoader，而PathClassLoader继承BaseDexClassLoader

```
 public ClassLoader getClassLoader() {
        synchronized (this) {
            if (mClassLoader == null) {
                createOrUpdateClassLoaderLocked(null /*addedPaths*/);
            }
            return mClassLoader;
        }
    }
private void createOrUpdateClassLoaderLocked(List<String> addedPaths) {
 //...省略代码
      if (mBaseClassLoader != null) {
            mClassLoader = mBaseClassLoader;
      } else {
            mClassLoader = ClassLoader.getSystemClassLoader();
      }
       //...省略代码
      mClassLoader = ApplicationLoaders.getDefault().getClassLoader(zip,
            mApplicationInfo.targetSdkVersion, isBundledApp, librarySearchPath,
            libraryPermittedPath, mBaseClassLoader);
        //...省略代码
}

 public ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled,
                                      String librarySearchPath, String libraryPermittedPath,
                                      ClassLoader parent) {
       //...省略代码
            PathClassLoader pathClassloader = new PathClassLoader(zip, parent);
            //...省略代码
            return pathClassloader;
        }
    }
```
###### 2、ClassLoader.loadClass()
回归到Activity启动分析中，ClassLoader得到后，那么将会通过ClassLoader.loadClass().newInstance去得到Activity的实例。ClassLoader.loadClass()所执行的是findClass（）,在ClassLoader类中的findClass（）是抛出异常，那么肯定BaseDexClassLoader重写了ClassLoader的findClass()方法。
   
```
protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
           
 //...省略代码
        if (c == null) {
            // If still not found, then invoke findClass in order
            // to find the class.
            long t1 = System.nanoTime();
            c = findClass(name);
            }
        }
        return c;
    }
```
###### 3、BaseDexClassLoader.findClass()
从重写的findClass可以看出，它其实是从pathList.findClass()得到其class
```
 @Override
52    protected Class<?> findClass(String name) throws ClassNotFoundException {
53        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
54        Class c = pathList.findClass(name, suppressedExceptions);
55        if (c == null) {
56            ClassNotFoundException cnfe = new ClassNotFoundException("Didn't find class \"" + name + "\" on path: " + pathList);
57            for (Throwable t : suppressedExceptions) {
58                cnfe.addSuppressed(t);
59            }
60            throw cnfe;
61        }
62        return c;
63    }
```

###### 4、pathList.findClass()
从pathList.findClass可以看出，里面通过遍历dexElements得到DexFile，通过DexFile去加载class文件，加载成功就返回，否则返回null。这里的DexFile就是Dex文件。
```
 public Class findClass(String name, List<Throwable> suppressed) {
        for (Element element : dexElements) {
            DexFile dex = element.dexFile;

            if (dex != null) {
                Class clazz = dex.loadClassBinaryName(name, definingContext, suppressed);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        if (dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
        return null;
    }
}
```

总结：从分析Activity启动过程，得出通过PathClassLoader实例化Activity。PathClassLoader.findclass()->BaseDexClass.findclass()->PathList.findclass()->遍历dexElements得到DexFile对象找到class并返回，从这里可以看出下次进行热修复讲解时，原理应该是反射得到dexElements进行处理。