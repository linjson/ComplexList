package itemtouchhelperextension;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

/**
 * Created by ljs on 16/9/20.
 */
public class RefreshPullView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private final int durationMillis = 300;

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
    private Interpolator mInterpolator;
    private int footerSrcPosition;
    private int flag;
    private int viewOffsetHeader;
    private int viewOffsetFooter;
    private ViewAnimation mAnimation;
    private boolean refreshing;
    private boolean loadingMore;
    private boolean refreshingDispatch;
    private boolean loadingMoreDispatch;
    private OnRefreshingListener mOnRefreshingListener;
    private OnLoadingMoreListener mOnLoadingMoreListener;

    private Animation.AnimationListener listener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            viewStopAnimator();
            if (refreshingDispatch) {
                refreshingDispatch = false;
                if (mOnRefreshingListener != null) {
                    mOnRefreshingListener.doRefreshingData(RefreshPullView.this);
                }
//                System.out.printf("==>refreshing is open \n");
            } else if (loadingMoreDispatch) {
                loadingMoreDispatch = false;
                if (mOnLoadingMoreListener != null) {
                    mOnLoadingMoreListener.doLoadingMoreData(RefreshPullView.this);
                }
//                System.out.printf("==>loadingmore is open \n");
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private int actionPointerId;
    private float mInitialDownY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private float mInitialMoveY;
    private boolean mChildBodyTouch;
    private boolean mNestedScroll;


    public RefreshPullView(Context context) {
        super(context);
        init();
    }


    public RefreshPullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshPullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mAnimation = new ViewAnimation();
        mInterpolator = new DecelerateInterpolator(0.5f);
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setNestedScrollingEnabled(true);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight() - getPaddingBottom();

        int left = getPaddingLeft();
        int top = getPaddingTop() + viewOffsetHeader == 0 ? viewOffsetFooter : viewOffsetHeader;

        if (childBody != null) {
            childBody.layout(left, top, width - left - getPaddingRight(), height + top);
        }

        if (childHead != null) {
            childHead.layout(0, headerSrcPosition + viewOffsetHeader, childHead.getMeasuredWidth(), headerSrcPosition + childHead.getMeasuredHeight() + viewOffsetHeader);
        }
        if (childFoot != null) {
            childFoot.layout(0, footerSrcPosition + viewOffsetFooter, childFoot.getMeasuredWidth(), footerSrcPosition + childFoot.getMeasuredHeight() + viewOffsetFooter);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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

    public void setHeaderView(View view) {
        childHead = view;
        addView(view);
    }

    public void setFooterView(View view) {
        childFoot = view;
        addView(view);
    }


    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
        if (childBody == null) {
            childBody = child;
            mNestedScroll = childBody instanceof NestedScrollingChild;
        } else if (childHead == null) {
            childHead = child;
        } else if (childFoot == null) {
            childFoot = child;
        }
    }

    private void setTargetOffset(int offsetY) {
        if (flag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (childHead != null) {
                headerScrolled += offsetY;
                childHead.offsetTopAndBottom(offsetY);
                childBody.offsetTopAndBottom(offsetY);
            }
        } else {
            if (childFoot != null) {
                footerScrolled += offsetY;
                childFoot.offsetTopAndBottom(offsetY);
                childBody.offsetTopAndBottom(offsetY);
            }
        }


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
        mAnimation.setInterpolator(mInterpolator);
        mAnimation.body = childBody;
        mAnimation.bodyTo = getPaddingTop() + offset;
        mAnimation.child = child;
        mAnimation.childTo = to;
        child.clearAnimation();


        if (child.getTop() == to) {
            return;
        }

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

        viewOffsetHeader = 0;
        viewOffsetFooter = 0;
    }

    public void setRefreshing(boolean open) {


        if (loadingMore || childHead == null) {
            return;
        }
        if (open) {
            viewStartAnimator(childHead, 0, -headerSrcPosition);

        } else {
            viewOffsetHeader = -headerSrcPosition + childHead.getTop();
            viewOffsetFooter = 0;
            viewStartAnimator(childHead, headerSrcPosition);
        }

        if (refreshing != open && open) {
            refreshingDispatch = true;
        }

        refreshing = open;
    }

    public void setLoadingMore(boolean open) {
        if (refreshing || childFoot == null) {
            return;
        }
        if (open) {
            viewStartAnimator(childFoot, footerSrcPosition - childFoot.getMeasuredHeight(), -childFoot.getMeasuredHeight());
        } else {
            viewOffsetHeader = 0;
            viewOffsetFooter = childFoot.getTop() - footerSrcPosition;
            viewStartAnimator(childFoot, footerSrcPosition);
        }

        if (loadingMore != open && open) {
            loadingMoreDispatch = true;
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
            return space;
        }

        return Math.min(space, dy);
    }

    public void setOnLoadingMoreListener(OnLoadingMoreListener onLoadingMoreListener) {
        mOnLoadingMoreListener = onLoadingMoreListener;
    }

    public void setOnRefreshingListener(OnRefreshingListener onRefreshingListener) {
        mOnRefreshingListener = onRefreshingListener;
    }

    //NestedScrollingParent begin

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
//        viewStopAnimator();
//        System.out.printf("==>onStartNestedScroll \n");
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
        if (headerScrolled > 0 && flag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (childHead.getTop() > 0 || refreshing) {
                setRefreshing(true);
            } else {
                viewStartAnimator(childHead, headerSrcPosition);
            }

            headerScrolled = 0;

        } else if (footerScrolled < 0 && flag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
            footerScrolled = 0;

            if (getMeasuredHeight() - childFoot.getBottom() >= 0 || loadingMore) {
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
//            System.out.printf("==>%s,%s \n", childHead.getTop(), dy);
            if (childHead.getTop() < 0 && !childBodyCanScrollUP() && dy < 0) {
                int x = Math.max(childHead.getTop(), dy);
                setTargetOffset(-x);
            }
        } else if (loadingMore) {

            if (childFoot.getTop() >= footerSrcPosition && !childBodyCanScrollDown() && dy > 0) {
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
//        System.out.printf("==>onNestedPreScroll,dx:%s,dy:%s,[0]:%s,[1]:%s \n", dx, dy, consumed[0], consumed[1]);
//        System.out.printf("==>headerScrolled=%s \n", headerScrolled);
//
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


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        System.out.printf("==>onInterceptTouchEvent \n");

        if (mNestedScroll) {
            return super.onInterceptTouchEvent(ev);
        }
        final int act = ev.getAction();
        if (act == MotionEvent.ACTION_DOWN) {
            flag = -1;
            actionPointerId = ev.getPointerId(0);
            mIsBeingDragged = false;
            mChildBodyTouch = false;
            final float y = getMotionY(ev);
            if (Float.isNaN(y)) {
                return false;
            }
            mInitialDownY = y;
        } else if (act == MotionEvent.ACTION_MOVE) {
            final float y = getMotionY(ev);
            if (Float.isNaN(y)) {
                return false;
            }
            final float diff = y - mInitialDownY;
            if (Math.abs(diff) > mTouchSlop) {
                if (!loadingMore && !childBodyCanScrollUP() && (diff > 0 || childHead.getTop() >= 0)) {
                    flag = ViewCompat.SCROLL_INDICATOR_TOP;
                } else if (!refreshing && !childBodyCanScrollDown() && (diff < 0 || childFoot.getBottom() <= footerSrcPosition)) {
                    flag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
                }
//                System.out.printf("==>flag:%s, %s,%s,%s\n", flag, childBodyCanScrollDown(), diff,
//                        VelocityTrackerCompat.getYVelocity(velocityTracker, actionPointerId));
                if (flag != -1 && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                    mInitialMoveY = y;// mInitialDownY + mTouchSlop;
                }

            }
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {


        boolean result = super.dispatchTouchEvent(ev);


        if (mChildBodyTouch) {
            final int act = ev.getAction();
            if (act == MotionEvent.ACTION_MOVE) {

                if (childBody.getTop() == 0) {
                    childBody.dispatchTouchEvent(ev);
                }
            }
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


//        if (childBodyCanScrollUP()) {
//            return false;
//        }
        if (mNestedScroll) {
            return super.onTouchEvent(ev);
        }
        final int act = ev.getAction();
        if (act == MotionEvent.ACTION_DOWN) {

            actionPointerId = ev.getPointerId(0);
            mIsBeingDragged = false;


        } else if (act == MotionEvent.ACTION_MOVE) {
            if (flag == -1) {
                return false;
            }

            final float y = getMotionY(ev);
            if (Float.isNaN(y)) {
                return false;
            }

            float overscrollTop = (y - mInitialMoveY);
            if (!loadingMore && mIsBeingDragged && flag == ViewCompat.SCROLL_INDICATOR_TOP) {
                int dy = overscrollTop > 0 ? (int) overscrollTop : getHeaderScrollUp((int) overscrollTop);

                if (childBodyCanScrollUP() && overscrollTop > 0) {
                    dy = 0;
                }
                mChildBodyTouch = dy == 0;
                setTargetOffset(dy);

            } else if (!refreshing && mIsBeingDragged && flag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
                int dy = overscrollTop < 0 ? (int) overscrollTop : getFooterScrollDown((int) overscrollTop);

                if (childBodyCanScrollDown() && overscrollTop < 0) {
                    dy = 0;
                }
                mChildBodyTouch = dy == 0;
                setTargetOffset(dy);
            }
            mInitialMoveY = y;
        } else if (act == MotionEvent.ACTION_CANCEL || act == MotionEvent.ACTION_UP) {
            if (flag == ViewCompat.SCROLL_INDICATOR_TOP) {
                if (childHead.getTop() > 0) {
                    setRefreshing(true);
                } else {
                    viewStartAnimator(childHead, headerSrcPosition);
                }
            } else if (flag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
                if (getMeasuredHeight() - childFoot.getBottom() >= 0) {
                    setLoadingMore(true);
                } else {
                    viewStartAnimator(childFoot, footerSrcPosition);
                }
            }

        }

        return true;
    }

    private float getMotionY(MotionEvent ev) {
        int index = ev.findPointerIndex(actionPointerId);
        if (index < 0) {
            return Float.NaN;
        }
        return ev.getY();
    }

    private static class ViewAnimation extends Animation {

        private View child;
        private View body;
        private int bodyTo;
        private int childTo;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            moveView(interpolatedTime, child, childTo);
            moveView(interpolatedTime, body, bodyTo);


        }

        private void moveView(float interpolatedTime, View child, int to) {
            int current = child.getTop();
            int result = (int) (current + ((to - current) * interpolatedTime));
            child.offsetTopAndBottom(result - current);
        }
    }

    public interface OnRefreshingListener {
        void doRefreshingData(RefreshPullView view);
    }

    public interface OnLoadingMoreListener {
        void doLoadingMoreData(RefreshPullView view);
    }
}
