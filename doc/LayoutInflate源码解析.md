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

##### 2.2 inflate解析

```
public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
    //省略代码...
     final String name = parser.getName();
        if (TAG_MERGE.equals(name)) {
            if (root == null || !attachToRoot) {
                    throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }

              rInflate(parser, root, inflaterContext, attrs, false);
        } else {
            // Temp is the root view that was found in the xml
             final View temp = createViewFromTag(root, name, inflaterContext, attrs);
        }
         return result;
        }
    }
```
上面代码大概意思，判断头布局是不是<merge />标签，不是则调用createViewFromTag方法。

```
 View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            boolean ignoreThemeAttr) {
        //省略代码...
        try {
            View view;
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }

            if (view == null && mPrivateFactory != null) {
                view = mPrivateFactory.onCreateView(parent, name, context, attrs);
            }

            if (view == null) {
                final Object lastContext = mConstructorArgs[0];
                mConstructorArgs[0] = context;
                try {
                    if (-1 == name.indexOf('.')) {
                        view = onCreateView(parent, name, attrs);
                    } else {
                        view = createView(name, null, attrs);
                    }
                } finally {
                    mConstructorArgs[0] = lastContext;
                }
            }

            return view;
        } catch (InflateException e) {
            throw e;
        } 
        //省略代码...
    }
```
createViewForTag中进行了mFactory判断，这个mFactory就是之前对AppCompatActivity中设置的Factory.那么就会去调用Factory里的onCreateView方法，从而拦截原先的Activity的onCreateVie方法，接着看-1 == name.indexOf('.')，这里的判断代表是不是自定义view,比如说原生的view就会<ImageView />,自定义的是<com.xx.xx.CustomView />。



```
 static final Class<?>[] mConstructorSignature = new Class[] {
            Context.class, AttributeSet.class};

 public final View createView(String name, String prefix, AttributeSet attrs)
            throws ClassNotFoundException, InflateException {
     

        try {
            Trace.traceBegin(Trace.TRACE_TAG_VIEW, name);

            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                //原生view，将包名和类名拼接起来获取class
                clazz = mContext.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);
              
              //获取构造函数，这里调用的是带有两个参数的构造函数
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } else {
               //省略代码...
            }

            Object[] args = mConstructorArgs;
            args[1] = attrs;

            //通过class.newInstance得到view实例
            final View view = constructor.newInstance(args);
            if (view instanceof ViewStub) {
                // Use the same context when inflating ViewStub later.
                final ViewStub viewStub = (ViewStub) view;
                viewStub.setLayoutInflater(cloneInContext((Context) args[0]));
            }
            return view;

        } catch (NoSuchMethodException e) {
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Error inflating class " + (prefix != null ? (prefix + name) : name), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;

        } catch (ClassCastException e) {
            // If loaded class is not a View subclass
            final InflateException ie = new InflateException(attrs.getPositionDescription()
                    + ": Class is not a View " + (prefix != null ? (prefix + name) : name), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } catch (ClassNotFoundException e) {
            // If loadClass fails, we should propagate the exception.
            throw e;
        } catch (Exception e) {
            final InflateException ie = new InflateException(
                    attrs.getPositionDescription() + ": Error inflating class "
                            + (clazz == null ? "<unknown>" : clazz.getName()), e);
            ie.setStackTrace(EMPTY_STACK_TRACE);
            throw ie;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
        }
    }
```
这里的createview就是创建view的过程，通过反射创建view的实例并缓存起来，这就是为什么不能嵌套太多布局的原因。

#### 3、总结

```
graph TD
LayoutInflate.from-->从注册的servie中得到实现的Inflate
从注册的servie中得到实现的Inflate-->解析xml布局
解析xml布局-->是merge标签
是merge标签-->执行其他的
解析xml布局-->不是merge标签
不是merge标签-->有设置的Factory,执行Factory的onCreateView
不是merge标签-->无Factory,判断自定义view还是原生view
无Factory,判断自定义view还是原生view-->自定义view,执行createview 
无Factory,判断自定义view还是原生view-->原生view,通过反射创建view
```


