package com.github.linjson.exlist;

import android.view.View;

public interface ViewHolderExtension {

    float getActionWidth();

    View getFrontView();

    boolean isFixed();

    int getGroupId();

    boolean isGroup();

    int getSwipeDirection();
}
