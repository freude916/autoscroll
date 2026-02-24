package com.dezier.autoscroll.service

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dezier.autoscroll.prefs.AppPreferences

/**
 * 管理标记点的状态（可见性）以及它对应的 overlay Window。
 *
 * 职责边界：
 *  - 状态：isVisible（Compose State，驱动 UI 重组）
 *  - Window：添加 / 移除 / 更新触摸拦截标志 / 通过 updateViewLayout 移动位置
 *
 * 与 PanelWindow 保持一致：坐标由 layout.x/y 维护，
 * 拖拽时由 MarkerOverlay 回调 onDrag，Window 层调用 updateViewLayout 移动 overlay。
 */
class MarkerWindow(
    private val windowManager: WindowManager,
    parent: MainService,
) {

    // MarkerOverlay 圆形直径 48dp，半径 24dp → px，用于换算圆心坐标
    private val markerRadiusPx: Float = 24f * parent.resources.displayMetrics.density

    // ── State ────────────────────────────────────────────────────────────────

    /** 是否可见；可见时 overlay 拦截触摸，隐藏时透传 */
    var isVisible: Boolean by mutableStateOf(false)
        private set

    // ── Window ───────────────────────────────────────────────────────────────

    val initialPosition: Pair<Int, Int> = AppPreferences.getMarkerPosition(parent)

    private val layout = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        x = initialPosition.first
        y = initialPosition.second
        gravity = Gravity.START or Gravity.TOP
    }

    private val view = ComposeView(parent).apply {
        setViewTreeLifecycleOwner(parent)
        setViewTreeSavedStateRegistryOwner(parent)
        setContent {
            MarkerOverlay(
                isVisible = isVisible,
                onDrag = { dragAmount ->
                    layout.x += dragAmount.x.toInt()
                    layout.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(this, layout)
                    AppPreferences.setMarkerPosition(parent, Pair(layout.x, layout.y))
                }
            )
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * 标记点圆心的屏幕绝对坐标（px）。
     * layout.x/y 是窗口左上角（Gravity.START|TOP），加半径得圆心。
     */
    val markPos: Offset
        get() {
            val loc = IntArray(2)
            view.getLocationOnScreen(loc) // 相对于屏幕左上角
            val cx = loc[0] - 1f // 避开一点，防止 marker 自己被拽跑了
            val cy = loc[1] + view.height / 2f
            return Offset(cx, cy)
        }

    fun attach() {
        windowManager.addView(view, layout)
    }

    fun detach() {
        windowManager.removeView(view)
    }

    fun toggleVisibility() {
        isVisible = !isVisible
        syncTouchFlag()
    }

    fun show() {
        // 只是停工的时候需要 hide ，目前好像不需要 show
        isVisible = true
        syncTouchFlag()
    }

    fun hide() {
        isVisible = false
        syncTouchFlag()
    }

    // ── Private ──────────────────────────────────────────────────────────────

    /** 可见时 overlay 接收触摸，隐藏时透传 */
    private fun syncTouchFlag() {
        layout.flags = if (isVisible) {
            layout.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        } else {
            layout.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        windowManager.updateViewLayout(view, layout)
    }
}
