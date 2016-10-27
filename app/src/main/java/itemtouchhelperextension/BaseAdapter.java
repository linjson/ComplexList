package itemtouchhelperextension;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ljs on 16/8/31.
 */
public abstract class BaseAdapter<T extends BaseViewHolder> extends RecyclerView.Adapter<BaseViewHolder> {


    private static final int HEADERVIEW = 8000000;
    private static final int FOOTERVIEW = 9000000;
    private SparseArrayCompat<View> headers = new SparseArrayCompat<>();
    private SparseArrayCompat<View> footers = new SparseArrayCompat<>();

    public void addHeaderView(View header) {
        headers.put(HEADERVIEW + headers.size(), header);
    }

    public void addFooterView(View footer) {
        footers.put(FOOTERVIEW + footers.size(), footer);
    }

    private boolean isHeaderView(int position) {
        return position < headers.size();
    }

    private boolean isFooterView(int position) {
        return position >= headers.size() + getChildrenCount();
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
            return footers.keyAt(position - getChildrenCount() - headers.size());
        } else {
            return getChildrenViewType(position - headers.size());
        }
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (headers.get(viewType) != null) {
            View v = headers.get(viewType);
            setWrapViewLayoutParams(v);
            BaseViewHolder vh = new BaseViewHolder(v);
            vh.setFixed(true);
            return vh;
        } else if (footers.get(viewType) != null) {
            View v = footers.get(viewType);
            setWrapViewLayoutParams(v);
            BaseViewHolder vh = new BaseViewHolder(v);
            vh.setFixed(true);
            return vh;

        } else {
            return onCreateChildrenViewHolder(parent, viewType);
        }
    }

    private void setWrapViewLayoutParams(View v) {
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        if (isHeaderView(position)) {
            return;
        } else if (isFooterView(position)) {
            return;
        } else {
            onBindChildrenViewHolder((T) holder, position - headers.size());
        }

    }


    @Override
    public final int getItemCount() {
        return getChildrenCount() + headers.size() + footers.size();
    }
}
