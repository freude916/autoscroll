package com.dezier.autoscroll.service

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MarkerSize = 48.dp
private val MarkerColor = Color(0xCC1976D2)
private val MarkerBorderColor = Color(0xFFFFFFFF)

private val FingerShape = RoundedCornerShape(50, 10, 10, 50)

/**
 * 可拖拽标记点 UI。
 *
 * 与 [PanelOverlay] 保持一致：
 *  - [layoutParams] 提供初始坐标（x/y）
 *  - [onDrag] 回调新的绝对坐标（px），由 [MarkerWindow] 调用 updateViewLayout 移动 overlay
 *  - [isVisible] 控制显示 / 隐藏（淡入淡出）
 */
@Composable
fun MarkerOverlay(
    isVisible: Boolean,
    onDrag: (Offset) -> Unit,
) {

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
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                }
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
