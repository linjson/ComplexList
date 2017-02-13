package com.github.linjson.exlist;

import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ljs on 2017/2/13.
 */

public class RPViewMarkController extends RPViewController {
    private int mHeaderSrcHeight;
    private int mFooterSrcHeight;

    public RPViewMarkController(RefreshPullView view) {
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
        int top = mView.getPaddingTop() + (mViewOffsetHeader == 0 ? mViewOffsetFooter : mViewOffsetHeader);

        if (mChildBody != null) {
            mChildBody.layout(left, top, width - left - mView.getPaddingRight(), height + top);
        }
        int offset = 0;
        if (mChildHead != null) {

            if (mViewOffsetHeader > mHeaderSrcHeight) {
                offset = mViewOffsetHeader - mHeaderSrcHeight;
            }
            mChildHead.layout(0, mHeaderSrcPosition + offset, mChildHead.getMeasuredWidth(), mHeaderSrcPosition + mChildHead.getMeasuredHeight() + offset);
        }
        if (mChildFoot != null) {

            if (Math.abs(mViewOffsetFooter) > mFooterSrcHeight) {
                offset = mViewOffsetFooter + mFooterSrcHeight;
            }
            mChildFoot.layout(0, mFooterSrcPosition + offset, mChildFoot.getMeasuredWidth(), mFooterSrcPosition + mChildFoot.getMeasuredHeight() + offset);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mChildHead != null) {
            mHeaderSrcPosition = 0;
            mHeaderSrcHeight = mChildHead.getMeasuredHeight();
        }
        if (mChildFoot != null) {
            mFooterSrcHeight = mChildFoot.getMeasuredHeight();
            mFooterSrcPosition = mView.getMeasuredHeight() - mFooterSrcHeight;
        }
    }

    @Override
    protected void setTargetOffset(int offsetY) {
        if (mFlag == ViewCompat.SCROLL_INDICATOR_TOP) {
            if (mChildHead != null) {
                if (mChildHead.getVisibility() == View.GONE) {
                    mChildHead.setVisibility(View.VISIBLE);
                }

                mHeaderScrolled += offsetY;
                mChildBody.offsetTopAndBottom(offsetY);


                if (mChildBody.getTop() > mHeaderSrcHeight) {
                    mChildHead.setTop(mChildBody.getTop() - mHeaderSrcHeight);
                    mChildHead.setBottom(mChildBody.getTop());
                } else if (mChildHead.getTop() != mHeaderSrcPosition) {
                    mChildHead.setTop(mHeaderSrcPosition);
                    mChildHead.setBottom(mHeaderSrcHeight);
                }

                if (!mRefreshing) {
                    float rate = Math.min(1, mChildBody.getTop() * 1.0f / mChildHead.getMeasuredHeight());
                    getWrapViewExtension(mChildHead).setRate(rate);

                    if (mHeaderScrolled == 0) {
                        getWrapViewExtension(mChildHead).showPreView();
                    }

                }


            }
        } else {
            if (mChildFoot != null) {
                if (mChildFoot.getVisibility() == View.GONE) {
                    mChildFoot.setVisibility(View.VISIBLE);
                }
                mFooterScrolled += offsetY;
                mChildBody.offsetTopAndBottom(offsetY);


                if (mChildBody.getBottom() < mFooterSrcPosition) {
                    mChildFoot.setTop(mChildBody.getBottom());
                    mChildFoot.setBottom(mChildBody.getBottom() + mFooterSrcHeight);
                } else if (mChildFoot.getTop() != mFooterSrcPosition) {
                    mChildFoot.setTop(mFooterSrcPosition);
                    mChildFoot.setBottom(mFooterSrcPosition + mFooterSrcHeight);
                }


                if (!mLoadingMore && mLoadingMoreEnable) {
                    float rate = Math.min(1, mChildBody.getTop() * -1.0f / mChildFoot.getMeasuredHeight());
                    getWrapViewExtension(mChildFoot).setRate(rate);

                    if (mFooterScrolled == 0) {
                        getWrapViewExtension(mChildFoot).showPreView();
                    }
                }

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
            viewStartAnimator(mChildHead, 0, mHeaderSrcHeight);
        } else {
            mViewOffsetHeader = mChildBody.getTop();
            mViewOffsetFooter = 0;
            viewStartAnimator(mChildHead, mHeaderSrcPosition);


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
            viewStartAnimator(mChildFoot, mFooterSrcPosition, -mFooterSrcHeight);
        } else {
            mViewOffsetHeader = 0;
            mViewOffsetFooter = -mFooterSrcHeight + mChildBody.getBottom() - mFooterSrcPosition;
            viewStartAnimator(mChildFoot, mFooterSrcPosition);
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
        int space = mHeaderSrcPosition - mChildBody.getTop();

        if (Math.abs(space) < dy) {
            return Math.abs(space);
        }

        return Math.max(dy, space);
    }

    protected int getFooterScrollDown(int dy) {
        int space = mFooterSrcPosition + mFooterSrcHeight - mChildBody.getBottom();

        if (space < Math.abs(dy)) {
            return -space;
        }

        return Math.min(space, dy);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !childBodyCanScrollUP()) {
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
//            if (mChildHead.getTop() >= mHeaderSrcPosition) {
            if (childBodyCanScrollUP()) {
                if (dy > 0) {
                    consumed[1] = getHeaderScrollUp(dy);
                    setTargetOffset(-consumed[1]);
                }
            } else {
                if (dy < 0) {
                    consumed[1] = dy;
                    setTargetOffset(-consumed[1]);
                }
            }
//            }
        } else if (mLoadingMore) {
//            if (mChildFoot.getTop() <= mFooterSrcPosition) {
            if (childBodyCanScrollDown()) {
                if (dy < 0) {
                    consumed[1] = getFooterScrollDown(dy);
                    setTargetOffset(-consumed[1]);
                }
            } else {
                if (dy > 0) {
                    consumed[1] = dy;
                    setTargetOffset(-consumed[1]);
                }
            }
//            }
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
            if (mChildHead != null && !mLoadingMore && !childBodyCanScrollUP() && ((diff < 0 && startDrag) || mChildBody.getTop() > 0)) {
                mFlag = ViewCompat.SCROLL_INDICATOR_TOP;
            } else if (mChildFoot != null && !mRefreshing && !childBodyCanScrollDown() && ((diff > 0 && startDrag) || mChildBody.getBottom() <= mFooterSrcPosition)) {
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
        if (mView.getMeasuredHeight() - mChildBody.getBottom() > mFooterSrcHeight) {
            setLoadingMore(true);
        } else if (mChildBody.getTop() != 0) {
            viewStartAnimator(mChildFoot, mFooterSrcPosition);
        }
    }

    protected void headerViewStopAction() {
        if (mChildBody.getTop() > mHeaderSrcHeight) {
            setRefreshing(true);
        } else if (mChildBody.getTop() != 0) {
            viewStartAnimator(mChildHead, mHeaderSrcPosition);
        }
        mHeaderScrolled = 0;
    }
}
