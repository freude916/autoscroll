package com.dezier.autoscroll.ui

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dezier.autoscroll.service.MainService

/**
 * 管理悬浮控制面板的 overlay Window。
 */
class PanelWindow(
    private val windowManager: WindowManager,
    parent: MainService,
    markerWindow: MarkerWindow,
    onClose: () -> Unit,
    onSettings: () -> Unit,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onAutoScroll: () -> Unit,
) {
    private val layout = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        x = 0
        y = 200
        gravity = Gravity.START or Gravity.TOP
    }


    private val view = ComposeView(parent as Context).apply {
        setViewTreeLifecycleOwner(parent)
        setViewTreeSavedStateRegistryOwner(parent)

        setContent {
            val isAutoScrolling by MainService.isAutoScrolling.collectAsState()
            PanelOverlay(
                onDrag         = { x, y ->
                    layout.x = x.toInt()
                    layout.y = y.toInt()
                    windowManager.updateViewLayout(this, layout)
                },
                onToggleMarker = { markerWindow.toggleVisibility() },
                onSettings = onSettings,
                onScrollUp = onScrollUp,
                onScrollDown = onScrollDown,
                onAutoScroll = onAutoScroll,
                isAutoScrolling = isAutoScrolling,
                onClose = onClose,
            )
        }
    }

    fun attach() {
        windowManager.addView(view, layout)
    }

    fun detach() {
        windowManager.removeView(view)
    }

    fun show() { view.visibility = View.VISIBLE }
    fun hide() { view.visibility = View.GONE }
}