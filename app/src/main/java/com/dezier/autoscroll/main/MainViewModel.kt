package com.dezier.autoscroll.main

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dezier.autoscroll.prefs.AppPreferences
import com.dezier.autoscroll.service.MainService
import com.dezier.utils.ShellUtilClient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import rikka.shizuku.Shizuku


class MainViewModel(application: Application) : AndroidViewModel(application) {
    data class ViewModelState(
        val hasAccessibility: Boolean = false,
        val shizukuFound: Boolean = false,
        val shizukuAccess: Boolean = false,
        val autoLaunchService: Boolean = false,
        val isServiceAlive: Boolean = false,
        val isServiceRunning: Boolean = false,
    )

    var state by mutableStateOf(ViewModelState())
        private set

    val hasAccessibility get() = state.hasAccessibility
    val shizukuFound get() = state.shizukuFound
    val shizukuAccess get() = state.shizukuAccess
    val autoLaunchService get() = state.autoLaunchService
    val isServiceAlive get() = state.isServiceAlive

    val isServiceRunning get() = state.isServiceRunning

    val shellUtilClient = ShellUtilClient(application, application.packageName)

    init {
        MainService.isAlive
            .onEach { running -> state = state.copy(isServiceAlive = running) }
            .launchIn(viewModelScope)
        MainService.isRunning
            .onEach { running -> state = state.copy(isServiceRunning = running) }
            .launchIn(viewModelScope)
    }

    companion object {
        fun isShizukuFound(): Boolean {
            return try {
                Shizuku.pingBinder()
            } catch (e: Exception) {
                false
            }
        }

        fun hasShizukuAccess(): Boolean {
            return if (isShizukuFound() && Shizuku.isPreV11()) {
                false // 不支持旧版 Shizuku
            } else if (isShizukuFound()) {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val a11m = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = a11m.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }

    fun refresh(context: Context) {

        val shizukuFound = isShizukuFound()
        val shizukuAccess = hasShizukuAccess()

        if (shizukuAccess && !shellUtilClient.isBound) {
            shellUtilClient.bind()
        } else if (!shizukuAccess && shellUtilClient.isBound) {
            shellUtilClient.unbind()
        }

        state = state.copy(
            hasAccessibility = isAccessibilityServiceEnabled(context),
            shizukuFound = shizukuFound,
            shizukuAccess = shizukuAccess,
            autoLaunchService = AppPreferences.isLaunchOnStartupEnabled(context)
        )
    }

    fun setAutoLaunchService(context: Context, enabled: Boolean) {
        AppPreferences.setLaunchOnStartup(context, enabled)
        state = state.copy(autoLaunchService = enabled)
    }

    fun startWork() {
        MainService.instance?.startWork()
    }

    fun stopWork() {
        MainService.instance?.stopWork()
    }
}
