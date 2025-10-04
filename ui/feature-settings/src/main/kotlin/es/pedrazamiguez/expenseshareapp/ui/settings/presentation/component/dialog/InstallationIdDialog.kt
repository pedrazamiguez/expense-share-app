package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder

@Composable
fun InstallationIdDialog(
    installationId: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Installation ID".placeholder) },
        text = { Text(installationId) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close".placeholder)
            }
        })
}
