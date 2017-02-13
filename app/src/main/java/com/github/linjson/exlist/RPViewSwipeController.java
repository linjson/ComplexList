package com.github.linjson.exlist;

import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ljs on 2017/2/13.
 */

public class RPViewSwipeController extends RPViewController {
    private int mHeaderSrcHeight;
    private int mFooterSrcHeight;

    public RPViewSwipeController(RefreshPullView view) {
        super(view);
    }

    @Override
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

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mChildHead != null) {
            mHeaderSrcPosition = -mChildHead.getMeasuredHeight();
        }
        if (mChildFoot != null) {
            mFooterSrcPosition = mView.getMeasuredHeight();
        }
    }

    @Override
    protected void setTargetOffset(int offsetY) {
        if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (mChildHead != null) {
                mView.invalidate();

                if (mChildHead.getVisibility() == View.GONE) {
                    mChildHead.setVisibility(View.VISIBLE);
                }

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

                if (mChildFoot.getVisibility() == View.GONE) {
                    mChildFoot.setVisibility(View.VISIBLE);
                }
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

    @Override
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

    @Override
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

    protected int getHeaderScrollUp(int dy) {
        int space = mHeaderSrcPosition - mChildHead.getTop();

        if (Math.abs(space) < dy) {
            return Math.abs(space);
        }

        return Math.max(dy, space);
    }

    protected int getFooterScrollDown(int dy) {
        int space = mFooterSrcPosition - mChildFoot.getTop();

        if (space < Math.abs(dy)) {
            return -space;
        }

        return Math.min(space, dy);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target,dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
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

    protected void footerViewStopAction() {
        mFooterScrolled = 0;
        if (mView.getMeasuredHeight() - mChildFoot.getBottom() >= 0) {
            setLoadingMore(true);
        } else {
            viewStartAnimator(mChildFoot, mFooterSrcPosition);
        }
    }

    protected void headerViewStopAction() {
        if (mChildHead.getTop() > 0) {
            setRefreshing(true);
        } else {
            viewStartAnimator(mChildHead, mHeaderSrcPosition);
        }
        mHeaderScrolled = 0;
    }
}
