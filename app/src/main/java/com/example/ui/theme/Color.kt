package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// .hack//sign Aesthetic: "The World" System Palette
val HackBlack = Color(0xFF050505)
val HackDeepOrange = Color(0xFFFF6B00)
val HackAmber = Color(0xFFFFA500)
val HackCyan = Color(0xFF00D1FF)
val HackSlate = Color(0xFF1A1A24)
val HackGold = Color(0xFFD4AF37)
val HackMutedOrange = Color(0xFF8B4513)
val HackGlowOrange = Color(0xFFFF6B00).copy(alpha = 0.5f)

// Core Palette Mapping
val SteelBackground = HackBlack
val SteelSurface = Color(0xFF111118)
val SteelSurfaceVariant = Color(0xFF1A1A24)
val SteelSurfaceContainer = Color(0xFF22222E)
val SteelSurfaceElevated = Color(0xFF2D2D3B)
val SteelBorder = Color(0xFF333344)
val SteelBorderHighlight = Color(0xFF444455)

val CyberCyan = HackCyan
val WarningAmber = HackDeepOrange
val LaserEmerald = Color(0xFF00FF9D)
val SynthPurple = Color(0xFFA855F7)
val DangerRed = Color(0xFFFF3366)
val CyberCrimson = Color(0xFFFF003C)
val ElectricBlue = Color(0xFF007BFF)

val TextSilver = Color(0xFFE2E8F0)
val TextSteelSecondary = Color(0xFF94A3B8)
val TextSteelMuted = Color(0xFF64748B)

// Backwards compatibility aliases
val GunmetalBackground = HackBlack
val DeepCyan = Color(0xFF008B8B)
val LaserEmeraldDim = LaserEmerald.copy(alpha = 0.1f)
val WarningAmberDim = WarningAmber.copy(alpha = 0.1f)
val CyberCrimsonDim = CyberCrimson.copy(alpha = 0.1f)

val DarkBackground = GunmetalBackground
val DarkSurface = SteelSurface
val DarkSurfaceVariant = SteelSurfaceVariant
val DarkSurfaceContainer = SteelSurfaceContainer
val PrimaryBlue = CyberCyan
val PrimaryBlueVariant = DeepCyan
val SecondaryTeal = LaserEmerald
val AccentIndigo = ElectricBlue
val CleanGreen = LaserEmerald
val CleanGreenContainer = LaserEmeraldDim
val WarningAmberContainer = WarningAmberDim
val DangerRedContainer = CyberCrimsonDim

val TextPrimary = TextSilver
val TextSecondary = TextSteelSecondary
val TextTertiary = TextSteelMuted
