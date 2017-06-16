package example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.linjson.exlist.DefaultDividerDecoration;

import java.util.ArrayList;

import example.emptyview.EmptyViewTest;
import example.fixheader.FixHeaderActivity;
import example.list.SwipeDragActivity;
import example.refreshpull.BaseNestScrollTest;
import example.refreshpull.BaseScrollTest;
import example.refreshpull.CoorRefreshListTest;
import example.refreshpull.RefreshListTest;

/**
 * Created by ljs on 2016/12/19.
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView mList;


    private ArrayList<Data> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        list.add(new Data("refreshpull-base", BaseScrollTest.class));
        list.add(new Data("refreshpull-base-nest", BaseNestScrollTest.class));
        list.add(new Data("refreshpull-list", RefreshListTest.class));
        list.add(new Data("refreshpull-CoordinatorLayout", CoorRefreshListTest.class));
        list.add(new Data("swipe-drag", SwipeDragActivity.class));
        list.add(new Data("fixheader-group", FixHeaderActivity.class));
        list.add(new Data("emptyview", EmptyViewTest.class));

        initView();


    }

    private void initView() {
        mList = (RecyclerView) findViewById(R.id.list);

        mList.setLayoutManager(new LinearLayoutManager(this));

        mList.addItemDecoration(new DefaultDividerDecoration(this));

        mList.setAdapter(new TestAdapter());
    }


    private class TestAdapter extends RecyclerView.Adapter<TestViewHolder> {
        @Override
        public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TestViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, final int position) {
            final Data data = list.get(position);
            holder.show(data.desc, v -> {
                Intent it = new Intent(MainActivity.this, data.cls);
                startActivity(it);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class TestViewHolder extends RecyclerView.ViewHolder {
        private final TextView v;

        public TestViewHolder(View itemView) {
            super(itemView);
            v = (TextView) itemView;
            v.setClickable(true);

        }

        public void show(String t, View.OnClickListener listen) {
            v.setText(t);
            v.setOnClickListener(listen);
        }
    }

    public static class Data {
        String desc;
        Class cls;

        public Data(String desc, Class cls) {
            this.desc = desc;
            this.cls = cls;
        }
    }
}
