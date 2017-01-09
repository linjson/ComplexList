package com.ljs.complexlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljs.complexlist.fixheader.FixHeaderActivity;
import com.ljs.complexlist.list.SwipeDragActivity;
import com.ljs.complexlist.refreshpull.Test;

/**
 * Created by ljs on 2016/12/19.
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView mList;

    private final String[] list = new String[]{"fixheader-group","swipe-drag", "refreshpull"};

    private final Class[] clazz = {FixHeaderActivity.class, SwipeDragActivity.class, Test.class};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        initView();


    }

    private void initView() {
        mList = (RecyclerView) findViewById(R.id.list);

        mList.setLayoutManager(new LinearLayoutManager(this));

        mList.addItemDecoration(new DividerItemDecoration(this));

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
            holder.show(list[position], new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(MainActivity.this, clazz[position]);
                    startActivity(it);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.length;
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
}
