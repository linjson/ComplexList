package example.fixheader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.linjson.exlist.BaseGroupAdapter;
import com.github.linjson.exlist.BaseGroupViewHolder;
import com.github.linjson.exlist.FixedHeaderListView;
import com.github.linjson.exlist.ItemTouchHelperExtension;

import java.util.List;

import example.R;


public class GroupRecyclerAdapter extends BaseGroupAdapter<GroupRecyclerAdapter.Test> {

    private final FixedHeaderListView fixedHeaderListView;
    private School mDatas;
    private Context mContext;
    private ItemTouchHelperExtension mItemTouchHelper;
    private boolean h;

    public GroupRecyclerAdapter(RecyclerView view, Context context, FixedHeaderListView fixedHeaderListView) {
        super(view);
        mContext = context;
        this.fixedHeaderListView = fixedHeaderListView;
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
        holder.mTextIndex.setOnTouchListener((v, event) -> {
            mItemTouchHelper.startDrag(holder);
            return false;
        });
    }

    @Override
    protected void onBindGroupViewHolder(final Test holder, final int position) {
        holder.bind(mDatas.clazz().get(position));
        holder.mTextTitle.setClickable(true);
        holder.mTextTitle.setOnClickListener(v -> {
//                Bundle bundle = new Bundle();
//                h=!h;
//                bundle.putBoolean("test",h);
//                notifyItemChanged(4,bundle);


            ModifiableSchool modifiableSchool = ModifiableSchool.create().from(mDatas);
            ModifiableClazz clazz = (ModifiableClazz) modifiableSchool.clazz().get(position);

            clazz.setHide(!clazz.hide());


            List<Student> stu = clazz.student();

            for (int i = 0; i < stu.size(); i++) {
                ModifiableStudent s = (ModifiableStudent) stu.get(i);
                s.setHide(clazz.hide());
            }

            ImmutableSchool news = modifiableSchool.toImmutable();
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new Diff(GroupRecyclerAdapter.this, mDatas, news), false);
            result.dispatchUpdatesTo(GroupRecyclerAdapter.this);
            setDatas(news);

            fixedHeaderListView.getRecyclerView().scrollToPosition(getGroupIndexToViewIndex(position));

            Toast.makeText(v.getContext(), "group" + position, Toast.LENGTH_SHORT).show();
        });

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
    public boolean onGroupSonDataMove(int from, int to, int fromGroup, int fromSon, int toGroup, int toSon) {

        ModifiableSchool modifiableSchool = ModifiableSchool.create().from(mDatas);
        ModifiableStudent m = (ModifiableStudent) modifiableSchool.clazz().get(fromGroup).student().remove(fromSon);

        m.setClazz(mDatas.clazz().get(toGroup).index());

        if (toSon == -1) {
            modifiableSchool.clazz().get(toGroup).student().add(m);
        } else {
            modifiableSchool.clazz().get(toGroup).student().add(toSon, m);
        }
        setDatas(modifiableSchool.toImmutable());

//        notifyItemChanged(to + getHeaderViewCount());
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
            mTextIndex = (TextView) itemView.findViewById(R.id.text_list_main_index);
            mViewContent = itemView.findViewById(R.id.view_list_main_content);
            mActionContainer = itemView.findViewById(R.id.view_list_repo_action_container);
        }

        public void bind(Clazz testModel) {
            mDataClass = testModel;
            mTextTitle.setText(testModel.name() + String.format("(%s)", testModel.student().size()));
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
            hideItemView(testModel.hide());
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
//        v.mTextTitle.setText(args.getString("test"));
        boolean h = args.getBoolean("test");
        v.hideItemView(h);
    }

    @Override
    public void onGroupChange(int toGroup) {

        fixedHeaderListView.refreshHeaderView(toGroup);
    }

    @NonNull
    @Override
    protected View onCreateEmptyView(ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.emptyview, parent, false);
    }
}
