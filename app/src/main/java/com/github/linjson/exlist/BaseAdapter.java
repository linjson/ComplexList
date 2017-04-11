package com.github.linjson.exlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ljs on 16/8/31.
 */
public abstract class BaseAdapter<T extends BaseViewHolder> extends RecyclerView.Adapter<BaseViewHolder> implements ItemTouchCallback {


    private static final int HEADERVIEW = 80000000;
    private static final int FOOTERVIEW = 90000000;
    private static final int EMPTYVIEW = 100000000;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private SparseArrayCompat<View> headers = new SparseArrayCompat<>();
    private SparseArrayCompat<View> footers = new SparseArrayCompat<>();
    private boolean noData;

    private boolean mShowEmptyView;

    public BaseAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void addHeaderView(View header) {
        headers.put(HEADERVIEW + getHeaderViewCount(), header);
    }

    public void addFooterView(View footer) {
        footers.put(FOOTERVIEW + getFooterViewCount(), footer);
    }

    private boolean isHeaderView(int position) {
        return position < getHeaderViewCount();
    }

    private boolean isFooterView(int position) {
        return position >= getHeaderViewCount() + getInnerChildrenCount();
    }

    private int getInnerChildrenCount() {
        return noData ? 1 : getChildrenCount();
    }

    public abstract int getChildrenCount();

    public abstract T onCreateChildrenViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindChildrenViewHolder(T holder, int position);

    public int getChildrenViewType(int position) {
        return 0;
    }

    @Override
    public final int getItemViewType(int position) {

        if (isHeaderView(position)) {
            return headers.keyAt(position);
        } else if (isFooterView(position)) {
            return footers.keyAt(position - getInnerChildrenCount() - getHeaderViewCount());
        } else if (noData) {
            return EMPTYVIEW | position;
        } else {
            return getChildrenViewType(position - getHeaderViewCount());
        }
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (headers.get(viewType) != null) {
            return createBaseVH(headers.get(viewType));
        } else if (footers.get(viewType) != null) {
            return createBaseVH(footers.get(viewType));
        } else if ((viewType & EMPTYVIEW) == EMPTYVIEW) {
            return createBaseVH(onCreateEmptyView(parent));
        } else {
            return onCreateChildrenViewHolder(parent, viewType);
        }
    }

    @NonNull
    protected View onCreateEmptyView(ViewGroup parent) {
        return new View(parent.getContext());
    }

    @NonNull
    private BaseViewHolder createBaseVH(View v) {
//        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        BaseViewHolder vh = new BaseViewHolder(v);
        vh.setFixed(true);
        return vh;
    }

    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        if (isHeaderView(position) || isFooterView(position) || noData) {
            return;
        } else {
            onBindChildrenViewHolder((T) holder, position - getHeaderViewCount());
        }

    }

    public final int getHeaderViewCount() {
        return headers.size();
    }

    public final int getFooterViewCount() {
        return footers.size();
    }

    @Override
    public final int getItemCount() {


        int childrenCount = getChildrenCount();

        if (childrenCount == 0) {
            if (mShowEmptyView) {
                noData = true;
                childrenCount = 1;
            }
        } else {
            noData = false;
        }

        return childrenCount +
                getHeaderViewCount() +
                getFooterViewCount();

    }

    @Override
    public void onMove(int listFrom, int listTo) {
        boolean move = onDataMove(listFrom - getHeaderViewCount(), listTo - getHeaderViewCount());

        if (move) {
            notifyItemMoved(listFrom, listTo);
        }


    }

    public abstract boolean onDataMove(int from, int to);

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position, List<Object> payloads) {
        if (isHeaderView(position) || isFooterView(position) || noData) {
            return;
        } else {
            if (payloads.isEmpty()) {
                onBindChildrenViewHolder((T) holder, position - getHeaderViewCount());
            } else {
                onBindChildrenViewHolder((T) holder, position - getHeaderViewCount(), payloads);
            }
        }
    }

    public void onBindChildrenViewHolder(T holder, int position, List<Object> payloads) {
        onBindChildrenViewHolder(holder, position - getHeaderViewCount());
    }

    public void setShowEmptyView(boolean showEmptyView) {
        this.mShowEmptyView = showEmptyView;
    }

    public Context getContext() {
        return mContext;
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }
}
