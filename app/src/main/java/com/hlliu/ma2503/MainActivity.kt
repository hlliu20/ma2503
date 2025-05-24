package com.hlliu.ma2503

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.hlliu.ma2503.ui.theme.MA2503Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        // 引导用户启用无障碍服务
//        val intent0 = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//        startActivity(intent0)

        // 检查是否已经授予管理所有文件权限
        if (!Environment.isExternalStorageManager()){
            // 引导用户手动开启权限
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        }


        val btn2music = findViewById<Button>(R.id.btn2music)
        btn2music.setOnClickListener {
            val intent = Intent(this,MusicActivity::class.java)
            startActivity(intent)
        }
//        val btn2music2 = findViewById<Button>(R.id.btn2music2)
//        btn2music2.setOnClickListener {
//            val intent = Intent(this,MusicActivity2::class.java)
//            startActivity(intent)
//        }
        val btn2image = findViewById<Button>(R.id.btn2image)
        btn2image.setOnClickListener {
            val intent = Intent(this,ImageTest::class.java)
            startActivity(intent)
        }
        val btn2Game2048 = findViewById<Button>(R.id.btn2game2048)
        btn2Game2048.setOnClickListener {
            val intent = Intent(this,Game2048::class.java)
            startActivity(intent)
        }
        val btn2compass = findViewById<Button>(R.id.btn2compass)
        btn2compass.setOnClickListener {
            val intent = Intent(this,ActivityCompass::class.java)
            startActivity(intent)
        }
        val btn2game1 = findViewById<Button>(R.id.btn2Game1)
        btn2game1.setOnClickListener {
            val intent = Intent(this,ActivityGame1::class.java)
            startActivity(intent)
        }
        // 引导用户前往通知监听服务设置页面
        val btnOpenSettings = findViewById<Button>(R.id.btn_open_settings)
        btnOpenSettings.setOnClickListener {
            val isNotificationListenerEnabled = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )?.contains(packageName) == true

            if (!isNotificationListenerEnabled) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
            // 引导用户启用无障碍服务
            val intent0 = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent0)
        }

        // 启动监听服务
        val btnStartListening = findViewById<Button>(R.id.btn_start_listening)
        btnStartListening.setOnClickListener {
            if (isNotificationServiceEnabled()) {
                val intent = Intent(this, MyNotificationListenerService::class.java)
                intent.action = "START_LISTENING"
                startService(intent)
            } else {
                Toast.makeText(this, "Please enable notification access in settings", Toast.LENGTH_LONG).show()
            }
        }

        // 停止监听服务
        val btnStopListening = findViewById<Button>(R.id.btn_stop_listening)
        btnStopListening.setOnClickListener {
            val intent = Intent(this, MyNotificationListenerService::class.java)
            intent.action = "STOP_LISTENING"
            startService(intent)
        }

        val btnStartMusic = findViewById<Button>(R.id.btn_start_music)
        btnStartMusic.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
            startService(intent)
        }
        val btnShowMusic = findViewById<Button>(R.id.btn_show_music)
        btnShowMusic.setOnClickListener {
            val intent = Intent(this, MusicActivity3::class.java)
            startActivity(intent)
        }
        val btnStopMusic = findViewById<Button>(R.id.btn_stop_music)
        btnStopMusic.setOnClickListener {
            val intent = Intent(this, MusicService::class.java).apply {
                this.action =  "com.hlliu.ma2503.ACTION_STOP"
            }
            startService(intent)
        }
    }
    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Green) {
        Text(
            text = "你好，$name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MA2503Theme {
        Greeting("林")
    }
}