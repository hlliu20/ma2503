package com.hlliu.ma2503
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream

class ImageAdapter(
    private val items: List<MediaItem>,
    private val onItemClickListener: (Uri) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        // 定义一个方法来处理点击事件
        fun bind(item: MediaItem, onItemClickListener: (Uri) -> Unit) {
            imageView.setOnClickListener {
                onItemClickListener(item.uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = items[position]
        loadBitmap(holder.imageView.context, item.uri) { bitmap ->
            // 确保在主线程中更新 UI
            holder.imageView.post {
                holder.imageView.setImageBitmap(bitmap)
            }
        }
        holder.bind(item, onItemClickListener)
    }

    override fun getItemCount(): Int = items.size

    private fun loadBitmap(context: Context, uri: Uri, callback: (Bitmap) -> Unit) {
        Thread {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true // 只解码图片边界
                inputStream?.use { input ->
                    BitmapFactory.decodeStream(input, null, this)
                }
                // 计算采样率
                val sampleSize = calculateInSampleSize(this, 200, 200)
                inSampleSize = sampleSize
                inJustDecodeBounds = false // 解码图片内容
            }
            inputStream?.close() // 关闭第一次使用的流

            // 重新打开流进行解码
            val inputStream2 = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()
            if (bitmap != null) {
                callback(bitmap)
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

