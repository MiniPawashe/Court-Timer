package com.example.model

import androidx.compose.ui.graphics.Color

enum class SpeakerRole(
    val title: String,
    val defaultName: String,
    val defaultRoleLabel: String,
    val primaryColorHex: Long,
    val accentColorHex: Long
) {
    JUDGE(
        title = "Judge",
        defaultName = "The Honorable Judge",
        defaultRoleLabel = "Presiding Bench",
        primaryColorHex = 0xFF7C3AED, // Royal Purple / Authority
        accentColorHex = 0xFFEAB308  // Judicial Gold
    ),
    PLAINTIFF(
        title = "Plaintiff",
        defaultName = "Plaintiff / Prosecution",
        defaultRoleLabel = "Moving Party",
        primaryColorHex = 0xFF2563EB, // Royal Court Blue
        accentColorHex = 0xFF60A5FA  // Sky Highlight
    ),
    DEFENDANT(
        title = "Defendant",
        defaultName = "Defendant / Defense",
        defaultRoleLabel = "Responding Party",
        primaryColorHex = 0xFFD97706, // Amber Bronze
        accentColorHex = 0xFFFBBF24  // Warm Gold
    );

    val primaryColor: Color get() = Color(primaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
}
