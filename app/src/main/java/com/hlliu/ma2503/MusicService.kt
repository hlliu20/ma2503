package com.hlliu.ma2503

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.random.Random

class MusicService : Service() {
    private val binder = MusicBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
                notifyProgressChanged(mediaPlayer.currentPosition)
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
    private val CHANNEL_ID: String = "music_channel"
    enum class PlayMode {
        RANDOM, // 随机播放
        LOOP,   // 列表循环
        REPEAT  // 单曲循环
    }
    private var playMode = PlayMode.RANDOM
    interface MusicServiceCallback {
        fun onPlayPauseChanged(isPlaying: Boolean)
        fun onSongChanged(songName: String, duration: Int)
        fun onProgressChanged(currentTime: Int)
        fun onPlayModeChanged(newPlayMode: PlayMode)
    }

    private val callbacks = mutableListOf<MusicServiceCallback>()

    fun registerCallback(callback: MusicServiceCallback) {
        callbacks.add(callback)
    }

    fun unregisterCallback(callback: MusicServiceCallback) {
        callbacks.remove(callback)
    }

    private fun notifyPlayPauseChanged(isPlaying: Boolean) {
        callbacks.forEach { it.onPlayPauseChanged(isPlaying) }
    }

    private fun notifySongChanged(songName: String, duration: Int) {
        callbacks.forEach { it.onSongChanged(songName, duration) }
    }

    private fun notifyProgressChanged(currentTime: Int) {
        callbacks.forEach { it.onProgressChanged(currentTime) }
    }
    private fun notifyPlayModeChanged(newPlayMode: PlayMode) {
        callbacks.forEach { it.onPlayModeChanged(newPlayMode) }
    }


    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    fun getCurrentSongName(): String {
        return if (songNames.isNotEmpty()) songNames[songIndex] else "No song playing"
    }
    fun getCurrentIsPlaying(): Boolean {
        return isPlaying
    }
    fun getCurrentPlayMode(): PlayMode {
        return playMode
    }
    fun getCurrentSongDuration(): Int {
        return mediaPlayer.duration
    }
    fun seekTo(position: Int) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(position)
        }
    }
    fun changePlayMode(newPlayMode: PlayMode){
        playMode = newPlayMode
    }

    private fun requestPermissions() {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
//                arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }){
            loadMusicFiles()
        } else {
            val intent = Intent(this, ActivityPermission::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            isPlaying = true
            mediaPlayer.start()
            startForeground(NOTIFICATION_ID, buildNotification())
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        }
        mediaPlayer.setOnCompletionListener {
            onCompletionPlay()
        }
        handler.post(runnable)

        songNames = emptyList()
        requestPermissions()
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
        val musicFiles = getMusicFilesFromDirectory()
        if (musicFiles.isNotEmpty()) {
            songNames = musicFiles.map { it.name }
            if(playMode == PlayMode.RANDOM){
                songIndex = Random.nextInt(0, songNames.size)
            }
            loadSong()
        } else {
            songNames = emptyList()
            println("$musicDir 文件夹内未找到.mp3文件")
        }
    }

    private fun getMusicFilesFromDirectory(): List<File> {
        val musicDir = File(musicDir)
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
            notifySongChanged(songNames[songIndex], mediaPlayer.duration)
            notifyProgressChanged(mediaPlayer.currentPosition)
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
        notificationLayout.setImageViewResource(
            R.id.ibtnMode,
            when(playMode){
                PlayMode.REPEAT -> R.drawable.akv
                PlayMode.RANDOM -> R.drawable.aku
                PlayMode.LOOP -> R.drawable.akt
            }
        )
        notificationLayout.setInt(R.id.notification_main, "setBackgroundColor", "#FF29B6F6".toColorInt())
        notificationLayout.setOnClickPendingIntent(R.id.ibtnMode, getPendingIntent(ACTION_MODE))
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
            .addAction(
                when(playMode){
                    PlayMode.REPEAT -> R.drawable.akv
                    PlayMode.RANDOM -> R.drawable.aku
                    PlayMode.LOOP -> R.drawable.akt
                },
                when(playMode){
                    PlayMode.REPEAT -> "单曲循环"
                    PlayMode.RANDOM -> "随机播放"
                    PlayMode.LOOP -> "列表循环"
                },
                getPendingIntent(ACTION_MODE)
            )
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
                ACTION_LOAD -> loadMusicFiles()
                ACTION_MODE -> changeToNextPlayMode()
            }
        }
        return START_NOT_STICKY
    }
    private fun changeToNextPlayMode(){
        when(playMode){
            PlayMode.LOOP -> {
                playMode = PlayMode.RANDOM
            }
            PlayMode.RANDOM -> {
                playMode = PlayMode.REPEAT
            }
            PlayMode.REPEAT -> {
                playMode = PlayMode.LOOP
            }
        }
        notifyPlayModeChanged(playMode)
    }

    internal fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        isPlaying = !isPlaying
        notifyPlayPauseChanged(isPlaying)
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
        when(playMode){
            PlayMode.RANDOM -> {
                songIndex = Random.nextInt(0, songNames.size)
                loadSong()
                mediaPlayer.start()
            }
            PlayMode.LOOP,PlayMode.REPEAT -> {
                if (songIndex < songNames.size - 1) {
                    songIndex++
                } else {
                    songIndex = 0
                }
                loadSong()
                mediaPlayer.start()
            }
        }
        isPlaying = true
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }
    private fun onCompletionPlay(){
        when(playMode){
            PlayMode.RANDOM -> {
                songIndex = Random.nextInt(0, songNames.size)
                loadSong()
                mediaPlayer.start()
            }
            PlayMode.LOOP -> {
                if (songIndex < songNames.size - 1) {
                    songIndex++
                } else {
                    songIndex = 0
                }
                loadSong()
                mediaPlayer.start()
            }
            PlayMode.REPEAT -> {
                mediaPlayer.seekTo(0)
            }
        }
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
        const val ACTION_LOAD = "com.hlliu.ma2503.ACTION_LOAD"
        const val ACTION_MODE = "com.hlliu.ma2503.ACTION_MODE"
    }
}