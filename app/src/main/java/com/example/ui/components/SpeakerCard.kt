package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeakerData
import com.example.model.SpeakerRole
import com.example.util.TimeFormatUtils

@Composable
fun SpeakerCard(
    speaker: SpeakerData,
    currentTimeMillis: Long,
    isActive: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elapsedMillis = speaker.currentElapsedMillis(currentTimeMillis)
    val role = speaker.role
    val roleColor = role.primaryColor
    val accentColor = role.accentColor

    // Pulsing animation for active speaker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${role.name}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = if (isActive) roleColor.copy(alpha = pulseAlpha) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        label = "border_color"
    )

    val cardElevation = if (isActive) 6.dp else 1.dp

    // Calculate time limit status if present
    val limitMinutes = speaker.timeLimitMinutes
    val limitMillis = (limitMinutes ?: 0) * 60 * 1000L
    val isOvertime = limitMinutes != null && elapsedMillis > limitMillis
    val isWarning = limitMinutes != null && !isOvertime && (limitMillis - elapsedMillis) <= 60000L

    val roleIcon = when (role) {
        SpeakerRole.JUDGE -> Icons.Default.Gavel
        SpeakerRole.PLAINTIFF -> Icons.Default.AccountBalance
        SpeakerRole.DEFENDANT -> Icons.Default.Shield
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaker_card_${role.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                roleColor.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        border = BorderStroke(if (isActive) 2.dp else 1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Role Icon, Name, and Edit action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = if (isActive) 0.25f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = "${role.title} icon",
                            tint = roleColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = speaker.customName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${role.title} • ${speaker.customRoleLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_${role.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${role.title} details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Display: Clock & Metric info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Clock digits
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = TimeFormatUtils.formatClockTime(elapsedMillis),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 36.sp,
                                letterSpacing = 1.sp
                            ),
                            color = when {
                                isOvertime -> Color(0xFFEF4444)
                                isWarning -> Color(0xFFF59E0B)
                                isActive -> roleColor
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = TimeFormatUtils.formatDecimalMinutes(elapsedMillis),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Turn counts and status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${speaker.turnCount} speech turns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (limitMinutes != null) {
                            Text(
                                text = " • Limit: ${limitMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isOvertime -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        isActive -> roleColor.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isOvertime -> Color(0xFFEF4444)
                            isActive -> roleColor.copy(alpha = pulseAlpha)
                            else -> Color.Transparent
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(roleColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = when {
                                isOvertime -> "OVERTIME"
                                isWarning -> "1M WARNING"
                                isActive -> "SPEAKING"
                                elapsedMillis > 0 -> "PAUSED"
                                else -> "READY"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = when {
                                isOvertime -> Color(0xFFEF4444)
                                isWarning -> Color(0xFFF59E0B)
                                isActive -> roleColor
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Time Limit Progress Bar (if limit is enabled)
            if (limitMinutes != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val progress = if (limitMillis > 0) {
                    (elapsedMillis.toFloat() / limitMillis).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        isOvertime -> Color(0xFFEF4444)
                        isWarning -> Color(0xFFF59E0B)
                        else -> roleColor
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (isOvertime) {
                    val overtimeMillis = elapsedMillis - limitMillis
                    Text(
                        text = "Exceeded allotted limit by ${TimeFormatUtils.formatMinutesAndSeconds(overtimeMillis)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BIG, ACCESSIBLE TOGGLE BUTTON
            Button(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("speaker_toggle_${role.name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) {
                        Color(0xFFDC2626) // Vivid red to pause
                    } else {
                        roleColor
                    },
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isActive) "Pause ${role.title} timer" else "Start ${role.title} timer",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) {
                            "PAUSE ${role.title.uppercase()} TIMER"
                        } else {
                            "START ${role.title.uppercase()} (${role.title})"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}
