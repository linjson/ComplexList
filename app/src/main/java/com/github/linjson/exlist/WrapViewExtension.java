package com.github.linjson.exlist;

import android.support.annotation.FloatRange;

/**
 * Created by ljs on 2017/2/6.
 */
public interface WrapViewExtension {

    int STATE_PRE=1;
    int STATE_START=2;


    void setRate(@FloatRange(from = 0, to = 1) float rate);

    void showPreView();

    void showStartView();

    void resetView();

    void showFinishView();

    int getState();

    void setState(int state);
}
