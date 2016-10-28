package com.ljs.complexlist.refreshpull;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ljs.complexlist.R;

/**
 * Created by ljs on 16/9/26.
 */

public class Test extends Activity {

    private boolean refreshing = false;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nesttest);

        final RefreshPullView nv = (RefreshPullView) findViewById(R.id.sv);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);

        for (int i = 0; i < 50; i++) {


            TextView textView = new TextView(this);
            textView.setText("text" + i);

            linearLayout.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        }

        findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nv.setRefresh(false);
                refreshing = !refreshing;
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nv.setLoadingMore(false);
                loading = !loading;
            }
        });


    }
}
