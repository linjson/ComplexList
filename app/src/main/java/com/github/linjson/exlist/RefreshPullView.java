package com.github.linjson.exlist;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import example.wrapview.RPViewFooter;
import example.wrapview.RPViewHeader;

/**
 * Created by ljs on 16/9/20.
 */
public class RefreshPullView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    public static final int SWIPE = 1;
    public static final int MARK = 2;

    private RPViewController mRPViewController;


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

        mRPViewController = new RPViewMarkController(this);
//        mRPViewController=new RPViewSwipeController(this);

    }

    public void setRPViewController(int type) {
        if (type == SWIPE) {
            RPViewSwipeController temp = new RPViewSwipeController(this);
            temp.copy(mRPViewController);
            mRPViewController = temp;
        } else {
            RPViewMarkController temp = new RPViewMarkController(this);
            temp.copy(mRPViewController);
            mRPViewController = temp;
        }

        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mRPViewController.onLayout();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        mRPViewController.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }


    @Override
    public void addView(View child, LayoutParams params) {
        mRPViewController.addView(child, params);
    }


    public void setRefreshing(boolean open) {
        mRPViewController.setRefreshing(open);
    }

    public void setLoadingMore(boolean open) {
        mRPViewController.setLoadingMore(open);
    }


    public void setOnLoadingMoreListener(OnLoadingMoreListener onLoadingMoreListener) {
        mRPViewController.setOnLoadingMoreListener(onLoadingMoreListener);

    }

    public void setOnRefreshingListener(OnRefreshingListener onRefreshingListener) {
        mRPViewController.setOnRefreshingListener(onRefreshingListener);

    }

    //NestedScrollingParent begin

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mRPViewController.onNestedScrollAccepted(child, target, nestedScrollAxes);
//        System.out.printf("==>onNestedScrollAccepted \n");
    }

    @Override
    public void onStopNestedScroll(View target) {

//        System.out.printf("==>onStopNestedScroll,%s,%s \n", mHeaderScrolled, mFlag);

        mRPViewController.onStopNestedScroll(target);


    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        System.out.printf("==>onNestedScroll,dyConsumed:%s,dyUnconsumed:%s \n", dyConsumed, dyUnconsumed);

        mRPViewController.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);


    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
//        System.out.printf("==>onNestedPreScroll,dx:%s,dy:%s,[0]:%s,[1]:%s \n", dx, dy, consumed[0], consumed[1]);

//
        mRPViewController.onNestedPreScroll(target, dx, dy, consumed);


    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
//        System.out.printf("==>onNestedFling \n");
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        System.out.printf("==>onNestedPreFling \n");

        return mRPViewController.onNestedPreFling(target, velocityX, velocityY);

    }

    @Override
    public int getNestedScrollAxes() {
//        System.out.printf("==>getNestedScrollAxes,%s \n", mNestedScrollingParentHelper.getNestedScrollAxes());
        return mRPViewController.getNestedScrollAxes();

    }

    //NestedScrollingParent end

    //NestedScrollingChild begin

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mRPViewController.setNestedScrollingEnabled(enabled);

    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mRPViewController.isNestedScrollingEnabled();

    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mRPViewController.startNestedScroll(axes);

    }

    @Override
    public void stopNestedScroll() {
        mRPViewController.stopNestedScroll();

    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mRPViewController.hasNestedScrollingParent();

    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mRPViewController.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);

    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mRPViewController.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);

    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mRPViewController.dispatchNestedFling(velocityX, velocityY, consumed);

    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mRPViewController.dispatchNestedPreFling(velocityX, velocityY);

    }

    //NestedScrollingChild end


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        System.out.printf("==>onInterceptTouchEvent \n");

        if (mRPViewController.isNestScroll()) {
            return super.onInterceptTouchEvent(ev);
        }

        return mRPViewController.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {


        boolean result = super.dispatchTouchEvent(ev);

        if (mRPViewController.allowDispatchTouch()) {
            mRPViewController.dispatchTouchEvent(ev);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mRPViewController.isNestScroll()) {
            return super.onTouchEvent(ev);
        }
        return mRPViewController.onTouchEvent(ev);

    }


    public void stopLoadingMore() {
        mRPViewController.stopLoadingMore();

    }

    public void setLoadingMoreEnable(boolean enable) {
        mRPViewController.setLoadingMoreEnable(enable);

    }

    public void setHeaderView(RPViewHeader head) {
        mRPViewController.setHeaderView(head);
    }

    public void setFooterView(RPViewFooter footer) {
        mRPViewController.setFooterView(footer);
    }


    public interface OnRefreshingListener {
        void doRefreshingData(RefreshPullView view);
    }

    public interface OnLoadingMoreListener {
        void doLoadingMoreData(RefreshPullView view);
    }


}
