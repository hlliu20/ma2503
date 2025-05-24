package com.hlliu.ma2503

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ActivityPermission : AppCompatActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value == true
        }
        if (granted) {
            // 所有请求的权限都被授予
            val intent = Intent(this, MusicService::class.java)
            intent.action = MusicService.ACTION_LOAD
            startService(intent)
        } else {
            // 至少有一个权限被拒绝
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            finish()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}