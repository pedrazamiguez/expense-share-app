package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * An async search-based multi-select component with autocomplete dropdown and removable chips.
 *
 * Unlike [SearchableChipSelector], this component does NOT filter items locally.
 * Instead, it delegates the search query to the caller via [onSearchQueryChanged],
 * which is responsible for fetching results asynchronously (e.g., from a remote API).
 * The caller provides the [searchResults] list, which is displayed in the dropdown.
 *
 * @param T The type of items being selected
 * @param searchResults Results returned from the async search (provided by caller/ViewModel)
 * @param selectedItems Currently selected items (displayed as chips)
 * @param onSearchQueryChanged Called when the search query changes (caller performs the search)
 * @param onItemAdded Called when an item is selected from the dropdown
 * @param onItemRemoved Called when a chip is removed (item deselected)
 * @param itemKey Extracts a unique key from an item
 * @param itemDisplayText Converts an item to its display text for chips and dropdown
 * @param itemSecondaryText Optional secondary text shown below the main text in dropdown
 * @param isSearching Whether an async search is in progress (shows loading indicator)
 * @param modifier Modifier to be applied to the component
 * @param title Optional title text displayed above the component
 * @param searchLabel Label for the search text field
 * @param searchPlaceholder Placeholder text for the search field
 * @param helperText Helper text shown when no items are selected and search is empty
 * @param noResultsText Text shown when a search returns no results
 * @param chipRemoveContentDescription Content description for chip remove icon
 * @param clearSearchContentDescription Content description for clear search icon
 * @param searchIcon Icon for the search field leading icon
 * @param minQueryLength Minimum characters required before triggering search
 * @param keyboardType Keyboard type for the search field
 * @param keyboardCapitalization Keyboard capitalization for the search field
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> AsyncSearchableChipSelector(
    searchResults: List<T>,
    selectedItems: List<T>,
    onSearchQueryChanged: (String) -> Unit,
    onItemAdded: (T) -> Unit,
    onItemRemoved: (T) -> Unit,
    itemKey: (T) -> Any,
    itemDisplayText: (T) -> String,
    isSearching: Boolean = false,
    modifier: Modifier = Modifier,
    itemSecondaryText: ((T) -> String)? = null,
    title: String? = null,
    searchLabel: String = "",
    searchPlaceholder: String = "",
    helperText: String? = null,
    noResultsText: String? = null,
    chipRemoveContentDescription: String? = null,
    clearSearchContentDescription: String? = null,
    searchIcon: ImageVector = Icons.Default.Search,
    minQueryLength: Int = 3,
    keyboardType: KeyboardType = KeyboardType.Email,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var hasSearchedOnce by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Show suggestions when we have results
    LaunchedEffect(searchResults) {
        expanded = searchResults.isNotEmpty()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Selected items as removable chips
        if (selectedItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selectedItems.forEach { item ->
                    key(itemKey(item)) {
                        InputChip(
                            selected = true,
                            onClick = { onItemRemoved(item) },
                            label = { Text(itemDisplayText(item)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = chipRemoveContentDescription,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Search field with autocomplete using ExposedDropdownMenuBox
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    if (newQuery.length >= minQueryLength) {
                        hasSearchedOnce = true
                        onSearchQueryChanged(newQuery)
                    } else {
                        expanded = false
                        // Clear results when query is too short
                        onSearchQueryChanged("")
                    }
                },
                label = if (searchLabel.isNotEmpty()) {
                    { Text(searchLabel) }
                } else null,
                placeholder = if (searchPlaceholder.isNotEmpty()) {
                    { Text(searchPlaceholder) }
                } else null,
                leadingIcon = { Icon(searchIcon, contentDescription = null) },
                trailingIcon = {
                    when {
                        isSearching -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                        searchQuery.isNotEmpty() -> Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = clearSearchContentDescription,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                expanded = false
                                hasSearchedOnce = false
                                onSearchQueryChanged("")
                            }
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done,
                    capitalization = keyboardCapitalization
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        expanded = false
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
            )

            // Show dropdown with results
            if (searchResults.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    searchResults.forEach { item ->
                        key(itemKey(item)) {
                            DropdownMenuItem(
                                text = {
                                    if (itemSecondaryText != null) {
                                        Column {
                                            Text(
                                                text = itemDisplayText(item),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = itemSecondaryText(item),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = itemDisplayText(item),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                onClick = {
                                    onItemAdded(item)
                                    searchQuery = ""
                                    expanded = false
                                    hasSearchedOnce = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }

        // No results text
        if (noResultsText != null) {
            AnimatedVisibility(
                visible = hasSearchedOnce &&
                        !isSearching &&
                        searchResults.isEmpty() &&
                        searchQuery.length >= minQueryLength
            ) {
                Text(
                    text = noResultsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Helper text
        if (helperText != null) {
            AnimatedVisibility(visible = selectedItems.isEmpty() && searchQuery.isEmpty()) {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

