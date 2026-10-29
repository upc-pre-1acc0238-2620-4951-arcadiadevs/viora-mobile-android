package pe.edu.upc.viora.features.telemetry.presentation.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * "Compartir con técnico" of an alert: the panel as the producer sees it (a PNG) with a short text, so WhatsApp or
 * mail show the chart and what to do, not only a line. The image lives in the app's cache and is lent read-only
 * through the FileProvider; each alert overwrites its own file.
 */
internal object AlertShare {

    private const val SHARED_DIR = "shared"

    suspend fun share(
        context: Context,
        panel: ImageBitmap?,
        text: String,
        incidentId: String,
        ioDispatcher: CoroutineDispatcher,
    ) {
        val image = panel?.let { runCatching { writePng(context, it, incidentId, ioDispatcher) }.getOrNull() }
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text)
        if (image == null) {
            intent.type = "text/plain"
        } else {
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    private suspend fun writePng(
        context: Context,
        panel: ImageBitmap,
        incidentId: String,
        ioDispatcher: CoroutineDispatcher,
    ): File = withContext(ioDispatcher) {
        val directory = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
        File(directory, "alerta-$incidentId.png").also { file ->
            file.outputStream().use { panel.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
