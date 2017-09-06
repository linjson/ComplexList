package example.fixheader;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.linjson.exlist.DefaultDividerDecoration;
import com.github.linjson.exlist.ItemTouchHelperExtension;
import com.github.linjson.exlist.RefreshPullView;
import com.github.linjson.exlist.StickHeaderLayoutManager;

import java.util.Random;

import example.ItemTouchHelperCallback;
import example.R;

public class FixHeaderActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private GroupRecyclerAdapter mAdapter;
    public ItemTouchHelperExtension mItemTouchHelper;
    public ItemTouchHelperExtension.Callback mCallback;
    private School testDatas;
    private RefreshPullView rpview;
    private static Handler sHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixheader);
        rpview = (RefreshPullView) findViewById(R.id.rpview);
        RecyclerView view = (RecyclerView) findViewById(R.id.recycler_main);
        mRecyclerView = view;
        StickHeaderLayoutManager layout = new StickHeaderLayoutManager(this);
        mRecyclerView.setLayoutManager(layout);
        mAdapter = new GroupRecyclerAdapter(mRecyclerView, this);


        rpview.setRPViewController(RefreshPullView.FIX);

        mAdapter.addHeaderView(createTestView("header1"));
        mAdapter.addHeaderView(createTestView("header2"));
        mAdapter.addFooterView(createTestView("footer2"));
        mAdapter.addFooterView(createTestView("footer1"));
        testDatas = createTestDatas();
//        mAdapter.setDatas(ImmutableSchool.copyOf(testDatas));
        mAdapter.setShowEmptyView(true);
        mRecyclerView.setAdapter(mAdapter);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        //item的改变不闪烁
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new DefaultDividerDecoration(this, LinearLayoutManager.VERTICAL));
//        mAdapter.updateData(createTestDatas());
        mCallback = new ItemTouchHelperCallback();
        mItemTouchHelper = new ItemTouchHelperExtension(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mItemTouchHelper.setMoveDiffGroup(true);
        mAdapter.setItemTouchHelper(mItemTouchHelper);


        findViewById(R.id.btn).setOnClickListener(this);

        rpview.setOnRefreshingListener(x -> {
            testDatas = createTestDatas();

            sHandler.postDelayed(() -> {
                mAdapter.updateData(testDatas);
                rpview.setRefreshing(false);
                Toast.makeText(FixHeaderActivity.this, "refreshing结束", Toast.LENGTH_SHORT).show();

            }, 1000);
        });
    }


    private TextView createTestView(final String text) {
        TextView a = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, mRecyclerView, false);
        int[] c = getRandColorCode();
        a.setBackgroundColor(c[0]);
        a.setTextColor(c[1]);
        a.setText(text);
        a.setTag(text);
        a.setClickable(true);
        a.setOnClickListener(v -> Toast.makeText(FixHeaderActivity.this, text, Toast.LENGTH_SHORT).show());
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


            for (int j = 0; j < (i == 0 ? 3 : 15); j++) {
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
//            mAdapter.updateData(createTestDatas());
//            mRecyclerView.getLayoutManager().setPendingScroll(10);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {


        //update
        ModifiableSchool m = ModifiableSchool.create().from(testDatas);
        ModifiableClazz c = (ModifiableClazz) m.clazz().get(0);
//        c.setName("change");
//
//        ModifiableStudent s = (ModifiableStudent) m.clazz().get(0).student().get(2);
//        s.setName("change-stu");

        //delete
//        m.clazz().get(0).student().remove(1);

        //add
        m.clazz().get(0).student().add(0, ImmutableStudent.builder().age(0).name("adsd").clazz(0).hide(false).build());


        testDatas = m.toImmutable();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new Diff(mAdapter, mAdapter.getDatas(), testDatas), false);
        mAdapter.setDatas(testDatas);
        result.dispatchUpdatesTo(mAdapter);

//        mAdapter.notifyDataSetChanged();

    }


}
