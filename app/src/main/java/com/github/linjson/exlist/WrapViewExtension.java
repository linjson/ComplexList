package com.github.linjson.exlist;

import android.support.annotation.FloatRange;

/**
 * Created by ljs on 2017/2/6.
 */
public interface WrapViewExtension {
    void setRate(@FloatRange(from = 0, to = 1) float rate);

    void showPreView();

    void showStartView();

    void resetView();

    void showFinishView();
}
