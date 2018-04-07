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
    private float mScaleLineSpaceWidth; //刻度间的宽度
    private int mScaleLineTextColor; //刻度值的颜色
    private float mScaleLineTextSize; //刻度值字体大小

    private float mMaxValue; //最大刻度值
    private float mMiniValue; //最小刻度值
    private float mPerValue; //每个刻度的差值
    private float mSelectorValue; //当前选中的刻度

    private boolean isMarkLineEnable; //标线是否显示
    private boolean isMarkLineTrayEnable; //标线顶部托盘是否显示
    private boolean isMarkLineRetainScaleLineValueEnable; //标线值显示时，对应的刻度值是否显示
    private int mMarkLineColor; //标线的颜色
    private float mMarkLineWidth; //标线的宽度
    private int mMarkLineTextColor; //标线值的颜色
    private float mMarkLineTextSize; //标线值字体大小

    private int mTotalLine; //刻度线的条数
    private int mMaxOffset;
    private float mOffset;
    private int mLastX, mMove;

    private Paint mBaseLinePaint; //基线画笔
    private Paint mScaleLinePaint; //刻度线画笔
    private Paint mScaleLineTextPaint; //刻度值画笔
    private Paint mMarkLinePaint; //标线画笔
    private Paint mMarkLineTextPaint; //标线值画笔

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker; //速度追踪
    private int mMiniVelocity; //临界速度

    private OnScaleChangeListener mListener;
    private List<String> mScaleValues; //全部刻度值

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
            mScaleLineSpaceWidth = typedArray.getDimension(R.styleable.ScaleView_scaleLineSpaceWidth, 10);
            mScaleLineTextColor = typedArray.getColor(R.styleable.ScaleView_scaleLineTextColor, Color.parseColor("#FFCFCFCF"));
            mScaleLineTextSize = typedArray.getDimension(R.styleable.ScaleView_scaleLineTextSize, 18);

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

    /**
     * 外部调用设置刻度值及初始位置
     *
     * @param values   所有的刻度值
     * @param position 默认选中刻度的位置
     */
    public void setScaleValue(List<String> values, int position) {
        this.mScaleValues = values;
        this.mSelectorValue = toFloat(values.get(position));
        this.mMaxValue = toFloat(values.get(values.size() - 1));
        this.mMiniValue = toFloat(values.get(0));
        this.mTotalLine = values.size();

        float per = (mMaxValue - mMiniValue) / (mTotalLine - 1);
        this.mPerValue = (int) (per * 10.0f);

        mMaxOffset = (int) (-(mTotalLine - 1) * mScaleLineSpaceWidth);
        mOffset = (mMiniValue - mSelectorValue) / mPerValue * mScaleLineSpaceWidth * 10;

        invalidate();
    }

    private float toFloat(String value) {
        if (!TextUtils.isEmpty(value)) {
            return Float.valueOf(value);
        }
        return 0.0f;
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
        float left = srcPointX + mOffset;
        if (isScaleLineExtendEnable) {
            for (int i = 0; i < left; i += mScaleLineSpaceWidth / 5) {
                canvas.drawLine(i, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            }
        }

        for (int i = 0; i < mTotalLine; i++) {
            left = srcPointX + mOffset + i * mScaleLineSpaceWidth;
            if (i != mTotalLine - 1) {
                for (int j = 1; j < 5; j++) {
                    float lef = left + j * mScaleLineSpaceWidth / 5;
                    canvas.drawLine(lef, (mScaleViewHeight - mBaseLineMarginBottom) * 5 / 6, lef, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
                }
            }
            canvas.drawLine(left, (mScaleViewHeight - mBaseLineMarginBottom) * 3 / 4, left, mScaleViewHeight - mBaseLineMarginBottom, mScaleLinePaint);
            if (left != srcPointX || isMarkLineRetainScaleLineValueEnable) {
                canvas.drawText(mScaleValues.get(i), left - mScaleLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mBaseLineMarginBottom) * 7 / 10, mScaleLineTextPaint);
            }
        }

        if (isScaleLineExtendEnable) {
            for (int i = (int) left; i < mScaleViewWidth; i += mScaleLineSpaceWidth / 5) {
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
        String value = String.valueOf(mSelectorValue);
        if (!isMarkLineEnable || TextUtils.isEmpty(value)) {
            return;
        }

        if (value.contains(".")) {
            value = value.split("\\.")[0];
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
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker();
                return false;
            default:
                break;
        }
        mLastX = xPosition;

        return true;
    }

    private void changeMoveAndValue() {
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
        mSelectorValue = mMiniValue + Math.round(Math.abs(mOffset) * 1.0f / mScaleLineSpaceWidth) * mPerValue / 10.0f;

        notifyValueChange();
        postInvalidate();
    }

    private void countMoveEnd() {
        mOffset -= mMove;
        if (mOffset <= mMaxOffset) {
            mOffset = mMaxOffset;
        } else if (mOffset >= 0) {
            mOffset = 0;
        }

        mLastX = 0;
        mMove = 0;

        mSelectorValue = mMiniValue + Math.round(Math.abs(mOffset) * 1.0f / mScaleLineSpaceWidth) * mPerValue / 10.0f;
        mOffset = (mMiniValue - mSelectorValue) * 10.0f / mPerValue * mScaleLineSpaceWidth;

        notifyValueChange();
        postInvalidate();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            String value = String.valueOf(mSelectorValue);
            if (TextUtils.isEmpty(value)) {
                return;
            }

            if (value.contains(".")) {
                mListener.onScaleChange(value.split("\\.")[0]);
            } else {
                mListener.onScaleChange(value);
            }
        }
    }

    private void countVelocityTracker() {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMiniVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove = (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }

    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        mListener = listener;
    }

    public interface OnScaleChangeListener {
        void onScaleChange(String scale);
    }
}
