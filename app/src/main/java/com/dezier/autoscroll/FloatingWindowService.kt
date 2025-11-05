package com.dezier.autoscroll

import android.graphics.PixelFormat
import android.util.Log
import android.view.WindowManager
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dezier.autoscroll.ui.theme.PanelButtonColor
import kotlin.math.roundToInt

fun Offset.toIntOffset(): IntOffset {
    return IntOffset(this.x.roundToInt(), this.y.roundToInt())
}

class FloatingWindowService : LifecycleService(), SavedStateRegistryOwner {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private lateinit var panelView: ComposeView
    private lateinit var panelLayout: WindowManager.LayoutParams
    private lateinit var displayView: ComposeView
    private lateinit var displayLayout: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        panelLayout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = 0
            y = 200
            gravity = android.view.Gravity.START or android.view.Gravity.TOP
        }

        panelView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingWindowService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)

            setContent {
                MyActionPanel(
                    panelLayout,
                    onDrag =  {offset ->
                        panelLayout.x = offset.x.toInt()
                        panelLayout.y = offset.y.toInt()
                        windowManager.updateViewLayout(panelView, panelLayout)
                    },
                    onClose = { stopSelf() }
                )
            }
        }

        windowManager.addView(panelView, panelLayout)


    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(panelView)
    }
}

@Composable
fun MyActionPanel(
    layoutParams: WindowManager.LayoutParams,
    onDrag: (Offset) -> Unit,
    onClose: () -> Unit
) {

    var offset =
        Offset(
            layoutParams.x.toFloat(),
            layoutParams.y.toFloat()
        )


    Box(
        modifier = Modifier
    ) {
        Card(
            modifier = Modifier
                .border(0.5.dp, Color(0xFF888888), shape = RoundedCornerShape(8.dp))
                .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp)),
            // give me a  gap between buttons
        ) {
            Column(modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 拖拽
                IconButton(
                    onClick = { /* only drag */ },
                    modifier = Modifier
                        .background(PanelButtonColor, shape = RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                                onDrag(offset)
                            }
                        }) {
                    Text("✥")
                }

                IconButton(
                    modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = { /* TODO: 在此添加逻辑 */ }) {
                    Text("+")
                }

                // TODO
                IconButton(modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = { /* TODO: 在此添加逻辑 */ }) {
                    Text("⋮")
                }

                // 关闭
                IconButton(modifier = Modifier.background(PanelButtonColor, shape = RoundedCornerShape(8.dp)),
                    onClick = onClose) {
                    Text("✕")

                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMyActionPanel() {
    MyActionPanel(
        layoutParams = WindowManager.LayoutParams(),
        onDrag = {},
        onClose = {}
    )
}