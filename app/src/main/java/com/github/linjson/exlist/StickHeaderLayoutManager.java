package com.github.linjson.exlist;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by ljs on 2017/9/5.
 */

public class StickHeaderLayoutManager<T extends RecyclerView.Adapter & StickHeader> extends LinearLayoutManager {

    private T mAdapter;
    private HeaderListDataObserver mHeaderListDataObserver;
    private ArrayList<Integer> mHeaderPosition = new ArrayList<>(0);
    private float mTranslationY = 0f;
    private float mTranslationX = 0f;
    private View mStickView;
    private int mStickPosition;

    public StickHeaderLayoutManager(Context context) {
        this(context, false);
    }

    public StickHeaderLayoutManager(Context context, boolean reverseLayout) {
        super(context, LinearLayoutManager.VERTICAL, reverseLayout);

        mHeaderListDataObserver = new HeaderListDataObserver();
    }


    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        setAdapter(view.getAdapter());

    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        setAdapter(newAdapter);
    }

    private void setAdapter(RecyclerView.Adapter adapter) {
        mHeaderListDataObserver.setStickHeaderLayout(this);
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mHeaderListDataObserver);
        }

        if (adapter instanceof StickHeader) {
            mAdapter = (T) adapter;
            mAdapter.registerAdapterDataObserver(mHeaderListDataObserver);
            mHeaderListDataObserver.onChanged();

        } else {
            mAdapter = null;
            mHeaderPosition.clear();
        }


    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

        detachStickHeaderView();
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        attachStickHeaderView();
        if (scroll != 0) {
            updateStickyHeader(recycler, false);
        }
        return scroll;

    }

    private void detachStickHeaderView() {
        if (mStickView != null) {
            detachView(mStickView);
        }
    }

    public void attachStickHeaderView() {
        if (mStickView != null) {
            attachView(mStickView);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachStickHeaderView();
        super.onLayoutChildren(recycler, state);
        attachStickHeaderView();
        if (!state.isPreLayout()) {
            updateStickyHeader(recycler, true);
        }
    }

    private void updateStickyHeader(RecyclerView.Recycler recycler, boolean layout) {


        final int childCount = getChildCount();

        final int stickCount = mHeaderPosition.size();

        if (stickCount == 0 || childCount == 0) {
            return;
        }
        int anchorPos = -1;
        View anchorView = null;
        int adapterDataPos = -1;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            RecyclerView.LayoutParams layoutParam = (RecyclerView.LayoutParams) v.getLayoutParams();
            if (isValidataView(v, layoutParam)) {
                anchorPos = i;
                anchorView = v;
                adapterDataPos = layoutParam.getViewAdapterPosition();
                break;
            }
        }

        if (anchorPos == -1 || anchorView == null) {
            return;
        }

        int stickHeaderIndex = findStickHeaderIndex(adapterDataPos);
        int stickAdapterIndex = stickHeaderIndex == -1 ? -1 : mHeaderPosition.get(stickHeaderIndex);
        int stickNextAdapterIndex = stickHeaderIndex + 1 < stickCount ? mHeaderPosition.get(stickHeaderIndex + 1) : -1;
        if (mStickView != null && stickHeaderIndex == -1) {
            scrapStickyHeader(recycler);
            return;
        }

        if (stickHeaderIndex == -1) {
            return;
        }

        if (mStickView == null) {
            createStickHeaderView(recycler, stickAdapterIndex);
        }

        if (layout || getPosition(mStickView) != stickAdapterIndex) {
            bindStickyHeader(recycler, stickAdapterIndex);
        }

        View nextStickView = null;
        if (stickNextAdapterIndex != -1) {
            nextStickView = getChildAt(anchorPos + stickNextAdapterIndex - adapterDataPos);
            if (nextStickView == mStickView) {
                nextStickView = null;
            }
        }


        mStickView.setTranslationY(getY(nextStickView));


    }

    private float getY(View nextView) {
        if (getOrientation() == VERTICAL) {
            float y = mTranslationY;
            if (getReverseLayout()) {
                y += getHeight() - mStickView.getHeight();
            }
            if (nextView != null) {

                if (getReverseLayout()) {
                    y = Math.max(nextView.getBottom(), y);
                } else {
                    y = Math.min(nextView.getTop() - mStickView.getHeight(), y);
                }
            }
            return y;


        } else {
            return mTranslationY;
        }
    }

    private void scrapStickyHeader(RecyclerView.Recycler recycler) {

        View stickView = mStickView;
        mStickView = null;
        mStickPosition = -1;

        stickView.setTranslationX(0);
        stickView.setTranslationY(0);

        mAdapter.hideStickHeader(stickView);

        stopIgnoringView(stickView);

        removeView(stickView);

        if (recycler != null) {
            recycler.recycleView(stickView);
        }


    }

    private void bindStickyHeader(RecyclerView.Recycler recycler, int stickAdapterIndex) {
        recycler.bindViewToPosition(mStickView, stickAdapterIndex);

        measureAndLayout(mStickView);
        mStickPosition = stickAdapterIndex;

    }

    private void createStickHeaderView(RecyclerView.Recycler recycler, int stickAdapterIndex) {

        View v = recycler.getViewForPosition(stickAdapterIndex);


        addView(v);
        measureAndLayout(v);
        ignoreView(v);

        mAdapter.showStickHeader(v);


        mStickView = v;
        mStickPosition = stickAdapterIndex;


    }

    private void measureAndLayout(View stickyHeader) {
        measureChildWithMargins(stickyHeader, 0, 0);
        if (getOrientation() == VERTICAL) {
            stickyHeader.layout(getPaddingLeft(), 0, getWidth() - getPaddingRight(), stickyHeader.getMeasuredHeight());
        } else {
            stickyHeader.layout(0, getPaddingTop(), stickyHeader.getMeasuredWidth(), getHeight() - getPaddingBottom());
        }
    }

    private int findStickHeaderIndex(int adapterDataPos) {
        final int stickCount = mHeaderPosition.size();
        int low = 0;
        int high = stickCount - 1;


        while (low <= high) {

            int m = (low + high) / 2;
            if (mHeaderPosition.get(m) > adapterDataPos) {
                high = m - 1;
            } else if (m + 1 < stickCount && mHeaderPosition.get(m + 1) <= adapterDataPos) {
                low = m + 1;
            } else {
                return m;
            }

        }


        return -1;
    }

    private boolean isValidataView(View view, RecyclerView.LayoutParams layoutParam) {

        if (!layoutParam.isItemRemoved() && !layoutParam.isViewInvalid()) {
            if (getOrientation() == VERTICAL) {
                if (getReverseLayout()) {
                    return view.getTop() + view.getTranslationY() <= getHeight() + mTranslationY;
                } else {
                    return view.getBottom() - view.getTranslationY() >= mTranslationY;
                }
            } else {
                if (getReverseLayout()) {
                    return view.getLeft() + view.getTranslationX() <= getWidth() + mTranslationX;
                } else {
                    return view.getRight() - view.getTranslationX() >= mTranslationX;
                }
            }
        }

        return false;

    }

    private class HeaderListDataObserver extends RecyclerView.AdapterDataObserver {


        StickHeaderLayoutManager mLayoutManager;
        StickHeader mStickHeader;

        public void setStickHeaderLayout(StickHeaderLayoutManager layoutManager) {
            mLayoutManager = layoutManager;
            if (mLayoutManager.mAdapter instanceof StickHeader) {
                mStickHeader = (StickHeader) mLayoutManager.mAdapter;
            }
        }

        @Override
        public void onChanged() {
            reset();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            reset();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            reset();

        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            reset();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            reset();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            reset();
        }

        private void reset() {

            mLayoutManager.mHeaderPosition.clear();

            final int count = mLayoutManager.mAdapter.getItemCount();
            if (mStickHeader != null) {
                for (int i = 0; i < count; i++) {
                    if (mStickHeader.isStickHeader(i)) {
                        mLayoutManager.mHeaderPosition.add(i);
                    }
                }

                if (mLayoutManager.mStickView != null && !mLayoutManager.mHeaderPosition.contains(mStickPosition)) {
                    mLayoutManager.scrapStickyHeader(null);
                }


            }
        }


    }

}
