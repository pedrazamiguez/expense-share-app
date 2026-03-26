package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard card used inside wizard steps for grouped content.
 *
 * Renders an optional section [title] above a [Card] that uses the
 * `surfaceContainerHigh` container colour and `shapes.large` corner radius.
 * The card body is a [Column] with consistent internal padding and spacing.
 *
 * @param modifier        Optional modifier applied to the outer wrapper.
 * @param title           Optional section title rendered above the card.
 * @param contentSpacing  Vertical gap between children inside the card (default 16 dp).
 * @param content         Card body content.
 */
@Composable
fun WizardStepCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentSpacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
                content = content
            )
        }
    }
}
