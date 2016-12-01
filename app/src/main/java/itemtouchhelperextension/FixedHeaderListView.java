package itemtouchhelperextension;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ljs on 2016/11/3.
 */

public class FixedHeaderListView extends FrameLayout {
    private RecyclerViewEx recyclerView;
    private FrameLayout headerLayout;
    private FixedController fixedController;

    public FixedHeaderListView(Context context) {
        super(context);
        init();
    }

    public FixedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FixedHeaderListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        headerLayout = new FrameLayout(getContext());
        headerLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        recyclerView = new RecyclerViewEx(getContext());
        recyclerView.setLayoutParams(generateDefaultLayoutParams());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addView(recyclerView);
        addView(headerLayout);

        fixedController = new FixedController(recyclerView, headerLayout);
        recyclerView.addOnScrollListener(fixedController);


    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 2) {
            throw new RuntimeException("不支持操作");
        }
        super.addView(child, index, params);
    }

    public RecyclerViewEx getRecyclerView() {
        return recyclerView;
    }

    public FrameLayout getHeaderLayout() {
        return headerLayout;
    }

    public void refreshHeaderView(int group){
        fixedController.refreshView(group);
    }
}
