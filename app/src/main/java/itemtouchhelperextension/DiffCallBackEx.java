package itemtouchhelperextension;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

/**
 * Created by ljs on 2016/10/31.
 */

public abstract class DiffCallBackEx extends DiffUtil.Callback {

    private final BaseAdapter mAdapter;

    public DiffCallBackEx(BaseAdapter adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public int getOldListSize() {
        return getOldDataSize() + mAdapter.getHeaderViewCount();
    }


    @Override
    public final int getNewListSize() {
        return getNewDataSize() + mAdapter.getHeaderViewCount();
    }


    private boolean isDataRange(int oldItemPosition, int newItemPosition) {
        if (oldItemPosition < mAdapter.getHeaderViewCount() || newItemPosition < mAdapter.getHeaderViewCount()) {
            return false;
        }
        return true;
    }

    @Override
    public final boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (!isDataRange(oldItemPosition, newItemPosition)) {
            return true;
        }
        return areDataTheSame(oldItemPosition - mAdapter.getHeaderViewCount(), newItemPosition - mAdapter.getHeaderViewCount());
    }


    @Override
    public final boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (!isDataRange(oldItemPosition, newItemPosition)) {
            return true;
        }
        return areDataContentsTheSame(oldItemPosition - mAdapter.getHeaderViewCount(), newItemPosition - mAdapter.getHeaderViewCount());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return getDataChangePayload(oldItemPosition - mAdapter.getHeaderViewCount(), newItemPosition - mAdapter.getHeaderViewCount());
    }

    protected Object getDataChangePayload(int oldItemPosition, int newItemPosition){
        return null;
    }

    protected abstract int getOldDataSize();

    protected abstract int getNewDataSize();

    protected abstract boolean areDataTheSame(int oldItemPosition, int newItemPosition);

    protected abstract boolean areDataContentsTheSame(int oldItemPosition, int newItemPosition);
}
