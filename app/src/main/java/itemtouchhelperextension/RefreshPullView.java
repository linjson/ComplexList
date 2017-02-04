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
    private final int mDurationMillis = 300;

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private View mChildBody;
    private View mChildHead;
    private View mChildFoot;
    private int[] mParentOffsetInWindow = new int[2];
    private int[] mParentScrollConsumed = new int[2];
    private int mHeaderScrolled;
    private int mFooterScrolled;
    private int mHeaderSrcPosition;
    private int mFooterSrcPosition;
    private Interpolator mInterpolator;
    private int mFlag;
    private int mViewOffsetHeader;
    private int mViewOffsetFooter;
    private ViewAnimation mAnimation;
    private boolean mRefreshing;
    private boolean mLoadingMore;
    private boolean mRefreshingDispatch;
    private boolean mLoadingMoreDispatch;
    private OnRefreshingListener mOnRefreshingListener;
    private OnLoadingMoreListener mOnLoadingMoreListener;
    private int mActionPointerId;
    private float mInitialDownY;
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private float mInitialMoveY;
    private boolean mChildBodyTouch;
    private boolean mNestedScroll;

    private Animation.AnimationListener listener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            viewStopAnimator();
            if (mRefreshingDispatch) {
                mRefreshingDispatch = false;
                if (mOnRefreshingListener != null) {
                    mOnRefreshingListener.doRefreshingData(RefreshPullView.this);
                }
//                System.out.printf("==>mRefreshing is open \n");
            } else if (mLoadingMoreDispatch) {
                mLoadingMoreDispatch = false;
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
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
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
        int top = getPaddingTop() + mViewOffsetHeader == 0 ? mViewOffsetFooter : mViewOffsetHeader;

        if (mChildBody != null) {
            mChildBody.layout(left, top, width - left - getPaddingRight(), height + top);
        }

        if (mChildHead != null) {
            mChildHead.layout(0, mHeaderSrcPosition + mViewOffsetHeader, mChildHead.getMeasuredWidth(), mHeaderSrcPosition + mChildHead.getMeasuredHeight() + mViewOffsetHeader);
        }
        if (mChildFoot != null) {
            mChildFoot.layout(0, mFooterSrcPosition + mViewOffsetFooter, mChildFoot.getMeasuredWidth(), mFooterSrcPosition + mChildFoot.getMeasuredHeight() + mViewOffsetFooter);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildBody != null) {
            mChildBody.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        }
        if (mChildHead != null) {
            measureChild(mChildHead, widthMeasureSpec, heightMeasureSpec);
            mHeaderSrcPosition = -mChildHead.getMeasuredHeight();
        }
        if (mChildFoot != null) {
            measureChild(mChildFoot, widthMeasureSpec, heightMeasureSpec);
            mFooterSrcPosition = getMeasuredHeight();
        }
    }

    public void setHeaderView(View view) {
        mChildHead = view;
        addView(view);
    }

    public void setFooterView(View view) {
        mChildFoot = view;
        addView(view);
    }


    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
        if (mChildBody == null) {
            mChildBody = child;
            judgeChildBodyNestScroll();
        } else if (mChildHead == null) {
            mChildHead = child;
        } else if (mChildFoot == null) {
            mChildFoot = child;
        }
    }

    private void judgeChildBodyNestScroll() {
        mNestedScroll = mChildBody instanceof NestedScrollingChild;

//        if (!mNestedScroll && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                Class clz = Class.forName(mChildBody.getClass().getName());
//                Method[] methods = clz.getMethods();
//                for (int i = 0; i < methods.length; i++) {
//                    if (methods[i].getName().startsWith("onNested")) {
//                        mNestedScroll = true;
//                        return;
//                    }
//                }
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void setTargetOffset(int offsetY) {
        if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (mChildHead != null) {
                mHeaderScrolled += offsetY;
                mChildHead.offsetTopAndBottom(offsetY);
                mChildBody.offsetTopAndBottom(offsetY);
            }
        } else {
            if (mChildFoot != null) {
                mFooterScrolled += offsetY;
                mChildFoot.offsetTopAndBottom(offsetY);
                mChildBody.offsetTopAndBottom(offsetY);
            }
        }

        if (mChildBody.getTop() == 0) {
            mViewOffsetHeader = 0;
            mViewOffsetFooter = 0;
        }

    }

    private boolean childBodyCanScrollUP() {
        return ViewCompat.canScrollVertically(mChildBody, -1);
    }

    private boolean childBodyCanScrollDown() {

        return ViewCompat.canScrollVertically(mChildBody, 1);
    }

    private void viewStartAnimator(View child, int to) {
        viewStartAnimator(child, to, 0);
    }

    private void viewStartAnimator(View child, int to, int offset) {
        mAnimation.reset();
        mAnimation.setDuration(mDurationMillis);
        mAnimation.setAnimationListener(listener);
        mAnimation.setInterpolator(mInterpolator);
        mAnimation.body = mChildBody;
        mAnimation.bodyTo = getPaddingTop() + offset;
        mAnimation.child = child;
        mAnimation.childTo = to;
        child.clearAnimation();


        mChildBodyTouch = true;
        child.startAnimation(mAnimation);

    }

    private void viewStopAnimator() {
        if (mChildHead != null) {
            mChildHead.clearAnimation();
        }
        if (mChildFoot != null) {
            mChildFoot.clearAnimation();
        }
        mHeaderScrolled = 0;
        mFooterScrolled = 0;

        if (!mRefreshing) {
            mViewOffsetHeader = 0;
        } else {
            mViewOffsetHeader = -mHeaderSrcPosition + mChildHead.getTop();
        }
        if (!mLoadingMore) {
            mViewOffsetFooter = 0;
        } else {
            mViewOffsetFooter = mChildFoot.getTop() - mFooterSrcPosition;
        }
    }

    public void setRefreshing(boolean open) {


        if (mLoadingMore || mChildHead == null) {
            return;
        }
        if (open) {
            viewStartAnimator(mChildHead, 0, -mHeaderSrcPosition);
        } else {
            mViewOffsetHeader = -mHeaderSrcPosition + mChildHead.getTop();
            mViewOffsetFooter = 0;
            viewStartAnimator(mChildHead, mHeaderSrcPosition);
        }

        if (mRefreshing != open && open) {
            mRefreshingDispatch = true;
        }

        mRefreshing = open;
    }

    public void setLoadingMore(boolean open) {
        if (mRefreshing || mChildFoot == null) {
            return;
        }
        if (open) {
            viewStartAnimator(mChildFoot, mFooterSrcPosition - mChildFoot.getMeasuredHeight(), -mChildFoot.getMeasuredHeight());
        } else {
            mViewOffsetHeader = 0;
            mViewOffsetFooter = mChildFoot.getTop() - mFooterSrcPosition;
            viewStartAnimator(mChildFoot, mFooterSrcPosition);
        }

        if (mLoadingMore != open && open) {
            mLoadingMoreDispatch = true;
        }

        mLoadingMore = open;
    }

    private int getHeaderScrollUp(int dy) {
        int space = mHeaderSrcPosition - mChildHead.getTop();

        if (Math.abs(space) < dy) {
            return Math.abs(space);
        }

        return Math.max(dy, space);
    }

    private int getFooterScrollDown(int dy) {
        int space = mFooterSrcPosition - mChildFoot.getTop();

        if (space < Math.abs(dy)) {
            return -space;
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
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        mChildBodyTouch = false;
        mHeaderScrolled = 0;
        mFooterScrolled = 0;
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
//        System.out.printf("==>onNestedScrollAccepted \n");
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
//        System.out.printf("==>onStopNestedScroll,%s,%s \n", mHeaderScrolled, mFlag);
        if (mHeaderScrolled != 0 && mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {

            headerViewStopAction();

        } else if (mFooterScrolled != 0 && mFlag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
            footerViewStopAction();
        }

        stopNestedScroll();

    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        System.out.printf("==>onNestedScroll,dyConsumed:%s,dyUnconsumed:%s \n", dyConsumed, dyUnconsumed);

        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (mRefreshing) {
            if (mChildHead.getTop() < 0 && !childBodyCanScrollUP() && dy < 0) {
                int y = Math.max(mChildHead.getTop(), dy);
                setTargetOffset(-y);
            }
        } else if (mLoadingMore) {

            if (mChildFoot.getTop() >= mFooterSrcPosition && !childBodyCanScrollDown() && dy > 0) {
                int y = Math.min(mChildFoot.getTop(), dy);
                setTargetOffset(-y);

            }


        } else if (dy < 0 && !childBodyCanScrollUP()) {
            mFlag = ViewCompat.SCROLL_INDICATOR_TOP;
            setTargetOffset(-dy);
        } else if (dy > 0 && !childBodyCanScrollDown()) {
            mFlag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
            setTargetOffset(-dy);
        }

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
//        System.out.printf("==>onNestedPreScroll,dx:%s,dy:%s,[0]:%s,[1]:%s \n", dx, dy, consumed[0], consumed[1]);

//
        if (mChildBodyTouch) {
            consumed[1] = dy;
            return;
        }

        if (mRefreshing) {
            if (mChildHead.getTop() >= mHeaderSrcPosition) {
                if (childBodyCanScrollUP()) {
                    if (dy > 0) {
                        consumed[1] = getHeaderScrollUp(dy);
                        setTargetOffset(-consumed[1]);
                    }
                } else {
                    consumed[1] = getHeaderScrollUp(dy);
                    setTargetOffset(-consumed[1]);
                }
            }
        } else if (mLoadingMore) {
            if (mChildFoot.getTop() <= mFooterSrcPosition) {
                if (childBodyCanScrollDown()) {
                    if (dy < 0) {
                        consumed[1] = getFooterScrollDown(dy);
                        setTargetOffset(-consumed[1]);
                    }
                } else {
                    consumed[1] = getFooterScrollDown(dy);
                    setTargetOffset(-consumed[1]);
                }
            }
        } else if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP && dy > 0 && mHeaderScrolled > 0) {
            consumed[1] = dy;
            setTargetOffset(-dy);
        } else if (mFlag == ViewCompat.SCROLL_INDICATOR_BOTTOM && dy < 0 && mFooterScrolled < 0) {
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

        if (mChildBody.getTop() != 0) {
            return true;
        }

        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
//        System.out.printf("==>getNestedScrollAxes,%s \n", mNestedScrollingParentHelper.getNestedScrollAxes());
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    //NestedScrollingParent end

    //NestedScrollingChild begin

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
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
            mFlag = -1;
            mActionPointerId = ev.getPointerId(0);
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
            final float diff = mInitialDownY - y;
            boolean startDrag = Math.abs(diff) > mTouchSlop;
            if (!mLoadingMore && !childBodyCanScrollUP() && ((diff < 0 && startDrag) || mChildHead.getTop() >= 0)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_TOP;
            } else if (!mRefreshing && !childBodyCanScrollDown() && ((diff > 0 && startDrag) || mChildFoot.getBottom() <= mFooterSrcPosition)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
            }
            if (mFlag != -1 && !mIsBeingDragged) {
                mIsBeingDragged = true;
                mInitialMoveY = y;
            }
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {


        boolean result = super.dispatchTouchEvent(ev);


        if (mChildBodyTouch && !mNestedScroll) {
            final int act = ev.getAction();
            if (act == MotionEvent.ACTION_MOVE) {

                if (mChildBody.getTop() == 0) {
                    mChildBody.dispatchTouchEvent(ev);
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

            mActionPointerId = ev.getPointerId(0);
            mIsBeingDragged = false;


        } else if (act == MotionEvent.ACTION_MOVE) {
            final float y = getMotionY(ev);
            if (Float.isNaN(y)) {
                return false;
            }

            float overscrollTop = mInitialMoveY - y;
            if (!mLoadingMore && mIsBeingDragged && mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
                int dy = overscrollTop < 0 ? (int) overscrollTop : getHeaderScrollUp((int) overscrollTop);

                if (childBodyCanScrollUP() && overscrollTop < 0) {
                    dy = 0;
                }
                mChildBodyTouch = dy == 0;
                setTargetOffset(-dy);

            } else if (!mRefreshing && mIsBeingDragged && mFlag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
                int dy = overscrollTop > 0 ? (int) overscrollTop : getFooterScrollDown((int) overscrollTop);

                if (childBodyCanScrollDown() && overscrollTop > 0) {
                    dy = 0;
                }
                mChildBodyTouch = dy == 0;
                setTargetOffset(-dy);
            }
            mInitialMoveY = y;
        } else if (act == MotionEvent.ACTION_CANCEL || act == MotionEvent.ACTION_UP) {
            if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
                headerViewStopAction();
            } else if (mFlag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
                footerViewStopAction();
            }

        }

        return true;
    }

    private void footerViewStopAction() {
        mFooterScrolled = 0;
        if (getMeasuredHeight() - mChildFoot.getBottom() >= 0) {
            setLoadingMore(true);
        } else {
            viewStartAnimator(mChildFoot, mFooterSrcPosition);
        }
    }

    private void headerViewStopAction() {
        if (mChildHead.getTop() > 0) {
            setRefreshing(true);
        } else {
            viewStartAnimator(mChildHead, mHeaderSrcPosition);
        }
        mHeaderScrolled = 0;
    }

    private float getMotionY(MotionEvent ev) {
        int index = ev.findPointerIndex(mActionPointerId);
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
            if (child.getTop() == childTo) {
                child.clearAnimation();
            }
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
