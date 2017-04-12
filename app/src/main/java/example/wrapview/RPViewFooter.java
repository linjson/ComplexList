package example.wrapview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.linjson.exlist.WrapViewExtension;

import example.R;

/**
 * Created by ljs on 2017/2/6.
 */

public class RPViewFooter extends FrameLayout implements WrapViewExtension {
    private LottieAnimationView percent;
    private TextView desc;
    private int mState;

    public RPViewFooter(Context context) {
        super(context);
        init();
    }

    public RPViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RPViewFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        View.inflate()
        inflate(getContext(), R.layout.header, this);
        percent = (LottieAnimationView) findViewById(R.id.animation_view);
        desc = (TextView) findViewById(R.id.desc);

        desc.setText("footer");
        desc.setTextColor(Color.RED);
//        addView(View.inflate(getContext(), R.layout.header, this));
    }

    @Override
    public void setRate(float rate) {
        percent.setProgress(rate);
    }

    @Override
    public void showPreView() {
        desc.setText("加载更多");
    }

    @Override
    public void showStartView() {
        desc.setText("开始加载");
        percent.playAnimation();
    }

    @Override
    public void resetView() {
        percent.cancelAnimation();
        desc.setText("加载更多");

    }

    @Override
    public void showFinishView() {
        percent.cancelAnimation();
        desc.setText("没有更多");
        System.out.printf("==>showFinishView \n");
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(int state) {
        mState=state;
    }
}
