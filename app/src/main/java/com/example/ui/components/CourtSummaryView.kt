package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlin.math.abs

@Composable
fun CourtSummaryView(
    uiState: CourtTimerUiState,
    onSaveSession: () -> Unit,
    onExportSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMillis = uiState.totalSpeakingMillis
    val judgeMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.JUDGE)
    val plaintiffMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.PLAINTIFF)
    val defendantMillis = uiState.getSpeakerElapsedMillis(SpeakerRole.DEFENDANT)

    val plaintiffShare = uiState.getSpeakerSharePercent(SpeakerRole.PLAINTIFF)
    val defendantShare = uiState.getSpeakerSharePercent(SpeakerRole.DEFENDANT)
    val judgeShare = uiState.getSpeakerSharePercent(SpeakerRole.JUDGE)

    val diffMillis = plaintiffMillis - defendantMillis
    val diffMinutes = abs(diffMillis) / 60000.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("court_summary_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Prominent Total Time Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_hero_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL PROCEEDINGS SPEAKING TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = TimeFormatUtils.formatClockTime(totalMillis),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 40.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = TimeFormatUtils.formatDecimalMinutes(totalMillis),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // Turn badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${uiState.turns.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Turns",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Case information bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Matter: ${uiState.caseTitle}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = uiState.caseNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. The Speaking Time Distribution Bar Chart
        SpeakingTimeBarChart(uiState = uiState)

        // 3. Proportional Distribution Bar (Segmented)
        TimeDistributionBar(uiState = uiState)

        // 4. Participant Detailed Breakdown Cards
        Text(
            text = "PARTICIPANT TIME BREAKDOWN",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpeakerRole.entries.forEach { role ->
                val speaker = uiState.speakers[role]
                val elapsed = uiState.getSpeakerElapsedMillis(role)
                val sharePct = uiState.getSpeakerSharePercent(role)
                val turnCount = speaker?.turnCount ?: 0
                val avgTurnMillis = if (turnCount > 0) elapsed / turnCount else 0L

                ParticipantSummaryMetricCard(
                    role = role,
                    customName = speaker?.customName ?: role.defaultName,
                    roleLabel = speaker?.customRoleLabel ?: role.defaultRoleLabel,
                    elapsedMillis = elapsed,
                    sharePercent = sharePct,
                    turnCount = turnCount,
                    avgTurnMillis = avgTurnMillis,
                    timeLimitMinutes = speaker?.timeLimitMinutes
                )
            }
        }

        // 5. Legal Trial Balance & Parity Insight
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trial_parity_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Trial balance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADVERSARIAL BALANCE & BENCH RATIO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val parityText = when {
                    totalMillis == 0L -> "No speaking time recorded yet."
                    diffMillis > 30000L -> "Plaintiff holds an advantage of ${String.format("%.1f", diffMinutes)} min (${String.format("%.1f", plaintiffShare - defendantShare)}% more floor time) over Defendant."
                    diffMillis < -30000L -> "Defendant holds an advantage of ${String.format("%.1f", diffMinutes)} min (${String.format("%.1f", defendantShare - plaintiffShare)}% more floor time) over Plaintiff."
                    else -> "Plaintiff and Defendant speaking time is essentially balanced within 30 seconds (${String.format("%.1f", plaintiffShare)}% vs ${String.format("%.1f", defendantShare)}%)."
                }

                Text(
                    text = parityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (totalMillis > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The Presiding Judge occupied ${String.format("%.1f", judgeShare)}% of total proceedings time across ${uiState.speakers[SpeakerRole.JUDGE]?.turnCount ?: 0} judicial exchanges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Action Buttons: Export & Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onExportSummary,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("summary_export_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export summary",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Record")
            }

            Button(
                onClick = onSaveSession,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("summary_save_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save session to database",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Records")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ParticipantSummaryMetricCard(
    role: SpeakerRole,
    customName: String,
    roleLabel: String,
    elapsedMillis: Long,
    sharePercent: Float,
    turnCount: Int,
    avgTurnMillis: Long,
    timeLimitMinutes: Int?
) {
    val roleColor = role.primaryColor
    val icon = when (role) {
        SpeakerRole.JUDGE -> Icons.Default.Gavel
        SpeakerRole.PLAINTIFF -> Icons.Default.AccountBalance
        SpeakerRole.DEFENDANT -> Icons.Default.Shield
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("metric_card_${role.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Name, Share Badge
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
                            .background(roleColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = role.title,
                            tint = roleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = customName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${role.title} • $roleLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = roleColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${String.format("%.1f", sharePercent)}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = roleColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Metric Row: Total Minutes, Turns, Avg Turn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Metric 1: Time
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "TOTAL MINUTES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeFormatUtils.formatMinutesAndSeconds(elapsedMillis),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = roleColor
                    )
                    Text(
                        text = TimeFormatUtils.formatDecimalMinutes(elapsedMillis),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Metric 2: Turns
                Column(modifier = Modifier.weight(0.9f)) {
                    Text(
                        text = "TURNS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$turnCount turns",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (timeLimitMinutes != null) "Limit: ${timeLimitMinutes}m" else "No limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Metric 3: Avg Duration
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AVG PER TURN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeFormatUtils.formatMinutesAndSeconds(avgTurnMillis),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = TimeFormatUtils.formatDecimalMinutes(avgTurnMillis),
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
