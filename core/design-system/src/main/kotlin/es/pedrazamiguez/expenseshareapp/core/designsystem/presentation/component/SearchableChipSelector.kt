package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
 * A reusable search-based multi-select component with autocomplete dropdown and removable chips.
 *
 * This component provides a clean UX for selecting multiple items from a large list,
 * with search/filter functionality and visual chips for selected items.
 *
 * @param T The type of items being selected
 * @param availableItems All items that can be searched and selected
 * @param selectedItems Currently selected items (displayed as chips)
 * @param onItemAdded Called when an item is selected from the dropdown
 * @param onItemRemoved Called when a chip is removed (item deselected)
 * @param itemKey Extracts a unique key from an item (used for filtering duplicates)
 * @param itemDisplayText Converts an item to its display text for chips and dropdown
 * @param itemSecondaryText Optional secondary text shown below the main text in dropdown
 * @param itemMatchesQuery Determines if an item matches the search query
 * @param excludedItems Items to exclude from search results (e.g., a "main" selection)
 * @param modifier Modifier to be applied to the component
 * @param title Optional title text displayed above the component
 * @param searchLabel Label for the search text field
 * @param searchPlaceholder Placeholder text for the search field
 * @param helperText Helper text shown when no items are selected and search is empty
 * @param chipRemoveContentDescription Content description for chip remove icon
 * @param clearSearchContentDescription Content description for clear search icon
 * @param searchIcon Icon for the search field leading icon
 * @param minQueryLength Minimum characters required before showing search results
 * @param maxSuggestions Maximum number of suggestions to show in dropdown
 * @param keyboardCapitalization Keyboard capitalization for the search field
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> SearchableChipSelector(
    availableItems: List<T>,
    selectedItems: List<T>,
    onItemAdded: (T) -> Unit,
    onItemRemoved: (T) -> Unit,
    itemKey: (T) -> Any,
    itemDisplayText: (T) -> String,
    itemMatchesQuery: (T, String) -> Boolean,
    modifier: Modifier = Modifier,
    excludedItems: List<T> = emptyList(),
    itemSecondaryText: ((T) -> String)? = null,
    title: String? = null,
    searchLabel: String = "",
    searchPlaceholder: String = "",
    helperText: String? = null,
    chipRemoveContentDescription: String? = null,
    clearSearchContentDescription: String? = null,
    searchIcon: ImageVector = Icons.Default.Search,
    minQueryLength: Int = 2,
    maxSuggestions: Int = 5,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Extract keys for efficient comparison
    val excludedKeys = remember(excludedItems) { excludedItems.map { itemKey(it) }.toSet() }
    val selectedKeys = remember(selectedItems) { selectedItems.map { itemKey(it) }.toSet() }

    // Filter items based on search query, excluding already selected and explicitly excluded items
    val filteredItems by remember(
        searchQuery,
        excludedKeys,
        selectedKeys,
        availableItems
    ) {
        derivedStateOf {
            if (searchQuery.length < minQueryLength) {
                emptyList()
            } else {
                availableItems
                    .filter { item ->
                        val key = itemKey(item)
                        key !in excludedKeys &&
                                key !in selectedKeys &&
                                itemMatchesQuery(item, searchQuery)
                    }
                    .take(maxSuggestions)
            }
        }
    }

    // Show suggestions when we have filtered results
    LaunchedEffect(filteredItems) {
        expanded = filteredItems.isNotEmpty()
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

        // Search field with autocomplete using ExposedDropdownMenuBox
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = if (searchLabel.isNotEmpty()) {
                    { Text(searchLabel) }
                } else null,
                placeholder = if (searchPlaceholder.isNotEmpty()) {
                    { Text(searchPlaceholder) }
                } else null,
                leadingIcon = { Icon(searchIcon, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = clearSearchContentDescription,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                expanded = false
                            }
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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

            // Only show dropdown when there are results
            if (filteredItems.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredItems.forEach { item ->
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
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
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
