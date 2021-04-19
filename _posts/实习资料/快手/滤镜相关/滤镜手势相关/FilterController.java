package com.yxcorp.gifshow.filter_gesture;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;

import com.yxcorp.gifshow.entity.FilterConfig;
import com.yxcorp.gifshow.filter_gesture.view.FilterTextSwitcherView;

/**
 * @Auther: weiwei.zhang06
 * @Date: 2020/12/28 15:40
 */
public class FilterController {

    public static final int ALPHA_ANIMATION_DURING_TIME = 1000;

    private IFilterGestureCallBack mIFilterGestureCallBack;
    private FilterTextSwitcherView mSwitcherView;
    private FilterConfig mFilterConfig; // 当前正在使用的滤镜
    private FilterConfig mNewEffectTempForSlide; // 下一个滤镜
    private ObjectAnimator mFilterNameAnimator; // 文字透明度动画
    private FilterTouchAndGestureHandler mTouchAndGestureHandler;

    public FilterController(FilterTextSwitcherView switcherView) {
        this.mSwitcherView = switcherView;
        initAnim();
    }

    private void initAnim() {
        mFilterNameAnimator = ObjectAnimator.ofFloat(mSwitcherView, View.ALPHA, 1f, 1f, 0);
        mFilterNameAnimator.setDuration(ALPHA_ANIMATION_DURING_TIME * 2);
    }

    public void showText() {
        if (mSwitcherView != null) {
            if (mFilterNameAnimator.isRunning()) {
                mFilterNameAnimator.cancel();
            }
            mSwitcherView.setAlpha(1f);
        }
    }

    public void hideText() {
        if (mFilterNameAnimator.isRunning()) {
            mFilterNameAnimator.cancel();
        }
        mFilterNameAnimator.start();
    }


    public FilterController setGestureCallBack(IFilterGestureCallBack iFilterGestureCallBack) {
        this.mIFilterGestureCallBack = iFilterGestureCallBack;
        mFilterConfig = iFilterGestureCallBack.getCurrentFilter();
        return this;
    }

    public FilterController setSwitcherView(FilterTextSwitcherView filterTextSwitcherView) {
        this.mSwitcherView = filterTextSwitcherView;
        return this;
    }

    public boolean beginFilterEffectSlide(float divideFactor) {
        if (mIFilterGestureCallBack == null) {
            return false;
        }
        mFilterConfig = mIFilterGestureCallBack.getCurrentFilter();
        FilterConfig nextFilterConfig = mIFilterGestureCallBack.findNextFilter(divideFactor < 0);
        if (nextFilterConfig == null || mFilterConfig == null) {
            return false;
        }
        mSwitcherView.setCurText(mFilterConfig.mName);
        showText();
        if (mSwitcherView != null) {
            mSwitcherView.setNewText(nextFilterConfig.mName);
            mSwitcherView.startSwitch(divideFactor);
        }
        mNewEffectTempForSlide = nextFilterConfig;
        if (mIFilterGestureCallBack != null) {
            mIFilterGestureCallBack.beginFilterEffectSlide(nextFilterConfig, divideFactor);
        }
        return true;
    }

    public boolean updateFilterEffectSlide(float divideFactor) {
        if (mIFilterGestureCallBack == null) {
            return false;
        }
        FilterConfig nextFilterConfig = mIFilterGestureCallBack.findNextFilter(divideFactor < 0);
        if (nextFilterConfig == null) {
            return false;
        }
        if (shouldUpdateFilterText()) {
            mSwitcherView.startSwitch(divideFactor);
        }
        if (mIFilterGestureCallBack != null) {
            mIFilterGestureCallBack.updateFilterEffectSlide(nextFilterConfig, divideFactor);
        }
        return true;
    }

    public boolean endFilterEffectSlide(boolean chooseNew, boolean force2End) {
        if (!force2End && getTouchAndGestureHandler().triggerAutoFinish(mSwitcherView, chooseNew)) {
            return false;
        }
        if (chooseNew) {
            mFilterConfig = mNewEffectTempForSlide;
        }
        mNewEffectTempForSlide = null;
        if (mIFilterGestureCallBack != null) {
            mIFilterGestureCallBack.endFilterEffectSlide(mFilterConfig, chooseNew);
        }
        if (shouldUpdateFilterText()) {
            if (chooseNew) {
                mSwitcherView.finishSwitch();
            } else {
                mSwitcherView.cancel();
            }
            hideText();
        }
        return true;
    }

    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        getTouchAndGestureHandler().handleScroll(e1, e2);
    }

    public void onDispatchTouchEventAfterGesture(MotionEvent event) {
        getTouchAndGestureHandler().handleDispatchTouchEventAfterGesture(event.getAction(),
                event.getX());
    }


    private boolean shouldUpdateFilterText() {
        return mSwitcherView != null && mSwitcherView.getVisibility() == View.VISIBLE;
    }

    private FilterTouchAndGestureHandler getTouchAndGestureHandler() {
        if (mTouchAndGestureHandler == null) {
            mTouchAndGestureHandler = new FilterTouchAndGestureHandler(this);
        }
        return mTouchAndGestureHandler;
    }
}
