package com.dezier.autoscroll.service

import android.accessibilityservice.AccessibilityService
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.dezier.autoscroll.ui.MarkerWindow
import com.dezier.autoscroll.ui.PanelWindow
import com.dezier.autoscroll.ui.SettingsWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AccessibilityService 入口。
 *
 * 只负责：
 *  1. Lifecycle / SavedState 基础设施
 *  2. 创建并协调 [com.dezier.autoscroll.ui.MarkerWindow] 与 [com.dezier.autoscroll.ui.PanelWindow]
 */
class MainService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private val _isAlive = MutableStateFlow(false)
        val isAlive: StateFlow<Boolean> = _isAlive

        /** 悬浮窗是否正在展示 */
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        /** 是否正在连续滚屏 */
        private val _isAutoScrolling = MutableStateFlow(false)
        val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling

        /** 服务运行时单例，供外部调用 */
        var instance: MainService? = null
            private set
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private lateinit var markerWindow: MarkerWindow
    private lateinit var panelWindow: PanelWindow
    private lateinit var settingsWindow: SettingsWindow
    private lateinit var scrollEngine: ScrollEngine

    override fun onCreate() {
        super.onCreate()
        instance = this
        _isAlive.value = true

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        scrollEngine = ScrollEngine(this)
        markerWindow = MarkerWindow(
            windowManager,
            this,
        )
        settingsWindow = SettingsWindow(
            windowManager,
            this,
            onClose = { settingsWindow.hide() }
        )
        panelWindow  = PanelWindow(
            windowManager,
            this,
            markerWindow,
            onClose = { stopWork() },
            onSettings = { settingsWindow.toggle() },
            onScrollUp = { scrollEngine.scrollUp(markerWindow.markPos.x, markerWindow.markPos.y) },
            onScrollDown = { scrollEngine.scrollDown(markerWindow.markPos.x, markerWindow.markPos.y) },
            onAutoScroll = { toggleAutoScroll() },
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 必须在 onServiceConnected 中 attach，因为 onCreate 时 accessibility token 还没准备好
        markerWindow.attach()
        panelWindow.attach()
        settingsWindow.attach()
        _isRunning.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isAlive.value = false
        _isRunning.value = false
        panelWindow.detach()
        markerWindow.detach()
        settingsWindow.detach()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    /** 展示悬浮窗，恢复运行状态 */
    fun startWork() {
        panelWindow.show()
        // markerWindow.show() // 不自动显示 marker，交由用户手动开启
        _isRunning.value = true
    }

    /** 隐藏悬浮窗，停止运行状态（不关闭 A11y Service） */
    fun stopWork() {
        panelWindow.hide()
        markerWindow.hide()
        settingsWindow.hide()
        stopAutoScroll()
        _isRunning.value = false
    }

    /** 切换连续滚屏状态 */
    private fun toggleAutoScroll() {
        if (scrollEngine.isAutoScrolling) {
            stopAutoScroll()
        } else {
            scrollEngine.startAutoScroll(markerWindow.markPos.x, markerWindow.markPos.y)
            _isAutoScrolling.value = true
        }
    }

    /** 停止连续滚屏 */
    private fun stopAutoScroll() {
        scrollEngine.stopAutoScroll()
        _isAutoScrolling.value = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
