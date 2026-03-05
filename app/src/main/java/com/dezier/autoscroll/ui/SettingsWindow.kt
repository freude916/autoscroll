package com.dezier.autoscroll.ui

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dezier.autoscroll.service.MainService

/**
 * 管理设置悬浮面板的 overlay Window。
 * UI 状态由 SettingsOverlay 自行从 AppPreferences 读写。
 */
class SettingsWindow(
    private val windowManager: WindowManager,
    parent: MainService,
    private val onClose: () -> Unit,
) {
    private val layout = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        x = 200
        y = 200
        gravity = Gravity.START or Gravity.TOP
    }

    private val view = ComposeView(parent as Context).apply {
        setViewTreeLifecycleOwner(parent)
        setViewTreeSavedStateRegistryOwner(parent)
        setContent {
            SettingsOverlay(onClose = onClose,
                onDrag = { x, y ->
                    layout.x = x.toInt()
                    layout.y = y.toInt()
                    windowManager.updateViewLayout(this, layout)
                }
                )
        }
    }

    fun attach() {
        windowManager.addView(view, layout)
        view.visibility = View.GONE // 默认隐藏
    }

    fun detach() {
        windowManager.removeView(view)
    }

    fun show() { view.visibility = View.VISIBLE }
    fun hide() { view.visibility = View.GONE }

    fun toggle() {
        if (view.visibility == View.VISIBLE) hide() else show()
    }

    val isVisible: Boolean
        get() = view.visibility == View.VISIBLE
}