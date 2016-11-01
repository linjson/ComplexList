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

//        mAdapter.addHeaderView(createTestView("header1"));
//        mAdapter.addHeaderView(createTestView("header2"));
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
        mItemTouchHelper.setMoveDiffGroup(true);
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
        for (int i = 0; i < 2; i++) {
            TestModel testModel = new TestModel(i, "group" + i);


            for (int j = 0; j < 3; j++) {
                TestModel e = new TestModel(j, "item" + j);
                e.pid = testModel.position;
                testModel.list.add(e);
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


    @Override
    public void onClick(View view) {


//        testDatas.remove(0);


        TestModel p = testDatas.get(0).clone();

        testDatas.remove(0);
        testDatas.add(0, p);

        TestModel m = testDatas.get(0).list.remove(1);
        TestModel n = new TestModel(m.position, m.title);
        n.title = "change";
        n.pid = m.pid;
        testDatas.get(0).list.add(1, n);

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
//            System.out.printf("==>getDataChangePayload:%s,%s \n", oldItemPosition, newItemPosition);
//            Bundle bundle = new Bundle();
//            bundle.putString("test", news.get(newItemPosition).title);

            return null;
        }

        private int getSize(List<TestModel> test) {
            int size = test.size();
            int t = size;

            for (int i = 0; i < size; i++) {
                t += test.get(i).list.size();
            }

            return t;
        }

        @Override
        public int getOldDataSize() {
            return getSize(olds);
        }


        @Override
        public int getNewDataSize() {
            return getSize(news);
        }

        private int[] getGroupSonPosition(List<TestModel> list, int pos) {

            int groupSize = list.size();
            int[] index = new int[2];
            int p = pos;
            for (int i = 0; i < groupSize; i++) {
                int temp = p - list.get(i).list.size() - 1;
                if (temp < 0) {
                    index[0] = i;
                    index[1] = p - 1;
                    return index;
                } else {
                    p = temp;
                }
            }
            return index;
        }

        @Override
        public boolean areDataTheSame(int oldItemPosition, int newItemPosition) {

            int[] oldPos = getGroupSonPosition(olds, oldItemPosition);
            int[] newPos = getGroupSonPosition(news, newItemPosition);

            String o = "", n = "";

            if (oldPos[1] == -1) {
                o = "p" + olds.get(oldPos[0]).position;
            } else {
                o = "s" + olds.get(oldPos[0]).list.get(oldPos[1]).position;
            }
            if (newPos[1] == -1) {
                n = "p" + news.get(newPos[0]).position;
            } else {
                n = "s" + news.get(newPos[0]).list.get(newPos[1]).position;
            }
            return o.equals(n);
        }

        @Override
        public boolean areDataContentsTheSame(int oldItemPosition, int newItemPosition) {

            int[] oldPos = getGroupSonPosition(olds, oldItemPosition);
            int[] newPos = getGroupSonPosition(news, newItemPosition);
            String o = "", n = "";

            if (oldPos[1] == -1) {
                o = "p" + olds.get(oldPos[0]).title;
            } else {
                o = "s" + olds.get(oldPos[0]).list.get(oldPos[1]).title;
            }
            if (newPos[1] == -1) {
                n = "p" + news.get(newPos[0]).title;
            } else {
                n = "s" + news.get(newPos[0]).list.get(newPos[1]).title;
            }
            return o.equals(n);
        }


    }
}
