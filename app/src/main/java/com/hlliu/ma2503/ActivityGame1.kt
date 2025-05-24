package com.hlliu.ma2503

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.Display
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ActivityGame1 : AppCompatActivity(), SensorEventListener {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_game1)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
    private lateinit var gameView: GameView1
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        // 检查设备支持的刷新率
//        val display = windowManager.defaultDisplay
//        val modes = display.supportedModes
//        for (mode in modes) {
//            Log.d("RefreshRate", "Supported Refresh Rate: ${mode.refreshRate} Hz")
//        }
//
//        // 设置目标刷新率
//        val targetMode = modes.find { it.refreshRate == 120.00001f }
//        if (targetMode != null) {
//            // 设置目标刷新率模式
//            val params = window.attributes
//            params.preferredDisplayModeId = targetMode.modeId
//            window.attributes = params
//        } else {
//            // 如果设备不支持 120Hz，打印错误信息
//            Log.d("RefreshRate", "120Hz refresh rate is not supported on this device.")
//        }

        gameView = GameView1(this)
        setContentView(gameView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 检查设备是否支持加速度传感器
        if (accelerometer == null) {
            Toast.makeText(this, "This device does not support an accelerometer.", Toast.LENGTH_LONG).show()
            finish() // 如果不支持传感器，退出应用
        }
    }

    override fun onResume() {
//        Toast.makeText(this,"onResume", Toast.LENGTH_SHORT).show()
        super.onResume()
        gameView.startGame()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
//        Toast.makeText(this,"onPause", Toast.LENGTH_SHORT).show()
        super.onPause()
        gameView.stopGame()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0] // X轴加速度
            val y = event.values[1] // Y轴加速度
            gameView.updateBallDirection(-x, y)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        gameView.stopGame()
        super.onDestroy()
    }

}