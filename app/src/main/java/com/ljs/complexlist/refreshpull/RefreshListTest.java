package com.ljs.complexlist.refreshpull;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljs.complexlist.DividerItemDecoration;
import com.ljs.complexlist.R;
import com.ljs.complexlist.fixheader.ImmutableClazz;
import com.ljs.complexlist.fixheader.ImmutableStudent;
import com.ljs.complexlist.fixheader.ModifiableClazz;

import itemtouchhelperextension.BaseAdapter;
import itemtouchhelperextension.BaseViewHolder;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.RefreshPullView;

/**
 * Created by ljs on 2017/1/11.
 */

public class RefreshListTest extends Activity implements RefreshPullView.OnLoadingMoreListener, RefreshPullView.OnRefreshingListener {
    private RecyclerViewEx recycler_main;
    private RefreshPullView rpview;
    private ImmutableClazz data;
    private static Handler sHandler = new Handler();
    private T adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refreshlist);
        initView();
    }

    private void initView() {
        recycler_main = (RecyclerViewEx) findViewById(R.id.recycler_main);
        rpview = (RefreshPullView) findViewById(R.id.rpview);


        LinearLayoutManager layout = new LinearLayoutManager(this);
        recycler_main.addItemDecoration(new DividerItemDecoration(this));
        recycler_main.setLayoutManager(layout);

        data = createStudents();

        adapter = new T(this, data);
        recycler_main.setAdapter(adapter);

        rpview.setOnLoadingMoreListener(this);
        rpview.setOnRefreshingListener(this);

    }

    private ImmutableClazz createStudents() {
        ImmutableClazz.Builder builder = ImmutableClazz.builder();
        builder.name("test").hide(false).name("one").index(0);

        for (int i = 0; i < 20; i++) {
            builder.addStudent(ImmutableStudent.builder().name("list" + i).age(i).clazz(0).hide(false).build());
        }

        return builder.build();
    }

    private ImmutableClazz addStudents() {
        ModifiableClazz clz = ModifiableClazz.create().from(data);

        for (int i = 0; i < 20; i++) {
            clz.addStudent(ImmutableStudent.builder().name("add" + i).age(i).clazz(0).hide(false).build());
        }

        return clz.toImmutable();

    }

    @Override
    public void doLoadingMoreData() {
        data = addStudents();

        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setData(data);
                rpview.setLoadingMore(false);
            }
        }, 2000);
    }

    @Override
    public void doRefreshingData() {
        data = createStudents();
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setData(data);
                rpview.setRefreshing(false);
            }
        }, 1000);
    }


    public static class T extends BaseAdapter<M> {

        private ImmutableClazz clazz;
        private final LayoutInflater layoutInflater;

        public T(Context c, ImmutableClazz clazz) {
            this.clazz = clazz;
            layoutInflater = LayoutInflater.from(c);
        }

        public void setData(ImmutableClazz clazz) {
            this.clazz = clazz;
            notifyDataSetChanged();
        }

        @Override
        public int getChildrenCount() {
            return clazz.student().size();
        }

        @Override
        public M onCreateChildrenViewHolder(ViewGroup parent, int viewType) {
            return new M(layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindChildrenViewHolder(M holder, int position) {
            holder.textView.setText(clazz.student().get(position).name());
        }

        @Override
        public boolean onDataMove(int from, int to) {
            return false;
        }


    }

    public static class M extends BaseViewHolder {

        private final TextView textView;

        public M(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
