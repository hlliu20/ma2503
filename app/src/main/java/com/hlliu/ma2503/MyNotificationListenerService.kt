package com.hlliu.ma2503

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyNotificationListenerService() : NotificationListenerService() {
    private var isListening = true // 控制是否监听通知

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun startForegroundService() {
        val channelId = "my_service"
        val channelName = "My Background Service"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Notification Listener Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.akv)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false) // 点击通知时不自动清除
            .setOngoing(true) // 设置为不可清除
            .build()

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        Log.d("NotificationListener", "Service created")
    }
    private fun isQQRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(10) // 获取前 10 个任务
        for (taskInfo in runningTasks) {
            if ("com.tencent.mobileqq" == taskInfo.baseActivity?.packageName) {
                return true // QQ 已在后台运行
            }
        }
        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

//        Toast.makeText(this, "post", Toast.LENGTH_LONG).show()
        if (!isListening) return // 如果未启用监听，则直接返回

        // 获取通知信息
        val notificationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(sbn.postTime))
        val notificationYMD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sbn.postTime))
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE).toString()
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT).toString()
        val subText = sbn.notification.extras.getString(Notification.EXTRA_SUB_TEXT).toString()
        val summaryText = sbn.notification.extras.getString(Notification.EXTRA_SUMMARY_TEXT).toString()
        Log.d("NotificationListener", "pkg:${sbn.packageName}, title: $title, Text: $text")
        // 构建保存的字符串
        var content = "Time: $notificationTime\nTitle: $title\nText: $text\n------------------------------\n"
        when (packageName) {
            "com.xiaomi.aicr" -> {
                content = "Time: $notificationTime\n------------------------------\n"
                saveToFIle(content, "$notificationYMD-null.txt")
            }
            "com.android.mms" -> {
                if(title == "“短信”正在运行") {
                    saveToFIle(content, "$notificationYMD-null.txt")
                } else {
                    content = "Time: $notificationTime\nPackage: $packageName\nTitle: $title\nSummary: $summaryText\nSub: $subText\nText: $text\n------------------------------\n"
                    saveToFIle(content, "$notificationYMD-mms.txt")
                }

            }
            "tv.danmaku.bili" -> {
                saveToFIle(content, "bilibili.txt")
            }
            "com.tencent.mobileqq" -> {
                saveToFIle(content, "$notificationYMD-mobileqq.txt")
//                Log.d("pendingIntent", sbn.notification.contentIntent.intentSender.toString())
                sbn.notification.contentIntent?.send()
            }
            "com.tencent.wework" -> {
                saveToFIle(content, "$notificationYMD-wework.txt")
            }
            "com.tencent.mm" -> {
                saveToFIle(content, "$notificationYMD-wechat.txt")
            }
            "com.android.incallui" -> {
                saveToFIle(content, "inCall.txt")
            }
            else -> {
                content = "Time: $notificationTime\nPackage: $packageName\nTitle: $title\nSummary: $summaryText\nSub: $subText\nText: $text\n------------------------------\n"
                saveToFIle(content, "$notificationYMD-other.txt")
            }
        }

//        // 检查通知内容是否包含关键词
//        if (text.contains("纳西妲")) {
//            Log.d("NotificationListener", "Keyword detected: 纳西妲")
//            showToast(sbn.packageName)
//        }
    }

    private fun saveToFIle(content: String, fileName: String = "notifications.txt") {
        val file = File("/storage/emulated/0/Documents/", fileName)
        try {
            FileOutputStream(file, true).use { fos -> // 追加模式
                fos.write(content.toByteArray())
            }
//            Toast.makeText(this, "sss", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationListener", "Notification removed: ${sbn.packageName}")
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_LISTENING") {
            isListening = false
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            stopSelf() // 停止服务
        } else if (intent?.action == "START_LISTENING") {
            isListening = true
            startForegroundService()
        }
        return START_STICKY
    }
}