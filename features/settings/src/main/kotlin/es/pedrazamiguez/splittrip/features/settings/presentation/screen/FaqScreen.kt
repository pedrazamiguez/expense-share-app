package es.pedrazamiguez.splittrip.features.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.component.FaqAccordionItem

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.ExtraLarge,
            vertical = MaterialTheme.spacing.ExtraLarge
        )
    ) {
        item {
            FlatCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    faqQuestions.forEachIndexed { index, (questionRes, answerRes) ->
                        val isExpanded = expandedIndices.contains(index)
                        FaqAccordionItem(
                            questionRes = questionRes,
                            answerRes = answerRes,
                            isExpanded = isExpanded,
                            onToggle = {
                                expandedIndices = if (isExpanded) {
                                    expandedIndices - index
                                } else {
                                    expandedIndices + index
                                }
                            }
                        )

                        if (index < faqQuestions.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
