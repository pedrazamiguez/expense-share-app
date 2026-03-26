package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard outer wrapper for a single wizard step.
 *
 * Every step in the app's wizard flows uses the same padding and spacing.
 * Wrap your step content in this layout to enforce visual consistency
 * without repeating the boilerplate Column/padding/arrangement.
 *
 * @param modifier        Optional modifier applied to the outer [Column].
 * @param verticalSpacing Vertical gap between child composables (default 16 dp).
 * @param content         Step content.
 */
@Composable
fun WizardStepLayout(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}
