package com.dezier.autoscroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dezier.autoscroll.prefs.AppPreferences
import com.dezier.autoscroll.utils.draggableHandler

/**
 * 设置悬浮面板 UI。
 * 直接从 AppPreferences 读写，避免中间层状态同步问题。
 *
 * @param onClose 关闭设置面板
 */
@Composable
fun SettingsOverlay(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val view = LocalView.current

    // 直接从 Prefs 读取并写入
    var scrollDistance by remember { mutableStateOf(AppPreferences.getScrollDistance(context)) }
    var scrollDuration by remember { mutableStateOf(AppPreferences.getScrollDuration(context)) }
    var scrollInterval by remember { mutableStateOf(AppPreferences.getAutoScrollInterval(context)) }
    var directionDown by remember { mutableStateOf(AppPreferences.isAutoScrollDirectionDown(context)) }

    Column(
        modifier = Modifier
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFF888888), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)

        ,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.width(200.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .draggableHandler(view, onDrag),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("设置", color = Color.Black, modifier = Modifier.padding(start = 8.dp))
            IconButton(
                modifier = Modifier.background(Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)),
                onClick = onClose
            ) { Text("✕", color = Color.Black) }
        }

        // 滚动距离
        Column {
            Row(
                modifier = Modifier.width(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("滚动距离", color = Color.DarkGray)
                Text("$scrollDistance px", color = Color.DarkGray)
            }
            Slider(
                value = scrollDistance.toFloat(),
                onValueChange = {
                    scrollDistance = it.toInt()
                    AppPreferences.setScrollDistance(context, it.toInt())
                },
                valueRange = 100f..600f,
                modifier = Modifier.width(200.dp),
            )
        }

        // 滚动时长
        Column {
            Row(
                modifier = Modifier.width(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("滚动时长", color = Color.DarkGray)
                Text("$scrollDuration ms", color = Color.DarkGray)
            }
            Slider(
                value = scrollDuration.toFloat(),
                onValueChange = {
                    scrollDuration = it.toLong()
                    AppPreferences.setScrollDuration(context, it.toLong())
                },
                valueRange = 10f..100f,
                modifier = Modifier.width(200.dp)
            )
        }

        // 滚动间隔
        Column {
            Row(
                modifier = Modifier.width(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("滚动间隔", color = Color.DarkGray)
                Text("$scrollInterval ms", color = Color.DarkGray)
            }
            Slider(
                value = scrollInterval.toFloat(),
                onValueChange = {
                    scrollInterval = it.toLong()
                    AppPreferences.setAutoScrollInterval(context, it.toLong())
                },
                valueRange = 50f..500f,
                modifier = Modifier.width(200.dp)
            )
        }

        // 滚动方向
        Row(
            modifier = Modifier.width(200.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("滚动方向", color = Color.DarkGray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("↑上", color = if (!directionDown) Color(0xFF4CAF50) else Color.Gray)
                Switch(
                    checked = directionDown,
                    onCheckedChange = {
                        directionDown = it
                        AppPreferences.setAutoScrollDirectionDown(context, it)
                    }
                )
                Text("↓下", color = if (directionDown) Color(0xFF4CAF50) else Color.Gray)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingsOverlay() {
    SettingsOverlay(onClose = {})
}