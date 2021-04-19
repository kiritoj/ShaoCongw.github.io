package com.yxcorp.gifshow.filter_gesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kwai.framework.app.AppEnv;
import com.yxcorp.gifshow.filter_gesture.FilterController;
import com.yxcorp.gifshow.filter_gesture.FilterDividerFactorUtils;
import com.yxcorp.gifshow.util.CommonUtil;
import com.yxcorp.utility.Log;
import com.yxcorp.utility.ViewUtil;

public class FilterTouchAndGestureHandler {
    private static final String TAG = "FilterTouchAndGesture";

    private final FilterController mFilterController;

    // 常量区
    // 第一次显示的条件，显示中不会判断该条件。后续的点要剪掉这个值，避免不能从0f开始显示。
    private static final float THRESHOLD_X_DISTANCE_SHOW_SWITCH_PX = CommonUtil.dip2px(30);
    // 滑多远松手算选中有效，不够为取消。用来除屏幕宽度的
    private static final float FACTOR_FOR_SWITCH_TO_NEW = 3.0f;
    // 用来计算滑动距离占据多少屏幕比，新旧比例依据计算结果显示
    private static float sScreenWidth = -1f;

    // 状态变量
    // 每次down事件时初始false；false时调用Begin，true时调用update；
    // 由于Show有阈值限制，可能出现Swipe事件触发，但是没有触发跟手显示；
    private boolean mSlideEffectShowing;
    // 用于在Swipe与Scroll/onDispatchTouchEventAfterGesture协调
    // 默认为false；Swipe如果处理事件，则为true；其它地方发现为true，则中断处理流程；
    private boolean mIgnoreTouchEvent;
    // 触发点，所有的触发，必定会初始化
    private float mDownPointX;
    // 由于触发swipe的距离和动画的有效距离可能不一致；
    // 初始与Down一致，每次有效的滚动时更新；
    private float mLastScrollPointX;
    // 记录上一次有效的位置，用于限制更新频率，同时用于自动结束动画
    private float mLastDivideFactor;

    // 松手后的自动动画补全
    private ObjectAnimator mAutoFinishAnimator;
    private static final int AUTO_FINISH_ANIMATION_DURATION_TIME = 250;

    public FilterTouchAndGestureHandler(FilterController filterController) {
        mFilterController = filterController;
    }

    // 增加对垂直偏移的限制，超出时，取消跟手，为了与聚焦圈避让
    public void handleScroll(MotionEvent downEvent, MotionEvent curEvent) {
        if (mIgnoreTouchEvent) {
            Log.d(TAG, mIgnoreTouchEvent ? "ignore" : "miss show threshold when scroll");
            return;
        }

        float curXSubThreshold = curEvent.getX();

        // 禁止滚动方向与初始方向相反：因begin之后，new filter已经固定
        if (mDownPointX != mLastScrollPointX
                && (mDownPointX < curXSubThreshold ^ mDownPointX < mLastScrollPointX)) {
            Log.d(TAG, "current pos against with ori direction.");
            return;
        }

        if (!mIgnoreTouchEvent) {
            mLastScrollPointX = curXSubThreshold;
            showOrUpdateFilterSwitch(getDivideFactor(curXSubThreshold));
        }
    }

    private float subPosWithShowThreshold(float curX) {
        return curX + (curX > mDownPointX
                ? -THRESHOLD_X_DISTANCE_SHOW_SWITCH_PX
                : THRESHOLD_X_DISTANCE_SHOW_SWITCH_PX);
    }

    private boolean reachThreshold(float downPx, float upPx, float threshold) {
        return Math.abs(downPx - upPx) > threshold;
    }

    // onDispatchTouchEventAfterGesture: 确保在Gesture之后，可以保证onSwipe先掉用
    // 处理UP, CANCEL事件，(由于onSwipe也响应多手指操作，因此此处也不过滤）
    // 如果UP没取消，停止跟手，选择滤镜，需要满足水平距离阈值
    // 由于Gesture不回传CANCEL事件，因此在此处理
    //
    // 增加DOWN事件，表示新的开始，复位相关参数
    // 增加多点触控，取消跟手，为了与缩放避让。
    public void handleDispatchTouchEventAfterGesture(int action, float xPos) {
        if (action == MotionEvent.ACTION_DOWN) {
            resetSwitchState(xPos);
            return;
        }

        final boolean isPointerDown =
                (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN;
        if ((action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL && !isPointerDown)
                || mIgnoreTouchEvent) {
            if (mIgnoreTouchEvent) {
                Log.d(TAG, "handled, ignore up");
            }
            return;
        }
        if (action == MotionEvent.ACTION_CANCEL || isPointerDown) {
            mIgnoreTouchEvent = true;
            Log.i(TAG, "handled, cancel action");
            cancelFilterSwitchIfNeeded();
            return;
        }

        if (mSlideEffectShowing) {
            finishFilterSwitch(
                    reachThreshold(mDownPointX, xPos, ViewUtil.getScreenWidth(AppEnv.getAppContext())
                            / FACTOR_FOR_SWITCH_TO_NEW),
                    false);
        }
    }

    void resetSwitchState(float downX) {
        mDownPointX = downX;
        mIgnoreTouchEvent = false;
        mSlideEffectShowing = false;
        mLastScrollPointX = downX;
        mLastDivideFactor = 0f;
        if (mAutoFinishAnimator != null
                && (mAutoFinishAnimator.isStarted() || mAutoFinishAnimator.isRunning())) {
            mAutoFinishAnimator.cancel();
        }
    }

    // 正数时：[0 - divideFactor]为new，[divideFactor - 1]为old
    // 负数时：[0 - divideFactor]为old，[divideFactor - 1]为new
    private float getDivideFactor(float curPx) {
        return FilterDividerFactorUtils.limit((curPx - mDownPointX) / ViewUtil.getScreenWidth(AppEnv.getAppContext()));
    }

    private boolean isLeft2Right() {
        return mDownPointX < mLastScrollPointX;
    }

    // 正数时：[0 - divideFactor]为new，[divideFactor - 1]为old
    // 负数时：[0 - divideFactor]为old，[divideFactor - 1]为new
    private void showOrUpdateFilterSwitch(float divideFactor) {
        if (mSlideEffectShowing) {
            Log.d(TAG, "last " + mLastDivideFactor + ", new " + divideFactor);
            if (FilterDividerFactorUtils.shouldIgnoreUpdate(mLastDivideFactor, divideFactor)) {
                Log.d(TAG, "ignore too quick");
                return;
            }
            mFilterController.updateFilterEffectSlide(divideFactor);
        } else {
            mSlideEffectShowing = mFilterController.beginFilterEffectSlide(divideFactor);
        }
        mLastDivideFactor = divideFactor;
    }

    private boolean isSwipeAndScrollDirSame(boolean right2Left) {
        // down可能为空；scrollPoint可能没有更新；
        // 能触发swipe，down和最后的scrollPoint要满足swipe距离。
        if (right2Left) {
            return mDownPointX > mLastScrollPointX;
        } else {
            return mLastScrollPointX > mDownPointX;
        }
    }

    // 1）如果swipe触发时，与滚动的方向不同，可丢弃该swipe；
    // 2）如果swipe触发时，与滚动的方向相同，
    // a）如果在跟手显示中，则强制end(new)（忽略滚动距离限制）
    // b) 否则由外部处理，并禁止跟手对后续up的响应
    public boolean handleSwipe(boolean next) {
        if (!isSwipeAndScrollDirSame(next)) {
            Log.d(TAG, "swipe against scroll");
            return true;
        }

        if (mSlideEffectShowing) {
            mIgnoreTouchEvent = true;
            finishFilterSwitch(true, false);
            Log.d(TAG, "swipe end scroll");
            return true;
        }
        return false;
    }

    private void finishFilterSwitch(boolean chooseNew, boolean force2End) {
        Log.i(TAG, "finishFilterSwitch " + chooseNew);
        mSlideEffectShowing = false;
        mFilterController.endFilterEffectSlide(chooseNew, force2End);
    }

    private void cancelFilterSwitchIfNeeded() {
        Log.d(TAG, "cancelFilterSwitch " + mSlideEffectShowing);
        if (mSlideEffectShowing) {
            finishFilterSwitch(false, false);
        }
    }

    void setIgnoreTouchEvent(boolean handled) {
        mIgnoreTouchEvent = handled;
    }

    public boolean needAutoFinish() {
        return !FilterDividerFactorUtils.isSpecialDivide(mLastDivideFactor);
    }

    public float getAutoFinishDiff(boolean chooseNew) {
        if (!chooseNew) {
            return 0 - mLastDivideFactor;
        }
        if (mLastScrollPointX > mDownPointX) {
            return 1 - mLastDivideFactor;
        }
        return -1 - mLastDivideFactor;
    }

    public void handleDummyScroll(float newDivide) {
        Log.d(TAG, "handleDummyScroll " + newDivide);
        showOrUpdateFilterSwitch(newDivide);
    }

    public boolean triggerAutoFinish(View filterContainer, boolean chooseNew) {
        if (filterContainer == null || !needAutoFinish()) {
            Log.d(TAG, "auto finish no need");
            return false;
        }
        final float totalDiff = getAutoFinishDiff(chooseNew);
        final float lastDivide = mLastDivideFactor;

        Log.d(TAG, "auto finish total diff " + totalDiff);
        if (mAutoFinishAnimator == null) {
            mAutoFinishAnimator = ObjectAnimator.ofFloat(filterContainer, View.ALPHA, 1f, 2f);
            mAutoFinishAnimator.setDuration(AUTO_FINISH_ANIMATION_DURATION_TIME);
            mAutoFinishAnimator.setInterpolator(new CubicEaseOutInterpolator());
        }
        mAutoFinishAnimator.removeAllUpdateListeners();
        mAutoFinishAnimator.addUpdateListener(animation -> handleDummyScroll(
                lastDivide + totalDiff * ((float) animation.getAnimatedValue() - 1f)));
        mAutoFinishAnimator.removeAllListeners();
        mAutoFinishAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCanceled = false;

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCanceled) {
                    return;
                }
                Log.d(TAG, "auto finish end");
                finishFilterSwitch(chooseNew, true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "auto finish cancel");
                mCanceled = true;
                finishFilterSwitch(chooseNew, true);
            }
        });
        mAutoFinishAnimator.start();
        Log.d(TAG, "auto finish start");
        return true;
    }

    void handleInterrupt() {
        Log.d(TAG, "interrupt");
        cancelFilterSwitchIfNeeded();
    }
}
