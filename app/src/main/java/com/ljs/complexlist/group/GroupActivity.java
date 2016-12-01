package com.ljs.complexlist.group;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ljs.complexlist.DividerItemDecoration;
import com.ljs.complexlist.ItemTouchHelperCallback;
import com.ljs.complexlist.R;

import java.util.List;
import java.util.Random;

import itemtouchhelperextension.DiffCallBackEx;
import itemtouchhelperextension.ItemTouchHelperExtension;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.SwipeRefreshLayoutEx;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerViewEx mRecyclerView;
    private GroupRecyclerAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    private SwipeRefreshLayoutEx swipe;
    private School testDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerViewEx) findViewById(R.id.recycler_main);
        swipe = (SwipeRefreshLayoutEx) findViewById(R.id.swipe);
        swipe.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupRecyclerAdapter(mRecyclerView,this);

        mAdapter.addHeaderView(createTestView("header1"));
        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.addFooterView(createTestView("footer2"));
        mAdapter.addFooterView(createTestView("footer1"));
        testDatas = createTestDatas();
        mAdapter.setDatas(ImmutableSchool.copyOf(testDatas));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
//        mAdapter.updateData(createTestDatas());
        mCallback = new ItemTouchHelperCallback();
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
                Toast.makeText(GroupActivity.this, text, Toast.LENGTH_SHORT).show();
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


    private School createTestDatas() {

        ImmutableSchool.Builder builder = ImmutableSchool.builder();


        for (int i = 0; i < 2; i++) {
            ImmutableClazz.Builder classBuilder = ImmutableClazz.builder().name("class" + i).index(i);


            for (int j = 0; j < 5; j++) {
                Student stu = ImmutableStudent.builder().name("test" + j).age(j).clazz(i).build();
                classBuilder.addStudent(stu);
            }

            builder.addClazz(classBuilder.build());
        }
        return builder.build();
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


        //update
        ModifiableSchool m = ModifiableSchool.create().from(testDatas);
        ModifiableClazz c = (ModifiableClazz) m.clazz().get(0);
        c.setName("change");

        ModifiableStudent s = (ModifiableStudent) m.clazz().get(0).student().get(2);
        s.setName("change-stu");

        //delete
        m.clazz().get(0).student().remove(1);

        //add
        m.clazz().get(0).student().add(0, ImmutableStudent.builder().age(0).name("adsd").clazz(0).build());


        testDatas = m.toImmutable();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new Diff(mAdapter, mAdapter.getDatas(), testDatas), false);
        result.dispatchUpdatesTo(mAdapter);
        mAdapter.setDatas(testDatas);


    }


    static class Diff extends DiffCallBackEx {

        private final School news;
        private final School olds;

        public Diff(GroupRecyclerAdapter adapter, School olds, School news) {
            super(adapter);
            this.olds = olds;
            this.news = news;

        }

        @Override
        protected Object getDataChangePayload(int oldItemPosition, int newItemPosition) {

            int[] newPos = getGroupSonPosition(news.clazz(), newItemPosition);

            Bundle bundle = new Bundle();
            if (newPos[1] == -1) {
                bundle.putString("test", news.clazz().get(newPos[0]).name());
            } else {
                bundle.putString("test", news.clazz().get(newPos[0]).student().get(newPos[1]).name());
            }
            return bundle;
        }

        private int getSize(School test) {
            int size = test.clazz().size();
            int t = size;

            for (int i = 0; i < size; i++) {
                t += test.clazz().get(i).student().size();
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

        private int[] getGroupSonPosition(List<Clazz> list, int pos) {

            int groupSize = list.size();
            int[] index = new int[2];
            int p = pos;
            for (int i = 0; i < groupSize; i++) {
                int temp = p - list.get(i).student().size() - 1;
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

            return oldItemPosition==newItemPosition;
        }

        @Override
        public boolean areDataContentsTheSame(int oldItemPosition, int newItemPosition) {

            int[] oldPos = getGroupSonPosition(olds.clazz(), oldItemPosition);
            int[] newPos = getGroupSonPosition(news.clazz(), newItemPosition);
//            String o = "", n = "";
            Object o = null, n = null;
            if (oldPos[1] == -1) {
                o = olds.clazz().get(oldPos[0]);
            } else {
                o = olds.clazz().get(oldPos[0]).student().get(oldPos[1]);
            }
            if (newPos[1] == -1) {
                n = news.clazz().get(newPos[0]);
            } else {
                n = news.clazz().get(newPos[0]).student().get(newPos[1]);
            }

            return o.equals(n);
        }


    }
}
