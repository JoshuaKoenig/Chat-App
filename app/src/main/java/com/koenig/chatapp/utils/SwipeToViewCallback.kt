package com.koenig.chatapp.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.R


abstract class SwipeToViewCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
{
    private val viewIcon = ContextCompat.getDrawable(context, R.drawable.ic_info)
    private val intrinsicWidth = viewIcon?.intrinsicWidth
    private val intrinsicHeight = viewIcon?.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#8682ff")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)}

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if(isCanceled)
        {
            clearCanvas(c, itemView.left +dX, itemView.top.toFloat(), itemView.left.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        background.color = backgroundColor
        background.setBounds(itemView.left + dX.toInt(), itemView.top, itemView.left, itemView.bottom)
        background.draw(c)

        val viewIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
        val viewIconMargin = (itemHeight - intrinsicHeight) / 2
        val viewIconLeft = itemView.right - viewIconMargin - intrinsicWidth!! - 810
        val viewIconRight = itemView.right - viewIconMargin - 810
        val viewIconBottom = viewIconTop + intrinsicHeight

        viewIcon?.setBounds(viewIconLeft, viewIconTop, viewIconRight, viewIconBottom)
        viewIcon?.draw(c)


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}