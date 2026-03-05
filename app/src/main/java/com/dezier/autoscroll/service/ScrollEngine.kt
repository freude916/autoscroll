package com.dezier.autoscroll.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import com.dezier.autoscroll.prefs.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 负责通过 a11y dispatchGesture 执行单次滚屏。
 *
 * 距离与耗时从 [AppPreferences] 实时读取，修改 prefs 即时生效，无需重启服务。
 *
 * 手势方向约定：
 *   - 下滚（内容向上）：手指从 startY 向上划，endY = startY - distance
 *   - 上滚（内容向下）：手指从 startY 向下划，endY = startY + distance
 */
class ScrollEngine(private val service: AccessibilityService) {

    private val tag = "ScrollEngine"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var autoScrollJob: Job? = null

    /** 是否正在连续滚屏 */
    val isAutoScrolling: Boolean
        get() = autoScrollJob?.isActive == true

    /** 向下滚一次（内容上移，手指上划）。*/
    fun scrollDown(startX: Float, startY: Float) = dispatch(startX, startY, direction = -1)

    /** 向上滚一次（内容下移，手指下划）。*/
    fun scrollUp(startX: Float, startY: Float) = dispatch(startX, startY, direction = +1)

    /** 开始连续滚屏（方向由偏好设置决定） */
    fun startAutoScroll(startX: Float, startY: Float) {
        if (autoScrollJob?.isActive == true) return
        
        autoScrollJob = scope.launch {
            while (isActive) {
                val directionDown = AppPreferences.isAutoScrollDirectionDown(service)
                if (directionDown) {
                    scrollDown(startX, startY)
                } else {
                    scrollUp(startX, startY)
                }
                delay(AppPreferences.getAutoScrollInterval(service))
            }
        }
    }

    /** 停止连续滚屏 */
    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    // direction: -1 = 手指上划(内容下滚), +1 = 手指下划(内容上滚)
    private fun dispatch(startX: Float, startY: Float, direction: Int) {
        val distance = AppPreferences.getScrollDistance(service)
        val duration = AppPreferences.getScrollDuration(service)
        val endY = startY + direction * distance

        val path1 = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }
        val stroke1 = GestureDescription.StrokeDescription(path1, 0L, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke1)
            .build()



        val ok = service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            /*override fun onCompleted(g: GestureDescription) {
                // Log.d(tag, "gesture ok  dir=$direction  dist=$distance  dur=${duration}ms")
            }*/
            /*override fun onCancelled(g: GestureDescription) {
                // Log.w(tag, "gesture cancelled")
            }*/
        }, null)

        if (!ok) Log.e(tag, "dispatchGesture rejected by system")
    }
}

