package com.yxcorp.gifshow.filter_gesture;

import com.yxcorp.gifshow.entity.FilterConfig;

/**
 * @author : weiwei.zhang06
 * @date : 2020/12/28 15:23
 */
public interface IFilterGestureCallBack {
    /**
     * 获取当前的滤镜
     * @return
     */
    FilterConfig getCurrentFilter();

    /**
     * 获取下一个滤镜
     * @param next 方向
     * @return
     */
    FilterConfig findNextFilter(boolean next);

    /**
     * 开始滑动
     * @param nextFilter
     * @param divideFactor
     */
    void beginFilterEffectSlide(FilterConfig nextFilter, float divideFactor);

    /**
     * 更新滑动
     * @param nextFilter
     * @param divideFactor
     */
    void updateFilterEffectSlide(FilterConfig nextFilter, float divideFactor);

    /**
     * 结束滑动
     * @param filter
     * @param chooseNew
     */
    void endFilterEffectSlide(FilterConfig filter, boolean chooseNew);
}
