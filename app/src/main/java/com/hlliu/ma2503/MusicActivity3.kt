package com.hlliu.ma2503

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MusicActivity3 : ComponentActivity(), MusicService.MusicServiceCallback {
    private lateinit var musicService: MusicService
    private var isBound = false
    private var isSeekBarTracking = false // 用于标记 SeekBar 是否正在被拖动
    private var playMode = MusicService.PlayMode.RANDOM


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            musicService.registerCallback(this@MusicActivity3)
            isBound = true
            // 请求当前播放的歌曲名并更新 UI
            updateCurrentSong()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService.unregisterCallback(this@MusicActivity3)
            isBound = false
        }
    }
    private fun updateCurrentSong() {
        val currentSongName = musicService.getCurrentSongName()
        val currentSongDuration = musicService.getCurrentSongDuration()
        val isPlaying = musicService.getCurrentIsPlaying()
        playMode = musicService.getCurrentPlayMode()
        val tvSongName = findViewById<TextView>(R.id.tv_song_name)
        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        val ibtnPlayPause = findViewById<ImageButton>(R.id.ibtn_play_pause)
        val ibtnMode = findViewById<ImageButton>(R.id.ibtnMode)
        tvSongName.text = currentSongName
        seekBar.max = currentSongDuration
        ibtnPlayPause.setImageResource(if (isPlaying) R.drawable.cud else R.drawable.cue)
        when(playMode){
            MusicService.PlayMode.REPEAT -> ibtnMode.setImageResource(R.drawable.akv)
            MusicService.PlayMode.LOOP -> ibtnMode.setImageResource(R.drawable.akt)
            MusicService.PlayMode.RANDOM -> ibtnMode.setImageResource(R.drawable.aku)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化按钮
        val ibtnPrevious = findViewById<ImageButton>(R.id.ibtn_previous)
        val ibtnPlayPause = findViewById<ImageButton>(R.id.ibtn_play_pause)
        val ibtnNext = findViewById<ImageButton>(R.id.ibtn_next)
        val ibtnStop = findViewById<ImageButton>(R.id.ibtn_select)
        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        val ibtnMode = findViewById<ImageButton>(R.id.ibtnMode)

        // 启动服务
        val startIntent = Intent(this, MusicService::class.java)
        startService(startIntent)
        bindService(startIntent, serviceConnection, BIND_AUTO_CREATE)

        // 设置按钮点击事件
        ibtnPrevious.setOnClickListener {
            musicService.playPreviousSong()
        }
        ibtnPlayPause.setOnClickListener {
            musicService.togglePlayPause()
        }
        ibtnNext.setOnClickListener {
            musicService.playNextSong()
        }
        ibtnStop.setOnClickListener {
            musicService.stopSelf()
            unbindService(serviceConnection)
            isBound = false
            finish()
        }
        ibtnMode.setOnClickListener {
            when(playMode){
                MusicService.PlayMode.LOOP -> {
                    playMode = MusicService.PlayMode.RANDOM
                    ibtnMode.setImageResource(R.drawable.aku)
                }
                MusicService.PlayMode.RANDOM -> {
                    playMode = MusicService.PlayMode.REPEAT
                    ibtnMode.setImageResource(R.drawable.akv)
                }
                MusicService.PlayMode.REPEAT -> {
                    playMode = MusicService.PlayMode.LOOP
                    ibtnMode.setImageResource(R.drawable.akt)
                }
            }
            musicService.changePlayMode(playMode)
        }
        // 设置 SeekBar 的监听器
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 当用户拖动 SeekBar 时，暂停 onProgressChanged 的更新
                    isSeekBarTracking = true
                    val tvCurrentTime = findViewById<TextView>(R.id.tv_current_time)
                    tvCurrentTime.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeekBarTracking = false
                val progress = seekBar!!.progress
                musicService.seekTo(progress)
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            musicService.unregisterCallback(this)
        }
    }
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.CHINA, "%02d:%02d", minutes, remainingSeconds)
    }
    override fun onPlayPauseChanged(isPlaying: Boolean) {
        val ibtnPlayPause = findViewById<ImageButton>(R.id.ibtn_play_pause)
        ibtnPlayPause.setImageResource(if (isPlaying) R.drawable.cud else R.drawable.cue)
    }

    override fun onSongChanged(songName: String, duration: Int) {
        val tvSongName = findViewById<TextView>(R.id.tv_song_name)
        tvSongName.text = songName
        val seekBar = findViewById<SeekBar>(R.id.seek_bar)
        seekBar.max = duration
        val tvTotalTime = findViewById<TextView>(R.id.tv_total_time)
        tvTotalTime.text = formatTime(duration)
    }

    override fun onProgressChanged(currentTime: Int) {
        if(!isSeekBarTracking){
            val tvCurrentTime = findViewById<TextView>(R.id.tv_current_time)
            val seekBar = findViewById<SeekBar>(R.id.seek_bar)
            seekBar.progress = currentTime
            tvCurrentTime.text = formatTime(currentTime)
        }
    }

    override fun onPlayModeChanged(newPlayMode: MusicService.PlayMode) {
        playMode = newPlayMode
        val ibtnMode = findViewById<ImageButton>(R.id.ibtnMode)
        when(playMode){
            MusicService.PlayMode.LOOP -> {
                ibtnMode.setImageResource(R.drawable.akt)
            }
            MusicService.PlayMode.RANDOM -> {
                ibtnMode.setImageResource(R.drawable.aku)
            }
            MusicService.PlayMode.REPEAT -> {
                ibtnMode.setImageResource(R.drawable.akv)
            }
        }
    }
}

