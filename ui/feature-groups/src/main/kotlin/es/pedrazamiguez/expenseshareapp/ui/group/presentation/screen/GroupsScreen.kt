package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.ui.group.R
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.component.GroupCard

@Composable
fun GroupsScreen(
    groups: List<Group> = emptyList(),
    loading: Boolean = false,
    errorMessage: String? = null,
    selectedGroupId: String? = null,
    onGroupClicked: (String) -> Unit = { _ -> }
) {

    Box(modifier = Modifier.fillMaxSize()) {

        when {

            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            errorMessage != null -> Text(
                text = errorMessage,
                modifier = Modifier.align(Alignment.Center)
            )

            groups.isEmpty() -> Text(
                text = stringResource(R.string.groups_not_found),
                modifier = Modifier.align(Alignment.Center)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(groups) { group ->
                    GroupCard(
                        group = group,
                        isSelected = group.id == selectedGroupId,
                        onClick = onGroupClicked
                    )
                }

            }
        }
    }

}
