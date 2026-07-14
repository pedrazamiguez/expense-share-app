package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ChevronRight
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.settings.R

private const val ROTATION_EXPANDED = 90f
private const val ROTATION_COLLAPSED = 0f

private val faqQuestions = listOf(
    R.string.faq_question_1 to R.string.faq_answer_1,
    R.string.faq_question_2 to R.string.faq_answer_2,
    R.string.faq_question_3 to R.string.faq_answer_3,
    R.string.faq_question_4 to R.string.faq_answer_4,
    R.string.faq_question_5 to R.string.faq_answer_5
)

@Composable
fun FaqScreen() {
    var expandedIndices by rememberSaveable { mutableStateOf(emptySet<Int>()) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(faqQuestions) { index, (questionRes, answerRes) ->
            val isExpanded = expandedIndices.contains(index)
            FlatCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.Default,
                        vertical = MaterialTheme.spacing.Small
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedIndices = if (isExpanded) {
                                expandedIndices - index
                            } else {
                                expandedIndices + index
                            }
                        }
                        .padding(MaterialTheme.spacing.Large)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = questionRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.Medium))
                        Icon(
                            imageVector = TablerIcons.Outline.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (isExpanded) ROTATION_EXPANDED else ROTATION_COLLAPSED)
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Text(
                            text = stringResource(id = answerRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.Medium)
                        )
                    }
                }
            }
        }
    }
}
