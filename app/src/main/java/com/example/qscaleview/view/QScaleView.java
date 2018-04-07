package com.example.qscaleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private boolean isMarkLineTrayEnable; //标线顶部托盘是否显示
    private boolean isMarkLineRetainScaleLineValueEnable; //标线值显示时，对应的刻度值是否显示
    private int mMarkLineColor; //标线的颜色
    private float mMarkLineWidth; //标线的宽度
    private int mMarkLineTextColor; //标线值的颜色
    private float mMarkLineTextSize; //标线值字体大小

    private Paint mBaseLinePaint; //基线画笔
    private Paint mScaleLinePaint; //刻度线画笔
    private Paint mScaleLineTextPaint; //刻度值画笔
    private Paint mMarkLinePaint; //标线画笔
    private Paint mMarkLineTextPaint; //标线值画笔

    private List<String> mScaleValues; //全部的刻度值
    private int mScaleLineCount; //刻度线总条数
    private int mLastX; //上次移动到的x坐标
    private int mMove;
    private float mOffset; //偏移量
    private float mMaxOffset; //最大偏移量
    private int mSelectedPosition; //选中刻度的位置

    private Scroller mScroller;
    private int mMiniVelocity; //临界速度
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
            isMarkLineTrayEnable = typedArray.getBoolean(R.styleable.ScaleView_markLineTrayEnable, true);
            isMarkLineRetainScaleLineValueEnable = typedArray.getBoolean(R.styleable.ScaleView_markLineRetainScaleLineValueEnable, true);
            mMarkLineColor = typedArray.getColor(R.styleable.ScaleView_markLineColor, Color.parseColor("#FFF87D2F"));
            mMarkLineWidth = typedArray.getDimension(R.styleable.ScaleView_markLineWidth, 1);
            mMarkLineTextColor = typedArray.getColor(R.styleable.ScaleView_markLineTextColor, Color.parseColor("#FFF87D2F"));
            mMarkLineTextSize = typedArray.getDimension(R.styleable.ScaleView_markLineTextSize, 40);

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
            mMarkLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMarkLineTextPaint.setTextSize(mMarkLineTextSize);
            mMarkLineTextPaint.setColor(mMarkLineTextColor);

            mScroller = new Scroller(context);
            mMiniVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //防止OOM
            typedArray.recycle();
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
            if (offsetX != srcPointX || isMarkLineRetainScaleLineValueEnable) {
                canvas.drawText(mScaleValues.get(i), offsetX - mScaleLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mBaseLineMarginBottom) * 7 / 10, mScaleLineTextPaint);
            }
        }

        if (isScaleLineExtendEnable) {
            float offsetX = srcPointX + mOffset;
            for (int i = (int) offsetX - 1; i > 0; i -= mScaleLineSpaceWidth / 5) {
                canvas.drawLine(i, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            }
            offsetX = srcPointX + mOffset + (mScaleLineCount - 1) * mScaleLineSpaceWidth;
            for (int i = (int) offsetX + 1; i < mScaleViewWidth; i += mScaleLineSpaceWidth / 5) {
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
        if (isMarkLineTrayEnable) {
            Path path = new Path();
            path.moveTo(mScaleViewWidth / 2 - mScaleLineSpaceWidth / 8, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2 + mScaleLineSpaceWidth / 8, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 3);
            path.close();
            canvas.drawPath(path, mMarkLinePaint);
        }
        canvas.drawLine(mScaleViewWidth / 2, mScaleViewHeight / 5 + mScaleLineSpaceWidth / 10, mScaleViewWidth / 2, mScaleViewHeight, mMarkLinePaint);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                moveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove = (mLastX - xPosition);
                moving();
                mLastX = xPosition;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int xPosition = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove = (mLastX - xPosition);
                moving();
                mLastX = xPosition;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                moveEnd();
                calculateVelocityTracker();
                return false;
            default:
                break;
        }
        return true;
    }

    private void moving() {
        mOffset -= mMove;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
            mMove = 0;
            mScroller.forceFinished(true);
        } else if (mOffset >= 0) {
            mOffset = 0;
            mMove = 0;
            mScroller.forceFinished(true);
        }
        mSelectedPosition = Math.round(Math.abs(mOffset) / mScaleLineSpaceWidth);

        notifyScaleChange();
        postInvalidate();
    }

    private void moveEnd() {
        mOffset -= mMove;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mLastX = 0;
        mMove = 0;

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
        if (Math.abs(xVelocity) > mMiniVelocity) {
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
