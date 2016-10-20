package com.ljs.complexlist;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import itemtouchhelperextension.ItemTouchHelperExtension;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.SwipeRefreshLayoutEx;

public class MainActivity extends AppCompatActivity {

    private RecyclerViewEx mRecyclerView;
    private MainRecyclerAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    public ItemTouchHelperCallback2 mCallback2;
    private SwipeRefreshLayoutEx swipe;
    private Handler mHandler = new Handler();
    private ItemTouchHelper mItemTouchHelper2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerViewEx) findViewById(R.id.recycler_main);
        swipe = (SwipeRefreshLayoutEx) findViewById(R.id.swipe);
        swipe.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainRecyclerAdapter(this);

//        mAdapter.addHeaderView(createTestView("header1"));
//        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.setDatas(createTestDatas());
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
//        mAdapter.updateData(createTestDatas());
        mCallback = new ItemTouchHelperCallback();
        mCallback2 = new ItemTouchHelperCallback2();
        mItemTouchHelper = new ItemTouchHelperExtension(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


//        mItemTouchHelper2=new ItemTouchHelper(mCallback2);
//        mItemTouchHelper2.attachToRecyclerView(mRecyclerView);
//        swipe.setProgressViewEndTarget(true,0);


//        ItemTouchHelper

//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
////                System.out.printf("==>onScrolled,%s \n",dy);
//            }
//        });

//        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                swipe.setRefreshing(false);
//            }
//        });
//
//        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
//
//        for (int i = 0; i < 50; i++) {
//
//
//            TextView textView = new TextView(this);
//            textView.setText("text" + i);
//
//            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//
//        }
    }


    private TextView createTestView(String text) {
        TextView a = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        a.setBackgroundColor(Color.BLUE);
        a.setText(text);
        a.setTag(text);
        return a;
    }

    private List<TestModel> createTestDatas() {
        List<TestModel> result = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            TestModel testModel = new TestModel(i, ":Item Swipe Action Button Container Width");
            if (i == 1) {
                testModel = new TestModel(i, "Item Swipe with Action container width and no spring");
            }
            if (i == 2) {
                testModel = new TestModel(i, "Item Swipe with RecyclerView Width");
            }
            result.add(testModel);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mAdapter.updateData(createTestDatas());
        }
        return super.onOptionsItemSelected(item);
    }
}
