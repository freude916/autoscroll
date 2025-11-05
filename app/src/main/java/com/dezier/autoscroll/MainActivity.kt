package com.dezier.autoscroll

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dezier.autoscroll.ui.theme.AutoScrollTheme
import com.dezier.autoscroll.ui.theme.ShizukuSectionPurple
import com.dezier.utils.ShellUtilClient
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku



fun startMainService(context: Context) {
    val intent = Intent(context, FloatingWindowService::class.java)
    context.startService(intent)
}

fun requestPermission(
    context: Context, permissionAction: String
) {
    val intent = Intent(permissionAction)
    when (permissionAction) {
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION -> {
            intent.data = "package:${context.packageName}".toUri()
        }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun PermissionRow(
    requestString: String, successString: String, isGranted: Boolean, onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(64.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = if (!isGranted) requestString else successString,
             fontWeight = FontWeight.Medium)
        if (!isGranted) {
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onGrant) {
                Text(text = "申请权限")
            }
        }
    }
}

suspend fun grantShizukuAppops(context: Context, shellUtilClient: ShellUtilClient) {
    if (!shellUtilClient.isBound) {
        Toast.makeText(context, "Shizuku not connected.", Toast.LENGTH_SHORT).show()
        return
    }

    val component = ComponentName(context.packageName, MyAccessibilityService::class.java.name)

    val r1 = shellUtilClient.grantOverlayPermission(context.packageName, context.applicationInfo.uid)

    val r2 = shellUtilClient.grantAccessibilityPermission(component.flattenToString())

    if (r1.isSuccess && r2.isSuccess) {
        Toast.makeText(context, "Shizuku AppOps ✅", Toast.LENGTH_SHORT).show()
    } else if (r1.isSuccess || r2.isSuccess) {
        Toast.makeText(context, "Shizuku AppOps 1/2", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Shizuku AppOps ❌", Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun ShizukuSection(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShizukuSectionPurple, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(text = "🟪👀 发现 Shizuku")
        }
        PermissionRow(
            requestString = "请求 Shizuku",
            successString = "Shizuku 已授权",
            isGranted = vm.shizukuAccess,
            onGrant = {
                Shizuku.requestPermission(1001)
            }
        )
        PermissionRow(
            requestString = "通过 Shizuku 授予应用权限",
            successString = "已获得必须的应用权限，无需操作",
            isGranted = vm.hasAccessibility && vm.canDrawOverlays,
            onGrant = {
                lifecycleOwner.lifecycleScope.launch {
                    grantShizukuAppops(context, vm.shellUtilClient)
                    vm.refresh(context)
                }
            }
        )
    }
}

@Composable
fun MainPage(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        vm.refresh(context)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refresh(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (vm.shizukuFound) {
        ShizukuSection()
    }

    PermissionRow(
        requestString = "悬浮窗权限",
        successString = "已获得悬浮窗权限",
        isGranted = vm.canDrawOverlays,
        onGrant = { requestPermission(context, Settings.ACTION_MANAGE_OVERLAY_PERMISSION) })

    PermissionRow(
        requestString = "无障碍权限",
        successString = "已获得无障碍权限",
        isGranted = vm.hasAccessibility,
        onGrant = { requestPermission(context, Settings.ACTION_ACCESSIBILITY_SETTINGS) })

    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (vm.hasAccessibility && vm.canDrawOverlays) {
            Text(text = "启动悬浮窗")
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { startMainService(context) }) {
                Text(text = "启动")
            }
        } else {
            Text(text = "未满足启动条件")
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "自动获取权限并启动服务 (experimental)")

        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = vm.autoLaunchService,
            enabled = vm.shizukuAccess,
            onCheckedChange = {
            vm.setAutoLaunchService(context, it)
        })
    }
}

@Composable
fun App() {
    AutoScrollTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(32.dp)
            ) {
                MainPage()
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var shellUtil: ShellUtilClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm : MainViewModel by viewModels()

        vm.refresh(this)

        if (vm.shizukuAccess && vm.autoLaunchService){
            lifecycleScope.launch {
                // how to delay to wait for Shizuku to be ready, 10 seconds or fail
                var wait = 0;
                while (wait < 10 && !vm.shellUtilClient.isBound) {
                    vm.shellUtilClient.bind()
                    wait++
                    kotlinx.coroutines.delay(1000)
                }
                if (vm.shizukuAccess) {
                    grantShizukuAppops(this@MainActivity, vm.shellUtilClient)
                    vm.refresh(this@MainActivity)
                    if (vm.hasAccessibility && vm.canDrawOverlays) {
                        startMainService(this@MainActivity)
                    }
                }
            }

        }

        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shellUtil.unbind()
    }
}



