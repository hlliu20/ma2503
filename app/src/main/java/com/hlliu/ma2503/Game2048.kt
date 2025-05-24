package com.hlliu.ma2503

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs
import kotlin.random.Random

class Game2048 : ComponentActivity() {
    companion object {
        const val SIZE = 4 // 游戏网格大小
    }
    private var board = Array(SIZE) { IntArray(SIZE) } // 游戏网格
    private var boardTemp = Array(SIZE) { IntArray(SIZE) } // 用于撤销
    private val random = Random

    private lateinit var gridLayout: GridLayout
    private val gridViews = mutableListOf<G2048TextView>()
    private var gameOverView: TextView? = null

    private lateinit var btnNew: Button
    private lateinit var btnAutoRun: Button
    private lateinit var btnUndo: Button
    private lateinit var tvBest: TextView
    private lateinit var tvScore: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private var bestScore: Int = 0
    private var score: Int = 0
    private var scoreTemp: Int = 0

    private fun addNewTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                if (board[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells[random.nextInt(emptyCells.size)]
            board[row][col] = if (random.nextDouble() < 0.9) 2 else 4
        }
    }
    private fun init() {
        // 读取当前存储的最高分
        val highScore = sharedPreferences.getInt("high_score", 0)
        // 比较当前得分和最高分
        if (bestScore > highScore) {
            // 更新最高分
            editor.putInt("high_score", bestScore)
            editor.apply()
        }
        for(i in board.indices){board[i].fill(0)}
        score = 0
        scoreTemp = 0

//        var ti: Int = 2
//        for (i in 1 until SIZE) {
//            for (j in 0 until SIZE) {
//                board[i][j] = ti
//                ti *= 2
//            }
//        }

        addNewTile() // 初始化时添加两个随机数字
        addNewTile()
        boardTemp = Array(SIZE) { board[it].clone() }
        updateGridUI()
    }
    // 判断是否有空位
    private fun hasEmptyCells(): Boolean {
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                if (board[i][j] == 0) return true
            }
        }
        return false
    }
    // 判断游戏是否结束
    private fun isGameOver(): Boolean {
        if (hasEmptyCells()) return false
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE - 1) {
                if (board[i][j] == board[i][j + 1]) return false
            }
        }
        for (i in 0 until SIZE - 1) {
            for (j in 0 until SIZE) {
                if (board[i][j] == board[i + 1][j]) return false
            }
        }
        return true
    }
    // 方向枚举
    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
    // 滑动操作（向上、向下、向左、向右）
    fun swipe(direction: Direction): Boolean {
        val originalBoard = Array(SIZE) { board[it].clone() }
        val originalScore = score
        when (direction) {
            Direction.UP -> swipeUp()
            Direction.DOWN -> swipeDown()
            Direction.LEFT -> swipeLeft()
            Direction.RIGHT -> swipeRight()
        }
        val changed = !board.contentDeepEquals(originalBoard)
        if (changed) {
            boardTemp = Array(SIZE) { originalBoard[it].clone() }
            scoreTemp = originalScore
            addNewTile()
        }
        return changed
    }
    private fun swipeUp() {
        for (col in 0 until SIZE) {
            compressColumn(col, true)
            mergeColumn(col, true)
            compressColumn(col, true)
        }
    }

    private fun swipeDown() {
        for (col in 0 until SIZE) {
            compressColumn(col, false)
            mergeColumn(col, false)
            compressColumn(col, false)
        }
    }

    private fun swipeLeft() {
        for (row in 0 until SIZE) {
            compressRow(row, true)
            mergeRow(row, true)
            compressRow(row, true)
        }
    }

    private fun swipeRight() {
        for (row in 0 until SIZE) {
            compressRow(row, false)
            mergeRow(row, false)
            compressRow(row, false)
        }
    }

    private fun compressColumn(col: Int, isUp: Boolean) {
        val start = if (isUp) 0 else SIZE - 1
        val step = 1
        var lastNonZeroRow = start
        if (isUp) {
            for (row in start until SIZE step step) {
                if (board[row][col] != 0) {
                    board[lastNonZeroRow][col] = board[row][col]
                    if (lastNonZeroRow != row) {
                        board[row][col] = 0
                    }
                    lastNonZeroRow += step
                }
            }
        } else {
            for (row in start downTo 0 step step) {
                if (board[row][col] != 0) {
                    board[lastNonZeroRow][col] = board[row][col]
                    if (lastNonZeroRow != row) {
                        board[row][col] = 0
                    }
                    lastNonZeroRow -= step
                }
            }
        }
    }

    private fun mergeColumn(col: Int, isUp: Boolean) {
        val start = if (isUp) 0 else SIZE - 1
        val step = 1
        if (isUp) {
            for (row in start until SIZE - step step step) {
                if (board[row][col] == board[row + step][col] && board[row][col] != 0) {
                    board[row][col] *= 2
                    score += board[row][col]
                    board[row + step][col] = 0
                }
            }
        } else {
            for (row in start downTo step step step) {
                if (board[row][col] == board[row - step][col] && board[row][col] != 0) {
                    board[row][col] *= 2
                    score += board[row][col]
                    board[row - step][col] = 0
                }
            }
        }
    }

    private fun compressRow(row: Int, isLeft: Boolean) {
        val start = if (isLeft) 0 else SIZE - 1
        val step = 1
        var lastNonZeroCol = start
        if (isLeft) {
            for (col in start until SIZE step step) {
                if (board[row][col] != 0) {
                    board[row][lastNonZeroCol] = board[row][col]
                    if (lastNonZeroCol != col) {
                        board[row][col] = 0
                    }
                    lastNonZeroCol += step
                }
            }
        } else {
            for (col in start downTo 0 step step) {
                if (board[row][col] != 0) {
                    board[row][lastNonZeroCol] = board[row][col]
                    if (lastNonZeroCol != col) {
                        board[row][col] = 0
                    }
                    lastNonZeroCol -= step
                }
            }
        }
    }

    private fun mergeRow(row: Int, isLeft: Boolean) {
        val start = if (isLeft) 0 else SIZE - 1
        val step = 1
        if (isLeft) {
            for (col in start until SIZE - step step step) {
                if (board[row][col] == board[row][col + step] && board[row][col] != 0) {
                    board[row][col] *= 2
                    score += board[row][col]
                    board[row][col + step] = 0
                }
            }
        } else {
            for (col in start downTo step step step) {
                if (board[row][col] == board[row][col - step] && board[row][col] != 0) {
                    board[row][col] *= 2
                    score += board[row][col]
                    board[row][col - step] = 0
                }
            }
        }
    }

    private fun adjustTextViewSize(gridLayout: GridLayout, rows: Int, cols: Int) {
        val parentWidth = gridLayout.width
        val totalMargins = 8 * (cols + 1) // 每列之间有 4dp 的外边距，两侧也有边距
        val textViewWidth = (parentWidth - totalMargins) / cols
        val textViewHeight = textViewWidth // 确保宽高相等

        for (i in 0 until gridLayout.childCount) {
            val textView = gridLayout.getChildAt(i) as G2048TextView
            textView.layoutParams.width = textViewWidth
            textView.layoutParams.height = textViewHeight
        }
        updateGridUI()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game2048)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 在游戏启动时读取最高分
        sharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        bestScore = sharedPreferences.getInt("high_score", 0) // 默认值为 0
        gridLayout = findViewById(R.id.gridLayoutG2)
        btnNew = findViewById(R.id.btnG2new)
        btnAutoRun = findViewById(R.id.btnG2autorun)
        btnUndo = findViewById(R.id.btnG2undo)
        tvBest = findViewById(R.id.tvG2Best)
        tvScore = findViewById(R.id.tvG2Score)
        gridLayout.rowCount = SIZE
        gridLayout.columnCount = SIZE
        // 动态创建 16 个格子
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                val textView = G2048TextView(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        rowSpec = GridLayout.spec(i)
                        columnSpec = GridLayout.spec(j)
                        // setPadding(8,8,8,8)
                        setMargins(4,4,4,4)
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                    }
                    text = ""
                    gravity = Gravity.CENTER
                    textSize = 32F
                }
                gridViews.add(textView)
                gridLayout.addView(textView)
            }
        }
        // 确保在布局完成后调整 TextView 的大小
        gridLayout.post {
            adjustTextViewSize(gridLayout, SIZE, SIZE)
        }
        // 设置触摸监听器
        val gestureDetector = GestureDetector(this, SwipeGestureListener())
        gridLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        btnNew.setOnClickListener {
            init()
        }
        btnUndo.setOnClickListener {
            board = boardTemp
            score = scoreTemp
            updateGridUI()
        }
        btnNew.callOnClick()
    }

//    override fun onStart() {
//        init()
//        super.onStart()
//    }
    // 显示游戏结束提示
    private fun showGameOver() {
        if (gameOverView == null) {
            gameOverView = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(0, SIZE)
                    columnSpec = GridLayout.spec(0, SIZE)
                }
                gravity = Gravity.CENTER
                textSize = 48f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.argb(128, 0, 0, 0)) // 半透明黑色背景
                text = "游戏结束"
            }
            gridLayout.addView(gameOverView)
        }
    }
    // 隐藏游戏结束提示
    private fun hideGameOver() {
        gameOverView?.let {
            gridLayout.removeView(it)
            gameOverView = null
        }
    }
    // 更新 UI
    private fun updateGridUI() {
        if(score > bestScore) {
            bestScore = score
        }
        tvBest.text = bestScore.toString()
        tvScore.text = score.toString()
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                val value = board[i][j]
                val textView = gridViews[i * SIZE + j]
                textView.text = value.toString()
            }
        }
        // 检查游戏是否结束
        if (isGameOver()) {
            // 读取当前存储的最高分
            val highScore = sharedPreferences.getInt("high_score", 0)

            // 比较当前得分和最高分
            if (bestScore > highScore) {
                // 更新最高分
                editor.putInt("high_score", bestScore)
                editor.apply()
            }
            showGameOver()
        } else {
            hideGameOver()
        }
    }
    // 内部类：处理滑动事件
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val distanceX = e2.x - e1!!.x
            val distanceY = e2.y - e1.y
            val absDistanceX = abs(distanceX)
            val absDistanceY = abs(distanceY)

            if (absDistanceX > absDistanceY && absDistanceX > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) {
                    // 向右滑动
                    swipe(Direction.RIGHT)
                } else {
                    // 向左滑动
                    swipe(Direction.LEFT)
                }
            } else if (absDistanceY > absDistanceX && absDistanceY > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceY > 0) {
                    // 向下滑动
                    swipe(Direction.DOWN)
                } else {
                    // 向上滑动
                    swipe(Direction.UP)
                }
            }
            updateGridUI()
            return true
        }
    }
}