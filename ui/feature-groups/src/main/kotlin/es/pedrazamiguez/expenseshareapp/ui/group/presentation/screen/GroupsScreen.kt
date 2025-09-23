package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder

@Composable
fun GroupsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text("Groups".placeholder)
    }
}
