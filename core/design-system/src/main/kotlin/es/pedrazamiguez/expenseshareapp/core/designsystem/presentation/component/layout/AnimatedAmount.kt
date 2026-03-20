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

/** Settle time (ms) for the last character's spring to visually rest after animation completes. */
private const val ANIMATION_SETTLE_DELAY_MS = 800L

/**
 * Groups the three text-appearance parameters shared by all animated-character composables.
 * Extracting them into a single value object keeps each composable below the
 * detekt [LongParameterList] threshold.
 */
private data class AnimationTextStyle(
    val style: TextStyle,
    val fontWeight: FontWeight?,
    val color: Color
)

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
 * Once the animation completes, the component keeps the same per-character [Row] layout
 * (instead of swapping to a single [Text]) to avoid any visible layout jump.
 *
 * @param formattedAmount     The current pre-formatted currency string (e.g. "€80.00").
 * @param shouldAnimate       Whether to play the rolling animation.
 * @param previousAmount      The previous value to transition **from** (e.g. "€100.00").
 *                            Ignored when [shouldAnimate] is `false`.
 * @param rollingUp           Direction hint for the rolling animation. `true` = digits roll
 *                            upward (value increased), `false` = digits roll downward.
 *                            The caller must determine direction from raw numeric values
 *                            because locale-formatted currency strings cannot be compared
 *                            lexicographically (e.g. "€10.00" < "€9.00" lexicographically).
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
    rollingUp: Boolean = true,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    staggerDelayMs: Long = 40L,
    onAnimationComplete: () -> Unit = {}
) {
    // Track whether we have ever animated for this formattedAmount.
    // Keyed on formattedAmount so state resets synchronously (same frame) when the value changes,
    // avoiding the 1-frame flicker that a LaunchedEffect-based reset would cause.
    var hasAnimated by remember(formattedAmount) { mutableStateOf(false) }

    // Stagger reveal: -1 = nothing revealed yet, advances left→right
    var revealedUpTo by remember(formattedAmount) { mutableStateOf(-1) }

    if (!shouldAnimate && !hasAnimated) {
        // Never animated for this value — plain Text, zero overhead
        Text(
            text = formattedAmount,
            style = style,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier
        )
        return
    }

    // Pad both strings to equal length so per-character slots align
    val maxLen = maxOf(formattedAmount.length, previousAmount.length)
    val paddedPrevious = previousAmount.padStart(maxLen)
    val paddedCurrent = formattedAmount.padStart(maxLen)

    LaunchedEffect(formattedAmount) {
        if (!shouldAnimate) return@LaunchedEffect

        for (i in paddedCurrent.indices) {
            delay(staggerDelayMs)
            revealedUpTo = i
        }
        // Let the last character's spring settle completely before removing AnimatedContent.
        // A MediumLow spring takes ~600-800ms to visually rest.
        delay(ANIMATION_SETTLE_DELAY_MS)
        hasAnimated = true
        onAnimationComplete()
    }

    val textStyle = AnimationTextStyle(style = style, fontWeight = fontWeight, color = color)

    Row(modifier = modifier) {
        AnimatedCharRow(
            paddedCurrent = paddedCurrent,
            paddedPrevious = paddedPrevious,
            hasAnimated = hasAnimated,
            shouldAnimate = shouldAnimate,
            revealedUpTo = revealedUpTo,
            rollingUp = rollingUp,
            textStyle = textStyle
        )
    }
}

@Composable
private fun AnimatedCharRow(
    paddedCurrent: String,
    paddedPrevious: String,
    hasAnimated: Boolean,
    shouldAnimate: Boolean,
    revealedUpTo: Int,
    rollingUp: Boolean,
    textStyle: AnimationTextStyle
) {
    paddedCurrent.forEachIndexed { index, newChar ->
        val oldChar = paddedPrevious.getOrElse(index) { ' ' }
        val displayChar = when {
            hasAnimated || !shouldAnimate -> newChar
            index <= revealedUpTo -> newChar
            else -> oldChar
        }
        key(index) {
            AnimatedCharSlot(
                displayChar = displayChar,
                newChar = newChar,
                oldChar = oldChar,
                hasAnimated = hasAnimated,
                shouldAnimate = shouldAnimate,
                rollingUp = rollingUp,
                textStyle = textStyle
            )
        }
    }
}

@Composable
private fun AnimatedCharSlot(
    displayChar: Char,
    newChar: Char,
    oldChar: Char,
    hasAnimated: Boolean,
    shouldAnimate: Boolean,
    rollingUp: Boolean,
    textStyle: AnimationTextStyle
) {
    val charChanged = oldChar != newChar
    if (hasAnimated || !shouldAnimate) {
        Text(
            text = if (displayChar == ' ') "" else displayChar.toString(),
            style = textStyle.style,
            fontWeight = textStyle.fontWeight,
            color = textStyle.color
        )
    } else {
        AnimatedCharContent(
            displayChar = displayChar,
            charChanged = charChanged,
            rollingUp = rollingUp,
            textStyle = textStyle
        )
    }
}

@Composable
private fun AnimatedCharContent(
    displayChar: Char,
    charChanged: Boolean,
    rollingUp: Boolean,
    textStyle: AnimationTextStyle
) {
    AnimatedContent(
        targetState = displayChar,
        transitionSpec = {
            if (charChanged) {
                val direction = if (rollingUp) -1 else 1
                (
                    slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetY = { fullHeight -> direction * fullHeight }
                    ) + fadeIn(animationSpec = tween(durationMillis = 150))
                    ) togetherWith (
                    slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetY = { fullHeight -> -direction * fullHeight }
                    ) + fadeOut(animationSpec = tween(durationMillis = 100))
                    )
            } else {
                (fadeIn(tween(0))) togetherWith (fadeOut(tween(0)))
            }
        },
        label = "roll_char"
    ) { char ->
        Text(
            text = if (char == ' ') "" else char.toString(),
            style = textStyle.style,
            fontWeight = textStyle.fontWeight,
            color = textStyle.color
        )
    }
}
