package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.features.expense.R

/**
 * Pinned submit button for the Add Expense form.
 * Always visible above the keyboard, adapts bottom padding
 * based on keyboard visibility and bottom navigation bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubmitButton(isFormValid: Boolean, isLoading: Boolean, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    val bottomNavPadding = LocalBottomPadding.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val effectiveBottomPadding = if (isKeyboardVisible) 12.dp else 12.dp + bottomNavPadding

    Surface(
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = effectiveBottomPadding)
                .height(56.dp),
            enabled = isFormValid && !isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.add_expense_submit_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
