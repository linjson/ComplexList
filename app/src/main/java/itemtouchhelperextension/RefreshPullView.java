package itemtouchhelperextension;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by ljs on 16/9/20.
 */
public class RefreshPullView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private NestedScrollingParentHelper nestedScrollingParentHelper;
    private NestedScrollingChildHelper nestedScrollingChildHelper;
    private View childBody;
    private View childHead;
    private View childFoot;
    private int[] mParentOffsetInWindow = new int[2];
    private int[] mParentScrollConsumed = new int[2];
    private int headerScrolled = 0;
    private int footerScrolled = 0;
    private int headerSrcPosition = 0;
    private DecelerateInterpolator mDecelerateInterpolator;
    private int footerSrcPosition;
    private int durationMillis = 400;
    private int desc;
    private Animation.AnimationListener listener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            viewStopAnimator();
            if (refreshing) {
                System.out.printf("==>refreshing is open \n");
            }

            if (loadingMore) {
                System.out.printf("==>loadingmore is open \n");
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };


    //    private Animation mAnimation = new Animation() {
//
//
//    };
    private int flag;
    private ViewAnimation mAnimation;
    private boolean refreshing;
    private boolean loadingMore;


    public RefreshPullView(Context context) {
        super(context);
        init(context);
    }


    public RefreshPullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshPullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mAnimation = new ViewAnimation();
        mDecelerateInterpolator = new DecelerateInterpolator(2f);
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int left = getPaddingLeft();
        int top = getPaddingTop();

        if (childBody != null) {
            childBody.layout(left, top, width - left - getPaddingRight(), height - top - getPaddingBottom());
        }

        if (childHead != null) {
            childHead.layout(0, headerSrcPosition, childHead.getMeasuredWidth(), headerSrcPosition + childHead.getMeasuredHeight());
        }
        if (childFoot != null) {
            childFoot.layout(0, footerSrcPosition, childFoot.getMeasuredWidth(), footerSrcPosition + childFoot.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        desc = getMeasuredHeight() / 4;
        if (childBody != null) {
            childBody.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        }
        if (childHead != null) {
            measureChild(childHead, widthMeasureSpec, heightMeasureSpec);
            headerSrcPosition = -childHead.getMeasuredHeight();
        }
        if (childFoot != null) {
            measureChild(childFoot, widthMeasureSpec, heightMeasureSpec);
            footerSrcPosition = getMeasuredHeight();
        }
    }

    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
        if (childHead == null) {
            childHead = child;
        } else if (childBody == null) {
            childBody = child;
        } else if (childFoot == null) {
            childFoot = child;
        }
    }

    private void setTargetOffset(int offsetY) {
        if (flag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (childHead != null) {
                headerScrolled += offsetY;
                childHead.offsetTopAndBottom(offsetY);
            }
        } else {
            if (childFoot != null) {
                footerScrolled += offsetY;
                childFoot.offsetTopAndBottom(offsetY);
            }
        }

        childBody.offsetTopAndBottom(offsetY);
    }

    private boolean childBodyCanScrollUP() {

        return ViewCompat.canScrollVertically(childBody, -1);
    }

    private boolean childBodyCanScrollDown() {

        return ViewCompat.canScrollVertically(childBody, 1);
    }

    private void viewStartAnimator(View child, int to) {
        viewStartAnimator(child, to, 0);
    }

    private void viewStartAnimator(View child, int to, int offset) {
        mAnimation.reset();
        mAnimation.setDuration(durationMillis);
        mAnimation.setAnimationListener(listener);
        mAnimation.setInterpolator(mDecelerateInterpolator);
        mAnimation.body = childBody;
        mAnimation.bodyTo = getPaddingTop() + offset;
        mAnimation.child = child;
        mAnimation.childTo = to;
        child.clearAnimation();
        child.startAnimation(mAnimation);
    }

    private void viewStopAnimator() {
        if (childHead != null) {
            childHead.clearAnimation();
        }
        if (childFoot != null) {
            childFoot.clearAnimation();
        }
        headerScrolled = 0;
        footerScrolled = 0;
    }

    public void setRefreshing(boolean open) {


//        if (loadingMore || open == refreshing) {
//            return;
//        }
        if (open) {
            viewStartAnimator(childHead, 0, -headerSrcPosition);
        } else {
            viewStartAnimator(childHead, headerSrcPosition);
        }
        refreshing = open;
    }

    public void setLoadingMore(boolean open) {
//        if (refreshing || open == loadingMore) {
//            return;
//        }
        if (open) {
            viewStartAnimator(childFoot, footerSrcPosition - childFoot.getMeasuredHeight(), -childFoot.getMeasuredHeight());
        } else {
            viewStartAnimator(childFoot, footerSrcPosition);
        }
        loadingMore = open;
    }

    private int getHeaderScrollUp(int dy) {
        int space = headerSrcPosition - childHead.getTop();

        if (Math.abs(space) < dy) {
            return Math.abs(space);
        }

        return Math.max(dy, space);
    }

    private int getFooterScrollDown(int dy) {
        int space = footerSrcPosition - childFoot.getTop();

        if (space < Math.abs(dy)) {
            return -space;
        }

        return Math.min(space, dy);
    }


    //NestedScrollingParent begin

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        viewStopAnimator();
//        System.out.printf("==>onStartNestedScroll \n");
//        if (refreshing || loadingMore) {
//            return false;
//        }
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
//        System.out.printf("==>onNestedScrollAccepted \n");
    }

    @Override
    public void onStopNestedScroll(View target) {
        nestedScrollingParentHelper.onStopNestedScroll(target);
//        System.out.printf("==>onStopNestedScroll,%s \n", headerScrolled);
        if (flag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (headerScrolled > 0) {
                if (childHead.getTop() > desc || refreshing) {
                    setRefreshing(true);
                } else {
                    viewStartAnimator(childHead, headerSrcPosition);
                }
            }

            headerScrolled = 0;

        } else if (footerScrolled < 0 && flag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
            footerScrolled = 0;

            if (getMeasuredHeight() - childFoot.getBottom() > desc || loadingMore) {
                setLoadingMore(true);
            } else {
                viewStartAnimator(childFoot, footerSrcPosition);
            }
        }

        stopNestedScroll();

    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        System.out.printf("==>onNestedScroll,dyConsumed:%s,dyUnconsumed:%s \n", dyConsumed, dyUnconsumed);
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (refreshing) {
            if (childHead.getTop() < 0 && !childBodyCanScrollUP()) {
                int x = Math.max(childHead.getTop(), dy);
                setTargetOffset(-x);
            }
        } else if (loadingMore) {
//            if (dy < 0 && !childBodyCanScrollDown()) {
//                flag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
//                setTargetOffset(-dy);
//            }
//            moveView(childBody, 0);
//            moveView(childFoot, footerSrcPosition);

            if (childFoot.getTop() >= footerSrcPosition && !childBodyCanScrollDown()) {
                int x = Math.min(childFoot.getTop(), dy);
                setTargetOffset(-x);

            }


        } else if (dy < 0 && !childBodyCanScrollUP()) {
            flag = ViewCompat.SCROLL_INDICATOR_TOP;
            setTargetOffset(-dy);
        } else if (dy > 0 && !childBodyCanScrollDown()) {
            flag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
            setTargetOffset(-dy);
        }

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        System.out.printf("==>onNestedPreScroll,dx:%s,dy:%s,[0]:%s,[1]:%s \n", dx, dy, consumed[0], consumed[1]);
//        System.out.printf("==>headerScrolled=%s \n", headerScrolled);

        if (refreshing) {
            if (childHead.getTop() >= headerSrcPosition) {
                consumed[1] = getHeaderScrollUp(dy);
                setTargetOffset(-consumed[1]);
            }
        } else if (loadingMore) {
            if (childFoot.getTop() <= footerSrcPosition) {
                consumed[1] = getFooterScrollDown(dy);
                setTargetOffset(-consumed[1]);
            }
        } else if (flag == ViewCompat.SCROLL_INDICATOR_TOP && dy > 0 && headerScrolled > 0) {
            consumed[1] = dy;
            setTargetOffset(-dy);
        } else if (flag == ViewCompat.SCROLL_INDICATOR_BOTTOM && dy < 0 && footerScrolled < 0) {
            consumed[1] = dy;
            setTargetOffset(-dy);
        }


        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }


    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
//        System.out.printf("==>onNestedFling \n");
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        System.out.printf("==>onNestedPreFling \n");
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
//        System.out.printf("==>getNestedScrollAxes,%s \n", nestedScrollingParentHelper.getNestedScrollAxes());
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }

    //NestedScrollingParent end

    //NestedScrollingChild begin

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    //NestedScrollingChild end


    private static class ViewAnimation extends Animation {

        private View child;
        private View body;
        private int bodyTo;
        private int childTo;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            moveView(interpolatedTime, child, childTo);
            moveView(interpolatedTime, body, bodyTo);
//            System.out.printf("==>in:%s \n", interpolatedTime);

        }

        private void moveView(float interpolatedTime, View child, int to) {
            int current = child.getTop();
            int result = (int) (current + ((to - current) * interpolatedTime));
            child.offsetTopAndBottom(result - current);
        }
    }
}
