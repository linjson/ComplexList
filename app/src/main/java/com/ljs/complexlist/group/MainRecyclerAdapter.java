package com.ljs.complexlist.group;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljs.complexlist.R;
import com.ljs.complexlist.TestModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import itemtouchhelperextension.BaseGroupAdapter;
import itemtouchhelperextension.BaseGroupViewHolder;
import itemtouchhelperextension.ItemTouchHelperExtension;

import static com.ljs.complexlist.R.id.text_list_main_index;

public class MainRecyclerAdapter extends BaseGroupAdapter<MainRecyclerAdapter.Test> {

    private List<TestModel> mDatas;
    private Context mContext;
    private ItemTouchHelperExtension mItemTouchHelper;

    public MainRecyclerAdapter(Context context) {
        mDatas = new ArrayList<>();
        mContext = context;
    }


    public List<TestModel> getDatas() {
        return mDatas;
    }

    public void setDatas(List<TestModel> datas) {
        mDatas = new ArrayList<>(datas);
    }

    public void updateData(List<TestModel> datas) {
        setDatas(datas);
        notifyDataSetChanged();
    }

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mContext);
    }


    private void doDelete(int adapterPosition) {
        mDatas.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }

    @Override
    public int getChildrenCount() {
        return mDatas.size();
    }

    @Override
    protected void onBindSonViewHolder(final Test holder, int position) {
        holder.bind(mDatas.get(position));
        holder.mTextIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mItemTouchHelper.startDrag(holder);
                return false;
            }
        });
    }

    @Override
    protected void onBindGroupViewHolder(Test holder, int position) {
        holder.bind(mDatas.get(position));


    }

    @Override
    public Test onCreateSonViewHolder(ViewGroup parent, int viewType) {
        return new Test(getLayoutInflater().inflate(R.layout.list_item_main, parent, false), false);
    }

    @Override
    public Test onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return new Test(getLayoutInflater().inflate(R.layout.list_item_group, parent, false), true);
    }

    @Override
    public boolean isGroupView(int position) {
        return mDatas.get(position).group;
    }

    public void setItemTouchHelper(ItemTouchHelperExtension itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }


    public static class Test extends BaseGroupViewHolder {
        TextView mTextTitle;
        TextView mTextIndex;
        View mViewContent;
        View mActionContainer;
        TestModel mData;

        public Test(View itemView, boolean group) {
            super(itemView, group);

            mTextTitle = (TextView) itemView.findViewById(R.id.text_list_main_title);
            mTextIndex = (TextView) itemView.findViewById(text_list_main_index);
            mViewContent = itemView.findViewById(R.id.view_list_main_content);
            mActionContainer = itemView.findViewById(R.id.view_list_repo_action_container);
        }

        public void bind(TestModel testModel) {
            mData = testModel;
            mTextTitle.setText(testModel.title);
            if (mTextIndex != null) {
                mTextIndex.setText("#" + testModel.position);
            }
        }

        @Override
        public int getGroupId() {
            return mData.pid;
        }
    }


    @Override
    public void onDataMove(int from, int to) {
        Collections.swap(mDatas, from, to);
    }


    @Override
    public void onBindSonViewHolder(Test v, int position, List<Object> payloads) {
        v.bind(mDatas.get(position));
        Bundle args = (Bundle) payloads.get(0);
        v.mTextTitle.setText(args.getString("test"));
    }

    @Override
    public void onBindGroupViewHolder(Test v, int position, List<Object> payloads) {
        v.bind(mDatas.get(position));
        Bundle args = (Bundle) payloads.get(0);
        v.mTextTitle.setText(args.getString("test"));
    }
}
