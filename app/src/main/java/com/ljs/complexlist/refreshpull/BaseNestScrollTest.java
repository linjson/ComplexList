package com.ljs.complexlist.refreshpull;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ljs.complexlist.R;

import itemtouchhelperextension.RefreshPullView;

/**
 * Created by ljs on 16/9/26.
 */

public class BaseNestScrollTest extends Activity implements RefreshPullView.OnRefreshingListener {

    private boolean refreshing = false;
    private boolean loading = false;
    private Handler mHandler = new Handler();
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nesttest);

        final RefreshPullView nv = (RefreshPullView) findViewById(R.id.sv);

        linearLayout = (LinearLayout) findViewById(R.id.ll);

        for (int i = 0; i < 50; i++) {


            TextView textView = getTextView("text" + i);

            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        }


//        TextView textView = new TextView(this);
//        textView.setText("header");
//        nv.setHeaderView(textView);
//
//        TextView textView2 = new TextView(this);
//        textView2.setText("footer");
//        nv.setFooterView(textView2);

        findViewById(R.id.up).setOnClickListener(v -> {

            nv.setRefreshing(false);
            refreshing = !refreshing;
        });

        findViewById(R.id.down).setOnClickListener(v -> {

            nv.setLoadingMore(false);
            loading = !loading;
        });


        nv.setOnRefreshingListener(this);

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
            TextView textView = getTextView("add" + 1);

            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            view.setRefreshing(false);
            Toast.makeText(BaseNestScrollTest.this, "refreshing结束", Toast.LENGTH_SHORT).show();
        }, 2000);
    }
}
