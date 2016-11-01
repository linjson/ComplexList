package com.ljs.complexlist.group;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ljs.complexlist.ItemTouchHelperCallback;
import com.ljs.complexlist.R;
import com.ljs.complexlist.TestModel;
import com.ljs.complexlist.list.ItemTouchHelperCallback2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import itemtouchhelperextension.DiffCallBackEx;
import itemtouchhelperextension.ItemTouchHelperExtension;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.SwipeRefreshLayoutEx;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerViewEx mRecyclerView;
    private MainRecyclerAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    public ItemTouchHelperCallback2 mCallback2;
    private SwipeRefreshLayoutEx swipe;
    private ItemTouchHelper mItemTouchHelper2;
    private ArrayList<TestModel> testDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerViewEx) findViewById(R.id.recycler_main);
        swipe = (SwipeRefreshLayoutEx) findViewById(R.id.swipe);
        swipe.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainRecyclerAdapter(this);

        mAdapter.addHeaderView(createTestView("header1"));
        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.addFooterView(createTestView("footer2"));
        mAdapter.addFooterView(createTestView("footer1"));
        testDatas = createTestDatas();
        mAdapter.setDatas(testDatas);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        mAdapter.updateData(createTestDatas());
        mCallback = new ItemTouchHelperCallback();
        mCallback2 = new ItemTouchHelperCallback2();
        mItemTouchHelper = new ItemTouchHelperExtension(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mAdapter.setItemTouchHelper(mItemTouchHelper);


        findViewById(R.id.btn).setOnClickListener(this);
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
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
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


    private ArrayList<TestModel> createTestDatas() {
        ArrayList<TestModel> result = new ArrayList<>();
        int pid = -1;
        for (int i = 0; i < 20; i++) {
            TestModel testModel;
            if (i == 0 || i == 10) {
                testModel = new TestModel(i, "group" + i, true);
                pid = i;
            } else {
                testModel = new TestModel(i, "item" + i);
            }
            testModel.pid = pid;
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


    @Override
    public void onClick(View view) {

        TestModel testModel = testDatas.remove(0);
        TestModel test = new TestModel(testModel.position, testModel.title, testModel.group);
        test.pid = testModel.pid;
        test.title = "change-group";
        testDatas.add(0, test);


//        testDatas.remove(1);

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new Diff(mAdapter, mAdapter.getDatas(), testDatas), false);
        result.dispatchUpdatesTo(mAdapter);
        mAdapter.setDatas(testDatas);

    }


    static class Diff extends DiffCallBackEx {

        private final List<TestModel> news;
        private final List<TestModel> olds;

        public Diff(MainRecyclerAdapter adapter, List<TestModel> olds, List<TestModel> news) {
            super(adapter);
            this.olds = olds;
            this.news = news;

        }

        @Override
        protected Object getDataChangePayload(int oldItemPosition, int newItemPosition) {
            System.out.printf("==>getDataChangePayload:%s,%s \n",oldItemPosition,newItemPosition);
            Bundle bundle=new Bundle();
            bundle.putString("test",news.get(newItemPosition).title);

            return bundle;
        }

        @Override
        public int getOldDataSize() {
            return olds.size();
        }


        @Override
        public int getNewDataSize() {
            return news.size();
        }

        @Override
        public boolean areDataTheSame(int oldItemPosition, int newItemPosition) {
            return olds.get(oldItemPosition).position == news.get(newItemPosition).position;
        }

        @Override
        public boolean areDataContentsTheSame(int oldItemPosition, int newItemPosition) {
            return olds.get(oldItemPosition).title.equals(news.get(newItemPosition).title);
        }



    }
}
