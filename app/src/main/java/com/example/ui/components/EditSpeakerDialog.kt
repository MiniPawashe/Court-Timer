package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.SpeakerData
import com.example.model.SpeakerRole

@Composable
fun EditSpeakerDialog(
    speaker: SpeakerData,
    onDismiss: () -> Unit,
    onSave: (newName: String, newRoleLabel: String, timeLimitMinutes: Int?) -> Unit
) {
    var nameInput by remember { mutableStateOf(speaker.customName) }
    var roleLabelInput by remember { mutableStateOf(speaker.customRoleLabel) }
    var limitInput by remember { mutableStateOf(speaker.timeLimitMinutes?.toString() ?: "") }
    var isUnlimited by remember { mutableStateOf(speaker.timeLimitMinutes == null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ${speaker.role.title} Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Participant Name") },
                    placeholder = { Text(speaker.role.defaultName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaker_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = roleLabelInput,
                    onValueChange = { roleLabelInput = it },
                    label = { Text("Title / Designation") },
                    placeholder = { Text(speaker.role.defaultRoleLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaker_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Time Limit Allotted (Minutes)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = isUnlimited,
                        onClick = {
                            isUnlimited = true
                            limitInput = ""
                        },
                        label = { Text("Unlimited") },
                        modifier = Modifier.testTag("chip_unlimited")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = !isUnlimited && limitInput == "15",
                        onClick = {
                            isUnlimited = false
                            limitInput = "15"
                        },
                        label = { Text("15 min") }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = !isUnlimited && limitInput == "30",
                        onClick = {
                            isUnlimited = false
                            limitInput = "30"
                        },
                        label = { Text("30 min") }
                    )
                }

                if (!isUnlimited) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Custom Limit (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("speaker_limit_input"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limit = if (isUnlimited) null else limitInput.toIntOrNull()
                    onSave(nameInput.trim(), roleLabelInput.trim(), limit)
                },
                modifier = Modifier.testTag("save_speaker_dialog")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
