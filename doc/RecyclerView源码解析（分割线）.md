#### 1、概述
此次涉及的是自定义recyclerview的分割线，那么一言不合就看源码。
#### 2、源码解析
##### 1、分割线跟子view相关，那么就去找measureChild()方法，可以看出mRecyclerView.getItemDecorInsetsForChild(child);这个方法跟分割相关，

```
 public void measureChild(View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;
            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
                    getPaddingLeft() + getPaddingRight() + widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
                    getPaddingTop() + getPaddingBottom() + heightUsed, lp.height,
                    canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }
```
##### 2、getItemDecorInsetsForChild(),在这里有个getItemOffsets(),也就是我们自定义分割线的时候，重写的方法之一。在这里，将我们设置的mTempRect的属性加到inserts中，回到measureChild方法又可以看到，将insets的属性加到了子view中，这里得出一个结论就是，分割线的宽都是以牺牲子view的空间而达到的效果。

```
Rect getItemDecorInsetsForChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.mInsetsDirty) {
            return lp.mDecorInsets;
        }

        if (mState.isPreLayout() && (lp.isItemChanged() || lp.isViewInvalid())) {
            // changed/invalid items should not be updated until they are rebound.
            return lp.mDecorInsets;
        }
        final Rect insets = lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        final int decorCount = mItemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            mTempRect.set(0, 0, 0, 0);
            mItemDecorations.get(i).getItemOffsets(mTempRect, child, this, mState);
            insets.left += mTempRect.left;
            insets.top += mTempRect.top;
            insets.right += mTempRect.right;
            insets.bottom += mTempRect.bottom;
        }
        lp.mInsetsDirty = false;
        return insets;
    }
```

##### 3、ondraw方法，在recyclerview的ondraw方法中，通过for循环去ondraw分割线。这里也说明了为啥我们要重写ondraw方法。这里有个state属性，暂不分析。

```
 @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).onDraw(c, this, mState);
        }
    }
```
#### 3、总结
getItemOffsets和ondraw方法，一个是计算位置，一个是绘制，从而得出我们的分割线。但这里有个bug,由于recyclerview的分割线的画法机制，若出现分割线的宽度过大，子view的高度和宽度会出现不一致的情况。这暂时无法解决。

