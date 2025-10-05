package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder

@Composable
fun InstallationIdDialog(
    installationId: String,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Installation ID".placeholder) },
        text = { Text(installationId) },
        confirmButton = {
            TextButton(onClick = {
                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "Installation ID",
                        installationId
                    )
                )
                Toast.makeText(
                    context,
                    "Copied to clipboard".placeholder,
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text("Copy".placeholder)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close".placeholder)
            }
        })
}
