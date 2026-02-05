package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.AppOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldDefaultPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "",
                onValueChange = {},
                label = "Group name",
                placeholder = "Enter group name"
            )
        }
    }
}

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldFilledPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "Summer Trip 2026",
                onValueChange = {},
                label = "Group name"
            )
        }
    }
}

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldErrorPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "",
                onValueChange = {},
                label = "Group name",
                isError = true,
                supportingText = "Hey! Your group needs a name"
            )
        }
    }
}

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldWithIconsPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "user@example.com",
                onValueChange = {},
                label = "Email",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldDisabledPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "Disabled field",
                onValueChange = {},
                label = "Status",
                enabled = false
            )
        }
    }
}

@PreviewLocales
@Composable
private fun AppOutlinedTextFieldReadOnlyPreview() {
    PreviewThemeWrapper {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppOutlinedTextField(
                value = "EUR - Euro",
                onValueChange = {},
                label = "Main currency",
                readOnly = true,
                onClick = {}
            )
        }
    }
}
