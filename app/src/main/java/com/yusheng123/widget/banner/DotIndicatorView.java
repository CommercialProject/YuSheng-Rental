package com.yusheng123.widget.banner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 花歹 on 2017/4/16.
 * Email:   gatsbywang@126.com
 */

public class DotIndicatorView extends View {
    private Drawable mDrawable;

    public DotIndicatorView(Context context) {
        super(context);
    }

    public DotIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DotIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable != null) {
//            mDrawable.setBounds(0,0,getMeasuredWidth(),getMeasuredHeight());
//            mDrawable.draw(canvas);

            //指示器为圆
            Bitmap bitmap = draw2Bitmap(mDrawable);
            Bitmap circleBitmap = getCircleBitmap(bitmap);

            canvas.drawBitmap(circleBitmap, 0, 0, null);
        }
    }

    /**
     * 把bitmap 变为
     *
     * @param bitmap
     * @return
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap circleBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);

        Paint paint = new Paint();
        //抗锯齿
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        //设置 防抖
        paint.setDither(true);
        //在画布上面画个圆
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, getMeasuredWidth() / 2, paint);

        //取bitmap（矩形）和circlebitmap（圆形）的交集
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //把原来的bitmap绘制到新的bitmap上面
        canvas.drawBitmap(bitmap, 0, 0, paint);
        //内存优化 回收bitmap
        bitmap.recycle();
        bitmap = null;

        return circleBitmap;
    }

    /**
     * 从drawable中得到bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap draw2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        //创建一个bitmap
        Bitmap outBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        //创建一个画布
        Canvas canvas = new Canvas(outBitmap);
        //把drawable画到canvas上
        drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        drawable.draw(canvas);
        return outBitmap;
    }


    /**
     * 设置drawable
     *
     * @param drawable
     */
    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        invalidate();
    }
}
