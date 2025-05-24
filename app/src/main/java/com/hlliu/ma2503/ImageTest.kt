package com.hlliu.ma2503

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL


data class MediaItem(
    val id: Long,
    val displayName: String,
    val mimeType: String,
    val uri: Uri
)

class ImageTest : ComponentActivity() {
    private lateinit var btnCheck: Button
    private lateinit var tvImageInfo: TextView
    private lateinit var ivImage: ImageView
    private lateinit var btnSelect: Button
    private lateinit var btnLoad: Button
    private lateinit var etImageUrl: EditText
    private lateinit var btnLoadGallery: Button
    private lateinit var rvGallery: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var images = mutableListOf<MediaItem>()
    private var page = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var pageSize = 20 // 默认值为 20

    private fun loadImagesFromGallery() {
        val newImages = getImagesFromGallery(contentResolver, page, pageSize)
        images.addAll(newImages)
        imageAdapter = ImageAdapter(images) { uri ->
            startImageDetailActivity(uri)
        }
        rvGallery.adapter = imageAdapter
        imageAdapter.notifyDataSetChanged() // 初始加载时使用
    }
    private fun getImagesFromGallery(
        contentResolver: ContentResolver,
        page: Int,
        pageSize: Int
    ): List<MediaItem> {
//        tvImageInfo.text = "进入函数 1"
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
        )

        val collectionUri =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val offset = page * pageSize
        val images = mutableListOf<MediaItem>()

        // 使用 Bundle 参数限制查询结果（适用于 Android 8.0 及以上）
        val queryArgs = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, null)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, null)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_ADDED))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, pageSize) // 限制查询结果
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset) // 偏移量
        }
//        tvImageInfo.text = "进入函数 1-1"
        contentResolver.query(collectionUri, projection, queryArgs, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val uri = ContentUris.withAppendedId(collectionUri, id)

                images.add(MediaItem(id, displayName, mimeType, uri))
            }
        }
//        tvImageInfo.text = "退出函数 1"
        return images
    }

    val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value == true
        }
        if (granted) {
            tvImageInfo.text = "权限已授予"
            loadImagesFromGallery()
        } else {
            tvImageInfo.text = "权限被拒绝"
        }
    }

    fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
//            arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (btnCheck.text == "check" && permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }){
            tvImageInfo.text = "权限已授予"
            loadImagesFromGallery()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        // 从 SharedPreferences 中读取 pageSize，如果没有则使用默认值 20
        pageSize = sharedPreferences.getInt("pageSize", 20)
        btnCheck = findViewById(R.id.btnCheck)
        tvImageInfo = findViewById(R.id.tv_images_info)
        ivImage = findViewById(R.id.iv_image)
        btnSelect = findViewById(R.id.btnSelect)
        btnCheck.setOnClickListener { requestPermissions() }
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let {
                    ivImage.setImageURI(it)
                }
            }
        }
        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
        btnLoad = findViewById(R.id.btnLoad)
        etImageUrl = findViewById(R.id.etImageUrl)
        btnLoad.setOnClickListener {
            val imageUrl = etImageUrl.text.toString()
            if(imageUrl.isNotEmpty()) {
                loadBitmap(imageUrl)
            } else {
                etImageUrl.error = "请输入图片网址！"
            }
        }
        btnLoadGallery = findViewById(R.id.btnLoadGallery)
        rvGallery = findViewById(R.id.rv_gallery)
//        rvGallery.layoutManager = LinearLayoutManager(this)
        rvGallery.layoutManager = GridLayoutManager(this, 3)
        rvGallery.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1)){
                    loadMoreImage()
                }
            }
        })

        btnLoadGallery.setOnClickListener {
            loadImagesFromGallery()
//            val images = getImagesFromGallery(contentResolver)
//            imageAdapter = ImageAdapter(images)
//            rvGallery.adapter = imageAdapter
////            tvImageInfo.text = "处理完成..."
        }
        imageAdapter = ImageAdapter(images) { uri ->
            // 点击图片时启动详情 Activity
            startImageDetailActivity(uri)
        }
        rvGallery.adapter = imageAdapter
    }

    private fun startImageDetailActivity(uri: Uri) {
        val intent = Intent(this, ImageDetailActivity::class.java).apply {
            putExtra("IMAGE_URI", uri.toString())
        }
        startActivity(intent)
    }
    private fun loadMoreImage() {
        page++
        val newImages = getImagesFromGallery(contentResolver, page, pageSize)
        val oldSize = images.size
        images.addAll(newImages)
        imageAdapter.notifyItemRangeInserted(oldSize, newImages.size)
    }

    private fun loadBitmap(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = URL(imageUrl).openStream().use { inputStream ->
                    BitmapFactory.decodeStream(inputStream as InputStream)
                }
                val file = saveBitmapToFile(bitmap)
                val uri = saveBitmapToGallery(bitmap)
                withContext(Dispatchers.Main) {
                    ivImage.setImageBitmap(bitmap)
                    Toast.makeText(this@ImageTest, "图片已保存到：$file", Toast.LENGTH_LONG).show()
                    Toast.makeText(this@ImageTest, "图片已保存到相册：$uri", Toast.LENGTH_LONG).show()

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ivImage.setImageResource(R.drawable.afg) // 加载失败的占位图
                    Toast.makeText(this@ImageTest, "加载失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap?): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyApp2503")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.also {
            contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
        return uri
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): File {
        val folder = File(this.getExternalFilesDir(null), "MyImages")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val file = File(folder, fileName)
        if (bitmap != null) {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
        return file
    }
}