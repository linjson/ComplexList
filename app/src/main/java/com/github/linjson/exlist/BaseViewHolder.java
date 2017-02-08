package com.github.linjson.exlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ljs on 16/9/6.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements ViewHolderExtension {

    private boolean fixed = false;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public float getActionWidth() {
        return 0;
    }

    @Override
    public View getFrontView() {
        return null;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public int getGroupId() {
        return -1;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public int getSwipeDirection() {
        return 0;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
}
