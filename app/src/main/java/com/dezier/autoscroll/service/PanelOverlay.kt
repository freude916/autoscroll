package com.dezier.autoscroll.service

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dezier.autoscroll.ui.theme.PanelButtonColor

/**
 * 悬浮控制面板 UI。
 *
 * @param onDrag         拖拽手柄时的位移回调
 * @param onToggleMarker 显示 / 隐藏标记点
 * @param onScrollUp     单次上滚（内容向下，手指向下划）
 * @param onScrollDown   单次下滚（内容向上，手指向上划）
 * @param onClose        关闭服务
 */
@Composable
fun PanelOverlay(
    onDrag: (Offset) -> Unit,
    onToggleMarker: () -> Unit,
    onScrollSpeed: () -> Unit = {},
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onClose: () -> Unit,
) {
    Box {
        Card(
            modifier = Modifier
                .border(0.5.dp, Color(0xFF888888), shape = RoundedCornerShape(8.dp))
                .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 拖拽手柄
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .background(PanelButtonColor, shape = RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            }
                        }
                ) { Text("✥") }

                // 显示 / 隐藏标记点
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onToggleMarker
                ) { Text("◎") }

                // 单次上滚
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onScrollUp
                ) { Text("↑") }

                // 单次下滚
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onScrollDown
                ) { Text("↓") }

                // 关闭服务
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onClose
                ) { Text("✕") }
            }
        }
    }
}

@Preview
@Composable
fun PreviewActionPanel() {
    PanelOverlay(
        onDrag         = {},
        onToggleMarker = {},
        onScrollUp     = {},
        onScrollDown   = {},
        onClose        = {}
    )
}
