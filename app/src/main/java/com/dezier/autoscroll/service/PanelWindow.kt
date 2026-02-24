package com.dezier.autoscroll.service

import android.app.Service
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * 管理悬浮控制面板的 overlay Window。
 *
 * 职责：创建 / 添加 / 更新 / 移除 Panel 的 ComposeView，
 * UI 逻辑由 [PanelOverlay] Composable 负责。
 */
class PanelWindow(
    private val windowManager: WindowManager,
    parent: MainService,
    markerWindow: MarkerWindow,
    onClose: () -> Unit,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
) {
    private val layout = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        x = 0
        y = 200
        gravity = Gravity.START or Gravity.TOP
    }

    private val view = ComposeView(parent as android.content.Context).apply {
        setViewTreeLifecycleOwner(parent)
        setViewTreeSavedStateRegistryOwner(parent)
        setContent {
            PanelOverlay(
                onDrag         = { dragAmount ->
                    layout.x += dragAmount.x.toInt()
                    layout.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(this, layout)
                },
                onToggleMarker = { markerWindow.toggleVisibility() },
                onScrollUp     = onScrollUp,
                onScrollDown   = onScrollDown,
                onClose        = onClose,
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
