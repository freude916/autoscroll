package com.dezier.autoscroll

import android.accessibilityservice.AccessibilityService

class MyAccessibilityService: AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // TODO
    }

    override fun onInterrupt() {
        // TODO
    }

}