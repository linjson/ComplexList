
package com.ljs.complexlist;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import itemtouchhelperextension.ItemTouchCallback;
import itemtouchhelperextension.ItemTouchHelperExtension;


public class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT );
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        ItemTouchCallback adapter = (ItemTouchCallback) recyclerView.getAdapter();
        adapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

//    @Override
//    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        System.out.printf("==>%s \n",dX);
//        SwipeDragAdapter.ItemBaseViewHolder holder = (SwipeDragAdapter.ItemBaseViewHolder) viewHolder;
//        if (viewHolder instanceof SwipeDragAdapter.ItemSwipeWithActionWidthNoSpringViewHolder) {
////            if (dX < -holder.mActionContainer.getWidth()) {
////                dX = -holder.mActionContainer.getWidth();
////            }
//            holder.mViewContent.setTranslationX(dX);
//            return;
//        }
////        if (viewHolder instanceof SwipeDragAdapter.ItemBaseViewHolder)
//            holder.mViewContent.setTranslationX(dX);
//    }
}
