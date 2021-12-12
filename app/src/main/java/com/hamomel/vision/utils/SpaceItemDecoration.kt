package com.hamomel.vision.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Роман Зотов on 12.12.2021
 *
 * This item decoration adds specified space between items.
 * It will work only with [LinearLayoutManager] if it is used with other layout manager, this decoration will do nothing.
 */
class SpaceItemDecoration(
    private val spaceSize: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val orientation = (parent.layoutManager as? LinearLayoutManager)?.orientation ?: return

        if (itemPosition != 0) {
            if (orientation == RecyclerView.VERTICAL) {
                outRect.top = spaceSize
            } else {
                outRect.right = spaceSize
            }
        }
    }
}
