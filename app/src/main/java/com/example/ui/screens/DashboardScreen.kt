package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SlicingPattern
import com.example.security.KeyManager
import com.example.service.AutoSlicerAccessibilityService
import com.example.service.FloatingWindowService
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderPink
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.GlassWhiteMedium
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    onOpenPracticeArena: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isAccessibilityActive by AutoSlicerAccessibilityService.isServiceActive.collectAsState()
    val isFloatingWindowActive by FloatingWindowService.isFloatingWindowRunning.collectAsState()
    val slicerConfig by AutoSlicerAccessibilityService.slicerConfig.collectAsState()
    val currentStep by AutoSlicerAccessibilityService.currentSliceStep.collectAsState()

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser_trail")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FRUIT NINJA AUTO SLICER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "VIP Activated • Anti-Bomb Enabled",
                        fontSize = 12.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        KeyManager.deactivate(context)
                        onLogout()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassWhiteLow)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = NeonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Checklist Card
            NeonGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isCyan = isAccessibilityActive && hasOverlayPermission
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM PERMISSIONS & ENGINE STATUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Accessibility Service
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAccessibilityActive) NeonGreen.copy(alpha = 0.1f) else NeonRed.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAccessibilityActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAccessibilityActive) NeonGreen else NeonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Accessibility Auto-Touch",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isAccessibilityActive) "Service Running & Ready" else "Tap to Enable Auto Slicing",
                                    fontSize = 11.sp,
                                    color = if (isAccessibilityActive) NeonGreen else NeonRed
                                )
                            }
                        }

                        if (!isAccessibilityActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonCyan)
                                    .clickable {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        context.startActivity(intent)
                                        Toast.makeText(
                                            context,
                                            "Enable 'Fruit Ninja Auto Slicer' in Accessibility Services",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("ENABLE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Overlay Permission
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasOverlayPermission) NeonGreen.copy(alpha = 0.1f) else NeonYellow.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) NeonGreen else NeonYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Floating Window Overlay",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (hasOverlayPermission) "Overlay Granted" else "Required for Floating HUD",
                                    fontSize = 11.sp,
                                    color = if (hasOverlayPermission) NeonGreen else NeonYellow
                                )
                            }
                        }

                        if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonYellow)
                                    .clickable {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("GRANT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Floating Window HUD Trigger & Master Toggle Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Floating Window",
                                tint = NeonPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FLOATING WINDOW HUD",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isFloatingWindowActive) "Active on Screen (Draggable)" else "Minimized / Hidden",
                                    fontSize = 11.sp,
                                    color = if (isFloatingWindowActive) NeonGreen else TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFloatingWindowActive) NeonRed.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f))
                                .clickable {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Please grant Overlay Permission first!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        if (isFloatingWindowActive) {
                                            FloatingWindowService.stopService(context)
                                        } else {
                                            FloatingWindowService.startService(context)
                                            Toast.makeText(context, "Floating HUD Started! You can drag it anywhere.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isFloatingWindowActive) "STOP HUD" else "START HUD",
                                color = if (isFloatingWindowActive) NeonRed else NeonPink,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Master Auto Slicer Action Button
                    val buttonBrush = if (slicerConfig.isRunning) {
                        Brush.horizontalGradient(listOf(NeonPink, NeonRed))
                    } else {
                        Brush.horizontalGradient(listOf(NeonCyan, NeonBlue))
                    }

                    GlassButton(
                        onClick = {
                            if (!isAccessibilityActive) {
                                Toast.makeText(context, "Please enable Accessibility Service first!", Toast.LENGTH_LONG).show()
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            } else {
                                AutoSlicerAccessibilityService.toggleSlicing()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("toggle_auto_slicer_button"),
                        gradient = buttonBrush
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (slicerConfig.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (slicerConfig.isRunning) "AUTO-SLICER ACTIVE (SLICING SCREEN)" else "START AUTO-SLICING SCREEN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Speed Controller Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkCard.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = NeonYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SLICING SPEED CONTROL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${slicerConfig.speedLevel}x (${slicerConfig.strokeDurationMs}ms / stroke)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = slicerConfig.speedLevel.toFloat(),
                        onValueChange = {
                            AutoSlicerAccessibilityService.setSpeedLevel(it.toInt())
                        },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonYellow,
                            activeTrackColor = NeonYellow,
                            inactiveTrackColor = GlassWhiteLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1x (Slow)", fontSize = 10.sp, color = TextMuted)
                        Text("5x (Standard)", fontSize = 10.sp, color = TextMuted)
                        Text("10x (Ultra Lightning)", fontSize = 10.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Point Trajectory Visualizer Card (Matching User Image Diagram)
            NeonGlassCard(
                modifier = Modifier.fillMaxWidth(),
                isCyan = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Trajectory",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DIAGRAM TRAJECTORY (1→2→3→4→5→6)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (slicerConfig.isRunning) {
                            Text(
                                text = "SLICING PT #$currentStep",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonPink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Canvas Preview of 1 -> 2 -> 3 -> 4 -> 5 -> 6
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkBackground.copy(alpha = 0.9f))
                            .border(1.dp, GlassBorderCyan, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            val w = size.width
                            val h = size.height

                            val pt1 = Offset(w * 0.88f, h * 0.22f)
                            val pt2 = Offset(w * 0.15f, h * 0.25f)
                            val pt3 = Offset(w * 0.15f, h * 0.52f)
                            val pt4 = Offset(w * 0.88f, h * 0.52f)
                            val pt5 = Offset(w * 0.88f, h * 0.80f)
                            val pt6 = Offset(w * 0.15f, h * 0.75f)

                            val points = listOf(pt1, pt2, pt3, pt4, pt5, pt6)

                            // Draw Bomb Hazard Zone (Bottom Center)
                            val bombZoneCenter = Offset(w * 0.5f, h * 0.88f)
                            drawCircle(
                                color = NeonRed.copy(alpha = 0.15f),
                                radius = w * 0.22f,
                                center = bombZoneCenter
                            )

                            // Connect points with sequence path
                            val path = Path().apply {
                                moveTo(pt1.x, pt1.y)
                                lineTo(pt2.x, pt2.y)
                                lineTo(pt3.x, pt3.y)
                                lineTo(pt4.x, pt4.y)
                                lineTo(pt5.x, pt5.y)
                                lineTo(pt6.x, pt6.y)
                                close()
                            }

                            drawPath(
                                path = path,
                                color = NeonCyan.copy(alpha = 0.35f),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Animated laser slicing trail along path
                            val activeSeg = (laserProgress * 6).toInt() % 6
                            val from = points[activeSeg]
                            val to = points[(activeSeg + 1) % 6]
                            val curX = from.x + (to.x - from.x) * ((laserProgress * 6) % 1f)
                            val curY = from.y + (to.y - from.y) * ((laserProgress * 6) % 1f)

                            drawLine(
                                color = NeonPink,
                                start = from,
                                end = Offset(curX, curY),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = Offset(curX, curY)
                            )

                            // Draw each point bubble with numbers 1..6
                            points.forEachIndexed { index, pt ->
                                val isCur = (currentStep == (index + 1))
                                drawCircle(
                                    color = if (isCur) NeonPink else NeonCyan,
                                    radius = if (isCur) 12.dp.toPx() else 9.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = DarkBackground,
                                    radius = if (isCur) 9.dp.toPx() else 7.dp.toPx(),
                                    center = pt
                                )
                            }
                        }

                        // Labels for each point overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2 (Top-L)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("1 (Top-R)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("3 (Mid-L)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("4 (Mid-R)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("6 (Bot-L)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("5 (Bot-R)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The 1→2→3→4→5→6 sequence sweeps across fruit arches while avoiding the bottom-center bomb spawner.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bomb Avoidance & Multi-Touch Options Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAFETY & ADVANCED OPTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellow,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bomb Avoidance Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bomb Avoidance Protection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Restricts cut coordinates away from bottom-center spawner",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = slicerConfig.avoidBombs,
                            onCheckedChange = {
                                AutoSlicerAccessibilityService.setBombAvoidance(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonGreen,
                                uncheckedTrackColor = GlassWhiteLow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi Finger Slash
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dual-Finger Multi Slash",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Dispatches parallel dual strokes for double combo multipliers",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = slicerConfig.multiFinger,
                            onCheckedChange = {
                                val cfg = slicerConfig.copy(multiFinger = it)
                                AutoSlicerAccessibilityService.activeInstance?.updateConfig(cfg)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPink,
                                uncheckedTrackColor = GlassWhiteLow
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Open Practice Arena & Fruit Ninja Game Shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // In-App Practice Arena Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(NeonGreen, Color(0xFF00B4D8))))
                        .clickable { onOpenPracticeArena() }
                        .testTag("practice_arena_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRACTICE ARENA",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                // Launch Fruit Ninja App
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(NeonPink, NeonOrange)))
                        .clickable {
                            val packageNames = listOf(
                                "com.halfbrick.fruitninjafree",
                                "com.halfbrick.fruitninja",
                                "com.halfbrick.fruitninjaclassic"
                            )
                            var launched = false
                            for (pkg in packageNames) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                    launched = true
                                    break
                                }
                            }
                            if (!launched) {
                                Toast.makeText(
                                    context,
                                    "Fruit Ninja not found! Launch Floating Window HUD and open Fruit Ninja game manually.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LAUNCH GAME",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Developer Watermark
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DEVELOPER: SHIBLU HASAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonYellow,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Custom Built Auto-Touch Automation for Android",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
