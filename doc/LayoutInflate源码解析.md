#### 1、概述
上次对AppCompatActivity的setContentView中发现，AppCompatActivity对LayoutInflate设置Factory,替换了AppCompat特有的view。那么接下来看LayoutInflate的解析过程。
#### 2、源码解读
国际惯例，找入口，从LayoutInflate.from开始

##### 2.1 LayoutInflate.from()

```
/**
     * Obtains the LayoutInflater from the given context.
     */
    public static LayoutInflater from(Context context) {
        LayoutInflater LayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (LayoutInflater == null) {
            throw new AssertionError("LayoutInflater not found.");
        }
        return LayoutInflater;
    }
```
调用的是getSytemService方法。

```
@Override
    public Object getSystemService(String name) {
        return SystemServiceRegistry.getSystemService(this, name);
    }
```
从SystemServiceRegistry里面拿。

```
  /**
     * Gets a system service from a given context.
     */
    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
        return fetcher != null ? fetcher.getService(ctx) : null;
    }
```

```
private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS =
            new HashMap<String, ServiceFetcher<?>>();
            
registerService(Context.LAYOUT_INFLATER_SERVICE, LayoutInflater.class,new CachedServiceFetcher<LayoutInflater>() {
            @Override
            public LayoutInflater createService(ContextImpl ctx) {
                return new PhoneLayoutInflater(ctx.getOuterContext());
            }});
```
从上面看出，LayoutInflate.from是从注册的Context.LAYOUT_INFLATER_SERVICE的hashmap中获取其PhoneLayoutInflater，这个LayoutInflate对象是单例(有兴趣可以跟CachedServiceFetcher里面看看),这就为我们进行hook拦截view进行插件换肤提供了便利。
