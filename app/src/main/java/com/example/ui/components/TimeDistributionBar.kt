package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeakerRole
import com.example.ui.CourtTimerUiState
import com.example.util.TimeFormatUtils

@Composable
fun TimeDistributionBar(
    uiState: CourtTimerUiState,
    modifier: Modifier = Modifier
) {
    val totalMillis = uiState.totalSpeakingMillis
    val judgeMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.JUDGE)
    val plaintiffMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.PLAINTIFF)
    val defendantMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.DEFENDANT)

    val judgeFraction = if (totalMillis > 0) judgeMillis.toFloat() / totalMillis else 0.333f
    val plaintiffFraction = if (totalMillis > 0) plaintiffMillis.toFloat() / totalMillis else 0.333f
    val defendantFraction = if (totalMillis > 0) defendantMillis.toFloat() / totalMillis else 0.334f

    val animatedJudge by animateFloatAsState(targetValue = judgeFraction, animationSpec = tween(400), label = "judge_pct")
    val animatedPlaintiff by animateFloatAsState(targetValue = plaintiffFraction, animationSpec = tween(400), label = "plaintiff_pct")
    val animatedDefendant by animateFloatAsState(targetValue = defendantFraction, animationSpec = tween(400), label = "def_pct")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("time_distribution_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEAKING TIME RATIO",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Total: ${TimeFormatUtils.formatMinutesAndSeconds(totalMillis)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment horizontal proportion bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (totalMillis > 0) {
                    Row(modifier = Modifier.matchParentSize()) {
                        if (animatedJudge > 0.001f) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedJudge.coerceAtLeast(0.001f))
                                    .fillMaxHeight()
                                    .background(SpeakerRole.JUDGE.primaryColor)
                            )
                        }
                        if (animatedPlaintiff > 0.001f) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedPlaintiff.coerceAtLeast(0.001f))
                                    .fillMaxHeight()
                                    .background(SpeakerRole.PLAINTIFF.primaryColor)
                            )
                        }
                        if (animatedDefendant > 0.001f) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedDefendant.coerceAtLeast(0.001f))
                                    .fillMaxHeight()
                                    .background(SpeakerRole.DEFENDANT.primaryColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpeakerRole.entries.forEach { role ->
                    val elapsed = uiState.getSpeakerElapsedMillis(role)
                    val pct = if (totalMillis > 0) (elapsed.toFloat() / totalMillis * 100f) else 0f

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(role.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = role.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.0f", pct)}% • ${TimeFormatUtils.formatClockTime(elapsed)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
