package com.github.linjson.exlist;

import android.content.Context;
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

/**
 * Created by ljs on 2017/2/13.
 */

public class RPViewController {

    private final int mDurationMillis = 300;
    private final Context mContext;
    private final RefreshPullView mView;
    private ViewAnimation mAnimation;
    private Interpolator mInterpolator;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private int mTouchSlop;
    private boolean mRefreshingDispatch;
    private boolean mLoadingMoreDispatch;
    private RefreshPullView.OnRefreshingListener mOnRefreshingListener;
    private RefreshPullView.OnLoadingMoreListener mOnLoadingMoreListener;
    private int mViewOffsetHeader;
    private int mViewOffsetFooter;
    private int mHeaderSrcPosition;
    private int mFooterSrcPosition;

    private View mChildBody;
    private View mChildHead;
    private View mChildFoot;

    private int[] mParentOffsetInWindow = new int[2];
    private int[] mParentScrollConsumed = new int[2];
    private int mHeaderScrolled;
    private int mFooterScrolled;
    private int mFlag;


    private boolean mRefreshing;
    private boolean mLoadingMore;

    private int mActionPointerId;
    private float mInitialDownY;

    private boolean mIsBeingDragged;
    private float mInitialMoveY;
    private boolean mChildBodyTouch;
    private boolean mNestedScroll;
    private boolean mLoadingMoreEnable = true;

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

    public void onLayout() {
        if (mView.getChildCount() == 0) {
            return;
        }
        int width = mView.getMeasuredWidth();
        int height = mView.getMeasuredHeight() - mView.getPaddingBottom();

        int left = mView.getPaddingLeft();
        int top = mView.getPaddingTop() + mViewOffsetHeader == 0 ? mViewOffsetFooter : mViewOffsetHeader;

        if (mChildBody != null) {
            mChildBody.layout(left, top, width - left - mView.getPaddingRight(), height + top);
        }

        if (mChildHead != null) {
            mChildHead.layout(0, mHeaderSrcPosition + mViewOffsetHeader, mChildHead.getMeasuredWidth(), mHeaderSrcPosition + mChildHead.getMeasuredHeight() + mViewOffsetHeader);
        }
        if (mChildFoot != null) {
            mChildFoot.layout(0, mFooterSrcPosition + mViewOffsetFooter, mChildFoot.getMeasuredWidth(), mFooterSrcPosition + mChildFoot.getMeasuredHeight() + mViewOffsetFooter);
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mChildHead != null) {
            mHeaderSrcPosition = -mChildHead.getMeasuredHeight();
        }
        if (mChildFoot != null) {
            mFooterSrcPosition = mView.getMeasuredHeight();
        }
    }

    public void setHeaderView(View view) {
        assertWrapViewExtension(view);
        if (mChildHead != view) {
            if (mChildHead != null) {
                mView.removeView(mChildHead);
            }
            mChildHead = view;
            mView.addView(view, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
            mView.addView(view, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (mChildBody == null) {
            mChildBody = child;
            judgeChildBodyNestScroll();
            mView.addView(child, -1, params);
        } else if (mChildHead == null) {
            assertWrapViewExtension(child);
            mChildHead = child;
            mView.addView(child, 0, params);
        } else if (mChildFoot == null) {
            assertWrapViewExtension(child);
            mChildFoot = child;
            mView.addView(child, 0, params);
        }
    }

    private void judgeChildBodyNestScroll() {
        mNestedScroll = mChildBody instanceof NestedScrollingChild;
    }


    private void setTargetOffset(int offsetY) {
        if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (mChildHead != null) {
                mView.invalidate();
                mChildHead.offsetTopAndBottom(offsetY);
                mChildBody.offsetTopAndBottom(offsetY);

                if (!mRefreshing) {
                    float rate = Math.min(1, mChildBody.getTop() * 1.0f / mChildHead.getMeasuredHeight());
                    getWrapViewExtension(mChildHead).setRate(rate);

                    if (mHeaderScrolled == 0) {
                        getWrapViewExtension(mChildHead).showPreView();
                    }

                }

                mHeaderScrolled += offsetY;

            }
        } else {
            if (mChildFoot != null) {
                mView.invalidate();
                mChildFoot.offsetTopAndBottom(offsetY);
                mChildBody.offsetTopAndBottom(offsetY);

                if (!mLoadingMore && mLoadingMoreEnable) {
                    float rate = Math.min(1, mChildBody.getTop() * -1.0f / mChildFoot.getMeasuredHeight());
                    getWrapViewExtension(mChildFoot).setRate(rate);

                    if (mFooterScrolled == 0) {
                        getWrapViewExtension(mChildFoot).showPreView();
                    }
                }
                mFooterScrolled += offsetY;
            }
        }

        if (mChildBody.getTop() == 0) {
            mViewOffsetHeader = 0;
            mViewOffsetFooter = 0;
        }

    }

    private WrapViewExtension getWrapViewExtension(View child) {
        if (child instanceof WrapViewExtension) {
            return (WrapViewExtension) child;
        }
        return null;
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

            getWrapViewExtension(mChildHead).resetView();

        }

        if (mRefreshing != open && open) {
            mRefreshingDispatch = true;

            getWrapViewExtension(mChildHead).showStartView();

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
            if (mLoadingMoreEnable) {
                getWrapViewExtension(mChildFoot).resetView();
            }
        }

        if (mLoadingMore != open && open) {
            mLoadingMoreDispatch = true;
            if (mLoadingMoreEnable) {
                getWrapViewExtension(mChildFoot).showStartView();
            }
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

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
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
            if (mChildHead != null && !mLoadingMore && !childBodyCanScrollUP() && ((diff < 0 && startDrag) || mChildHead.getTop() >= 0)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_TOP;
            } else if (mChildFoot != null && !mRefreshing && !childBodyCanScrollDown() && ((diff > 0 && startDrag) || mChildFoot.getBottom() <= mFooterSrcPosition)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_BOTTOM;
            }
            if (mFlag != -1 && !mIsBeingDragged) {
                mIsBeingDragged = true;
                mInitialMoveY = y;
            }
        }
        return mIsBeingDragged;
    }

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


    private void footerViewStopAction() {
        mFooterScrolled = 0;
        if (mView.getMeasuredHeight() - mChildFoot.getBottom() >= 0) {
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

}
