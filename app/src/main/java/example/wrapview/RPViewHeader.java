package example.wrapview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.linjson.exlist.WrapViewExtension;

import example.R;

/**
 * Created by ljs on 2017/2/6.
 */

public class RPViewHeader extends FrameLayout implements WrapViewExtension {
    private LottieAnimationView percent;
    private TextView desc;
    private int mState;

    public RPViewHeader(Context context) {
        super(context);
        init();
    }

    public RPViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RPViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        View.inflate()
        inflate(getContext(), R.layout.header, this);
        percent = (LottieAnimationView) findViewById(R.id.animation_view);
        desc = (TextView) findViewById(R.id.desc);
//        addView(View.inflate(getContext(), R.layout.header, this));
    }

    @Override
    public void setRate(float rate) {
        percent.setProgress(rate);
    }

    @Override
    public void showPreView() {
        desc.setText("下拉刷新");
        System.out.printf("==>%s \n","showPreView");
    }

    @Override
    public void showStartView() {
        System.out.printf("==>%s \n","showStartView");
        desc.setText("开始刷新");
        percent.playAnimation();
    }

    @Override
    public void resetView() {
        percent.cancelAnimation();
        desc.setText("下拉刷新");

    }

    @Override
    public void showFinishView() {

    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(int state) {
        mState=state;
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
////
////        setMeasuredDimension(getChildAt(0).getMeasuredWidth(), getChildAt(0).getMeasuredHeight());
//    }
}
