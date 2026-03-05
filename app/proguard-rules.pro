# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Shizuku AIDL ──────────────────────────────────────────────────────────
# AIDL 生成的 Stub/Proxy 需要保留，否则 Binder 跨进程调用会 ClassNotFound
-keep class com.dezier.utils.IShellUtilService { *; }
-keep class com.dezier.utils.IShellUtilService$* { *; }
-keep class com.dezier.utils.ShellUtilService { *; }

# Shizuku 库本身
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

# ── AccessibilityService ──────────────────────────────────────────────────
# 系统通过 Manifest 中的类名反射创建，不能混淆
-keep class com.dezier.autoscroll.service.MainService { *; }

# ── Compose ───────────────────────────────────────────────────────────────
# R8 全模式下保留 Compose 运行时必要的内省信息
-dontwarn androidx.compose.**
