package com.github.linjson.old;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.github.linjson.exlist.BaseGroupAdapter;
import com.github.linjson.exlist.BaseGroupViewHolder;

/**
 * Created by ljs on 2016/11/3.
 */

public class FixedController extends RecyclerView.OnScrollListener {


    private final LinearLayoutManager layoutManger;
    private final RecyclerView mView;
    private final FrameLayout header;
    private int currentGroupIndex = -1;

    public FixedController(RecyclerView view, FrameLayout header) {
        this.mView = view;
        this.header = header;
        layoutManger = (LinearLayoutManager) view.getLayoutManager();
    }

    private BaseGroupAdapter getAdapter() {
        return (BaseGroupAdapter) mView.getAdapter();
    }

    private int findFirstVisibleItemPosition() {
        return layoutManger.findFirstVisibleItemPosition() - getAdapter().getHeaderViewCount();
    }

    private int findNextVisibleItemPosition() {
        int next = layoutManger.findFirstCompletelyVisibleItemPosition() - getAdapter().getHeaderViewCount();

        while (next < getAdapter().getChildrenCount()) {
            RecyclerView.ViewHolder viewHolder = mView.findViewHolderForAdapterPosition(next + getAdapter().getHeaderViewCount());

            if (viewHolder != null && viewHolder.itemView.getHeight() > 0) {
                return next;
            }
            next++;
        }
        return next;
    }

    private void clearHeaderView() {
        if (header.getChildCount() != 0) {
            header.removeAllViews();
        }

    }

    private void addHeaderView(final View view, boolean isUpper) {
        clearHeaderView();
        measureViewSize(view);
        int height = -view.getMeasuredHeight();
        header.setTranslationY(isUpper ? height : 0);
        header.addView(view);
        measureViewSize(header);


    }

    private void measureViewSize(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(mView.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (getAdapter() == null) {
            throw new RuntimeException("Error -> adapter must be BaseGroupAdapter");
        }
        int a = findFirstVisibleItemPosition();
        int b = findNextVisibleItemPosition();
        if (a >= 0) {
            int[] pos = getAdapter().getGroupSonPosition(a);
            int[] next = getAdapter().getGroupSonPosition(b);

            if (pos[0] != currentGroupIndex) {
                boolean isUpper = pos[0] < currentGroupIndex && recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE;
                currentGroupIndex = pos[0];
                BaseGroupViewHolder holer = getAdapter().onCreateGroupViewHolder(mView, getAdapter().getGroupViewType(currentGroupIndex));
                getAdapter().onBindGroupViewHolder(holer, currentGroupIndex);
                addHeaderView(holer.itemView, isUpper);

            } else if (pos[0] != next[0] && pos[0] == currentGroupIndex) {
                RecyclerView.ViewHolder holder = mView.findViewHolderForAdapterPosition(b + getAdapter().getHeaderViewCount());

                int nextViewtop = holder.itemView.getTop();
                if (nextViewtop <= header.getHeight()) {
                    header.setTranslationY(nextViewtop - header.getHeight());
                } else {
                    header.setTranslationY(0);
                }

            } else {
                header.setTranslationY(0);
            }
        } else {
            currentGroupIndex = -1;
            clearHeaderView();
        }
    }

    public void refreshView(int group) {
        int viewid = getAdapter().getGroupIndexToViewIndex(group);
        if (currentGroupIndex == -1) {
            RecyclerView.ViewHolder oldViewHolder = mView.findViewHolderForAdapterPosition(viewid);
            if (oldViewHolder != null) {
                getAdapter().onBindGroupViewHolder((BaseGroupViewHolder) oldViewHolder, group);
            } else {
                getAdapter().notifyItemChanged(viewid);
            }
            return;
        }

        RecyclerView.ViewHolder oldViewHolder = mView.findViewHolderForAdapterPosition(viewid);
        if (oldViewHolder == null) {
            getAdapter().notifyItemChanged(viewid);
        }
        BaseGroupViewHolder holer = getAdapter().onCreateGroupViewHolder(mView, getAdapter().getGroupViewType(group));
        getAdapter().onBindGroupViewHolder(holer, group);
        clearHeaderView();
        header.addView(holer.itemView);
    }


}
