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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

/**
 * Displays a formatted amount with an optional per-character rolling entrance animation.
 *
 * When [shouldAnimate] is `true`, each character rolls in from above one by one (left to
 * right) with a short stagger, like an odometer settling. When `false`, it renders plain
 * [Text] with zero overhead.
 *
 * The caller controls **when** to animate — typically only when the balance value has
 * changed compared to a previously persisted snapshot.
 *
 * @param formattedAmount The pre-formatted currency string to display (e.g. "€1,234.56").
 * @param shouldAnimate   Whether to play the rolling entrance animation.
 * @param modifier        Optional [Modifier].
 * @param style           [TextStyle] applied to every character.
 * @param fontWeight      Optional [FontWeight] override.
 * @param color           Text color.
 * @param staggerDelayMs  Delay in ms between each character's animation start.
 * @param onAnimationComplete Called once after the full animation finishes.
 */
@Composable
fun AnimatedAmount(
    formattedAmount: String,
    shouldAnimate: Boolean,
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

    // Each character gets its own reveal index:
    //  - When revealedUpTo < index → character is "hidden" (targetState = 0)
    //  - When revealedUpTo >= index → character is "shown" (targetState = 1)
    // The AnimatedContent transition handles the rolling effect.
    var revealedUpTo by remember { mutableIntStateOf(-1) }

    LaunchedEffect(formattedAmount) {
        revealedUpTo = -1
        for (i in formattedAmount.indices) {
            delay(staggerDelayMs)
            revealedUpTo = i
        }
        // Allow the last character's animation to settle
        delay(300L)
        onAnimationComplete()
    }

    Row(modifier = modifier) {
        formattedAmount.forEachIndexed { index, char ->
            val revealed = index <= revealedUpTo

            key(index) {
                AnimatedContent(
                    targetState = revealed,
                    transitionSpec = {
                        if (targetState) {
                            // Entering: slide down from above + fade in
                            (slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialOffsetY = { -it }
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 150)
                            )) togetherWith (slideOutVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessHigh
                                ),
                                targetOffsetY = { it }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 50)
                            ))
                        } else {
                            // No reverse animation needed
                            (fadeIn(tween(0))) togetherWith (fadeOut(tween(0)))
                        }
                    },
                    label = "roll_$index"
                ) { isRevealed ->
                    Text(
                        text = if (isRevealed) char.toString() else char.toString(),
                        style = style,
                        fontWeight = fontWeight,
                        color = if (isRevealed) color else Color.Transparent
                    )
                }
            }
        }
    }
}
