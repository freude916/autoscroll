package com.dezier.utils

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.shared.BuildConfig

class ShellUtilClient(private val context: Context, private val packageName: String) {

    companion object {
        private const val TAG = "ShellUtilClient"
    }

    private val shellUtilServiceArgs = Shizuku.UserServiceArgs(ComponentName(packageName, ShellUtilService::class.java.name)).apply {
        daemon(false)
        processNameSuffix(":shizuku_service")
        debuggable(BuildConfig.DEBUG)
        version(1)
    }

    private var shellUtilService: IShellUtilService? = null
    var isBound: Boolean = false
        private set

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.i(TAG, "ShellUtilService connected.")
            if (service.isBinderAlive) {

                shellUtilService = IShellUtilService.Stub.asInterface(service)
                isBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.w(TAG, "ShellUtilService disconnected.")
            shellUtilService = null
            isBound = false
        }
    }

    fun bind() {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 10) {
            Log.e(TAG, "Shizuku version is too low.")
            return
        }

        if (!Shizuku.checkSelfPermission().equals(android.content.pm.PackageManager.PERMISSION_GRANTED)) {
            Log.e(TAG, "Shizuku permission not granted.")
            return
        }

        if (isBound) return
        Shizuku.bindUserService(shellUtilServiceArgs, serviceConnection)
    }

    fun unbind() {
        if (!isBound) return
        Shizuku.unbindUserService(shellUtilServiceArgs, serviceConnection, true)
        isBound = false
        shellUtilService = null
    }
    
    suspend fun grantOverlayPermission(packageName: String, uid: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val service = shellUtilService?: return@withContext Result.failure(IllegalStateException("service not connected"))
        try {
            Log.d(TAG, "Calling grantOverlayPermission...")
            val rv = service.grantOverlayPermission(packageName, uid)

            delay(500) // 等待系统设置生效
            if (rv != 0){
                Result.failure(RuntimeException())
            }else{
                Result.success(Unit)
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to grant overlay permission via Shizuku", e)
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun grantAccessibilityPermission(accessibilityServiceComponent: String): Result<Unit> = withContext(Dispatchers.IO) {
        val service = shellUtilService?: return@withContext Result.failure(IllegalStateException("service not connected"))
        try {
            Log.d(TAG, "Calling grantAccessibilityPermission...")
            val rv = service.grantAccessibilityPermission(accessibilityServiceComponent)

            delay(500) // 等待系统设置生效

            if (rv != 0){
                Result.failure(RuntimeException())
            }else{
                Result.success(Unit)
            }

            Result.success(Unit)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to grant accessibility permission via Shizuku", e)
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}