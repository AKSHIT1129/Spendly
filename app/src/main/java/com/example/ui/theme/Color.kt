package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Spendly Brand Colors (Emerald, Mint, Slate, Luxury Indigo)
val EmeraldPrimary = Color(0xFF10B981) // Crisp financial green
val EmeraldLight = Color(0xFF34D399) // Soft cash inflow green
val MintAccent = Color(0xFF86EFAC) // Gentle mint green element (removed harsh neon)
val IndigoAccent = Color(0xFF6366F1) // Capital market deep indigo
val CoralRed = Color(0xFFF87171) // Safe elegant pastel red for outflows

// Raw values for constructing Light and Dark ColorSchemes in Theme.kt
val RawSlateBg = Color(0xFF0F1013) // Ultra-premium rich charcoal background
val RawSlateSurface = Color(0xFF17191E) // Distinct refined surface cards
val RawSlateCard = Color(0xFF22252C) // Slightly elevated charcoal surfaces
val RawSlateLine = Color(0xFF2D323E) // Minimal premium container border

val RawLightText = Color(0xFFF3F4F6) // Fresh light-gray body text
val RawGrayText = Color(0xFF9CA3AF) // Sophisticated subtext gray

val RawLightBg = Color(0xFFF9FAFB)
val RawLightSurface = Color(0xFFFFFFFF)
val RawLightCard = Color(0xFFF3F4F6)
val RawLightBorder = Color(0xFFE5E7EB)

val RawDarkText = Color(0xFF111827)
val RawDarkGrayText = Color(0xFF4B5563)

// Dynamic theme-aware colors mapping directly to current MaterialTheme.colorScheme properties
val SlateBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val SlateSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val SlateCard: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SlateLine: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val LightText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val GrayText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant
