package com.dezier.autoscroll.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dezier.autoscroll.utils.draggableHandler

private val MarkerSize = 48.dp
private val MarkerColor = Color(0xCC1976D2)
private val MarkerBorderColor = Color(0xFFFFFFFF)

private val FingerShape = RoundedCornerShape(50, 10, 10, 50)

/**
 * 可拖拽标记点 UI。
 *
 *  - [onDrag] 回调新的绝对坐标（px）
 */
@Composable
fun MarkerOverlay(
    isVisible: Boolean,
    onDrag: (x: Float, y: Float) -> Unit,
) {

    val view = LocalView.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MarkerSize)
                .background(color = MarkerColor, shape = FingerShape)
                .border(width = 2.dp, color = MarkerBorderColor, shape = FingerShape)
                .draggableHandler(
                    view,
                    onDrag
                )
        ) {
            Text(
                text = "👈",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
