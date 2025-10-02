package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Create groups".placeholder)
    }
}
