package com.dezier.utils

import android.annotation.SuppressLint
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import rikka.shizuku.SystemServiceHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import kotlin.system.exitProcess

class ShellUtilService : IShellUtilService.Stub() {

    companion object {
        private const val TAG = "ShizukuUserService"
        private const val OP_SYSTEM_ALERT_WINDOW = 24
        private const val MODE_ALLOWED = 0

        // IContentProvider.get/putStringForUser 的底层 transaction code
        // 从 Android 源码中的 IContentProvider.aidl 或 ContentProviderNative.java 可查到
        private const val CALL_TRANSACTION = 21
        private const val GET_STRING_FOR_USER_TRANSACTION = 24
        private const val PUT_STRING_FOR_USER_TRANSACTION = 25
    }

    override fun exit(){
        exitProcess(0)
    }

    @SuppressLint("PrivateApi")
    override fun grantOverlayPermission(packageName: String, uid: Int): Int {
        Log.i(TAG, "Attempting to grant overlay permission for $packageName (uid: $uid)")
        try {
            // 1. 直接获取原始的、已通过 Shizuku 代理的 Binder
            val appOpsBinder = SystemServiceHelper.getSystemService("appops")
            if (appOpsBinder == null) {
                Log.e(TAG, "Failed to get appops service binder.")
                throw RuntimeException("Failed to get appops service binder.")
            }

            // 2. 通过反射获取 asInterface 方法，将原始 Binder 转换为 IAppOpsService 接口
            val appOpsService = Class.forName("com.android.internal.app.IAppOpsService\$Stub")
                .getMethod("asInterface", IBinder::class.java)
                .invoke(null, appOpsBinder) // <-- 关键修正：传入原始 binder

            // 3. 调用 setMode 方法
            val setModeMethod: Method = appOpsService::class.java.getMethod(
                "setMode", Int::class.java, Int::class.java, String::class.java, Int::class.java
            )
            setModeMethod.invoke(appOpsService, OP_SYSTEM_ALERT_WINDOW, uid, packageName, MODE_ALLOWED)

            Log.i(TAG, "Overlay permission granted successfully for $packageName")

            return 0
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to grant overlay permission", e)
            throw e
        }
    }

    override fun grantAccessibilityPermission(componentName: String): Int {
        Log.i(TAG, "Attempting to grant accessibility permission for $componentName using shell command.")
        try {
            // 1. 读取当前值
            val commandGet = "settings get secure ${Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES}"
            val oldValue = executeShellCommand(commandGet)?.trim()
            Log.d(TAG, "Current ENABLED_ACCESSIBILITY_SERVICES: $oldValue")

            if (oldValue?.split(':')?.contains(componentName) == true) {
                Log.i(TAG, "Accessibility service already enabled.")
                return 0 // anyway, enabled is good
            }

            // 2. 构造新值
            val newValue = if (oldValue.isNullOrEmpty() || oldValue == "null") {
                componentName
            } else {
                "$oldValue:$componentName"
            }

            // 3. 写入新值
            val commandPut = "settings put secure ${Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES} $newValue"
            executeShellCommand(commandPut)


            // 4. [验证] 再次读取值
            val finalValue = executeShellCommand(commandGet)?.trim()
            Log.d(TAG, "Value after setting: $finalValue")
            if (finalValue?.contains(componentName) == true) {
                Log.i(TAG, "Verification successful: Accessibility setting applied.")
                return 0
            } else {
                Log.w(TAG, "Verification failed: System did not apply the accessibility setting. Final value is '$finalValue'.")
                throw RuntimeException("Accessibility setting applied failed.")
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Failed to grant accessibility permission using shell command", e)
            throw e // throw back to client via Shizuku channel, anyway, good for debugging
        }
    }

    /**
     * 执行一条 shell 命令并返回其标准输出。
     */
    private fun executeShellCommand(command: String): String? {
        return try {
            Log.d(TAG, "Shell command executed: $command")
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            Log.e(TAG, "Error executing shell command: $command", e)
            null
        }
    }

    override fun destroy() {
        Log.i(TAG, "destroy")
        System.exit(0)
    }
}