package itemtouchhelperextension;

import android.view.View;

/**
 * Created by ljs on 2016/10/28.
 */

public class BaseGroupViewHolder extends BaseViewHolder {

    private final boolean group;
    private final int old;

    public BaseGroupViewHolder(View itemView, boolean group) {
        super(itemView);
        this.group = group;
        old=itemView.getLayoutParams().height;
    }

    public int getGroupId() {
        return -1;
    }

    @Override
    public boolean isGroup() {
        return group;
    }


    public BaseGroupViewHolder(View itemView) {
        this(itemView, false);
    }

    public void hideItemView(boolean h) {
        if(h){
            itemView.getLayoutParams().height=0;
        }else{
            itemView.getLayoutParams().height=old;
        }
    }
}
