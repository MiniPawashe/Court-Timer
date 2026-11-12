package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeakerRole
import com.example.ui.CourtTimerUiState

@Composable
fun SpeakerQuickBar(
    uiState: CourtTimerUiState,
    onSpeakerSelect: (SpeakerRole) -> Unit,
    onPauseAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("speaker_quick_bar"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RAPID SPEAKER SWITCH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.activeSpeaker != null) {
                    Text(
                        text = "● ${uiState.activeSpeaker.title} speaking",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = uiState.activeSpeaker.primaryColor
                    )
                } else {
                    Text(
                        text = "All Timers Paused",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Judge Pill
                QuickSpeakerButton(
                    role = SpeakerRole.JUDGE,
                    icon = Icons.Default.Gavel,
                    isActive = uiState.activeSpeaker == SpeakerRole.JUDGE,
                    onClick = { onSpeakerSelect(SpeakerRole.JUDGE) },
                    modifier = Modifier.weight(1f)
                )

                // Plaintiff Pill
                QuickSpeakerButton(
                    role = SpeakerRole.PLAINTIFF,
                    icon = Icons.Default.AccountBalance,
                    isActive = uiState.activeSpeaker == SpeakerRole.PLAINTIFF,
                    onClick = { onSpeakerSelect(SpeakerRole.PLAINTIFF) },
                    modifier = Modifier.weight(1f)
                )

                // Defendant Pill
                QuickSpeakerButton(
                    role = SpeakerRole.DEFENDANT,
                    icon = Icons.Default.Shield,
                    isActive = uiState.activeSpeaker == SpeakerRole.DEFENDANT,
                    onClick = { onSpeakerSelect(SpeakerRole.DEFENDANT) },
                    modifier = Modifier.weight(1f)
                )

                // Pause All Button
                QuickPauseButton(
                    isAnyActive = uiState.activeSpeaker != null,
                    onClick = onPauseAll
                )
            }
        }
    }
}

@Composable
private fun QuickSpeakerButton(
    role: SpeakerRole,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roleColor = role.primaryColor
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) roleColor else roleColor.copy(alpha = 0.12f),
        label = "quick_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) Color.White else roleColor,
        label = "quick_content"
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color.White.copy(alpha = 0.8f) else roleColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("quick_button_${role.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role.title,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = role.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun QuickPauseButton(
    isAnyActive: Boolean,
    onClick: () -> Unit
) {
    val enabled = isAnyActive
    val bg = if (enabled) Color(0xFFDC2626) else MaterialTheme.colorScheme.surfaceVariant
    val tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("quick_pause_all"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Pause all timers",
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
