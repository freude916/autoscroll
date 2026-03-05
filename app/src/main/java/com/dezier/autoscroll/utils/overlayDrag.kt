package com.dezier.autoscroll.utils

import android.view.View
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 使悬浮面板可拖动。
 */
fun Modifier.draggableHandler(
    view: View,
    onDrag: (x: Float, y: Float) -> Unit,
): Modifier = this.pointerInput(Unit) {
    var virtualX = 0f
    var virtualY = 0f

    detectDragGestures(
        onDragStart = {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            virtualX = location[0].toFloat()
            virtualY = location[1].toFloat()
        },
        onDrag = { change, dragAmount ->
            change.consume()

            virtualX += dragAmount.x
            virtualY += dragAmount.y

            onDrag(virtualX, virtualY)
        }
    )
}