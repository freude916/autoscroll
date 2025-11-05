
package com.dezier.autoscroll

import android.content.Context
import androidx.core.content.edit

// 使用 object 创建一个单例来管理 SharedPreferences
object AppPreferences {

    private const val PREFS_NAME = "auto_scroll_prefs"
    private const val KEY_AUTO_LAUNCH_SERVICE = "auto_launch_service"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLaunchOnStartupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_LAUNCH_SERVICE, false)
    }

    fun setLaunchOnStartup(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_AUTO_LAUNCH_SERVICE, enabled) }
    }
}
