package com.hlliu.ma2503

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

/**
 * TODO: document your custom view class.
 */
class G2048TextView(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text is String && text != "" && text.all { it.isDigit() }) {
            val number = text.toInt()
            val spannableString = SpannableString(text)
            val n = calculatePowerOfTwo(number)
            val textColor = getColorForDigit(n)
            val backgroundColor = getBackgroundColorForDigit(n)
            // val textSize = getTextSizeForDigit(number)
            setTextColor(textColor)
            setBackgroundColor(backgroundColor)
            // setTextSize(TypedValue.COMPLEX_UNIT_SP ,textSize.toFloat())
            super.setText(spannableString, type)
        } else {
            super.setText(text, type)
        }
    }
    private fun calculatePowerOfTwo(n: Int): Int {
        var power = 0
        var num = n
        while (num > 1) {
            num = num shr 1 // 右移一位
            power++
        }
        return power
    }

    private fun getColorForDigit(digit: Int): Int {
        return when (digit) {
            0 -> Color.WHITE
            1 -> Color.BLUE
            2 -> Color.RED
            3 -> Color.MAGENTA
            4 -> Color.BLUE
            5 -> Color.CYAN
            6 -> Color.YELLOW
            7 -> Color.BLACK
            8 -> Color.LTGRAY
            9 -> Color.WHITE
            10 -> Color.GREEN
            else -> Color.CYAN
        }
    }

    private fun getBackgroundColorForDigit(digit: Int): Int {
        return when (digit) {
            0 -> Color.WHITE
            1 -> Color.LTGRAY
            2 -> Color.CYAN
            3 -> Color.GREEN
            4 -> Color.YELLOW
            5 -> Color.MAGENTA
            6 -> Color.BLUE
            7 -> Color.RED
            8 -> Color.GRAY
            9 -> Color.DKGRAY
            10 -> Color.RED
            else -> Color.BLUE
        }
    }

    private fun getTextSizeForDigit(digit: Int): Int {
        return when {
            digit < 100 -> 48
            digit < 1000 -> 32
            digit < 10000 -> 16
            else -> 8
        }
    }
}