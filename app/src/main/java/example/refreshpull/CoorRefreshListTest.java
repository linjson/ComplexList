package example.refreshpull;

import example.R;

/**
 * Created by ljs on 2017/1/11.
 */

public class CoorRefreshListTest extends RefreshListTest {

    protected void onCreateAfter() {
        setContentView(R.layout.coor_refreshlist);
        initView();
    }


}
