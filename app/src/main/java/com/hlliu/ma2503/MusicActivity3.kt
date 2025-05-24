package com.hlliu.ma2503

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MusicActivity3 : ComponentActivity() {
    private lateinit var musicService: MusicService
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
        }
    }
}