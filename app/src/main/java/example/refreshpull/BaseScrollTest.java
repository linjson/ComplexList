package example.refreshpull;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.linjson.exlist.RefreshPullView;

import example.R;
import example.wrapview.RPViewFooter;
import example.wrapview.RPViewHeader;

/**
 * Created by ljs on 16/9/26.
 */

public class BaseScrollTest extends AppCompatActivity implements RefreshPullView.OnRefreshingListener {

    private boolean refreshing = false;
    private boolean loading = false;
    private Handler mHandler = new Handler();
    private LinearLayout linearLayout;
    private int count;
    private int testNumber = 2;
    private Toolbar toolView;
    private RefreshPullView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scrolltest);

        nv = (RefreshPullView) findViewById(R.id.sv);

//        toolView = (Toolbar) findViewById(toolbar);
//
//        setSupportActionBar(toolView);


        linearLayout = (LinearLayout) findViewById(R.id.ll);

        for (int i = 0; i < 50; i++) {


            TextView textView = getTextView("text" + i);

            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        }


        RPViewHeader head = new RPViewHeader(this);
        nv.setHeaderView(head);

        RPViewFooter footer = new RPViewFooter(this);
        nv.setFooterView(footer);

        findViewById(R.id.up).setOnClickListener(v -> {

            nv.setRefreshing(false);
            refreshing = !refreshing;
        });

        findViewById(R.id.down).setOnClickListener(v -> {

            nv.setLoadingMore(false);
            loading = !loading;
        });


        nv.setOnRefreshingListener(this);

        nv.setOnLoadingMoreListener(view -> mHandler.postDelayed(() -> {

            if (count < testNumber) {
                TextView textView = getTextView("add" + 1);

                linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                count++;

            }

            if (count >= testNumber) {
                view.stopLoadingMore();
            }

            view.setLoadingMore(false);
        }, 2000));

    }


    @NonNull
    private TextView getTextView(String i) {
        TextView textView = new TextView(this);
        textView.setText(i);
        return textView;
    }

    @Override
    public void doRefreshingData(final RefreshPullView view) {
        mHandler.postDelayed(() -> {
            TextView textView = getTextView("refresh" + 1);

            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            view.setRefreshing(false);
            Toast.makeText(BaseScrollTest.this, "refreshing结束", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rp, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_mark) {
            nv.setRPViewController(RefreshPullView.MARK);
        } else {
            nv.setRPViewController(RefreshPullView.SWIPE);
        }
        return super.onOptionsItemSelected(item);
    }

}
