package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.CourtTimerUiState
import com.example.util.TimeFormatUtils

@Composable
fun SaveSessionDialog(
    uiState: CourtTimerUiState,
    onDismiss: () -> Unit,
    onConfirmSave: (notes: String) -> Unit
) {
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Hearing Record",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Archive this session to local database with all participant minutes and transcript turns.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Case: ${uiState.caseTitle} (${uiState.caseNumber})\nTotal Time: ${TimeFormatUtils.formatMinutesAndSeconds(uiState.totalSpeakingMillis)} (${TimeFormatUtils.formatDecimalMinutes(uiState.totalSpeakingMillis)})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Session Notes / Ruling / Adjournment") },
                    placeholder = { Text("e.g. Oral argument concluded. Matter taken under advisement.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_session_notes_input"),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmSave(notesInput.trim()) },
                modifier = Modifier.testTag("confirm_save_session_btn")
            ) {
                Text("Save to Records")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
