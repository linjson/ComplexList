package example.refreshpull;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.linjson.exlist.BaseAdapter;
import com.github.linjson.exlist.BaseViewHolder;
import com.github.linjson.exlist.DefaultDividerDecoration;
import com.github.linjson.exlist.RefreshPullView;

import example.R;
import example.fixheader.ImmutableClazz;
import example.fixheader.ImmutableStudent;
import example.fixheader.ModifiableClazz;

/**
 * Created by ljs on 2017/1/11.
 */

public class RefreshListTest extends Activity implements RefreshPullView.OnLoadingMoreListener, RefreshPullView.OnRefreshingListener {
    private RecyclerView recycler_main;
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
        recycler_main = (RecyclerView) findViewById(R.id.recycler_main);
        rpview = (RefreshPullView) findViewById(R.id.rpview);


        LinearLayoutManager layout = new LinearLayoutManager(this);
        recycler_main.addItemDecoration(new DefaultDividerDecoration(this));
        recycler_main.setLayoutManager(layout);

//        data = createStudents();

        adapter = new T(this, data);
        adapter.setShowEmptyView(true);
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
    public void doLoadingMoreData(final RefreshPullView view) {
        data = addStudents();

        sHandler.postDelayed(() -> {
            adapter.setData(data);
            view.setLoadingMore(false);
            Toast.makeText(RefreshListTest.this, "loading结束", Toast.LENGTH_SHORT).show();
        }, 1000);
    }

    @Override
    public void doRefreshingData(final RefreshPullView view) {
        data = createStudents();


        sHandler.postDelayed(() -> {
            adapter.setData(data);
            view.setRefreshing(false);
            Toast.makeText(RefreshListTest.this, "refreshing结束", Toast.LENGTH_SHORT).show();

        }, 1000);
    }


    public static class T extends BaseAdapter<M> {

        private ImmutableClazz clazz;

        public T(Context c, ImmutableClazz clazz) {
            super(c);
            this.clazz = clazz;
        }

        public void setData(ImmutableClazz clazz) {
            this.clazz = clazz;
            notifyDataSetChanged();
        }

        @Override
        public int getChildrenCount() {
            return clazz == null ? 0 : clazz.student().size();
        }

        @Override
        public M onCreateChildrenViewHolder(ViewGroup parent, int viewType) {
            return new M(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindChildrenViewHolder(M holder, int position) {
            holder.textView.setText(clazz.student().get(position).name());
        }

        @Override
        public boolean onDataMove(int from, int to) {
            return false;
        }

        @NonNull
        @Override
        protected View onCreateEmptyView(ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.emptyview, parent, false);
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
