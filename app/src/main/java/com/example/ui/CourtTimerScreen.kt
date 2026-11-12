package com.example.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeakerRole
import com.example.ui.components.CaseHeader
import com.example.ui.components.CourtSummaryView
import com.example.ui.components.EditSpeakerDialog
import com.example.ui.components.SaveSessionDialog
import com.example.ui.components.SessionHistoryDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeakerCard
import com.example.ui.components.SpeakerQuickBar
import com.example.ui.components.SpeakingTimeBarChart
import com.example.ui.components.SpeechTurnLogView
import com.example.ui.components.TimeDistributionBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtTimerScreen(
    viewModel: CourtTimerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pastSessions by viewModel.pastSessions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Keep Screen Awake
    val activity = context as? Activity
    DisposableEffect(uiState.keepScreenOn) {
        if (uiState.keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Status message snackbar
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val onShareSummary = {
        val summary = viewModel.generateExportSummary()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Courtroom Speaking Time Record", summary)
        clipboard?.setPrimaryClip(clip)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Courtroom Time Record")
        context.startActivity(shareIntent)

        scope.launch {
            snackbarHostState.showSnackbar("Summary copied to clipboard & share dialog opened")
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("court_timer_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SpeakerQuickBar(
                uiState = uiState,
                onSpeakerSelect = { role -> viewModel.toggleSpeaker(role) },
                onPauseAll = { viewModel.pauseAll() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Case Title & Hearing Timer Summary Header
            item(key = "case_header") {
                CaseHeader(
                    uiState = uiState,
                    onUpdateCaseInfo = { title, number -> viewModel.updateCaseInfo(title, number) },
                    onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                    onOpenHistory = { viewModel.setShowHistoryDialog(true) },
                    onOpenSave = { viewModel.setShowSaveDialog(true) },
                    onOpenReset = { viewModel.setShowResetDialog(true) },
                    onExportSummary = { onShareSummary() }
                )
            }

            // View Mode Tab Row: "Live Timers" vs "Summary & Bar Chart"
            item(key = "view_mode_tabs") {
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("view_mode_tabs"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("tab_live_timers"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Live Timers",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Live Timers",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    )

                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("tab_summary_chart"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Summary and Chart",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Summary & Chart",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    )
                }
            }

            if (uiState.selectedTab == 0) {
                // ================= TAB 0: LIVE TIMERS =================

                // Proportional Speaking Ratio Bar
                item(key = "distribution_bar") {
                    TimeDistributionBar(uiState = uiState)
                }

                // 1. JUDGE CARD
                item(key = "card_judge") {
                    val judge = uiState.speakers[SpeakerRole.JUDGE]
                    if (judge != null) {
                        SpeakerCard(
                            speaker = judge,
                            currentTimeMillis = uiState.currentTimeMillis,
                            isActive = uiState.activeSpeaker == SpeakerRole.JUDGE,
                            onToggle = { viewModel.toggleSpeaker(SpeakerRole.JUDGE) },
                            onEdit = { viewModel.setEditingSpeaker(SpeakerRole.JUDGE) }
                        )
                    }
                }

                // 2. PLAINTIFF CARD
                item(key = "card_plaintiff") {
                    val plaintiff = uiState.speakers[SpeakerRole.PLAINTIFF]
                    if (plaintiff != null) {
                        SpeakerCard(
                            speaker = plaintiff,
                            currentTimeMillis = uiState.currentTimeMillis,
                            isActive = uiState.activeSpeaker == SpeakerRole.PLAINTIFF,
                            onToggle = { viewModel.toggleSpeaker(SpeakerRole.PLAINTIFF) },
                            onEdit = { viewModel.setEditingSpeaker(SpeakerRole.PLAINTIFF) }
                        )
                    }
                }

                // 3. DEFENDANT CARD
                item(key = "card_defendant") {
                    val defendant = uiState.speakers[SpeakerRole.DEFENDANT]
                    if (defendant != null) {
                        SpeakerCard(
                            speaker = defendant,
                            currentTimeMillis = uiState.currentTimeMillis,
                            isActive = uiState.activeSpeaker == SpeakerRole.DEFENDANT,
                            onToggle = { viewModel.toggleSpeaker(SpeakerRole.DEFENDANT) },
                            onEdit = { viewModel.setEditingSpeaker(SpeakerRole.DEFENDANT) }
                        )
                    }
                }

                // Bar Chart also viewable directly on the Live tab
                item(key = "inline_bar_chart") {
                    SpeakingTimeBarChart(uiState = uiState)
                }

                // Speech Turns Transcript Timeline
                item(key = "turns_log") {
                    SpeechTurnLogView(
                        turns = uiState.turns,
                        onAddNote = { turnId, note -> viewModel.addNoteToTurn(turnId, note) }
                    )
                }
            } else {
                // ================= TAB 1: SUMMARY & BAR CHART VIEW =================
                item(key = "court_summary_content") {
                    CourtSummaryView(
                        uiState = uiState,
                        onSaveSession = { viewModel.setShowSaveDialog(true) },
                        onExportSummary = { onShareSummary() }
                    )
                }
            }

            // Bottom spacer to ensure scrolling clears the rapid switch dock
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Edit Speaker Dialog
    if (uiState.editingSpeaker != null) {
        val speaker = uiState.speakers[uiState.editingSpeaker!!]
        if (speaker != null) {
            EditSpeakerDialog(
                speaker = speaker,
                onDismiss = { viewModel.setEditingSpeaker(null) },
                onSave = { name, roleLabel, limit ->
                    viewModel.updateSpeakerDetails(speaker.role, name, roleLabel, limit)
                }
            )
        }
    }

    // Settings Dialog
    if (uiState.showSettingsDialog) {
        SettingsDialog(
            uiState = uiState,
            onDismiss = { viewModel.setShowSettingsDialog(false) },
            onToggleSound = { viewModel.toggleSoundCues(it) },
            onToggleHaptic = { viewModel.toggleHapticFeedback(it) },
            onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn(it) },
            onApplyOralArgumentPreset = { preset ->
                viewModel.updateSpeakerDetails(
                    SpeakerRole.PLAINTIFF,
                    uiState.speakers[SpeakerRole.PLAINTIFF]?.customName ?: SpeakerRole.PLAINTIFF.defaultName,
                    uiState.speakers[SpeakerRole.PLAINTIFF]?.customRoleLabel ?: SpeakerRole.PLAINTIFF.defaultRoleLabel,
                    preset
                )
                viewModel.updateSpeakerDetails(
                    SpeakerRole.DEFENDANT,
                    uiState.speakers[SpeakerRole.DEFENDANT]?.customName ?: SpeakerRole.DEFENDANT.defaultName,
                    uiState.speakers[SpeakerRole.DEFENDANT]?.customRoleLabel ?: SpeakerRole.DEFENDANT.defaultRoleLabel,
                    preset
                )
            }
        )
    }

    // Save Session Dialog
    if (uiState.showSaveDialog) {
        SaveSessionDialog(
            uiState = uiState,
            onDismiss = { viewModel.setShowSaveDialog(false) },
            onConfirmSave = { notes -> viewModel.saveCurrentSession(notes) }
        )
    }

    // Reset Confirmation Dialog
    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowResetDialog(false) },
            title = {
                Text(
                    text = "Reset Courtroom Timers?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will reset all speaking timers for Judge, Plaintiff, and Defendant to 0 and clear unarchived turns. If you wish to save this record, please save before resetting.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetSession() },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(
                        text = "Reset All",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowResetDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // History Records Dialog
    if (uiState.showHistoryDialog) {
        SessionHistoryDialog(
            sessions = pastSessions,
            selectedSession = uiState.selectedSessionForDetail,
            selectedTurns = uiState.selectedSessionTurns,
            onSelectSession = { session -> viewModel.loadSessionDetails(session) },
            onBackToList = { viewModel.clearSessionDetail() },
            onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
            onDismiss = {
                viewModel.clearSessionDetail()
                viewModel.setShowHistoryDialog(false)
            }
        )
    }
}
