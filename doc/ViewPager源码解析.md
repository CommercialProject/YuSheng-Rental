#### 1、概述
此次ViewPager源码解析，主要是是针对轮播自定义view的开发，所以对于ViewPager里2000多行的代码，不能全看，只能是针对性的看。
#### 2、源码解读
1、从viewpager的adapter看起，源码里有些注释，喜欢深究的可以了解下。setadapter方法里有个populate()，这个方法主要是创建和销毁页面。
    
```
public void setAdapter(PagerAdapter adapter) {
    //1.如果已经设置过PagerAdapter，即mAdapter != null，
    // 则做一些清理工作
    if (mAdapter != null) {
        //2.清除观察者
        mAdapter.setViewPagerObserver(null);
        //3.回调startUpdate函数，告诉PagerAdapter开始更新要显示的页面
        mAdapter.startUpdate(this);
        //4.如果之前保存有页面，则将之前所有的页面destroy掉
        for (int i = 0; i < mItems.size(); i++) {
            final ItemInfo ii = mItems.get(i);
            mAdapter.destroyItem(this, ii.position, ii.object);
        }
        //5.回调finishUpdate，告诉PagerAdapter结束更新
        mAdapter.finishUpdate(this);
        //6.将所有的页面清除
        mItems.clear();
        //7.将所有的非Decor View移除，即将页面移除
        removeNonDecorViews();
        //8.当前的显示页面重置到第一个
        mCurItem = 0;
        //9.滑动重置到(0,0)位置
        scrollTo(0, 0);
    }

    //10.保存上一次的PagerAdapter
    final PagerAdapter oldAdapter = mAdapter;
    //11.设置mAdapter为新的PagerAdapter
    mAdapter = adapter;
    //12.设置期望的适配器中的页面数量为0个
    mExpectedAdapterCount = 0;
    //13.如果设置的PagerAdapter不为null
    if (mAdapter != null) {
        //14.确保观察者不为null，观察者主要是用于监视数据源的内容发生变化
        if (mObserver == null) {
            mObserver = new PagerObserver();
        }
        //15.将观察者设置到PagerAdapter中
        mAdapter.setViewPagerObserver(mObserver);
        mPopulatePending = false;
        //16.保存上一次是否是第一次Layout
        final boolean wasFirstLayout = mFirstLayout;
        //17.设定当前为第一次Layout
        mFirstLayout = true;
        //18.更新期望的数据源中页面个数
        mExpectedAdapterCount = mAdapter.getCount();
        //19.如果有数据需要恢复
        if (mRestoredCurItem >= 0) {
            //20.回调PagerAdapter的restoreState函数
            mAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader);
            setCurrentItemInternal(mRestoredCurItem, false, true);
            //21.标记无需再恢复
            mRestoredCurItem = -1;
            mRestoredAdapterState = null;
            mRestoredClassLoader = null;
        } else if (!wasFirstLayout) {//如果在此之前不是第一次Layout
            //22.由于ViewPager并不是将所有页面作为子View，
            // 而是最多缓存用户指定缓存个数*2（左右两边，可能左边或右边没有那么多页面）
            //因此需要创建和销毁页面，populate主要工作就是这些
            populate();
        } else {
            //23.重新布局（Layout）
            requestLayout();
        }
    }
    //24.如果PagerAdapter发生变化，并且设置了OnAdapterChangeListener监听器
    // 则回调OnAdapterChangeListener的onAdapterChanged函数
    if (mAdapterChangeListener != null && oldAdapter != adapter) {
        mAdapterChangeListener.onAdapterChanged(oldAdapter, adapter);
    }
}
```
2、populate(int newCurrentItem)方法,从这个方法可以看出会不断的去销毁和创建页面，并通过mOffscreenPageLimit这个变量去决定了缓存多少个item，创建新的缓存item，并销毁多余的item。这就是为什么ViewPager里面有多少界面都不会卡。

```
 void populate(int newCurrentItem) {
    ItemInfo oldCurInfo = null;
    if (mCurItem != newCurrentItem) {
        oldCurInfo = infoForPosition(mCurItem);
        mCurItem = newCurrentItem;
    }

    if (mAdapter == null) {
        //对子View的绘制顺序进行排序，优先绘制Decor View
        //再按照position从小到大排序
        sortChildDrawingOrder();
        return;
    }

    //如果我们正在等待populate,那么在用户手指抬起切换到新的位置期间应该推迟创建子View，
    // 直到滚动到最终位置再去创建，以免在这个期间出现差错
    if (mPopulatePending) {
        if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...");
        //对子View的绘制顺序进行排序，优先绘制Decor View
        //再按照position从小到大排序
        sortChildDrawingOrder();
        return;
    }

    //同样，在ViewPager没有attached到window之前，不要populate.
    // 这是因为如果我们在恢复View的层次结构之前进行populate，可能会与要恢复的内容有冲突
    if (getWindowToken() == null) {
        return;
    }
    //回调PagerAdapter的startUpdate函数，
    // 告诉PagerAdapter开始更新要显示的页面
    mAdapter.startUpdate(this);

    final int pageLimit = mOffscreenPageLimit;
    //确保起始位置大于等于0，如果用户设置了缓存页面数量，第一个页面为当前页面减去缓存页面数量
    final int startPos = Math.max(0, mCurItem - pageLimit);
    //保存数据源中的数据个数
    final int N = mAdapter.getCount();
    //确保最后的位置小于等于数据源中数据个数-1，
    // 如果用户设置了缓存页面数量，第一个页面为当前页面加缓存页面数量
    final int endPos = Math.min(N - 1, mCurItem + pageLimit);

    //判断用户是否增减了数据源的元素，如果增减了且没有调用notifyDataSetChanged，则抛出异常
    if (N != mExpectedAdapterCount) {
        //resName用于抛异常显示
        String resName;
        try {
            resName = getResources().getResourceName(getId());
        } catch (Resources.NotFoundException e) {
            resName = Integer.toHexString(getId());
        }
        throw new IllegalStateException("The application's PagerAdapter changed the adapter's" +
                " contents without calling PagerAdapter#notifyDataSetChanged!" +
                " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N +
                " Pager id: " + resName +
                " Pager class: " + getClass() +
                " Problematic adapter: " + mAdapter.getClass());
    }

    //定位到当前获焦的页面，如果没有的话，则添加一个
    int curIndex = -1;
    ItemInfo curItem = null;
    //遍历每个页面对应的ItemInfo，找出获焦页面
    for (curIndex = 0; curIndex < mItems.size(); curIndex++) {
        final ItemInfo ii = mItems.get(curIndex);
        //找到当前页面对应的ItemInfo后，跳出循环
        if (ii.position >= mCurItem) {
            if (ii.position == mCurItem) curItem = ii;
            break;
        }
    }
    //如果没有找到获焦的页面，说明mItems列表里面没有保存获焦页面，
    // 需要将获焦页面加入到mItems里面
    if (curItem == null && N > 0) {
        curItem = addNewItem(mCurItem, curIndex);
    }

    //默认缓存当前页面的左右两边的页面，如果用户设定了缓存页面数量，
    // 则将当前页面两边都缓存用户指定的数量的页面
    //如果当前没有页面，则我们啥也不需要做
    if (curItem != null) {
        float extraWidthLeft = 0.f;
        //左边的页面
        int itemIndex = curIndex - 1;
        //如果当前页面左边有页面，则将左边页面对应的ItemInfo取出，否则左边页面的ItemInfo为null
        ItemInfo ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
        //保存显示区域的宽度
        final int clientWidth = getClientWidth();
        //算出左边页面需要的宽度，注意，这里的宽度是指实际宽度与可视区域宽度比例，
        // 即实际宽度=leftWidthNeeded*clientWidth
        final float leftWidthNeeded = clientWidth <= 0 ? 0 :
                2.f - curItem.widthFactor + (float) getPaddingLeft() / (float) clientWidth;
        //从当前页面左边第一个页面开始，左边的页面进行遍历
        for (int pos = mCurItem - 1; pos >= 0; pos--) {
            //如果左边的宽度超过了所需的宽度，并且当前当前页面位置比第一个缓存页面位置小
            //这说明这个页面需要Destroy掉
            if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                //如果左边已经没有页面了，跳出循环
                if (ii == null) {
                    break;
                }
                //将当前页面destroy掉
                if (pos == ii.position && !ii.scrolling) {
                    mItems.remove(itemIndex);
                    //回调PagerAdapter的destroyItem
                    mAdapter.destroyItem(this, pos, ii.object);
                    if (DEBUG) {
                        Log.i(TAG, "populate() - destroyItem() with pos: " + pos +
                                " view: " + ((View) ii.object));
                    }
                    //由于mItems删除了一个元素
                    //需要将索引减一
                    itemIndex--;
                    curIndex--;
                    ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                }
            } else if (ii != null && pos == ii.position) {
                //如果当前位置是需要缓存的位置，并且这个位置上的页面已经存在
                //则将左边宽度加上当前位置的页面
                extraWidthLeft += ii.widthFactor;
                //mItems往左遍历
                itemIndex--;
                //ii设置为当前遍历的页面的左边一个页面
                ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
            } else {//如果当前位置是需要缓存，并且这个位置没有页面
                //需要添加一个ItemInfo,而addNewItem是通过PagerAdapter的instantiateItem获取对象
                ii = addNewItem(pos, itemIndex + 1);
                //将左边宽度加上当前位置的页面
                extraWidthLeft += ii.widthFactor;
                //由于新加了一个元素，当前的索引号需要加1
                curIndex++;
                //ii设置为当前遍历的页面的左边一个页面
                ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
            }
        }
        //同理，右边需要添加缓存的页面
        //......

       // 省略右边添加缓存页面代码  

       //......

        calculatePageOffsets(curItem, curIndex, oldCurInfo);
    }

    if (DEBUG) {
        Log.i(TAG, "Current page list:");
        for (int i = 0; i < mItems.size(); i++) {
            Log.i(TAG, "#" + i + ": page " + mItems.get(i).position);
        }
    }
    //回调PagerAdapter的setPrimaryItem，告诉PagerAdapter当前显示的页面
    mAdapter.setPrimaryItem(this, mCurItem, curItem != null ? curItem.object : null);
    //回调PagerAdapter的finishUpdate，告诉PagerAdapter页面更新结束
    mAdapter.finishUpdate(this);


    //检查页面的宽度是否测量，如果页面的LayoutParams数据没有设定，则去重新设定好
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
        final View child = getChildAt(i);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.childIndex = i;
        if (!lp.isDecor && lp.widthFactor == 0.f) {
            // 0 means requery the adapter for this, it doesn't have a valid width.
            final ItemInfo ii = infoForChild(child);
            if (ii != null) {
                lp.widthFactor = ii.widthFactor;
                lp.position = ii.position;
            }
        }
    }
    //重新对页面排序
    sortChildDrawingOrder();
    //如果ViewPager被设定为可获焦的，则将当前显示的页面设定为获焦
    if (hasFocus()) {
        View currentFocused = findFocus();
        ItemInfo ii = currentFocused != null ? infoForAnyChild(currentFocused) : null;
        if (ii == null || ii.position != mCurItem) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                ii = infoForChild(child);
                if (ii != null && ii.position == mCurItem) {
                    if (child.requestFocus(View.FOCUS_FORWARD)) {
                        break;
                    }
                }
            }
        }
    }
}
```
3、addNewItem,创建新的item,这里会回调mAdapter.instantiateItem(this, position);，说明了这个object就是我们创建的view。

```
   ItemInfo addNewItem(int position, int index) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = mAdapter.instantiateItem(this, position);
        ii.widthFactor = mAdapter.getPageWidth(position);
        if (index < 0 || index >= mItems.size()) {
            mItems.add(ii);
        } else {
            mItems.add(index, ii);
        }
        return ii;
    }
```
4、销毁item,销毁item的时候也回调了mAdapter.destroyItem(this, pos, ii.object);

```
//省略代码...
if (pos == ii.position && !ii.scrolling) {
                        mItems.remove(itemIndex);
                        mAdapter.destroyItem(this, pos, ii.object);
                        if (DEBUG) {
                            Log.i(TAG, "populate() - destroyItem() with pos: " + pos
                                    + " view: " + ((View) ii.object));
                        }
                        itemIndex--;
                        curIndex--;
                        ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                    }
//省略代码...
```
5、setCurrentItem方法中,可以看出 切换item的时候，会调用populate(item)方法，又再一次进行新建和销毁item。之后调用 scrollToItem(item, smoothScroll, velocity, dispatchSelected);

```
void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
       //省略代码...

        if (mFirstLayout) {
            // We don't have any idea how big we are yet and shouldn't have any pages either.
            // Just set things up and let the pending layout handle things.
            mCurItem = item;
            if (dispatchSelected) {
                dispatchOnPageSelected(item);
            }
            requestLayout();
        } else {
            populate(item);
            scrollToItem(item, smoothScroll, velocity, dispatchSelected);
        }
    }

```

6、smoothScrollTo方法，    mScroller.startScroll(sx, sy, dx, dy, duration);这个方法将会执行带有动画的滑动效果，这也是决定了自动轮播的滑动速度问题。

```
 void smoothScrollTo(int x, int y, int velocity) {
        //省略代码...

        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
        mIsScrollStarted = false;
        mScroller.startScroll(sx, sy, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

```


