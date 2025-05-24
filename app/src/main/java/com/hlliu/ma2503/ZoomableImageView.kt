package com.hlliu.ma2503

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mode = NONE
    private var lastInterceptX = 0f
    private var lastInterceptY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var prevDistance = 0f
    private var viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    private val bitmapRect = RectF()
    private val viewRect = RectF()

    private val touchSlop: Int = viewConfiguration.scaledTouchSlop

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            viewRect.set(0f, 0f, w.toFloat(), h.toFloat())
            updateView()
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (bm != null) {
            bitmapRect.set(0f, 0f, bm.width.toFloat(), bm.height.toFloat())
            updateView()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mode = DRAG
                lastTouchX = x
                lastTouchY = y
                lastInterceptX = x
                lastInterceptY = y
                savedMatrix.set(matrix)
//                Log.d("ZoomableImageView::Action_down:", "x=$lastTouchX , y=$lastTouchY")
            }
            MotionEvent.ACTION_MOVE -> {
//                Log.d("ZoomableImageView::Action_move:", "x=$x , y=$y")
//                Log.d("ZoomableImageView::Action_move:", "matrix=$matrix")
                if (mode == DRAG) {
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
//                    matrix.set(savedMatrix)
                    matrix.postTranslate(dx, dy)
                    imageMatrix = matrix
                } else if (mode == ZOOM) {
                    val newDistance = spacing(event)
                    if (newDistance > touchSlop) {
                        val scale = newDistance / prevDistance
                        matrix.set(savedMatrix)
                        val midX = (event.getX(0) + event.getX(1)) / 2
                        val midY = (event.getY(0) + event.getY(1)) / 2
                        matrix.postScale(scale, scale, midX, midY)
                        imageMatrix = matrix
                    }
                }
                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
                savedMatrix.set(matrix)
                prevDistance = spacing(event)
            }
            MotionEvent.ACTION_POINTER_UP -> mode = DRAG
            MotionEvent.ACTION_UP -> mode = NONE
        }
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun updateView() {
        val bitmapWidth = drawable?.intrinsicWidth?.toFloat() ?: 1f
        val bitmapHeight = drawable?.intrinsicHeight?.toFloat() ?: 1f
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val scale = Math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
        val dx = (viewWidth - bitmapWidth * scale) / 2
        val dy = (viewHeight - bitmapHeight * scale) / 2

        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
