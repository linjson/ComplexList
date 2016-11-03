package itemtouchhelperextension;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by ljs on 16/9/13.
 */
public class RecyclerViewEx extends RecyclerView {

    public RecyclerViewEx(Context context) {
        super(context);
        init(context);
    }

    public RecyclerViewEx(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecyclerViewEx(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {


    }

}
