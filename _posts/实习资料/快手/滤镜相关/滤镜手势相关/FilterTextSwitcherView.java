package com.yxcorp.gifshow.filter_gesture.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.yxcorp.gifshow.filter_gesture.FilterDividerFactorUtils;
import com.yxcorp.gifshow.util.CommonUtil;

import androidx.annotation.Nullable;

/**
 * 滤镜文字展示的容器类
 * 内部有两个滤镜文字类：正常时显示一个；
 */
public class FilterTextSwitcherView extends View {
    private static final String TAG = "FilterTextSwitcher";

    private Matrix mMatrix = new Matrix();

    private static final float FADE_EDGE_PX = CommonUtil.dip2px(8);

    // 当前显示的
    private FilterTextView mCurView;
    // 左边或者右边，可以滑动出来的
    private FilterTextView mNewView;

    // 参见DividerFactor，对应到view布局上时可以按照下面做法
    // mCurView偏离的位置: 0为不动，1为view.left从左移到右，-1为view.right从右移到左
    private float mCurDivideFactor = 0f;

    private boolean isLeft2Right() {
        return mCurDivideFactor > 0;
    }

    public FilterTextSwitcherView(Context context) {
        this(context, null, 0);
    }

    public FilterTextSwitcherView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterTextSwitcherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCurView = new FilterTextView(getContext());
        mNewView = new FilterTextView(getContext());

        mNewView.setVisibility(View.GONE);

        setHorizontalFadingEdgeEnabled(true);
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        return FADE_EDGE_PX;
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        return FADE_EDGE_PX;
    }

    public void startSwitch(float divideFactor) {
        if (FilterDividerFactorUtils.shouldIgnoreUpdate(mCurDivideFactor, divideFactor)) {
            return;
        }
        mCurDivideFactor = divideFactor;
        mNewView.setVisibility(View.VISIBLE);
        relayoutAndInvalidate();
    }

    public void cancel() {
        Log.d(TAG, "cancel");
        mCurDivideFactor = 0f;
        switchViewInternal(true);
        relayoutAndInvalidate();
    }

    public void finishSwitch() {
        Log.d(TAG, "finish");
        mCurDivideFactor = 0f;
        switchViewInternal(false);
        relayoutAndInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mCurView.measure(widthMeasureSpec, heightMeasureSpec);
        if (mNewView.getVisibility() == View.VISIBLE) {
            mNewView.measure(widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(mCurView.getMeasuredWidth(), mCurView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;

        final int curTextWidth = mCurView.getMeasuredWidth();
        final int curTextHeight = mCurView.getMeasuredHeight();

        int curL;
        final int curT = (height - curTextHeight) / 2;

        if (mNewView.getVisibility() == View.VISIBLE) {
            final int newTextWidth = mNewView.getMeasuredWidth();
            final int distance = width;
            final int offset = (int) (distance * mCurDivideFactor);
            curL = offset;

            int newTextL = (isLeft2Right() ? -newTextWidth : width) + offset;
            mNewView.layout(newTextL, curT, newTextL + newTextWidth, curT + curTextHeight);
            mCurView.layout(curL, curT, curL + curTextWidth, curT + curTextHeight);
        } else {
            curL = 0;
            mCurView.layout(curL, curT, curL + curTextWidth, curT + curTextHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNewView.getVisibility() != VISIBLE) {
            mCurView.draw(canvas);
        } else {
            drawTwoView(canvas);
        }
    }

    private void drawTwoView(Canvas canvas) {
        drawCurView(mCurView, canvas, isLeft2Right());
        drawNewView(mNewView, canvas, isLeft2Right());
    }

    private void drawCurView(FilterTextView view, Canvas canvas, boolean isLeft2Right) {
        canvas.save();

        // 移动到当前view的位置
        canvas.translate(view.getX(), view.getY());

        final Matrix matrix = mMatrix;

        if (!isLeft2Right) {
            matrix.preTranslate(-view.getWidth(), 0);
            matrix.postTranslate(view.getWidth(), 0);
        }
        canvas.concat(matrix);
        view.draw(canvas);
        canvas.restore();
    }

    private void drawNewView(FilterTextView view, Canvas canvas, boolean isLeft2Right) {
        canvas.save();

        // 移动到当前view的位置
        canvas.translate(view.getX(), view.getY());

        final Matrix matrix = mMatrix;

        matrix.reset();

        if (isLeft2Right) {
            matrix.preTranslate(-view.getWidth(), 0);
            matrix.postTranslate(view.getWidth(), 0);
        }
        canvas.concat(matrix);
        view.draw(canvas);

        canvas.restore();
    }


    public void setCurText(String displayName) {
        mCurView.setName(displayName);
        relayoutAndInvalidate();
    }

    public void setNewText(String displayName) {
        mNewView.setName(displayName);
        // no need update
    }

    public CharSequence getCurName() {
        return mCurView.getName();
    }


    private void relayoutAndInvalidate() {
        requestLayout();
        invalidate();
    }

    private void switchViewInternal(boolean cancel) {
        if (cancel) {
            mNewView.setVisibility(View.GONE);
        } else {
            mCurView.setVisibility(View.GONE);

            FilterTextView view = mCurView;
            mCurView = mNewView;
            mNewView = view;
        }
    }
}

