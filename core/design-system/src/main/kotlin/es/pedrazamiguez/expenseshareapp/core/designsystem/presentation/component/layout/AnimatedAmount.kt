package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

/**
 * Displays a formatted amount with an optional per-character rolling animation.
 *
 * When [shouldAnimate] is `true`, the component transitions from [previousAmount] to
 * [formattedAmount] character by character — digits roll up or down like an odometer,
 * with a short stagger between each position. The old value is always visible until
 * each character rolls to its new value, so there is never a blank moment.
 *
 * When [shouldAnimate] is `false`, it renders plain [Text] with zero overhead.
 *
 * @param formattedAmount     The current pre-formatted currency string (e.g. "€80.00").
 * @param shouldAnimate       Whether to play the rolling animation.
 * @param previousAmount      The previous value to transition **from** (e.g. "€100.00").
 *                            Ignored when [shouldAnimate] is `false`.
 * @param modifier            Optional [Modifier].
 * @param style               [TextStyle] applied to every character.
 * @param fontWeight          Optional [FontWeight] override.
 * @param color               Text color.
 * @param staggerDelayMs      Delay in ms between each character's animation start.
 * @param onAnimationComplete Called once after the full animation finishes.
 */
@Composable
fun AnimatedAmount(
    formattedAmount: String,
    shouldAnimate: Boolean,
    previousAmount: String = "",
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    staggerDelayMs: Long = 40L,
    onAnimationComplete: () -> Unit = {}
) {
    if (!shouldAnimate || formattedAmount.isBlank()) {
        Text(
            text = formattedAmount,
            style = style,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier
        )
        return
    }

    // Pad the shorter string so both have equal length for per-character pairing.
    // Pad on the left for the numeric portion (keeps currency symbol aligned).
    val maxLen = maxOf(formattedAmount.length, previousAmount.length)
    val paddedPrevious = previousAmount.padStart(maxLen)
    val paddedCurrent = formattedAmount.padStart(maxLen)

    // Determine overall direction: value went up → roll upward, down → roll downward
    val rollingUp = formattedAmount >= previousAmount

    // Each slot holds the character currently displayed. Starts with the old value.
    // As revealedUpTo advances, each slot transitions from old char → new char.
    var revealedUpTo by remember { mutableStateOf(-1) }

    LaunchedEffect(formattedAmount) {
        revealedUpTo = -1
        for (i in paddedCurrent.indices) {
            delay(staggerDelayMs)
            revealedUpTo = i
        }
        // Let the last character's spring settle
        delay(300L)
        onAnimationComplete()
    }

    Row(modifier = modifier) {
        paddedCurrent.forEachIndexed { index, newChar ->
            val oldChar = paddedPrevious.getOrElse(index) { ' ' }
            // Target state: show old char until this index is revealed, then new char
            val displayChar = if (index <= revealedUpTo) newChar else oldChar
            val charChanged = oldChar != newChar

            key(index) {
                AnimatedContent(
                    targetState = displayChar,
                    transitionSpec = {
                        if (charChanged) {
                            val direction = if (rollingUp) -1 else 1
                            (slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialOffsetY = { fullHeight -> direction * fullHeight }
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 150)
                            )) togetherWith (slideOutVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                targetOffsetY = { fullHeight -> -direction * fullHeight }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 100)
                            ))
                        } else {
                            // Character didn't change — instant swap, no animation
                            (fadeIn(tween(0))) togetherWith (fadeOut(tween(0)))
                        }
                    },
                    label = "roll_$index"
                ) { char ->
                    Text(
                        text = if (char == ' ') "" else char.toString(),
                        style = style,
                        fontWeight = fontWeight,
                        color = color
                    )
                }
            }
        }
    }
}
