package com.ljs.complexlist.fixheader;

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
import com.ljs.complexlist.group.ImmutableClazz;
import com.ljs.complexlist.group.ImmutableSchool;
import com.ljs.complexlist.group.ImmutableStudent;
import com.ljs.complexlist.group.ModifiableClazz;
import com.ljs.complexlist.group.ModifiableSchool;
import com.ljs.complexlist.group.ModifiableStudent;
import com.ljs.complexlist.group.School;
import com.ljs.complexlist.group.Student;

import java.util.Random;

import itemtouchhelperextension.FixedHeaderListView;
import itemtouchhelperextension.ItemTouchHelperExtension;
import itemtouchhelperextension.RecyclerViewEx;
import itemtouchhelperextension.SwipeRefreshLayoutEx;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerViewEx mRecyclerView;
    private GroupRecyclerAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    private SwipeRefreshLayoutEx swipe;
    private School testDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixheader);

        FixedHeaderListView view = (FixedHeaderListView) findViewById(R.id.recycler_main);
        mRecyclerView = view.getRecyclerView();
        swipe = (SwipeRefreshLayoutEx) findViewById(R.id.swipe);
        swipe.setEnabled(false);

        mAdapter = new GroupRecyclerAdapter(mRecyclerView,this,view);

        mAdapter.addHeaderView(createTestView("header1"));
        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.addFooterView(createTestView("footer2"));
        mAdapter.addFooterView(createTestView("footer1"));
        testDatas = createTestDatas();
        mAdapter.setDatas(ImmutableSchool.copyOf(testDatas));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
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


    private School createTestDatas() {

        ImmutableSchool.Builder builder = ImmutableSchool.builder();


        for (int i = 0; i < 3; i++) {
            ImmutableClazz.Builder classBuilder = ImmutableClazz.builder().name("class" + i).index(i).hide(false);


            for (int j = 0; j < (i==0?3:15); j++) {
                Student stu = ImmutableStudent.builder().name("test" + j).age(j).clazz(i).hide(false).build();
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




}
