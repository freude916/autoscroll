package com.dezier.autoscroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dezier.autoscroll.ui.theme.PanelButtonColor
import com.dezier.autoscroll.utils.draggableHandler

/**
 * 悬浮控制面板 UI。
 *
 * @param onDrag         拖拽回调，传递屏幕绝对坐标 (rawX, rawY)
 * @param onToggleMarker 显示 / 隐藏标记点
 * @param onSettings     打开设置面板
 * @param onScrollUp     单次上滚
 * @param onScrollDown   单次下滚
 * @param onAutoScroll   连续滚屏开关
 * @param isAutoScrolling 是否正在连续滚屏
 * @param onClose        关闭服务
 */
@Composable
fun PanelOverlay(
    onDrag: (x: Float, y: Float) -> Unit,
    onToggleMarker: () -> Unit,
    onSettings: () -> Unit = {},
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onAutoScroll: () -> Unit,
    isAutoScrolling: Boolean = false,
    onClose: () -> Unit,
) {

    val view = LocalView.current

    Box (
        modifier = Modifier.draggableHandler(view, onDrag)
    ){
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
//                IconButton(
//                    onClick = {},
//                    modifier = Modifier
//                        .background(PanelButtonColor, shape = RoundedCornerShape(8.dp))
//                        .draggableHandler(
//                            view,
//                            onDrag,
//                        )
//                ) { Text("✥") }

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

                // 连续滚屏
                IconButton(
                    modifier = Modifier.background(
                        if (isAutoScrolling) Color(0xFF4CAF50) else PanelButtonColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                    onClick = onAutoScroll
                ) { Text(if (isAutoScrolling) "⏸️" else "▶") }

                // 单次下滚
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onScrollDown
                ) { Text("↓") }



                // 设置
                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onSettings
                ) { Text("⚙") }

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
        onDrag         = {_, _ -> },
        onToggleMarker = {},
        onSettings = {},
        onScrollUp = {},
        onScrollDown = {},
        onAutoScroll = {},
        isAutoScrolling = false,
        onClose = {}
    )
}
