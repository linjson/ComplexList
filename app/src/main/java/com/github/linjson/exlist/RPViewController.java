package com.github.linjson.exlist;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import static android.view.View.GONE;

/**
 * Created by ljs on 2017/2/13.
 */

public abstract class RPViewController {

    private final int mDurationMillis = 300;
    protected final Context mContext;
    protected final RefreshPullView mView;
    private ViewAnimation mAnimation;
    private Interpolator mInterpolator;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    protected int mTouchSlop;
    protected boolean mRefreshingDispatch;
    protected boolean mLoadingMoreDispatch;
    private RefreshPullView.OnRefreshingListener mOnRefreshingListener;
    private RefreshPullView.OnLoadingMoreListener mOnLoadingMoreListener;
    protected int mViewOffsetHeader;
    protected int mViewOffsetFooter;
    protected int mHeaderSrcPosition;
    protected int mFooterSrcPosition;
    protected int mHeaderSrcHeight;
    protected int mFooterSrcHeight;

    protected View mChildBody;
    protected View mChildHead;
    protected View mChildFoot;

    protected int[] mParentOffsetInWindow = new int[2];
    protected int[] mParentScrollConsumed = new int[2];
    protected int mHeaderScrolled;
    protected int mFooterScrolled;
    protected int mFlag;


    protected boolean mRefreshing;
    protected boolean mLoadingMore;

    protected int mActionPointerId;
    protected float mInitialDownY;

    protected boolean mIsBeingDragged;
    protected float mInitialMoveY;
    protected boolean mChildBodyTouch;
    protected boolean mNestedScroll;
    protected boolean mLoadingMoreEnable = true;

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
                    mOnRefreshingListener.doRefreshingData(mView);
                }
//                System.out.printf("==>mRefreshing is open \n");
            } else if (mLoadingMoreDispatch) {
                mLoadingMoreDispatch = false;
                if (mOnLoadingMoreListener != null) {
                    mOnLoadingMoreListener.doLoadingMoreData(mView);
                }
//                System.out.printf("==>loadingmore is open \n");
            } else {
                if (!mRefreshing && mChildHead != null) {
                    mChildHead.setVisibility(GONE);
                    getWrapViewExtension(mChildHead).resetView();
                }
                if (!mLoadingMore && mChildFoot != null) {
                    mChildFoot.setVisibility(GONE);
                    if (mLoadingMoreEnable) {
                        getWrapViewExtension(mChildFoot).resetView();
                    }
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public RPViewController(RefreshPullView view) {
        this.mContext = view.getContext();
        this.mView = view;
        mAnimation = new ViewAnimation();
        mInterpolator = new DecelerateInterpolator(0.5f);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(mView);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(mView);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        setNestedScrollingEnabled(true);
    }

    public abstract void onLayout();

    public abstract void onMeasure(int widthMeasureSpec, int heightMeasureSpec);

    public void setHeaderView(View view) {
        assertWrapViewExtension(view);
        if (mChildHead != view) {
            if (mChildHead != null) {
                mView.removeView(mChildHead);
            }
            mChildHead = view;
            mChildHead.setVisibility(View.GONE);
            if (mView.indexOfChild(mChildHead) == -1) {
                mView.addView(view, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void assertWrapViewExtension(View view) {
        if (!(view instanceof WrapViewExtension)) {
            throw new RuntimeException("view must be implement <WrapViewExtension>");
        }
    }

    public void setFooterView(View view) {
        assertWrapViewExtension(view);
        if (mChildFoot != view) {
            if (mChildFoot != null) {
                mView.removeView(mChildFoot);
            }
            mChildFoot = view;
            mChildFoot.setVisibility(View.GONE);
            if (mView.indexOfChild(mChildFoot) == -1) {
                mView.addView(view, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (mChildBody == null) {
            mChildBody = child;
            if (child.getBackground() == null) {
                child.setBackgroundColor(Color.WHITE);
            }
            judgeChildBodyNestScroll();
            mView.addView(child, -1, params);
        } else if (mChildHead == null) {
            assertWrapViewExtension(child);
            mChildHead = child;
            mChildHead.setVisibility(View.GONE);
            mView.addView(child, 0, params);
        } else if (mChildFoot == null) {
            assertWrapViewExtension(child);
            mChildFoot = child;
            mChildFoot.setVisibility(View.GONE);
            mView.addView(child, 0, params);
        }
    }

    private void judgeChildBodyNestScroll() {
        mNestedScroll = mChildBody instanceof NestedScrollingChild;
    }


    protected abstract void setTargetOffset(int offsetY);

    protected WrapViewExtension getWrapViewExtension(View child) {
        if (child instanceof WrapViewExtension) {
            return (WrapViewExtension) child;
        }
        return null;
    }

    protected boolean childBodyCanScrollUP() {
        return ViewCompat.canScrollVertically(mChildBody, -1);
    }

    protected boolean childBodyCanScrollDown() {

        return ViewCompat.canScrollVertically(mChildBody, 1);
    }

    protected void viewStartAnimator(View child, int to) {
        viewStartAnimator(child, to, 0);
    }

    protected void viewStartAnimator(View child, int to, int offset) {
        mAnimation.reset();
        mAnimation.setDuration(mDurationMillis);
        mAnimation.setAnimationListener(listener);
        mAnimation.setInterpolator(mInterpolator);
        mAnimation.body = mChildBody;
        mAnimation.bodyTo = mView.getPaddingTop() + offset;
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

    public abstract void setRefreshing(boolean open);

    public abstract void setLoadingMore(boolean open);

    protected abstract int getHeaderScrollUp(int dy);

    protected abstract int getFooterScrollDown(int dy);

    public void setOnLoadingMoreListener(RefreshPullView.OnLoadingMoreListener onLoadingMoreListener) {
        mOnLoadingMoreListener = onLoadingMoreListener;
    }

    public void setOnRefreshingListener(RefreshPullView.OnRefreshingListener onRefreshingListener) {
        mOnRefreshingListener = onRefreshingListener;
    }

    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        mChildBodyTouch = false;
        mHeaderScrolled = 0;
        mFooterScrolled = 0;
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        if (mHeaderScrolled != 0 && mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {

            headerViewStopAction();

        } else if (mFooterScrolled != 0 && mFlag == ViewCompat.SCROLL_INDICATOR_BOTTOM) {
            footerViewStopAction();
        }
        stopNestedScroll();
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {


    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (mChildBody.getTop() != 0) {
            return true;
        }

        return dispatchNestedPreFling(velocityX, velocityY);
    }

    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    //
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {

        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {

        mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {

        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
    //

    public boolean onInterceptTouchEvent(MotionEvent ev) {
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
            if (checkHeaderMove(diff, startDrag)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_TOP;
            } else if (checkFooterMove(diff, startDrag)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
            }
            if (mFlag != -1 && !mIsBeingDragged) {
                mIsBeingDragged = true;
                mInitialMoveY = y;
            }
        }
        return mIsBeingDragged;
    }

    protected abstract boolean checkFooterMove(float diff, boolean startDrag);

    protected abstract boolean checkHeaderMove(float diff, boolean startDrag);

    public void dispatchTouchEvent(MotionEvent ev) {
        final int act = ev.getAction();
        if (act == MotionEvent.ACTION_MOVE) {

            if (mChildBody.getTop() == 0) {
                mChildBody.dispatchTouchEvent(ev);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
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


    protected void footerViewStopAction() {

    }

    protected void headerViewStopAction() {

    }

    protected float getMotionY(MotionEvent ev) {
        int index = ev.findPointerIndex(mActionPointerId);
        if (index < 0) {
            return Float.NaN;
        }
        return ev.getY();
    }

    public void stopLoadingMore() {
        if (mLoadingMoreEnable) {
            getWrapViewExtension(mChildFoot).showFinishView();
        }
        mLoadingMoreEnable = false;
    }

    public void setLoadingMoreEnable(boolean enable) {
        if (!mLoadingMoreEnable) {
            getWrapViewExtension(mChildFoot).resetView();
        }
        mLoadingMoreEnable = enable;
    }

    public boolean isNestScroll() {
        return mNestedScroll;
    }

    public boolean allowDispatchTouch() {
        return mChildBodyTouch && !mNestedScroll;
    }

    protected void copy(RPViewController controller){
        this.mChildHead=controller.mChildHead;
        this.mChildBody=controller.mChildBody;
        this.mChildFoot=controller.mChildFoot;
        this.mOnRefreshingListener=controller.mOnRefreshingListener;
        this.mOnLoadingMoreListener=controller.mOnLoadingMoreListener;
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
            if (hasStarted() && body.getTop() == childTo) {
                child.clearAnimation();
            }
        }

        private void moveView(float interpolatedTime, View child, int to) {
            int current = child.getTop();
            int result = (int) (current + ((to - current) * interpolatedTime));
            child.offsetTopAndBottom(result - current);
        }
    }

}
