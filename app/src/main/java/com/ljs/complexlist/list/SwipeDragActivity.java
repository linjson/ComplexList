package com.ljs.complexlist.list;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ljs.complexlist.ItemTouchHelperCallback;
import com.ljs.complexlist.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import itemtouchhelperextension.ItemTouchHelperExtension;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.RefreshPullView;

public class SwipeDragActivity extends AppCompatActivity {

    private RecyclerViewEx mRecyclerView;
    private SwipeDragAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    public ItemTouchHelperCallback2 mCallback2;
    private RefreshPullView swipe;
    private Handler mHandler = new Handler();
    private ItemTouchHelper mItemTouchHelper2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerViewEx) findViewById(R.id.recycler_main);
        swipe = (RefreshPullView) findViewById(R.id.swipe);
//        swipe.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SwipeDragAdapter(this);

        mAdapter.addHeaderView(createTestView("header1"));
        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.addFooterView(createTestView("footer2"));
        mAdapter.addFooterView(createTestView("footer1"));
        mAdapter.setDatas(createTestDatas());
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
//        mAdapter.updateData(createTestDatas());
        mCallback = new ItemTouchHelperCallback();
        mCallback2 = new ItemTouchHelperCallback2();
        mItemTouchHelper = new ItemTouchHelperExtension(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mAdapter.setItemTouchHelper(mItemTouchHelper);


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

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipe.setRefreshing(false);
            }
        });
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


    private TextView createTestView(final String text) {
        TextView a = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        int[] c = getRandColorCode();
        a.setBackgroundColor(c[0]);
        a.setTextColor(c[1]);
        a.setText(text);
        a.setTag(text);
        a.setClickable(true);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SwipeDragActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
        return a;
    }

    private int[] getRandColorCode() {
        int r, g, b;
        Random random = new Random();
        r = random.nextInt(256);
        g = random.nextInt(256);
        b = random.nextInt(256);

        return new int[]{(0xFF << 24) | (r << 16) | (g << 8) | b, (0xFF << 24) | ((256 - r) << 16) | ((256 - g) << 8) | (256 - b)};
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
