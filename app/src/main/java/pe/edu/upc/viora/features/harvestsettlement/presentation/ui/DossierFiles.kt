package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import pe.edu.upc.viora.R

/**
 * What P77 does with the sealed PDF on the phone. The file lives in the app's private files, so other apps receive it
 * through the FileProvider with a read-only grant.
 */
internal object DossierFiles {

    private const val PDF = "application/pdf"
    private const val WHATSAPP = "com.whatsapp"

    /** "Abrir PDF": the phone's PDF viewer. */
    fun open(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uriOf(context, file), PDF)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.dossier_pdf_no_app, Toast.LENGTH_LONG).show()
        }
    }

    /** "Compartir por WhatsApp" with the code in the message; without WhatsApp, the phone's share sheet. */
    fun shareOnWhatsApp(context: Context, file: File, message: String) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType(PDF)
            .putExtra(Intent.EXTRA_STREAM, uriOf(context, file))
            .putExtra(Intent.EXTRA_TEXT, message)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(Intent(intent).setPackage(WHATSAPP))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent.createChooser(intent, null))
        }
    }

    /** "Guardar en el teléfono": copies the PDF where the producer chose (a CreateDocument result). */
    fun copyTo(context: Context, file: File, target: Uri) {
        val saved = runCatching {
            context.contentResolver.openOutputStream(target)?.use { output -> file.inputStream().use { it.copyTo(output) } }
        }.getOrNull() != null
        if (saved) Toast.makeText(context, R.string.dossier_pdf_saved, Toast.LENGTH_SHORT).show()
    }

    private fun uriOf(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
