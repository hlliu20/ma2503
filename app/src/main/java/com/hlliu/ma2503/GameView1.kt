package com.hlliu.ma2503

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView1(context: Context) : SurfaceView(context), Runnable {
    private val surfaceHolder: SurfaceHolder = holder
    private var gameThread: Thread? = null
    private var updateThread: Thread? = null
    private var isRunning = false
    private var isUpdating = false

    private var fps = 0
    private val paint = Paint()

    private var startTime = 0L


    private var ballX = 200f
    private var ballY = 200f
    private val ballRadius = 50f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private var ballAccelX = 0f
    private var ballAccelY = 0f

    private var frameCount = 0

    private val maxSpeed = 30f
    private var updateTime = System.currentTimeMillis()
//    private var lastDrawTime = System.currentTimeMillis()

    override fun run() {
        while (isUpdating) {
            updateBallPosition()
            try {
                Thread.sleep(16)
            } catch (e: InterruptedException) {
                e.printStackTrace();
                // 重新设置中断状态
                Thread.currentThread().interrupt();
            }

        }
    }

    fun startGame() {
        isRunning = true
        isUpdating = true

        startTime = System.currentTimeMillis()
        updateTime = startTime
        frameCount = 0

        updateThread = Thread(this)
        updateThread?.start()

        gameThread = Thread {
            while (isRunning) {
                val canvas: Canvas? = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    try {
                        drawGame(canvas)
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
//                Thread.sleep(50)
            }
        }
        gameThread?.start()
    }

    fun stopGame() {
        isRunning = false
        isUpdating = false
        updateThread?.interrupt()
        gameThread?.interrupt()
        updateThread = null
        gameThread = null
    }



    private fun drawGame(canvas: Canvas) {

        frameCount++

        canvas.drawColor(Color.BLACK)
        paint.color = Color.RED
        canvas.drawCircle(ballX, ballY, ballRadius, paint)

//        updateBallPosition()
////        canvas.drawBitmap(bitmap, x, y, null)

        val currentTime = System.currentTimeMillis()
//        val timeGap = currentTime - lastDrawTime

//        if(timeGap <= 500){
            if (currentTime - startTime >= 1000) {
                fps = (frameCount * 1000 / (currentTime - startTime)).toInt()
                frameCount = 0
                startTime = currentTime
            }
//        } else {
//            frameCount = 0
//            startTime = currentTime
//        }
//        lastDrawTime = currentTime

        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("FPS: $fps", 100f, 200f, paint)

    }

    private lateinit var bitmap: Bitmap
    private var x = 0f
    private var y = 0f

    init {
        paint.color = Color.RED
        setWillNotDraw(false)
//        bitmap = BitmapFactory.decodeResource(resources, R.drawable.ad_)
    }

    fun updateBallDirection(x: Float, y: Float) {
        ballAccelX = x * 100 // 根据 X 轴加速度调整速度
        ballAccelY = y * 100 // 根据 Y 轴加速度调整速度
    }

    private fun updateBallPosition() {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - updateTime) / 1000.0f
        updateTime = currentTime

        ballSpeedX += ballAccelX * deltaTime
        ballSpeedY += ballAccelY * deltaTime

        if(ballSpeedX > maxSpeed) ballSpeedX = maxSpeed
        else if(ballSpeedX < -maxSpeed) ballSpeedX = -maxSpeed
        if(ballSpeedY > maxSpeed) ballSpeedY = maxSpeed
        else if(ballSpeedY < -maxSpeed) ballSpeedY = -maxSpeed

        ballX += ballSpeedX
        ballY += ballSpeedY

        // 确保球始终在屏幕边界内
        if (ballX - ballRadius < 0) {
            ballX = ballRadius
            ballSpeedX = 0f
        } else if (ballX + ballRadius > width) {
            ballX = width - ballRadius
            ballSpeedX = 0f
        }

        if (ballY - ballRadius < 0) {
            ballY = ballRadius
            ballSpeedY = 0f
        } else if (ballY + ballRadius > height) {
            ballY = height - ballRadius
            ballSpeedY = 0f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 处理按下事件
            }

            MotionEvent.ACTION_MOVE -> {
                // 处理移动事件
                this.x = x
                this.y = y
            }

            MotionEvent.ACTION_UP -> {
                // 处理抬起事件
            }
        }
        return true
    }
}
