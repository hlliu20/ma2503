package com.hlliu.ma2503

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException
import java.util.Locale

class MusicActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                tvCurrentTime.text = formatTime(mediaPlayer.currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvSongName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var ibtnPrevious: ImageButton
    private lateinit var ibtnPlayPause: ImageButton
    private lateinit var ibtnNext: ImageButton
    private lateinit var ibtnSelect: ImageButton
    private var isPlaying = false
    private var songIndex = 0
    private var songTotalTime: Int = 0
    private val musicDir = "/storage/emulated/0/01myfile/music/"
    private lateinit var songNames: List<String>
    // 请求权限
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予，加载歌曲
            loadMusicFiles()
        } else {
            // 权限未授予，提示用户
            tvSongName.text = "权限未授予"
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
        // 初始化组件
        tvSongName = findViewById(R.id.tv_song_name)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        ibtnPrevious = findViewById(R.id.ibtn_previous)
        ibtnPlayPause = findViewById(R.id.ibtn_play_pause)
        ibtnNext = findViewById(R.id.ibtn_next)
        ibtnSelect = findViewById(R.id.ibtn_select)
        seekBar = findViewById(R.id.seek_bar)

//         请求存储权限
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        // 初始化 MediaPlayer
        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnPreparedListener {
            songTotalTime = mediaPlayer.duration
            seekBar.max = songTotalTime
            tvTotalTime.text = formatTime(songTotalTime)
            tvSongName.text = songNames[songIndex]
            mediaPlayer.start()
            ibtnPlayPause.setImageResource(R.drawable.cud)
            isPlaying = true
        }
        mediaPlayer.setOnCompletionListener {
            playNextSong()
        }
        // 设置 SeekBar 的监听器
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // 设置播放进度更新
        handler.postDelayed(runnable, 1000)
        ibtnPrevious.setOnClickListener {
            playPreviousSong()
        }
        ibtnPlayPause.setOnClickListener {
            togglePlayPause()
        }
        ibtnNext.setOnClickListener {
            playNextSong()
        }
    }
    private fun loadMusicFiles() {
        val musicFiles = getMusicFilesFromDirectory(musicDir)
        if (musicFiles.isNotEmpty()) {
            val songNames = mutableListOf<String>()
            for(music in musicFiles){
                val musicName: String = music.name.toString()
                songNames.add(musicName)
            }
            tvSongName.text = songNames[0]
            this.songNames = songNames
            loadSong()

        } else {
            this.songNames = emptyList<String>()
            println("$musicDir 文件夹内未找到.mp3文件")
        }
    }
    private fun getMusicFilesFromDirectory(directory: String): List<File> {
        val musicDir = File(directory)
        return musicDir.listFiles { file -> file.name.endsWith(".mp3") }?.toList() ?: emptyList()
    }
    private fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause()
            ibtnPlayPause.setImageResource(R.drawable.cue)
        } else {
            mediaPlayer.start()
            ibtnPlayPause.setImageResource(R.drawable.cud)
        }
        isPlaying = !isPlaying
    }

    private fun playPreviousSong() {
        if (songIndex > 0) {
            songIndex--
        } else {
            songIndex = songNames.size - 1
        }
        loadSong()
    }

    private fun playNextSong() {
        if (songIndex < songNames.size - 1) {
            songIndex++
        } else {
            songIndex = 0
        }
        loadSong()
    }
    private fun loadSong() {
        if(songNames.isEmpty()){
            tvSongName.text = "列表为空"
            songIndex = 0
            return
        }
        tvSongName.text = songNames[songIndex]
        try {
            // 设置数据源
            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicDir + songNames[songIndex])
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            tvSongName.text = "${songNames[songIndex]} 歌曲加载失败"
            return
        }
    }
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.Companion.format(Locale.CHINA, "%02d:%02d", minutes, remainingSeconds)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        // 移除延迟任务
        handler.removeCallbacks(runnable)
        // 释放 MediaPlayer 资源
        mediaPlayer.release()
    }

}