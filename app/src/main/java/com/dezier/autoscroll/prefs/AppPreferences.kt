package com.dezier.autoscroll.prefs

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.core.content.edit

// 使用 object 创建一个单例来管理 SharedPreferences
object AppPreferences {

    private const val PREFS_NAME = "auto_scroll_prefs"
    private const val KEY_AUTO_LAUNCH_SERVICE = "auto_launch_service"

    /** 每次滚屏的像素距离，默认 800 px */
    const val KEY_SCROLL_DISTANCE = "scroll_distance"
    const val DEFAULT_SCROLL_DISTANCE = 400

    /** 每次滚屏手势的持续时间（毫秒），默认 50 ms */
    const val KEY_SCROLL_DURATION = "scroll_duration"
    const val DEFAULT_SCROLL_DURATION = 100L

    const val KEY_MARKER_POS_X = "marker_pos_x"

    const val DEFAULT_MARKER_POS_X = 200

    const val KEY_MARKER_POS_Y = "marker_pos_y"

    const val DEFAULT_MARKER_POS_Y = 200

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLaunchOnStartupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_LAUNCH_SERVICE, false)
    }

    fun setLaunchOnStartup(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_AUTO_LAUNCH_SERVICE, enabled) }
    }

    fun getScrollDistance(context: Context): Int =
        getPrefs(context).getInt(KEY_SCROLL_DISTANCE, DEFAULT_SCROLL_DISTANCE)

    fun setScrollDistance(context: Context, px: Int) {
        getPrefs(context).edit { putInt(KEY_SCROLL_DISTANCE, px) }
    }

    fun getScrollDuration(context: Context): Long =
        getPrefs(context).getLong(KEY_SCROLL_DURATION, DEFAULT_SCROLL_DURATION)

    fun setScrollDuration(context: Context, ms: Long) {
        getPrefs(context).edit { putLong(KEY_SCROLL_DURATION, ms) }
    }

    fun getMarkerPosition(context: Context): Pair<Int, Int> {
        val x = getPrefs(context).getInt(KEY_MARKER_POS_X, DEFAULT_MARKER_POS_X)
        val y = getPrefs(context).getInt(KEY_MARKER_POS_Y, DEFAULT_MARKER_POS_Y)
        return Pair(x, y)
    }

    fun setMarkerPosition(context: Context, pos: Pair<Int, Int>) {
        getPrefs(context).edit {
            putInt(KEY_MARKER_POS_X, pos.first)
            putInt(KEY_MARKER_POS_Y, pos.second)
        }
    }
}