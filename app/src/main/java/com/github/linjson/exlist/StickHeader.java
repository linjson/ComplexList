package com.github.linjson.exlist;

import android.view.View;

/**
 * Created by ljs on 2017/9/5.
 */

public interface StickHeader {

    boolean isStickHeader(int pos);

    void showStickHeader(View view);

    void hideStickHeader(View view);
}
