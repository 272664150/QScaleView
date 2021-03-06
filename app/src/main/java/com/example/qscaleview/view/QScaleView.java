package com.example.qscaleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.example.qscaleview.R;

import java.util.List;

public class QScaleView extends View {

    private int mScaleViewWidth; //view的宽度
    private int mScaleViewHeight; //view的高度

    private boolean isBaseLineEnable; //基线是否显示
    private int mBaseLineColor; //基线的颜色
    private float mBaseLineHeight; //基线的高度
    private float mBaseLineMarginBottom; //基线与底部的间距

    private boolean isScaleLineExtendEnable; //刻度尺两侧空白区域的刻度线是否显示
    private int mScaleLineColor; //刻度线的颜色
    private float mScaleLineWidth; //刻度线的宽度
    private int mScaleLineTextColor; //刻度值的颜色
    private float mScaleLineTextSize; //刻度值字体大小
    private float mScaleLineSpaceWidth; //刻度线间的宽度
    private int mScaleLineSubSpaceCount; //刻度线间的子区间数

    private boolean isMarkLineEnable; //标线是否显示
    private int mMarkLineColor; //标线的颜色
    private float mMarkLineWidth; //标线的宽度
    private int mMarkLineTrayColor; //标线顶部托盘的颜色
    private int mMarkLineTrayStyle; //标线顶部托盘的样式
    private int mMarkLineTextColor; //标线值的颜色
    private float mMarkLineTextSize; //标线值字体大小
    private boolean isMarkLineRetainScaleLineValueEnable; //显示标线值时，对应的刻度值是否隐藏

    private Paint mBaseLinePaint; //基线画笔
    private Paint mScaleLinePaint; //刻度线画笔
    private Paint mScaleLineTextPaint; //刻度值画笔
    private Paint mMarkLinePaint; //标线画笔
    private Paint mMarkLineTrayPaint; //标线顶部托盘画笔
    private Paint mMarkLineTextPaint; //标线值画笔

    private List<String> mScaleValues; //全部的刻度值
    private int mScaleLineCount; //刻度线总条数
    private int mSelectedPosition; //选中刻度的位置
    private float mOffset; //偏移量
    private float mMaxOffset; //最大偏移量
    private int mLastX, mDeltaX, mLastY, mDeltaY;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker; //速度追踪

    private OnScaleChangeListener mListener;

    public QScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleView);
        try {
            isBaseLineEnable = typedArray.getBoolean(R.styleable.ScaleView_baseLineEnable, true);
            mBaseLineColor = typedArray.getColor(R.styleable.ScaleView_baseLineColor, Color.parseColor("#FFCFCFCF"));
            mBaseLineHeight = typedArray.getDimension(R.styleable.ScaleView_baseLineHeight, 1);
            mBaseLineMarginBottom = typedArray.getDimension(R.styleable.ScaleView_baseLineMarginBottom, 0);

            isScaleLineExtendEnable = typedArray.getBoolean(R.styleable.ScaleView_scaleLineExtendEnable, false);
            mScaleLineColor = typedArray.getColor(R.styleable.ScaleView_scaleLineColor, Color.parseColor("#FFCFCFCF"));
            mScaleLineWidth = typedArray.getDimension(R.styleable.ScaleView_scaleLineWidth, 1);
            mScaleLineTextColor = typedArray.getColor(R.styleable.ScaleView_scaleLineTextColor, Color.parseColor("#FFCFCFCF"));
            mScaleLineTextSize = typedArray.getDimension(R.styleable.ScaleView_scaleLineTextSize, 18);
            mScaleLineSpaceWidth = typedArray.getDimension(R.styleable.ScaleView_scaleLineSpaceWidth, 10);
            mScaleLineSubSpaceCount = typedArray.getInteger(R.styleable.ScaleView_scaleLineSubSpaceCount, 5);

            isMarkLineEnable = typedArray.getBoolean(R.styleable.ScaleView_markLineEnable, true);
            mMarkLineColor = typedArray.getColor(R.styleable.ScaleView_markLineColor, Color.parseColor("#FFF87D2F"));
            mMarkLineWidth = typedArray.getDimension(R.styleable.ScaleView_markLineWidth, 1);
            mMarkLineTrayColor = typedArray.getColor(R.styleable.ScaleView_markLineTrayColor, Color.parseColor("#FFF87D2F"));
            mMarkLineTrayStyle = typedArray.getInteger(R.styleable.ScaleView_markLineTrayStyle, 0);
            mMarkLineTextColor = typedArray.getColor(R.styleable.ScaleView_markLineTextColor, Color.parseColor("#FFF87D2F"));
            mMarkLineTextSize = typedArray.getDimension(R.styleable.ScaleView_markLineTextSize, 40);
            isMarkLineRetainScaleLineValueEnable = typedArray.getBoolean(R.styleable.ScaleView_markLineRetainScaleLineValueEnable, true);

            mBaseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBaseLinePaint.setStrokeWidth(mBaseLineHeight / 2);
            mBaseLinePaint.setColor(mBaseLineColor);

            mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mScaleLinePaint.setStrokeWidth(mScaleLineWidth / 2);
            mScaleLinePaint.setColor(mScaleLineColor);
            mScaleLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mScaleLineTextPaint.setTextSize(mScaleLineTextSize);
            mScaleLineTextPaint.setColor(mScaleLineTextColor);

            mMarkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMarkLinePaint.setStrokeWidth(mMarkLineWidth / 2);
            mMarkLinePaint.setColor(mMarkLineColor);
            mMarkLineTrayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMarkLineTrayPaint.setStrokeWidth(mMarkLineWidth / 4);
            mMarkLineTrayPaint.setColor(mMarkLineTrayColor);
            mMarkLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMarkLineTextPaint.setTextSize(mMarkLineTextSize);
            mMarkLineTextPaint.setColor(mMarkLineTextColor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle(); //防止OOM
            mScroller = new Scroller(context);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mScaleViewWidth = w;
            mScaleViewHeight = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBaseLine(canvas);
        drawScaleLines(canvas);
        drawMarkLine(canvas);
    }

    /**
     * 画基线
     *
     * @param canvas
     */
    private void drawBaseLine(Canvas canvas) {
        if (!isBaseLineEnable) {
            return;
        }
        canvas.drawLine(0, mScaleViewHeight - mBaseLineMarginBottom, mScaleViewWidth, mScaleViewHeight - mBaseLineMarginBottom, mBaseLinePaint);
    }

    /**
     * 画刻度线及刻度值
     *
     * @param canvas
     */
    private void drawScaleLines(Canvas canvas) {
        int srcPointX = mScaleViewWidth / 2;

        for (int i = 0; i < mScaleLineCount; i++) {
            float offsetX = srcPointX + mOffset + i * mScaleLineSpaceWidth;
            if (i != mScaleLineCount - 1) {
                for (int j = 1; j < mScaleLineSubSpaceCount; j++) {
                    float x = offsetX + j * mScaleLineSpaceWidth / mScaleLineSubSpaceCount;
                    canvas.drawLine(x, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, x, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
                }
            }
            canvas.drawLine(offsetX, (mScaleViewHeight - mBaseLineMarginBottom) * 3 / 4, offsetX, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            if (Math.abs(offsetX - srcPointX) >= mScaleLineSpaceWidth / mScaleLineSubSpaceCount || isMarkLineRetainScaleLineValueEnable) {
                canvas.drawText(mScaleValues.get(i), offsetX - mScaleLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mBaseLineMarginBottom) * 7 / 10, mScaleLineTextPaint);
            }
        }

        if (isScaleLineExtendEnable) {
            float offsetX = srcPointX + mOffset;
            for (int i = (int) offsetX - 1; i > 0; i -= mScaleLineSpaceWidth / 5) { //显示刻度尺左侧空白区域的刻度线
                canvas.drawLine(i, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            }
            offsetX = srcPointX + mOffset + (mScaleLineCount - 1) * mScaleLineSpaceWidth;
            for (int i = (int) offsetX + 1; i < mScaleViewWidth; i += mScaleLineSpaceWidth / 5) { //显示刻度尺右侧空白区域的刻度线
                canvas.drawLine(i, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            }
        }
    }

    /**
     * 画标线及标线值
     *
     * @param canvas
     */
    private void drawMarkLine(Canvas canvas) {
        String value = String.valueOf(getSelectedValue());
        if (!isMarkLineEnable || TextUtils.isEmpty(value)) {
            return;
        }

        canvas.drawText(value, mScaleViewWidth / 2 - mMarkLineTextPaint.measureText(value) / 2, mScaleViewHeight / 5, mMarkLineTextPaint);

        if (mMarkLineTrayStyle == 1) { //标线顶部托盘为实线
            canvas.drawLine(mScaleViewWidth / 2 - mScaleLineSpaceWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10, mScaleViewWidth / 2 + mScaleLineSpaceWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10, mMarkLineTrayPaint);
        } else if (mMarkLineTrayStyle == 2) { //标线顶部托盘为虚线
            setLayerType(View.LAYER_TYPE_SOFTWARE, mMarkLineTrayPaint); //画虚线需要关闭view层硬件加速
            mMarkLineTrayPaint.setPathEffect(new DashPathEffect(new float[]{13, 4}, 0));
            canvas.drawLine(mScaleViewWidth / 2 - mScaleLineSpaceWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10, mScaleViewWidth / 2 + mScaleLineSpaceWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10, mMarkLineTrayPaint);
        } else if (mMarkLineTrayStyle == 3) { //标线顶部托盘为倒三角
            Path path = new Path();
            path.moveTo(mScaleViewWidth / 2 - mScaleLineSpaceWidth / 8, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2 + mScaleLineSpaceWidth / 8, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 3);
            path.close();
            canvas.drawPath(path, mMarkLineTrayPaint);
        }

        canvas.drawLine(mScaleViewWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 7, mScaleViewWidth / 2, mScaleViewHeight, mMarkLinePaint);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                moveEnd();
                mLastX = 0;
                mDeltaX = 0;
            } else {
                int x = mScroller.getCurrX();
                mDeltaX = mLastX - x;
                moving();
                mLastX = x;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                mDeltaY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mDeltaX = mLastX - x;
                mDeltaY = mLastY - y;
                //当触摸scaleView在竖直方向滑动时，为了响应、跟随父view一起滑动，所以在 Math.abs(deltaX) < Math.abs(deltaY) / 5 的小范围内，把事件交给父view处理
                if (Math.abs(mDeltaX) < Math.abs(mDeltaY) / 5) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        mLastY = y;

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = x;
                mDeltaX = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                moving();
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveEnd();
                calculateVelocityTracker();
                mLastX = 0;
                mDeltaX = 0;
                break;
            default:
                break;
        }
        return true;
    }

    private void moving() {
        mOffset -= mDeltaX;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
            mDeltaX = 0;
            mScroller.forceFinished(true);
        } else if (mOffset >= 0) {
            mOffset = 0;
            mDeltaX = 0;
            mScroller.forceFinished(true);
        }

        mSelectedPosition = Math.round(Math.abs(mOffset) / mScaleLineSpaceWidth);

        notifyScaleChange();
        postInvalidate();
    }

    private void moveEnd() {
        mOffset -= mDeltaX;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mSelectedPosition = Math.round(Math.abs(mOffset) / mScaleLineSpaceWidth);
        mOffset = -mSelectedPosition * mScaleLineSpaceWidth;

        notifyScaleChange();
        postInvalidate();
    }

    private void notifyScaleChange() {
        if (mListener != null) {
            String value = String.valueOf(getSelectedValue());
            if (TextUtils.isEmpty(value)) {
                return;
            }
            mListener.onScaleChange(value, mSelectedPosition);
        }
    }

    private void calculateVelocityTracker() {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity()) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 获取选中的刻度值
     *
     * @return
     */
    private String getSelectedValue() {
        if (mScaleValues != null && mScaleValues.size() > mSelectedPosition) {
            String value = mScaleValues.get(mSelectedPosition);
            if (TextUtils.isEmpty(value)) {
                return "";
            }
            if (value.contains(".")) {
                value = value.split("\\.")[0];
            }
            return value;
        }
        return "";
    }

    /**
     * 初始化全部的刻度值及默认位置
     *
     * @param values   全部的刻度值
     * @param position 默认位置
     */
    public void setScaleInfo(List<String> values, int position) {
        this.mScaleValues = values;
        this.mScaleLineCount = values.size();
        this.mSelectedPosition = position;
        this.mOffset = -position * mScaleLineSpaceWidth;
        this.mMaxOffset = -(mScaleLineCount - 1) * mScaleLineSpaceWidth;

        invalidate();
    }

    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mListener = listener;
    }

    public interface OnScaleChangeListener {
        void onScaleChange(String scale, int position);
    }
}
