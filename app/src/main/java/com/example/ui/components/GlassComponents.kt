package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderPink
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.GlassWhiteMedium
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = DarkSurface.copy(alpha = 0.75f),
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
    ) {
        content()
    }
}

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isCyan: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        listOf(
            if (isCyan) GlassBorderCyan else GlassBorderPink,
            Color.Transparent,
            GlassBorder
        )
    )

    Box(
        modifier = modifier
            .shadow(12.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkCard.copy(alpha = 0.85f),
                        DarkSurface.copy(alpha = 0.90f)
                    )
                )
            )
            .border(1.5.dp, borderBrush, shape)
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    gradient: Brush = Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF00B4D8))),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(10.dp, shape)
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
    ) {
        content()
    }
}
