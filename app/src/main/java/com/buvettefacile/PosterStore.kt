package com.buvettefacile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max

class PosterStore(private val context: Context) {
    private val posterFile get() = File(context.filesDir, "poster.jpg")
    private val boxesFile get() = File(context.filesDir, "boxes.json")

    fun hasSavedRegister() = posterFile.exists() && boxesFile.exists()

    fun loadBitmap(): Bitmap? = if (posterFile.exists()) BitmapFactory.decodeFile(posterFile.path) else null

    fun loadBoxes(): List<PriceBox> {
        if (!boxesFile.exists()) return emptyList()
        val array = runCatching { JSONArray(boxesFile.readText()) }.getOrNull() ?: return emptyList()
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            PriceBox(
                id = o.getLong("id"),
                left = o.getDouble("left").toFloat(),
                top = o.getDouble("top").toFloat(),
                right = o.getDouble("right").toFloat(),
                bottom = o.getDouble("bottom").toFloat(),
                cents = o.getInt("cents"),
                label = o.optString("label", ""),
            )
        }
    }

    fun saveBoxes(boxes: List<PriceBox>) {
        val array = JSONArray()
        boxes.forEach { b ->
            array.put(
                JSONObject()
                    .put("id", b.id)
                    .put("left", b.left.toDouble())
                    .put("top", b.top.toDouble())
                    .put("right", b.right.toDouble())
                    .put("bottom", b.bottom.toDouble())
                    .put("cents", b.cents)
                    .put("label", b.label)
            )
        }
        boxesFile.writeText(array.toString())
    }

    fun clear() {
        posterFile.delete()
        boxesFile.delete()
    }

    fun cameraTempUri(): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(dir, "capture.jpg"))
    }

    fun importPhoto(uri: Uri): Boolean {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val probe = resolver.openInputStream(uri) ?: return false
        probe.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return false

        val rotation = resolver.openInputStream(uri)?.use { runCatching { ExifInterface(it).rotationDegrees }.getOrDefault(0) } ?: 0
        val bitmap = if (rotation == 0) decoded else {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }
        posterFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return true
    }

    private companion object {
        const val MAX_SIDE = 2048
    }
}
