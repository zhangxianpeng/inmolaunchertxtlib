package com.inmoglass.otaupgrade.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.math.BigDecimal;

public class CustomProgress extends View implements View.OnClickListener {
    public static String textcontext = null;
    private Paint defaultPaint;
    private Paint rollPaint;
    private float percent = 0.0f;
    private Paint textPaint;
    private Handler mHandler = new Handler();
    private String data="";
    private MyRunnable myRunnable;
    public CustomProgress(Context context) {
        this(context, null);
    }

    public CustomProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myRunnable = new MyRunnable();
        initPaint();
        setOnClickListener(this);
    }

    private void initPaint() {
        defaultPaint = new Paint();
        defaultPaint.setColor(Color.parseColor("#999999"));
        defaultPaint.setStyle(Paint.Style.FILL);


        rollPaint = new Paint();
        rollPaint.setColor(Color.parseColor("#1C86EE"));
        rollPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#202020"));
        textPaint.setTextSize(sp2px(15));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            height = 320;
        }
        return height;
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            width = 320;
        }
        return width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDefaultProgressButton(canvas);
        drawRollProgressButton(canvas);
        drawText(canvas);
    }
    public void drawText(Canvas canvas) {
        if (null == canvas) {
            return;
        }
        Log.d("数据有没有传递进来",textcontext);
        if (percent == 0) {
            String text = "下载";
            Paint.FontMetricsInt fm = textPaint.getFontMetricsInt();
            canvas.drawText(text, getWidth() / 2 - textPaint.measureText(text) / 2,
                    getHeight() / 2 - (fm.bottom + fm.top) / 2, textPaint);
        } else if(percent>0) {
            BigDecimal re1=new BigDecimal(Float.toString(percent));
            BigDecimal re2=new BigDecimal(Float.toString(100.0f));
            String text ="正在下载"+ re1.multiply(re2).floatValue()+"%";
            Paint.FontMetricsInt fm = textPaint.getFontMetricsInt();
            canvas.drawText(text, getWidth() / 2 - textPaint.measureText(text) / 2,
                    getHeight() / 2 - (fm.bottom + fm.top) / 2, textPaint);
            mHandler.postDelayed(myRunnable,500);
        }
    }

    private void drawRollProgressButton(Canvas canvas) {
        if (null == canvas) {
            return;
        }
        canvas.save();
        Rect rect = new Rect(0, 0, (int) (percent*getMeasuredWidth()), getMeasuredHeight());
        canvas.clipRect(rect);
        canvas.drawRect(rect,rollPaint);
        canvas.restore();
    }

    private void drawDefaultProgressButton(Canvas canvas) {
        if (null == canvas) {
            return;
        }
        canvas.drawRect(0,0,getMeasuredWidth(),getMeasuredHeight(),defaultPaint);
    }

    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    private int px2dp(float pxValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        percent+=0.1f;
        invalidate();
    }


    class MyRunnable implements Runnable{
        @Override
        public void run() {
            if(percent>=1.0){
                return;
            }
            BigDecimal bigDecimal = new BigDecimal(Float.toString(percent));
            BigDecimal bigDecimal1 = new BigDecimal(Float.toString(0.1f));
            percent=bigDecimal.add(bigDecimal1).floatValue();
            postInvalidate();
        }
    }
    public void datacontext(String context){
        context=textcontext;
        invalidate();
    }
}
