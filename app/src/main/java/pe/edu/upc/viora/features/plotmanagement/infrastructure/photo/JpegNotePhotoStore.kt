package pe.edu.upc.viora.features.plotmanagement.infrastructure.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.net.toUri
import android.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upc.viora.core.di.IoDispatcher
import pe.edu.upc.viora.features.plotmanagement.domain.service.NotePhotoStore
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.FieldNoteRepositoryImpl

/**
 * Stores the photo of a note in `filesDir/field-notes` as an upright JPEG whose longer side is at most
 * [MAX_SIDE] px, lowering the quality until it fits the server's 1 MiB limit.
 */
class JpegNotePhotoStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotePhotoStore {

    override suspend fun keep(uri: String): String? = withContext(ioDispatcher) {
        try {
            val source = uri.toUri()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
            val decoded = context.contentResolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@withContext null
            val rotation = context.contentResolver.openInputStream(source)?.use { rotationOf(ExifInterface(it)) } ?: 0f
            val upright = scaleAndRotate(decoded, rotation)

            val bytes = compress(upright) ?: return@withContext null
            val dir = File(context.filesDir, FieldNoteRepositoryImpl.PHOTO_DIR).apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            file.writeBytes(bytes)
            file.absolutePath
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun discard(path: String) {
        withContext(ioDispatcher) { File(path).delete() }
    }

    private fun scaleAndRotate(bitmap: Bitmap, rotation: Float): Bitmap {
        val longer = maxOf(bitmap.width, bitmap.height)
        val scale = if (longer > MAX_SIDE) MAX_SIDE.toFloat() / longer else 1f
        if (scale == 1f && rotation == 0f) return bitmap
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun compress(bitmap: Bitmap): ByteArray? {
        var quality = START_QUALITY
        while (quality >= MIN_QUALITY) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (out.size() <= MAX_BYTES) return out.toByteArray()
            quality -= QUALITY_STEP
        }
        return null
    }

    private fun rotationOf(exif: ExifInterface): Float = when (
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    private companion object {
        const val MAX_SIDE = 1600
        const val MAX_BYTES = 1024 * 1024
        const val START_QUALITY = 85
        const val MIN_QUALITY = 40
        const val QUALITY_STEP = 15
    }
}
