package com.opr.snowflakesview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.IllegalFormatCodePointException;
import java.util.Random;

/**
 * Created by opr on 16-8-4.
 */
public class SnowflakesView extends View {

    private FallSnowThread mFallSnowThread;
    private Snow[] mSnows; //雪花对象集合
    private Drawable mSnowFlowerDrawable;
    private int mSnowFlowerHeight; //雪花原始高度
    private int mSnowViewWidth; //整个view的高度
    private int mSnowViewHeight; //整个view的宽度

    private Random mRandom; //随机数发生器，发生雪花缩放大小、雪花初始x轴坐标

    private float mFallSpeed; //雪花下降速度
    private int mSnowCount; //雪花总数量

    private enum FallSnowState {START, PAUSE, RUNNING, STOP}
    private FallSnowState mFallSnowState;


    public SnowflakesView(Context context) {
        this(context, null);
    }

    public SnowflakesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnowflakesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initVariable();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SnowflakesView);
        mFallSpeed = typedArray.getFloat(R.styleable.SnowflakesView_fallSpeed, (float) 0.1);
        mSnowCount = typedArray.getInteger(R.styleable.SnowflakesView_snowCount, 10);
        typedArray.recycle();
        judgeAttrIsReasonable();
    }

    //判断自定义参数是否合法
    private void judgeAttrIsReasonable() {
        if (mSnowCount <= 0) {
            throw new RuntimeException("snow count must > 0");
        }

        if (mFallSpeed <= 0) {
            throw new RuntimeException("fall snow speed must > 0");
        }
    }

    public void initVariable() {
        mRandom = new Random();
        mSnowFlowerDrawable = getResources().getDrawable(R.drawable.image_cpu_cooler_snow);
        mSnowFlowerHeight = mSnowFlowerDrawable.getIntrinsicHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mSnowViewWidth = r - l;
        mSnowViewHeight = b - t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mSnows == null) {
            return;
        }

        for (Snow snow : mSnows) {
            canvas.save();
            mSnowFlowerDrawable.setBounds((int) snow.mX, (int) snow.mY, (int) (snow.mX + mSnowFlowerHeight * snow.mScale), (int) (snow.mY + mSnowFlowerHeight * snow.mScale));
            mSnowFlowerDrawable.setAlpha(snow.mAlpha);
            mSnowFlowerDrawable.draw(canvas);
            canvas.restore();
        }

    }


    //caculate all snow next attrs , include position,size etc. 计算所有雪花的相关参数  包括位置  大小等
    private void calculateSnowsNextAttr() {
        for (Snow snow : mSnows) {
            snow.calculateNextAttr();
        }
    }

    private class Snow {
        public float mScale;
        public int mAlpha; //appha
        public float mX;
        public float mY;

        public Snow() {
            init();
        }

        private void init() {
            mAlpha = mRandom.nextInt(200) + 55; //55~255 alpha
            mScale = (mRandom.nextFloat() + 1) / 2;  //0.5~1.5 scale
            mX = mRandom.nextInt(mSnowViewWidth);
            mY = -mSnowFlowerHeight;
        }

        //caculate the snow's next attrs , include position,size etc. 计算下一个雪花的相关参数  包括位置  大小等
        public void calculateNextAttr() {
            if (mY > mSnowViewHeight / mScale) {
                init();
            } else {
                mY = mY + mScale * mSnowFlowerHeight * mFallSpeed;
            }
        }
    }

    public void startSnow() {
        mFallSnowState = FallSnowState.START;

        if (mFallSnowThread == null) {
            mFallSnowThread = new FallSnowThread();
        }

        mSnows = new Snow[mSnowCount];
        for (int i = 0; i < mSnows.length; i++) {
            mSnows[i] = new Snow();
        }

        if (mFallSnowState == FallSnowState.START) {
            mFallSnowThread.start();
            mFallSnowState = FallSnowState.RUNNING;
        }


    }

    public void pauseSnow() {
        if (mFallSnowState == FallSnowState.RUNNING) {
            mFallSnowState = FallSnowState.PAUSE;
        }
    }

    public void resumeSnow() {
        if (mFallSnowState == FallSnowState.PAUSE) {
            mFallSnowState = FallSnowState.RUNNING;
        }
    }

    public void stopSnow() {
        mFallSnowState = FallSnowState.STOP;
    }

    private class FallSnowThread extends Thread {
        @Override
        public void run() {
            while (true) {
                switch (mFallSnowState) {
                    case RUNNING:
                        calculateSnowsNextAttr();
                        postInvalidate();
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case PAUSE:
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case STOP:
                        Thread.interrupted();
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
