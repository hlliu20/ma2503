package com.hlliu.ma2503

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.nio.charset.Charset

abstract class QQNotificationService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("QQNotificationService", "Service connected")
        val serviceInfo = serviceInfo
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        setServiceInfo(serviceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val packageName = event.packageName.toString()
            if (packageName == "com.tencent.mobileqq") {
                val title = event.text.toString()
                val content = event.contentDescription.toString()
                handleQQNotification(title, content)
            }
        }
    }

    private fun handleQQNotification(title: String, content: String) {
        // 在这里处理 QQ 通知，例如显示在 UI 上或保存到数据库
        Log.d("QQNotification", "System default charset: ${Charset.defaultCharset()}")
        Log.d("QQNotification", "Title: $title, Content: $content")
    }

//    override fun onServiceDisconnected() {
//        super.onServiceDisconnected()
//        Log.d("QQNotificationService", "Service disconnected")
//    }
}