package itemtouchhelperextension;

import android.view.View;

public interface Extension {

    float getActionWidth();

    View getFrontView();

    boolean isFixed();

    int getGroupId();

    boolean isGroup();
}
