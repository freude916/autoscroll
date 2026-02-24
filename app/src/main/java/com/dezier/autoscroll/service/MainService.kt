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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AccessibilityService 入口。
 *
 * 只负责：
 *  1. Lifecycle / SavedState 基础设施
 *  2. 创建并协调 [MarkerWindow] 与 [PanelWindow]
 */
class MainService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private val _isAlive = MutableStateFlow(false)
        val isAlive: StateFlow<Boolean> = _isAlive

        /** 悬浮窗是否正在展示（与 Service 是否存活解耦）。未来滚屏服务监听此值自动停止。 */
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

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
        panelWindow  = PanelWindow(
            windowManager,
            this,
            markerWindow,
            onClose         = { stopWork() },
            onScrollUp      = { scrollEngine.scrollUp(markerWindow.markPos.x, markerWindow.markPos.y) },
            onScrollDown    = { scrollEngine.scrollDown(markerWindow.markPos.x, markerWindow.markPos.y) },
        )

        markerWindow.attach()
        panelWindow.attach()
        _isRunning.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isAlive.value = false
        _isRunning.value = false
        panelWindow.detach()
        markerWindow.detach()
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
        _isRunning.value = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
