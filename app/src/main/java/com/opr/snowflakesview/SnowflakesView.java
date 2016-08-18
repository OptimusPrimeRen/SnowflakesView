package com.opr.snowflakesview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

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
        //下落速度
        mFallSpeed = typedArray.getFloat(R.styleable.SnowflakesView_fallSpeed, (float) 0.1);
        //下落雪花个数
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
        //此处使用Drawable对象而不是Bitmap，原因是：Drawable提供setAlpha方法，而Bitmap没有。当然，也可以通过设置canvas的alpha，不过测试表示效率较低。
        mSnowFlowerDrawable = getResources().getDrawable(R.drawable.image_cpu_cooler_snow);
        //获取图片原始高度，用于之后计算
        mSnowFlowerHeight = mSnowFlowerDrawable.getIntrinsicHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //整个view的宽高
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
            //通过设置Drawable边界，从而Drawable会自动对其中图片进行缩放，注意：这里的四个参数，分别是图片在整个view（canvas）中的顶点
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

    //单个雪花对象，用于保存雪花的状态
    private class Snow {
        public float mScale;
        public int mAlpha; //alpha
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
            //很简单，当监测到雪花落到view底部时，重新计算初始值。
            if (mY > mSnowViewHeight / mScale) {
                init();
            } else {
                //没到底部，以一定速率下降，这里乘以mScale使得越大的落得越快
                mY = mY + mScale * mSnowFlowerHeight * mFallSpeed;
            }
        }
    }

    //开始下雪
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

    //暂停下雪
    public void pauseSnow() {
        if (mFallSnowState == FallSnowState.RUNNING) {
            mFallSnowState = FallSnowState.PAUSE;
        }
    }

    //继续下雪
    public void resumeSnow() {
        if (mFallSnowState == FallSnowState.PAUSE) {
            mFallSnowState = FallSnowState.RUNNING;
        }
    }

    //停止下雪
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
                            //大约30帧的动画速度，不会太多冗余计算与刷新
                            //当然，如果4.0以上，可以使用valueAnimator，不需新开线程，损耗较小。
                            //也可以使用HandlerThread，Timer等，不过本质上都是线程。
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
