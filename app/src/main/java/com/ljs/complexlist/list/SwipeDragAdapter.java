package com.ljs.complexlist.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ljs.complexlist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import itemtouchhelperextension.BaseAdapter;
import itemtouchhelperextension.BaseViewHolder;
import itemtouchhelperextension.ItemTouchHelperExtension;

import static com.ljs.complexlist.R.id.text_list_main_index;

public class SwipeDragAdapter extends BaseAdapter<BaseViewHolder> {

    public static final int ITEM_TYPE_RECYCLER_WIDTH = 1000;
    public static final int ITEM_TYPE_ACTION_WIDTH = 1001;
    public static final int ITEM_TYPE_ACTION_WIDTH_NO_SPRING = 1002;
    private List<TestModel> mDatas;
    private Context mContext;
    private ItemTouchHelperExtension mItemTouchHelper;

    public SwipeDragAdapter(Context context) {
        mDatas = new ArrayList<>();
        mContext = context;
    }

    public void setDatas(List<TestModel> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
    }

    public void updateData(List<TestModel> datas) {
        setDatas(datas);
        notifyDataSetChanged();
    }

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mContext);
    }


    private void doDelete(int adapterPosition) {
        mDatas.remove(adapterPosition - getHeaderViewCount());
        notifyItemRemoved(adapterPosition);
    }

    @Override
    public int getChildrenCount() {
        return mDatas.size();
    }


    @Override
    public BaseViewHolder onCreateChildrenViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.list_item_main, parent, false);
        if (viewType == ITEM_TYPE_ACTION_WIDTH) return new ItemSwipeWithActionWidthViewHolder(view);
        if (viewType == ITEM_TYPE_RECYCLER_WIDTH) {
            view = getLayoutInflater().inflate(R.layout.list_item_with_single_delete, parent, false);
            return new ItemViewHolderWithRecyclerWidth(view);
        }
        return new ItemSwipeWithActionWidthNoSpringViewHolder(view);
    }


    @Override
    public void onBindChildrenViewHolder(final BaseViewHolder holder, int position) {
        ItemBaseViewHolder baseViewHolder = (ItemBaseViewHolder) holder;
        baseViewHolder.bind(mDatas.get(position));
        baseViewHolder.mTextIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mItemTouchHelper.startDrag(holder);
                return false;
            }
        });
        baseViewHolder.mViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Item Content click: #" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });
        if (holder instanceof ItemViewHolderWithRecyclerWidth) {
            ItemViewHolderWithRecyclerWidth viewHolder = (ItemViewHolderWithRecyclerWidth) holder;
            viewHolder.mActionViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doDelete(holder.getAdapterPosition());
                }
            });
        } else if (holder instanceof ItemSwipeWithActionWidthViewHolder) {
            ItemSwipeWithActionWidthViewHolder viewHolder = (ItemSwipeWithActionWidthViewHolder) holder;
            viewHolder.mActionViewRefresh.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(mContext, "Refresh Click" + holder.getAdapterPosition()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }

            );
            viewHolder.mActionViewDelete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            doDelete(holder.getAdapterPosition());
                        }
                    }
            );
        }
    }

    @Override
    public int getChildrenViewType(int position) {
        if (mDatas.get(position).position == 1) {
            return ITEM_TYPE_ACTION_WIDTH_NO_SPRING;
        }
        if (mDatas.get(position).position == 2) {
            return ITEM_TYPE_RECYCLER_WIDTH;
        }
        return ITEM_TYPE_ACTION_WIDTH;
    }

    public void setItemTouchHelper(ItemTouchHelperExtension itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }


    class ItemBaseViewHolder extends BaseViewHolder {
        TextView mTextTitle;
        TextView mTextIndex;
        View mViewContent;
        View mActionContainer;

        public ItemBaseViewHolder(View itemView) {
            super(itemView);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_list_main_title);
            mTextIndex = (TextView) itemView.findViewById(text_list_main_index);
            mTextIndex.setClickable(true);
            mViewContent = itemView.findViewById(R.id.view_list_main_content);
            mActionContainer = itemView.findViewById(R.id.view_list_repo_action_container);
        }

        public void bind(TestModel testModel) {
            mTextTitle.setText(testModel.title);
            mTextIndex.setText("#" + testModel.position);
        }

        @Override
        public float getActionWidth() {
            return mActionContainer.getWidth();
        }

        @Override
        public View getFrontView() {
            return mViewContent;
        }
    }


    class ItemViewHolderWithRecyclerWidth extends ItemBaseViewHolder {

        View mActionViewDelete;

        public ItemViewHolderWithRecyclerWidth(View itemView) {
            super(itemView);
            mActionViewDelete = itemView.findViewById(R.id.view_list_repo_action_delete);
        }

        @Override
        public float getActionWidth() {
            return mActionContainer.getWidth();
        }
    }

    class ItemSwipeWithActionWidthViewHolder extends ItemBaseViewHolder {

        View mActionViewDelete;
        View mActionViewRefresh;

        public ItemSwipeWithActionWidthViewHolder(View itemView) {
            super(itemView);
            mActionViewDelete = itemView.findViewById(R.id.view_list_repo_action_delete);
            mActionViewRefresh = itemView.findViewById(R.id.view_list_repo_action_update);
        }

        @Override
        public float getActionWidth() {
            return mActionContainer.getWidth();
        }

        @Override
        public int getSwipeDirection() {
            return ItemTouchHelperExtension.LEFT;
        }
    }

    class ItemSwipeWithActionWidthNoSpringViewHolder extends ItemSwipeWithActionWidthViewHolder {

        public ItemSwipeWithActionWidthNoSpringViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public float getActionWidth() {
            return mActionContainer.getWidth();
//            return 0;
        }

    }


    @Override
    public boolean onDataMove(int from, int to) {
        Collections.swap(mDatas, from, to);
        return true;
    }
}
