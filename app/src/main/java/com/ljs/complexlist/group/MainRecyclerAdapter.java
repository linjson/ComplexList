package com.ljs.complexlist.group;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljs.complexlist.R;

import java.util.List;

import itemtouchhelperextension.BaseGroupAdapter;
import itemtouchhelperextension.BaseGroupViewHolder;
import itemtouchhelperextension.ItemTouchHelperExtension;

import static com.ljs.complexlist.R.id.text_list_main_index;

public class MainRecyclerAdapter extends BaseGroupAdapter<MainRecyclerAdapter.Test> {

    private School mDatas;
    private Context mContext;
    private ItemTouchHelperExtension mItemTouchHelper;

    public MainRecyclerAdapter(Context context) {
        mContext = context;
    }

    public School getDatas() {
        return mDatas;
    }

    public void setDatas(School datas) {
        mDatas = datas;
    }

    public void updateData(School datas) {
        setDatas(datas);
        notifyDataSetChanged();
    }


    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mContext);
    }

    @Override
    protected void onBindSonViewHolder(final Test holder, int groupPos, int sonPos) {
        holder.bind(mDatas.clazz().get(groupPos).student().get(sonPos));
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
        holder.bind(mDatas.clazz().get(position));


    }

    @Override
    public Test onCreateSonViewHolder(ViewGroup parent, int viewType) {
        return new Test(getLayoutInflater().inflate(R.layout.list_item_main, parent, false), false);
    }

    @Override
    public Test onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return new Test(getLayoutInflater().inflate(R.layout.list_item_group, parent, false), true);
    }

    public void setItemTouchHelper(ItemTouchHelperExtension itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }

    @Override
    public boolean onGroupSonDataMove(int fromGroup, int fromSon, int toGroup, int toSon) {

        ModifiableSchool modifiableSchool = ModifiableSchool.create().from(mDatas);

        ModifiableStudent m = (ModifiableStudent) modifiableSchool.clazz().get(fromGroup).student().remove(fromSon);

        m.setClazz(mDatas.clazz().get(toGroup).index());

        if (toSon == -1) {
            modifiableSchool.clazz().get(toGroup).student().add(m);
        } else {
            modifiableSchool.clazz().get(toGroup).student().add(toSon, m);
        }
        setDatas(modifiableSchool.toImmutable());
        return true;
    }

    public static class Test extends BaseGroupViewHolder {
        TextView mTextTitle;
        TextView mTextIndex;
        View mViewContent;
        View mActionContainer;
        Clazz mDataClass;
        Student mDataStudent;

        public Test(View itemView, boolean group) {
            super(itemView, group);

            mTextTitle = (TextView) itemView.findViewById(R.id.text_list_main_title);
            mTextIndex = (TextView) itemView.findViewById(text_list_main_index);
            mViewContent = itemView.findViewById(R.id.view_list_main_content);
            mActionContainer = itemView.findViewById(R.id.view_list_repo_action_container);
        }

        public void bind(Clazz testModel) {
            mDataClass = testModel;
            mTextTitle.setText(testModel.name());
            if (mTextIndex != null) {
                mTextIndex.setText("#" + testModel.index());
            }
        }

        public void bind(Student testModel) {
            mDataStudent = testModel;
            mTextTitle.setText(testModel.name());
            if (mTextIndex != null) {
                mTextIndex.setText("#" + testModel.age());
            }
        }


        @Override
        public int getGroupId() {
            if (mDataClass != null) {
                return mDataClass.index();
            } else {
                return mDataStudent.clazz();
            }
        }
    }


//    @Override
//    public void onDataMove(int from, int to) {
////        Collections.swap(mDatas, from, to);
//    }


//    @Override
//    public void onBindSonViewHolder(Test v, int position, List<Object> payloads) {
//        v.bind(mDatas.get(position));
//        Bundle args = (Bundle) payloads.get(0);
//        v.mTextTitle.setText(args.getString("test"));
//    }

    @Override
    public int getGroupSize() {
        return mDatas == null ? 0 : mDatas.clazz().size();
    }

    @Override
    public int getSonSize(int groupIndex) {
        return mDatas == null ? 0 : mDatas.clazz().get(groupIndex).student().size();
    }

    @Override
    public void onBindGroupViewHolder(Test v, int position, List<Object> payloads) {
        v.bind(mDatas.clazz().get(position));
        Bundle args = (Bundle) payloads.get(0);
        v.mTextTitle.setText(args.getString("test"));
    }

    @Override
    public void onBindSonViewHolder(Test v, int groupPos, int sonPos, List<Object> payloads) {
        v.bind(mDatas.clazz().get(groupPos).student().get(sonPos));
        Bundle args = (Bundle) payloads.get(0);
        v.mTextTitle.setText(args.getString("test"));
    }
}
