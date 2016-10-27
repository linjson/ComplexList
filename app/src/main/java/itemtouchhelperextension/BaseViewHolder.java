package itemtouchhelperextension;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ljs on 16/9/6.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements Extension {

    private boolean fixed = false;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public float getActionWidth() {
        return 0;
    }

    @Override
    public View getFrontView() {
        return null;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
}
