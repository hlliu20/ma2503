package com.hlliu.ma2503

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MusicActivity2 : ComponentActivity() {
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
    private var isPlaying = false
    private var songIndex = 0
    private var songTotalTime: Int = 0
    private val songResources = listOf(R.raw.a001)
//        listOf(R.raw.a001, R.raw.a002, R.raw.a003, R.raw.a004, R.raw.a005, R.raw.a006, R.raw.a007, R.raw.a008, R.raw.a009, R.raw.a010, R.raw.a011, R.raw.a012, R.raw.a013, R.raw.a014, R.raw.a015, R.raw.a016, R.raw.a017, R.raw.a018, R.raw.a019, R.raw.a020, R.raw.a021, R.raw.a022, R.raw.a023, R.raw.a024, R.raw.a025)
//    private lateinit var songs:  List<Pair<String, Int>>
    private var songNames: List<String> = listOf("巴啦啦小魔仙") //,"室内系的TrackMaker","The Truth That You Leave","开心往前飞","侧脸","碧波摇篮曲","我从草原来","玉盘 (2025中央广播电视总台春节联欢晚会现场)","快乐小神仙","月亮船","只要你开心的笑","公主请开心","猴哥","不怕 (动画片《神兵小将》片尾曲)","自娱自乐","雨爱","少年英雄小哪吒","奇迹再现","梦的光点　(神兵小将片头曲)","北京欢迎你","不老梦","快乐酷宝","猪猪侠","果宝特攻","オラはにんきもの (我最受欢迎)")

//    // 请求权限
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            // 权限已授予，加载歌曲
//            loadSongs()
//        } else {
//            // 权限未授予，提示用户
//            tvSongName.text = "Permission not granted"
//        }
//    }
//    // 请求用户选择文件夹
//    private val requestFolderLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == RESULT_OK) {
//            val uri: Uri? = result.data?.data
//            if (uri != null) {
//                val folder = DocumentFile.fromTreeUri(this, uri)
//                if (folder != null) {
//                    loadSongsFromFolder(folder)
//                }
//            }
//        }
//    }
//    private fun loadSongsFromFolder(folder: DocumentFile) {
//        val songs = mutableListOf<File>()
//        val files = folder.listFiles()
//        for (file in files) {
//            if (file.name?.endsWith(".mp3") == true) {
//                val songFile = File(file.uri.path)
//                songs.add(songFile)
//            }
//        }
//        if (songs.isEmpty()) {
//            tvSongName.text = "No songs found"
//            return
//        }
//        this.songs = songs
//        loadSong()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        if (NetworkUtil.isNetworkConnected(this)) {
//            // 设备已连接到网络
//            Toast.makeText(this, "已连接到网络", Toast.LENGTH_SHORT).show()
//        } else {
//            // 设备未连接到网络
//            Toast.makeText(this, "未连接到网络", Toast.LENGTH_SHORT).show()
//        }
        // 初始化组件
        tvSongName = findViewById(R.id.tv_song_name)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)
        ibtnPrevious = findViewById(R.id.ibtn_previous)
        ibtnPlayPause = findViewById(R.id.ibtn_play_pause)
        ibtnNext = findViewById(R.id.ibtn_next)
        seekBar = findViewById(R.id.seek_bar)

//        // 请求存储权限
//        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
//
//        // 引导用户选择文件夹
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//        requestFolderLauncher.launch(intent)

//        // 动态获取 res/raw 目录下的所有歌曲文件
//        songs = getRawAudioFiles()

        // 设置歌曲名
        tvSongName.text = songNames[songIndex]

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

        playSong(songIndex)

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

        // 设置按钮的点击事件
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
    private fun playSong(songIndex: Int) {
        val afd: AssetFileDescriptor = resources.openRawResourceFd(songResources[songIndex])
        mediaPlayer.reset()
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer.prepareAsync()
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
            songIndex = songResources.size - 1
        }
        playSong(songIndex)
    }

    private fun playNextSong() {
        if (songIndex < songResources.size - 1) {
            songIndex++
        } else {
            songIndex = 0
        }
        playSong(songIndex)
    }


//    private fun loadSong() {
//        mediaPlayer.reset()
//        mediaPlayer = MediaPlayer.create(this, resources.getIdentifier(songs[songIndex].first, "raw", packageName))
//        seekBar.max = mediaPlayer.duration
//        tvSongName.text = songNames[songIndex]
//        tvTotalTime.text = formatTime(mediaPlayer.duration)
//        mediaPlayer.start()
//        ibtnPlayPause.setImageResource(R.drawable.cud)
//        isPlaying = true
//    }
//private fun loadSong() {
//    val songUri = songs[songIndex]
//    val songName = getFileNameFromUri(songUri)
//    tvSongName.text = songName
//
//    mediaPlayer = MediaPlayer()
//    try {
//        val inputStream = contentResolver.openInputStream(songUri)
//        if (inputStream != null) {
//            mediaPlayer.setDataSource(inputStream.fd)
//            inputStream.close()
//        }
//        mediaPlayer.prepare()
//    } catch (e: IOException) {
//        e.printStackTrace()
//        tvSongName.text = "Failed to load song"
//        return
//    }
//
//
//    seekBar.max = mediaPlayer.duration
//    tvTotalTime.text = formatTime(mediaPlayer.duration)
//
//    // 设置播放进度更新
//    val handler = Handler(Looper.getMainLooper())
//    handler.postDelayed(object : Runnable {
//        override fun run() {
//            if (mediaPlayer.isPlaying) {
//                seekBar.progress = mediaPlayer.currentPosition
//                tvCurrentTime.text = formatTime(mediaPlayer.currentPosition)
//            }
//            handler.postDelayed(this, 1000)
//        }
//    }, 1000)
//
//    // 设置 SeekBar 的监听器
//    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//            if (fromUser) {
//                mediaPlayer.seekTo(progress)
//            }
//        }
//
//        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//    })
//
//    // 设置按钮的点击事件
//    ibtnPlayPause.setOnClickListener {
//        if (isPlaying) {
//            mediaPlayer.pause()
//            ibtnPlayPause.setImageResource(R.drawable.cue)
//        } else {
//            mediaPlayer.start()
//            ibtnPlayPause.setImageResource(R.drawable.cud)
//        }
//        isPlaying = !isPlaying
//    }
//
//    ibtnPrevious.setOnClickListener {
//        songIndex = (songIndex - 1 + songs.size) % songs.size
//        loadSong()
//    }
//
//    ibtnNext.setOnClickListener {
//        songIndex = (songIndex + 1) % songs.size
//        loadSong()
//    }
//}

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.Companion.format(Locale.CHINA, "%02d:%02d", minutes, remainingSeconds)
    }

//    private fun loadSongs() {
//        // 获取音乐文件目录
//        val musicDir = File(
//            Environment.getExternalStorageDirectory().absolutePath +
//                    "/01myfile/music"
//        )
//        if (!musicDir.exists()) {
//            tvSongName.text = "Music directory not found"
//            return
//        }
//
//        // 获取目录下的所有 .mp3 文件
//        songs = musicDir.listFiles { file -> file.extension == "mp3" }?.toList() ?: emptyList()
//
//        if (songs.isEmpty()) {
//            tvSongName.text = "No songs found"
//            return
//        }
//
//        // 加载第一首歌
//        loadSong()
//    }

//    private fun getRawAudioFiles(): List<Pair<String, Int>> {
//        val fieldArray = R.raw::class.java.fields
//        val songs = mutableListOf<Pair<String, Int>>()
//
//        for (field in fieldArray) {
//            val name = field.name
//            val resId = field.getInt(field)
//            songs.add(Pair(name, resId))
//        }
//        return songs
//    }

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