package itemtouchhelperextension;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ljs on 16/8/31.
 */
public abstract class BaseGroupAdapter<T extends BaseGroupViewHolder> extends BaseAdapter<BaseGroupViewHolder> implements ItemTouchCallback {

    static final int GROUP = -100000000;
    private final RecyclerView view;

    public BaseGroupAdapter(RecyclerView view) {
        this.view = view;
    }

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
        System.out.printf("==>onbind:%s \n", position);
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

//                if (index[1] == -1) {
//                    index[0] =Math.max(index[0]-1,0);
//                    index[1] = getSonSize(index[0]);
//                }

                return index;
            } else {
                p = temp;
            }
        }
        return index;
    }

    public int getGroupIndexToDataIndex(int groupIndex) {
        int g = 0;
        for (int i = 0; i < groupIndex; i++) {
            g += getSonSize(i) + 1;
        }
        return g;
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


        boolean move = onGroupSonDataMove(from, to, src[0], src[1], desc[0], desc[1]);

//        View v=view.getLayoutManager().findViewByPosition(from+getHeaderViewCount());


//        System.out.printf("==>%s \n", v==null);
//        onBindGroupViewHolder((T) descViewHolder, desc[0]);


        return move;

    }

    @Override
    public void onMove(int listFrom, int listTo) {
        super.onMove(listFrom, listTo);

        int[] src = getGroupSonPosition(listFrom - getHeaderViewCount());
        int[] desc = getGroupSonPosition(listTo - getHeaderViewCount());
//        System.out.printf("==>%s,%s,%s,%s,%s,%s \n", listFrom - getHeaderViewCount(), listTo - getHeaderViewCount(), src[0], src[1], desc[0], desc[1]);
        RecyclerView.ViewHolder src_vh = view.findViewHolderForAdapterPosition(getGroupIndexToDataIndex(src[0]) + getHeaderViewCount());
        if (src_vh != null) {
            onBindGroupViewHolder((T) src_vh, src[0]);
        }

        if (src[1] == -1) {
            if (listFrom > listTo) {
                onGroupChange(desc[0]);
            } else if (listFrom < listTo) {
                onGroupChange(src[0] - 1);
            }
        }


    }

    public void onGroupChange(int group) {

    }

    public boolean onGroupSonDataMove(int from, int to, int fromGroup, int fromSon, int toGroup, int toSon) {
        return true;
    }

    protected abstract void onBindSonViewHolder(T holder, int groupPos, int sonPos);

    protected abstract void onBindGroupViewHolder(T holder, int position);

    public abstract T onCreateSonViewHolder(ViewGroup parent, int viewType);

    public abstract T onCreateGroupViewHolder(ViewGroup parent, int viewType);

    public abstract int getGroupSize();

    public abstract int getSonSize(int groupIndex);

}
