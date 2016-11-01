package itemtouchhelperextension;

import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ljs on 16/8/31.
 */
public abstract class BaseGroupAdapter<T extends BaseGroupViewHolder> extends BaseAdapter<BaseGroupViewHolder> implements ItemTouchCallback {

    static final int GROUP = -100000000;

    @Override
    public final void onBindChildrenViewHolder(BaseGroupViewHolder holder, int position) {
        if (isGroupView(position)) {
            onBindGroupViewHolder((T) holder, position);
        } else {
            onBindSonViewHolder((T) holder, position);
        }
    }

    protected abstract void onBindSonViewHolder(T holder, int position);

    protected abstract void onBindGroupViewHolder(T holder, int position);


    @Override
    public final T onCreateChildrenViewHolder(ViewGroup parent, int viewType) {
        if (viewType == GROUP) {
            return onCreateGroupViewHolder(parent, viewType);
        }
        return onCreateSonViewHolder(parent, viewType);
    }

    public abstract T onCreateSonViewHolder(ViewGroup parent, int viewType);

    public abstract T onCreateGroupViewHolder(ViewGroup parent, int viewType);


    @Override
    public final int getChildrenViewType(int position) {
        if (isGroupView(position)) {
            return GROUP;
        } else {
            return getSonViewType(position);
        }
    }

    public abstract boolean isGroupView(int position);

    public int getSonViewType(int position) {
        return 0;
    }

    @Override
    public void onBindChildrenViewHolder(BaseGroupViewHolder holder, int position, List<Object> payloads) {
        if (isGroupView(position)) {
            if (payloads.isEmpty()) {
                onBindGroupViewHolder((T) holder, position);
            } else {
                onBindGroupViewHolder((T) holder, position, payloads);
            }
        } else {
            if (payloads.isEmpty()) {
                onBindSonViewHolder((T) holder, position);
            } else {
                onBindSonViewHolder((T) holder, position, payloads);
            }
        }
    }

    public void onBindGroupViewHolder(T holder, int position, List<Object> payloads) {
        onBindGroupViewHolder(holder, position);
    }

    public void onBindSonViewHolder(T holder, int position, List<Object> payloads) {
        onBindSonViewHolder(holder, position);
    }
}
