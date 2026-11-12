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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeakerRole
import com.example.ui.CourtTimerUiState
import com.example.util.TimeFormatUtils
import kotlin.math.ceil
import kotlin.math.max

enum class ChartType {
    VERTICAL_COLUMNS,
    HORIZONTAL_BARS
}

@Composable
fun SpeakingTimeBarChart(
    uiState: CourtTimerUiState,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(ChartType.VERTICAL_COLUMNS) }

    val judgeMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.JUDGE)
    val plaintiffMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.PLAINTIFF)
    val defendantMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.DEFENDANT)
    val totalMillis = uiState.totalSpeakingMillis

    // Max millis among the 3 participants to scale bars (minimum 60s for clean empty state)
    val rawMax = max(max(judgeMillis, plaintiffMillis), defendantMillis)
    val maxMillis = max(rawMax, 60000L) // scale at least 1 minute

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaking_time_bar_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Chart Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar chart icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TIME DISTRIBUTION BAR CHART",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Comparative speaking minutes per participant",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Switch between vertical and horizontal layout
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == ChartType.VERTICAL_COLUMNS,
                        onClick = { chartType = ChartType.VERTICAL_COLUMNS },
                        label = { Text("Columns", fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = chartType == ChartType.HORIZONTAL_BARS,
                        onClick = { chartType = ChartType.HORIZONTAL_BARS },
                        label = { Text("Bars", fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (chartType == ChartType.VERTICAL_COLUMNS) {
                VerticalColumnsChart(
                    uiState = uiState,
                    maxMillis = maxMillis,
                    totalMillis = totalMillis
                )
            } else {
                HorizontalBarsChart(
                    uiState = uiState,
                    maxMillis = maxMillis,
                    totalMillis = totalMillis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Total Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Proceeding Time:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${TimeFormatUtils.formatMinutesAndSeconds(totalMillis)} (${TimeFormatUtils.formatDecimalMinutes(totalMillis)})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VerticalColumnsChart(
    uiState: CourtTimerUiState,
    maxMillis: Long,
    totalMillis: Long
) {
    val chartHeight = 180.dp
    val maxMinutes = (maxMillis / 60000.0).toFloat()
    val midMinutes = maxMinutes / 2f

    Column(modifier = Modifier.fillMaxWidth()) {
        // Grid reference lines and columns
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            // Background horizontal dashed/guide lines
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", maxMinutes)}m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    )
                }

                // Mid line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", midMinutes)}m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    )
                }

                // Base line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }

            // The 3 vertical bars placed above the lines
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 42.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                SpeakerRole.entries.forEach { role ->
                    val elapsed = uiState.getSpeakerElapsedMillis(role)
                    val pctOfMax = if (maxMillis > 0) (elapsed.toFloat() / maxMillis).coerceIn(0f, 1f) else 0f
                    val shareOfTotal = if (totalMillis > 0) (elapsed.toFloat() / totalMillis * 100f) else 0f
                    val isSpeaking = uiState.activeSpeaker == role

                    VerticalBarColumn(
                        role = role,
                        speaker = uiState.speakers[role],
                        elapsedMillis = elapsed,
                        pctOfMax = pctOfMax,
                        shareOfTotal = shareOfTotal,
                        isSpeaking = isSpeaking,
                        maxHeight = chartHeight - 24.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom labels for roles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpeakerRole.entries.forEach { role ->
                val speaker = uiState.speakers[role]
                val roleColor = role.primaryColor

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(76.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(roleColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = role.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = speaker?.customName ?: role.defaultName,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalBarColumn(
    role: SpeakerRole,
    speaker: com.example.model.SpeakerData?,
    elapsedMillis: Long,
    pctOfMax: Float,
    shareOfTotal: Float,
    isSpeaking: Boolean,
    maxHeight: androidx.compose.ui.unit.Dp
) {
    val animatedFraction by animateFloatAsState(
        targetValue = pctOfMax,
        animationSpec = tween(500),
        label = "bar_height_${role.name}"
    )

    val roleColor = role.primaryColor
    val barHeight = (maxHeight * animatedFraction).coerceAtLeast(8.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Value pill on top of bar
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSpeaking) roleColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = TimeFormatUtils.formatDecimalMinutes(elapsedMillis),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = if (isSpeaking) roleColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%.0f", shareOfTotal)}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = roleColor
                )
            }
        }

        // The Bar
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            roleColor,
                            roleColor.copy(alpha = 0.75f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun HorizontalBarsChart(
    uiState: CourtTimerUiState,
    maxMillis: Long,
    totalMillis: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SpeakerRole.entries.forEach { role ->
            val elapsed = uiState.getSpeakerElapsedMillis(role)
            val speaker = uiState.speakers[role]
            val pctOfMax = if (maxMillis > 0) (elapsed.toFloat() / maxMillis).coerceIn(0f, 1f) else 0f
            val shareOfTotal = if (totalMillis > 0) (elapsed.toFloat() / totalMillis * 100f) else 0f
            val isSpeaking = uiState.activeSpeaker == role
            val roleColor = role.primaryColor

            val animatedWidth by animateFloatAsState(
                targetValue = pctOfMax,
                animationSpec = tween(500),
                label = "h_bar_${role.name}"
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(roleColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${role.title} (${speaker?.customName ?: role.defaultName})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSpeaking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "● SPEAKING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = roleColor
                            )
                        }
                    }

                    Text(
                        text = "${TimeFormatUtils.formatMinutesAndSeconds(elapsed)} • ${TimeFormatUtils.formatDecimalMinutes(elapsed)} (${String.format("%.1f", shareOfTotal)}%)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = roleColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Track and filled bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedWidth.coerceAtLeast(0.015f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        roleColor.copy(alpha = 0.8f),
                                        roleColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
