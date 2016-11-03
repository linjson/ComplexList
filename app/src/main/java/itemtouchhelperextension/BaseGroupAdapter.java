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
        int[] ix = getGroupSonPosition(position);
        if (ix[1] == -1) {
            onBindGroupViewHolder((T) holder, ix[0]);
        } else {
            onBindSonViewHolder((T) holder, ix[0], ix[1]);
        }
    }

    @Override
    public final int getChildrenCount() {
        int size = getGroupSize();
        int t = size;
        for (int i = 0; i < size; i++) {
            t += getSonSize(i);
        }
        return t;
    }

    @Override
    public final T onCreateChildrenViewHolder(ViewGroup parent, int viewType) {
        if ((viewType & GROUP) == GROUP) {
            return onCreateGroupViewHolder(parent, viewType);
        }
        return onCreateSonViewHolder(parent, viewType);
    }


    @Override
    public final int getChildrenViewType(int position) {

        int[] ix = getGroupSonPosition(position);

        if (ix[1] == -1) {
            return getGroupViewType(ix[0]) | GROUP;
        } else {
            return getSonViewType(ix[0], ix[1]);
        }
    }

    public int getGroupViewType(int position) {
        return 0;
    }

    public int getSonViewType(int groupPos, int sonPos) {
        return 0;
    }

    @Override
    public void onBindChildrenViewHolder(BaseGroupViewHolder holder, int position, List<Object> payloads) {
        int[] ix = getGroupSonPosition(position);
        if (ix[1] == -1) {
            if (payloads.isEmpty()) {
                onBindGroupViewHolder((T) holder, ix[0]);
            } else {
                onBindGroupViewHolder((T) holder, ix[0], payloads);
            }
        } else {
            if (payloads.isEmpty()) {
                onBindSonViewHolder((T) holder, ix[0], ix[1]);
            } else {
                onBindSonViewHolder((T) holder, ix[0], ix[1], payloads);
            }
        }
    }

    public void onBindGroupViewHolder(T holder, int position, List<Object> payloads) {
        onBindGroupViewHolder(holder, position);
    }

    public void onBindSonViewHolder(T holder, int groupPos, int sonPos, List<Object> payloads) {
        onBindSonViewHolder(holder, groupPos, sonPos);
    }

    public int[] getGroupSonPosition(int pos) {

        int groupSize = getGroupSize();
        int[] index = {-1, -1};
        int p = pos;
        for (int i = 0; i < groupSize; i++) {
            int temp = p - getSonSize(i) - 1;
            if (temp < 0) {
                index[0] = i;
                index[1] = p - 1;
                return index;
            } else {
                p = temp;
            }
        }
        return index;
    }

    @Override
    public final boolean onDataMove(int from, int to) {
        int[] src = getGroupSonPosition(from);
        int[] desc = getGroupSonPosition(to);


        if (src[0] == desc[0] && desc[1] == -1) {
            desc[0] -= 1;
            if (desc[0] < 0) {
                return false;
            }
            desc[1] = getSonSize(desc[0]);
        }
        return onGroupSonDataMove(src[0], src[1], desc[0], desc[1]);

    }

    public boolean onGroupSonDataMove(int fromGroup, int fromSon, int toGroup, int toSon) {
        return true;
    }

    protected abstract void onBindSonViewHolder(T holder, int groupPos, int sonPos);

    protected abstract void onBindGroupViewHolder(T holder, int position);

    public abstract T onCreateSonViewHolder(ViewGroup parent, int viewType);

    public abstract T onCreateGroupViewHolder(ViewGroup parent, int viewType);

    public abstract int getGroupSize();

    public abstract int getSonSize(int groupIndex);

}
