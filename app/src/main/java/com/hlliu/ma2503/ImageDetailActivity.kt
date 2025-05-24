package com.hlliu.ma2503

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ImageDetailActivity : AppCompatActivity() {

//    private lateinit var imageView: ImageView
//    private lateinit var scaleGestureDetector: ScaleGestureDetector
//    private var matrix = Matrix()
//    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_detail)

        val imageView: ZoomableImageView = findViewById(R.id.imageViewDetail)
        val imageUri: Uri? = intent.getStringExtra("IMAGE_URI")?.let { Uri.parse(it) }

        imageUri?.let {
            loadBitmap(it) { bitmap ->
                imageView.setImageBitmap(bitmap)
            }
        }
//        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//            override fun onScale(detector: ScaleGestureDetector): Boolean {
//                scaleFactor *= detector.scaleFactor
//                matrix.setScale(scaleFactor, scaleFactor)
//                imageView.imageMatrix = matrix
//                return true
//            }
//        })
//
//        imageView.setOnTouchListener { _, event ->
//            scaleGestureDetector.onTouchEvent(event)
//            true
//        }
    }

    private fun loadBitmap(uri: Uri, callback: (Bitmap) -> Unit) {
        Thread {
            try {
                val inputStream = contentResolver.openInputStream(uri)
//                val options = BitmapFactory.Options().apply {
//                    inJustDecodeBounds = true // 只解码图片边界
//                    inputStream?.use { input ->
//                        BitmapFactory.decodeStream(input, null, this)
//                    }
//                    // 计算采样率
//                    val sampleSize = calculateInSampleSize(this, 800, 800)
//                    inSampleSize = sampleSize
//                    inJustDecodeBounds = false // 解码图片内容
//                }
//                inputStream?.close() // 关闭第一次使用的流
//
//                // 重新打开流进行解码
//                val inputStream2 = contentResolver.openInputStream(uri)
//                val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
//                inputStream2?.close() // 关闭流

                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close() // 关闭流

                // 回调主线程更新 UI
                runOnUiThread {
                    if (bitmap != null) {
                        callback(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
