package es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.sheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.settings.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyableTextSheet(
    title: String = "",
    copyableText: String? = null,
    notAvailableText: String = "",
    onDismiss: () -> Unit = { }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current

    var isCopied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon header with animated background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isCopied, transitionSpec = {
                        scaleIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith scaleOut(
                            spring(stiffness = Spring.StiffnessMedium)
                        )
                    }, label = "iconAnimation"
                ) { copied ->
                    Icon(
                        imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Copyable text in a styled surface
            if (copyableText != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = copyableText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text(
                    text = notAvailableText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.size(8.dp))

            // Copy button with animated state
            FilledTonalButton(
                enabled = copyableText != null && !isCopied, onClick = {
                    // Haptic feedback
                    @Suppress("DEPRECATION") view.performHapticFeedback(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.VIRTUAL_KEY
                        }
                    )

                    // Copy to clipboard
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(title, copyableText)
                    )

                    isCopied = true

                    coroutineScope.launch {
                        delay(800) // Brief delay to show success state
                        sheetState.hide()
                    }.invokeOnCompletion {
                        onDismiss()
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = isCopied, transitionSpec = {
                        scaleIn(spring(stiffness = Spring.StiffnessMedium)) togetherWith scaleOut(
                            spring(stiffness = Spring.StiffnessMedium)
                        )
                    }, label = "buttonIconAnimation"
                ) { copied ->
                    Icon(
                        imageVector = if (copied) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (isCopied) {
                        stringResource(R.string.settings_copied)
                    } else {
                        stringResource(R.string.settings_copy)
                    }, style = MaterialTheme.typography.labelLarge
                )
            }
        }

    }

}
