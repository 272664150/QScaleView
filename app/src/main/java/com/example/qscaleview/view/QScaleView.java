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

    private float mLineMarginBottom; //与底部的间距

    private int mLineColor; //刻度线的颜色
    private float mLineWidth; //刻度线的宽度
    private float mLineSpaceWidth; //刻度间的宽度
    private int mLineTextColor; //刻度值的颜色
    private float mLineTextSize; //刻度值字体大小

    private float mMaxValue; //最大刻度值
    private float mMiniValue; //最小刻度值
    private float mPerValue; //每个刻度的差值
    private float mSelectorValue; //当前选中的刻度

    private boolean isLineHorizontalEnable; //横线是否显示
    private int mLineHorizontalColor; //横线的颜色
    private float mLineHorizontalHeight; //横线的高度

    private boolean isPointerLineEnable; //指针是否显示
    private boolean isPointerLineTriangleEnable; //指针顶部倒三角是否显示
    private int mPointerLineColor; //指针的颜色
    private float mPointerLineWidth; //指针的宽度
    private int mPointerLineTextColor; //指针值的颜色
    private float mPinterLineTextSize; //指针值字体大小

    private int mTotalLine; //刻度线的条数
    private int mMaxOffset;
    private float mOffset;
    private int mLastX, mMove;

    private Paint mLinePaint; //刻度线画笔
    private Paint mLineTextPaint; //刻度值画笔
    private Paint mLineHorizontalPaint; //横线画笔
    private Paint mPinterLinePaint; //指针画笔
    private Paint mPinterLineTextPaint; //指针值画笔

    private int mMiniVelocity; //临界速度
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker; //速度追踪

    private List<String> mScaleValues; //全部刻度值

    private OnScaleChangeListener mListener;

    public QScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleView);
        try {
            mLineMarginBottom = typedArray.getDimension(R.styleable.ScaleView_lineMarginBottom, 0);

            mLineColor = typedArray.getColor(R.styleable.ScaleView_lineColor, Color.parseColor("#FFCFCFCF"));
            mLineWidth = typedArray.getDimension(R.styleable.ScaleView_lineWidth, 1);
            mLineSpaceWidth = typedArray.getDimension(R.styleable.ScaleView_lineSpaceWidth, 10);
            mLineTextColor = typedArray.getColor(R.styleable.ScaleView_lineTextColor, Color.parseColor("#FFCFCFCF"));
            mLineTextSize = typedArray.getDimension(R.styleable.ScaleView_lineTextSize, 18);

            isLineHorizontalEnable = typedArray.getBoolean(R.styleable.ScaleView_lineHorizontalEnable, true);
            mLineHorizontalColor = typedArray.getColor(R.styleable.ScaleView_lineHorizontalColor, Color.parseColor("#FFCFCFCF"));
            mLineHorizontalHeight = typedArray.getDimension(R.styleable.ScaleView_lineHorizontalHeight, 1);

            isPointerLineEnable = typedArray.getBoolean(R.styleable.ScaleView_pointerLineEnable, true);
            isPointerLineTriangleEnable = typedArray.getBoolean(R.styleable.ScaleView_pointerLineTriangleEnable, true);
            mPointerLineColor = typedArray.getColor(R.styleable.ScaleView_pointerLineColor, Color.parseColor("#FFF87D2F"));
            mPointerLineWidth = typedArray.getDimension(R.styleable.ScaleView_pointerLineWidth, 1);
            mPointerLineTextColor = typedArray.getColor(R.styleable.ScaleView_pointerLineTextColor, Color.parseColor("#FFF87D2F"));
            mPinterLineTextSize = typedArray.getDimension(R.styleable.ScaleView_pointerLineTextSize, 40);

            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setStrokeWidth(mLineWidth / 2);
            mLinePaint.setColor(mLineColor);
            mLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLineTextPaint.setTextSize(mLineTextSize);
            mLineTextPaint.setColor(mLineTextColor);

            mLineHorizontalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLineHorizontalPaint.setStrokeWidth(mLineHorizontalHeight / 2);
            mLineHorizontalPaint.setColor(mLineHorizontalColor);

            mPinterLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPinterLinePaint.setStrokeWidth(mPointerLineWidth / 2);
            mPinterLinePaint.setColor(mPointerLineColor);
            mPinterLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPinterLineTextPaint.setTextSize(mPinterLineTextSize);
            mPinterLineTextPaint.setColor(mPointerLineTextColor);

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

        mMaxOffset = (int) (-(mTotalLine - 1) * mLineSpaceWidth);
        mOffset = (mMiniValue - mSelectorValue) / mPerValue * mLineSpaceWidth * 10;

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
        drawHorizontalLine(canvas);
        drawVerticalLineScale(canvas);
        drawScalePointer(canvas);
    }

    /**
     * 画底部的横线
     *
     * @param canvas
     */
    private void drawHorizontalLine(Canvas canvas) {
        if (!isLineHorizontalEnable) {
            return;
        }
        canvas.drawLine(0, mScaleViewHeight - mLineMarginBottom, mScaleViewWidth, mScaleViewHeight - mLineMarginBottom, mLineHorizontalPaint);
    }

    /**
     * 画刻度线及刻度值
     *
     * @param canvas
     */
    private void drawVerticalLineScale(Canvas canvas) {
//        int srcPointX = mScaleViewWidth / 2;
//        for (int i = 0; i < mTotalLine; i++) {
//            float left = srcPointX + mOffset + i * mLineSpaceWidth;
//            if (i != mTotalLine - 1) {
//                for (int j = 1; j < 5; j++) {
//                    float lef = left + j * mLineSpaceWidth / 5;
//                    canvas.drawLine(lef, (mScaleViewHeight - mLineMarginBottom) * 5 / 6, lef, mScaleViewHeight - mLineMarginBottom, mLinePaint);
//                }
//            }
//            canvas.drawLine(left, (mScaleViewHeight - mLineMarginBottom) * 3 / 4, left, mScaleViewHeight - mLineMarginBottom, mLinePaint);
//            if (left != srcPointX) {
//                canvas.drawText(mScaleValues.get(i), left - mLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mLineMarginBottom) * 7 / 10, mLineTextPaint);
//            }
//        }


//        int i = 0;
//        int srcPointX = mScaleViewWidth / 2;
//        for (int x = 0; x < mScaleViewWidth; x += mLineSpaceWidth / 5) {
//            float left = srcPointX + mOffset + i * mLineSpaceWidth;
//            if (i < mTotalLine && Math.abs(left - x) < 0.000001) {
//                canvas.drawLine(x, (mScaleViewHeight - mLineMarginBottom) * 3 / 4, x, mScaleViewHeight - mLineMarginBottom, mLinePaint);
//                canvas.drawText(mScaleValues.get(i), x - mLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mLineMarginBottom) * 7 / 10, mLineTextPaint);
//                i++;
//            } else {
//                canvas.drawLine(x, (mScaleViewHeight - mLineMarginBottom) * 5 / 6, x, mScaleViewHeight - mLineMarginBottom, mLinePaint);
//            }
//        }


        int srcPointX = mScaleViewWidth / 2;
        float left = srcPointX + mOffset;
        for (int i = 0; i < left; i += mLineSpaceWidth / 5) {
            canvas.drawLine(i, (mScaleViewHeight - mLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mLineMarginBottom, mLinePaint);
        }

        for (int i = 0; i < mTotalLine; i++) {
            left = srcPointX + mOffset + i * mLineSpaceWidth;
            if (i != mTotalLine - 1) {
                for (int j = 1; j < 5; j++) {
                    float lef = left + j * mLineSpaceWidth / 5;
                    canvas.drawLine(lef, (mScaleViewHeight - mLineMarginBottom) * 5 / 6, lef, mScaleViewHeight - mLineMarginBottom, mLinePaint);
                }
            }
            canvas.drawLine(left, (mScaleViewHeight - mLineMarginBottom) * 3 / 4, left, mScaleViewHeight - mLineMarginBottom, mLinePaint);
            if (left != srcPointX) {
                canvas.drawText(mScaleValues.get(i), left - mLineTextPaint.measureText(mScaleValues.get(i)) / 2, (mScaleViewHeight - mLineMarginBottom) * 7 / 10, mLineTextPaint);
            }
        }

        for (int i = (int) left; i < mScaleViewWidth; i += mLineSpaceWidth / 5) {
            canvas.drawLine(i, (mScaleViewHeight - mLineMarginBottom) * 5 / 6, i, mScaleViewHeight - mLineMarginBottom, mLinePaint);
        }
    }

    /**
     * 画刻度指针及刻度值
     *
     * @param canvas
     */
    private void drawScalePointer(Canvas canvas) {
        String value = String.valueOf(mSelectorValue);
        if (!isPointerLineEnable || TextUtils.isEmpty(value)) {
            return;
        }

        if (value.contains(".")) {
            value = value.split("\\.")[0];
        }
        canvas.drawText(value, mScaleViewWidth / 2 - mPinterLineTextPaint.measureText(value) / 2, mScaleViewHeight / 5, mPinterLineTextPaint);

        if (isPointerLineTriangleEnable) {
            Path path = new Path();
            path.moveTo(mScaleViewWidth / 2 - mLineSpaceWidth / 8, mScaleViewHeight / 5 + mLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2 + mLineSpaceWidth / 8, mScaleViewHeight / 5 + mLineSpaceWidth / 10);
            path.lineTo(mScaleViewWidth / 2, mScaleViewHeight / 5 + mLineSpaceWidth / 3);
            path.close();
            canvas.drawPath(path, mPinterLinePaint);
        }

        canvas.drawLine(mScaleViewWidth / 2, mScaleViewHeight / 5 + mLineSpaceWidth / 10, mScaleViewWidth / 2, mScaleViewHeight, mPinterLinePaint);
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
        mSelectorValue = mMiniValue + Math.round(Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue / 10.0f;

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

        mSelectorValue = mMiniValue + Math.round(Math.abs(mOffset) * 1.0f / mLineSpaceWidth) * mPerValue / 10.0f;
        mOffset = (mMiniValue - mSelectorValue) * 10.0f / mPerValue * mLineSpaceWidth;

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
