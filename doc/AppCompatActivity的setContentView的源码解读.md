#### 1、概述
之前分析过Activity的源码解读此次来解读下AppCompatActivity的源码解析

#### 2、源码解析
国际惯例，先找入口，从AppCompatActivity的setContentVew开始
##### 2.1 AppCompatActivity的setContentVew()

```
@Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

```

```
 /**
     * @return The {@link AppCompatDelegate} being used by this Activity.
     */
    @NonNull
    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, this);
        }
        return mDelegate;
    }
```

```
private static AppCompatDelegate create(Context context, Window window,
            AppCompatCallback callback) {
        final int sdk = Build.VERSION.SDK_INT;
        if (BuildCompat.isAtLeastN()) {
            return new AppCompatDelegateImplN(context, window, callback);
        } else if (sdk >= 23) {
            return new AppCompatDelegateImplV23(context, window, callback);
        } else if (sdk >= 14) {
            return new AppCompatDelegateImplV14(context, window, callback);
        } else if (sdk >= 11) {
            return new AppCompatDelegateImplV11(context, window, callback);
        } else {
            return new AppCompatDelegateImplV9(context, window, callback);
        }
    }
```
从上面三段代码看出，AppContentView的setContentView并不是我们所理解的getWindow的setContentView，而是调用了AppCompatDelegate中setContentView。


##### 2.2 AppCompatDelegate中setContentView
```
  @Override
    public void setContentView(int resId) {
        ensureSubDecor();
        ViewGroup contentParent = (ViewGroup) mSubDecor.findViewById(android.R.id.content);
        contentParent.removeAllViews();
        LayoutInflater.from(mContext).inflate(resId, contentParent);
        mOriginalWindowCallback.onContentChanged();
    }
```

```
private ViewGroup createSubDecor() {
        //省略代码
        mWindow.setContentView(subDecor);
        return subDecor;
    }
```
其实AppCompatDelegate多个实现类中，最终调用的的是AppCompatDelegateImplV9的setContentView()方法 ，然后调用了window的setContentView,从这里看跟Activity的setContentView方法没啥区别。

##### 2.3 AppCompatActivity的OnCreate()

```
 @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final AppCompatDelegate delegate = getDelegate();
        delegate.installViewFactory();
        delegate.onCreate(savedInstanceState);
        if (delegate.applyDayNight() && mThemeId != 0) {
            // If DayNight has been applied, we need to re-apply the theme for
            // the changes to take effect. On API 23+, we should bypass
            // setTheme(), which will no-op if the theme ID is identical to the
            // current theme ID.
            if (Build.VERSION.SDK_INT >= 23) {
                onApplyThemeResource(getTheme(), mThemeId, false);
            } else {
                setTheme(mThemeId);
            }
        }
        super.onCreate(savedInstanceState);
    }
```

```
@Override
    public void installViewFactory() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (layoutInflater.getFactory() == null) {
            LayoutInflaterCompat.setFactory(layoutInflater, this);
        } else {
            if (!(LayoutInflaterCompat.getFactory(layoutInflater)
                    instanceof AppCompatDelegateImplV9)) {
                Log.i(TAG, "The Activity's LayoutInflater already has a Factory installed"
                        + " so we can not install AppCompat's");
            }
        }
    }
```
从oncreate方法上看出，AppCompatActivity的OnCreate()做了一件事，就说给当前的Context的LayoutInflate设置了LayoutInflateFactory。

##### 2.4 AppCompatActivity实现的onCreateView()

```
 /**
     * From {@link android.support.v4.view.LayoutInflaterFactory}
     */
    @Override
    public final View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        // First let the Activity's Factory try and inflate the view
        final View view = callActivityOnCreateView(parent, name, context, attrs);
        if (view != null) {
            return view;
        }

        // If the Factory didn't handle it, let our createView() method try
        return createView(parent, name, context, attrs);
    }
```


```
 public final View createView(View parent, final String name, @NonNull Context context,
            @NonNull AttributeSet attrs, boolean inheritContext,
            boolean readAndroidTheme, boolean readAppTheme, boolean wrapContext) {
     
        //省略代码...
        // We need to 'inject' our tint aware Views in place of the standard framework versions
        switch (name) {
            case "TextView":
                view = new AppCompatTextView(context, attrs);
                break;
            case "ImageView":
                view = new AppCompatImageView(context, attrs);
                break;
            case "Button":
                view = new AppCompatButton(context, attrs);
                break;
            case "EditText":
                view = new AppCompatEditText(context, attrs);
                break;
            case "Spinner":
                view = new AppCompatSpinner(context, attrs);
                break;
            case "ImageButton":
                view = new AppCompatImageButton(context, attrs);
                break;
            case "CheckBox":
                view = new AppCompatCheckBox(context, attrs);
                break;
            case "RadioButton":
                view = new AppCompatRadioButton(context, attrs);
                break;
            case "CheckedTextView":
                view = new AppCompatCheckedTextView(context, attrs);
                break;
            case "AutoCompleteTextView":
                view = new AppCompatAutoCompleteTextView(context, attrs);
                break;
            case "MultiAutoCompleteTextView":
                view = new AppCompatMultiAutoCompleteTextView(context, attrs);
                break;
            case "RatingBar":
                view = new AppCompatRatingBar(context, attrs);
                break;
            case "SeekBar":
                view = new AppCompatSeekBar(context, attrs);
                break;
        }

        if (view == null && originalContext != context) {
            // If the original context does not equal our themed context, then we need to manually
            // inflate it using the name so that android:theme takes effect.
            view = createViewFromTag(context, name, attrs);
        }

        if (view != null) {
            // If we have created a view, check it's android:onClick
            checkOnClickListener(view, attrs);
        }

        return view;
    }
```
从这里明白，AppCompatActivity设置了LayoutInflateFactory把其中的view更换为自己所特有的view，所以为什么新版本会有一些新样式控件。

#### 3、总结
总结就应该画图

```
graph TD
onCreate-->delegate.installViewFactory
delegate.installViewFactory-->LayoutInflaterCompat.setFactory
LayoutInflaterCompat.setFactory
onCreate-->setContentView
LayoutInflaterCompat.setFactory-->setContentView
setContentView-->调用window的setContenview
调用window的setContenview-->进行inflate的时候替换view
```
大概流程如上图所示。
