package com.hlliu.ma2503

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.IOException
import java.util.Locale
import androidx.core.graphics.toColorInt

class MusicService : Service() {
    private val binder = MusicBinder()
    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
            handler.postDelayed(this, 1000)
        }
    }
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationManager: NotificationManager
    private lateinit var songNames: List<String>
    private var songIndex = 0
    private var isPlaying = false
    private val musicDir = "/storage/emulated/0/01myfile/music/"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "music_channel"

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
        loadMusicFiles()
        mediaPlayer.setOnPreparedListener {
            isPlaying = true
            mediaPlayer.start()
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        mediaPlayer.setOnCompletionListener {
            playNextSong()
        }
        handler.post(runnable)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun loadMusicFiles() {
        val musicFiles = getMusicFilesFromDirectory(musicDir)
        if (musicFiles.isNotEmpty()) {
            songNames = musicFiles.map { it.name }
            loadSong()
        } else {
            songNames = emptyList()
            println("$musicDir 文件夹内未找到.mp3文件")
        }
    }

    private fun getMusicFilesFromDirectory(directory: String): List<File> {
        val musicDir = File(directory)
        return musicDir.listFiles { file -> file.name.endsWith(".mp3") }?.toList() ?: emptyList()
    }

    private fun loadSong() {
        if (songNames.isEmpty()) {
            return
        }
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicDir + songNames[songIndex])
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.music_service_notification)
        notificationLayout.setTextViewText(R.id.tv_song_name, songNames[songIndex])
        notificationLayout.setTextViewText(R.id.tv_current_time, formatTime(mediaPlayer.currentPosition))
        notificationLayout.setTextViewText(R.id.tv_total_time, formatTime(mediaPlayer.duration))
        notificationLayout.setImageViewResource(
            R.id.ibtn_play_pause,
            if (isPlaying) R.drawable.cud else R.drawable.cue
        )
        notificationLayout.setInt(R.id.notification_main, "setBackgroundColor", "#FF29B6F6".toColorInt())
        notificationLayout.setOnClickPendingIntent(R.id.ibtn_previous, getPendingIntent(ACTION_PREVIOUS))
        notificationLayout.setOnClickPendingIntent(R.id.ibtn_play_pause, getPendingIntent(ACTION_PLAY_PAUSE))
        notificationLayout.setOnClickPendingIntent(R.id.ibtn_next, getPendingIntent(ACTION_NEXT))
        notificationLayout.setOnClickPendingIntent(R.id.ibtn_stop, getPendingIntent(ACTION_STOP))

        val notificationIntent = Intent(this, MusicActivity3::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.music)
            .setContentTitle(songNames[songIndex])
            .setContentText(formatTime(mediaPlayer.currentPosition))
            .setCustomContentView(notificationLayout)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.akw, "Previous", getPendingIntent(ACTION_PREVIOUS))
            .addAction(
                if (isPlaying) R.drawable.cud else R.drawable.cue,
                if (isPlaying) "Play" else "Pause",
                getPendingIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(R.drawable.akq, "Next", getPendingIntent(ACTION_NEXT))
            .addAction(R.drawable.xxx, "Stop", getPendingIntent(ACTION_STOP))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PLAY_PAUSE -> togglePlayPause()
                ACTION_PREVIOUS -> playPreviousSong()
                ACTION_NEXT -> playNextSong()
                ACTION_STOP -> stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    internal fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        isPlaying = !isPlaying
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    internal fun playPreviousSong() {
        if (songIndex > 0) {
            songIndex--
        } else {
            songIndex = songNames.size - 1
        }
        loadSong()
        mediaPlayer.start()
        isPlaying = true
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    internal fun playNextSong() {
        if (songIndex < songNames.size - 1) {
            songIndex++
        } else {
            songIndex = 0
        }
        loadSong()
        mediaPlayer.start()
        isPlaying = true
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.CHINA, "%02d:%02d", minutes, remainingSeconds)
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.hlliu.ma2503.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.hlliu.ma2503.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.hlliu.ma2503.ACTION_NEXT"
        const val ACTION_STOP = "com.hlliu.ma2503.ACTION_STOP"
    }
}