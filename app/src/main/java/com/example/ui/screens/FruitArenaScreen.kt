package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SlicerConfig
import com.example.service.AutoSlicerAccessibilityService
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderPink
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

enum class FruitType(val emoji: String, val color: Color, val radius: Float, val isBomb: Boolean) {
    WATERMELON("🍉", Color(0xFF22C55E), 42f, false),
    ORANGE("🍊", Color(0xFFF97316), 36f, false),
    BANANA("🍌", Color(0xFFFBBF24), 38f, false),
    STRAWBERRY("🍓", Color(0xFFEF4444), 30f, false),
    PINEAPPLE("🍍", Color(0xFFEAB308), 44f, false),
    BOMB("💣", Color(0xFF475569), 38f, true)
}

data class ArenaFruit(
    val id: Long,
    val type: FruitType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var isSliced: Boolean = false,
    var sliceAngle: Float = 0f,
    var halfOffset: Float = 0f,
    var alpha: Float = 1f
)

data class SlashTrailPoint(
    val x: Float,
    val y: Float,
    val timestamp: Long
)

@Composable
fun FruitArenaScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val slicerConfig by AutoSlicerAccessibilityService.slicerConfig.collectAsState()

    var score by remember { mutableIntStateOf(0) }
    var comboCount by remember { mutableIntStateOf(0) }
    var comboText by remember { mutableStateOf<String?>(null) }
    var comboAlpha by remember { mutableFloatStateOf(0f) }

    val fruits = remember { mutableStateListOf<ArenaFruit>() }
    val slashPoints = remember { mutableStateListOf<SlashTrailPoint>() }

    // Game loop and physics simulation
    LaunchedEffect(Unit) {
        var fruitIdGen = 1L
        while (true) {
            val now = System.currentTimeMillis()

            // Spawn fruits periodically
            if (Random.nextFloat() < 0.08f && fruits.size < 8) {
                val isBomb = Random.nextFloat() < 0.22f // 22% chance of bomb
                val type = if (isBomb) FruitType.BOMB else {
                    val nonBombs = FruitType.values().filter { !it.isBomb }
                    nonBombs.random()
                }

                // Bomb spawns primarily at bottom center; fruits spawn at wider edges
                val spawnX = if (isBomb) {
                    Random.nextFloat() * 200f + 250f
                } else {
                    Random.nextFloat() * 600f + 50f
                }

                fruits.add(
                    ArenaFruit(
                        id = fruitIdGen++,
                        type = type,
                        x = spawnX,
                        y = 1100f,
                        vx = if (spawnX < 350f) Random.nextFloat() * 4f + 2f else -(Random.nextFloat() * 4f + 2f),
                        vy = -(Random.nextFloat() * 8f + 17f)
                    )
                )
            }

            // Update fruit physics (gravity, velocity)
            val iterator = fruits.iterator()
            while (iterator.hasNext()) {
                val fruit = iterator.next()
                fruit.x += fruit.vx
                fruit.y += fruit.vy
                fruit.vy += 0.38f // Gravity

                if (fruit.isSliced) {
                    fruit.halfOffset += 4f
                    fruit.alpha -= 0.04f
                }

                // Remove off-screen or faded fruits
                if (fruit.y > 1300f || fruit.alpha <= 0f) {
                    iterator.remove()
                }
            }

            // Remove old slash trails
            slashPoints.removeAll { now - it.timestamp > 180 }

            delay(16) // ~60fps
        }
    }

    // Auto Slicer simulation loop in Arena
    LaunchedEffect(slicerConfig.isRunning, slicerConfig.speedLevel) {
        if (!slicerConfig.isRunning) return@LaunchedEffect

        var step = 0
        val points = SlicerConfig.IMAGE_DIAGRAM_POINTS

        while (slicerConfig.isRunning) {
            val fromRelative = points[step % points.size]
            val toRelative = points[(step + 1) % points.size]

            val fromX = fromRelative.relativeX * 700f
            val fromY = fromRelative.relativeY * 1000f
            val toX = toRelative.relativeX * 700f
            val toY = toRelative.relativeY * 1000f

            // Trace stroke into slash points
            val stepsCount = 12
            var slicedInThisStroke = 0

            for (i in 0..stepsCount) {
                val t = i.toFloat() / stepsCount
                val curX = fromX + (toX - fromX) * t
                val curY = fromY + (toY - fromY) * t

                slashPoints.add(SlashTrailPoint(curX, curY, System.currentTimeMillis()))

                // Collision detection with fruits
                fruits.forEach { fruit ->
                    if (!fruit.isSliced) {
                        val dist = hypot(fruit.x - curX, fruit.y - curY)
                        if (dist < fruit.type.radius + 30f) {
                            if (fruit.type.isBomb) {
                                // If bomb avoidance is on, smart slicer trajectory already skips bombs!
                                if (!slicerConfig.avoidBombs) {
                                    // Hit bomb!
                                    fruit.isSliced = true
                                    score = (score - 50).coerceAtLeast(0)
                                    comboText = "BOMB HIT! -50"
                                    comboAlpha = 1f
                                }
                            } else {
                                // Sliced fruit!
                                fruit.isSliced = true
                                slicedInThisStroke++
                                score += 10

                                if (slicerConfig.vibrationFeedback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                        }
                    }
                }
            }

            if (slicedInThisStroke >= 2) {
                score += slicedInThisStroke * 15
                comboText = "COMBO x$slicedInThisStroke! +${slicedInThisStroke * 15}"
                comboAlpha = 1f
            }

            step++
            delay(slicerConfig.intervalDelayMs.coerceAtLeast(15))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val pos = change.position
                    slashPoints.add(SlashTrailPoint(pos.x, pos.y, System.currentTimeMillis()))

                    // Manual finger slice collision
                    fruits.forEach { fruit ->
                        if (!fruit.isSliced) {
                            val dist = hypot(fruit.x - pos.x, fruit.y - pos.y)
                            if (dist < fruit.type.radius + 35f) {
                                fruit.isSliced = true
                                if (fruit.type.isBomb) {
                                    score = (score - 50).coerceAtLeast(0)
                                    comboText = "BOMB DETONATED! -50"
                                    comboAlpha = 1f
                                } else {
                                    score += 10
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Game arena canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Draw celestial starfield grid background like Fruit Ninja Dojo
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )

            // Draw trajectory indicator points 1..6
            SlicerConfig.IMAGE_DIAGRAM_POINTS.forEach { pt ->
                val px = pt.relativeX * canvasW
                val py = pt.relativeY * canvasH

                drawCircle(
                    color = NeonCyan.copy(alpha = 0.25f),
                    radius = 16.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.6f),
                    radius = 6.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // Draw connecting diagram lines (1->2->3->4->5->6)
            val pts = SlicerConfig.IMAGE_DIAGRAM_POINTS.map { Offset(it.relativeX * canvasW, it.relativeY * canvasH) }
            val diagramPath = Path().apply {
                if (pts.isNotEmpty()) {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
                    close()
                }
            }
            drawPath(
                path = diagramPath,
                color = NeonCyan.copy(alpha = 0.15f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Safe Bomb Spawner Warning Zone (Bottom center)
            drawCircle(
                color = NeonRed.copy(alpha = 0.12f),
                radius = canvasW * 0.28f,
                center = Offset(canvasW * 0.5f, canvasH * 0.95f)
            )

            // Draw flying fruits & bombs
            fruits.forEach { fruit ->
                val fx = (fruit.x / 700f) * canvasW
                val fy = (fruit.y / 1000f) * canvasH

                if (fruit.isSliced) {
                    // Two fruit halves separating
                    val r = fruit.type.radius
                    val off = fruit.halfOffset
                    drawCircle(
                        color = fruit.type.color.copy(alpha = fruit.alpha),
                        radius = r * 0.8f,
                        center = Offset(fx - off, fy - off)
                    )
                    drawCircle(
                        color = fruit.type.color.copy(alpha = fruit.alpha),
                        radius = r * 0.8f,
                        center = Offset(fx + off, fy + off)
                    )
                } else {
                    // Whole fruit
                    drawCircle(
                        color = fruit.type.color,
                        radius = fruit.type.radius,
                        center = Offset(fx, fy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = fruit.type.radius * 0.4f,
                        center = Offset(fx - fruit.type.radius * 0.3f, fy - fruit.type.radius * 0.3f)
                    )
                }
            }

            // Draw glowing Katana blade trails
            if (slashPoints.size > 1) {
                val trailPath = Path().apply {
                    val first = slashPoints.first()
                    moveTo((first.x / 700f) * canvasW, (first.y / 1000f) * canvasH)
                    for (i in 1 until slashPoints.size) {
                        val pt = slashPoints[i]
                        lineTo((pt.x / 700f) * canvasW, (pt.y / 1000f) * canvasH)
                    }
                }

                // Outer neon glow
                drawPath(
                    path = trailPath,
                    color = NeonPink.copy(alpha = 0.5f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                // Core laser
                drawPath(
                    path = trailPath,
                    color = Color.White,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Header & Live Controls HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD: Score & Speed
            Column {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurface.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Score Display Card
                    GlassCard(
                        backgroundColor = DarkSurface.copy(alpha = 0.85f),
                        borderColor = GlassBorderPink
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCORE: ",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$score",
                                color = NeonYellow,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Reset Score
                    IconButton(
                        onClick = { score = 0; fruits.clear() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurface.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = NeonCyan
                        )
                    }
                }

                // Combo Text Popup
                if (comboText != null && comboAlpha > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comboText!!,
                            color = NeonPink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Bottom Arena Controls HUD
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface.copy(alpha = 0.90f),
                borderColor = GlassBorderCyan
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (slicerConfig.isRunning) NeonGreen else NeonYellow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIMULATED AUTO-SLICER",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Pattern 1→2→3→4→5→6",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ON / OFF Toggle Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (slicerConfig.isRunning) NeonPink else NeonCyan)
                                .clickable {
                                    AutoSlicerAccessibilityService.toggleSlicing()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (slicerConfig.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (slicerConfig.isRunning) Color.White else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (slicerConfig.isRunning) "STOP AUTO" else "START AUTO",
                                    color = if (slicerConfig.isRunning) Color.White else Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Speed Indicator & Quick Controls
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassWhiteLow)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${slicerConfig.speedLevel}x",
                                color = NeonYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    AutoSlicerAccessibilityService.setSpeedLevel(slicerConfig.speedLevel - 1)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("-", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            IconButton(
                                onClick = {
                                    AutoSlicerAccessibilityService.setSpeedLevel(slicerConfig.speedLevel + 1)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("+", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
