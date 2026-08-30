package com.example.model

data class RelativePoint(
    val id: Int,
    val relativeX: Float, // 0.0f to 1.0f
    val relativeY: Float  // 0.0f to 1.0f
)

enum class SlicingPattern(
    val title: String,
    val description: String
) {
    SIX_POINT_DIAGRAM(
        "6-Point Zig-Zag (1→2→3→4→5→6)",
        "Image sequence: 1(Top-R) → 2(Top-L) → 3(Mid-L) → 4(Mid-R) → 5(Bot-R) → 6(Bot-L)"
    ),
    CROSS_CUT(
        "Cross Slash (X-Blade)",
        "Fast diagonal double cross slices across upper and mid fruit arcs"
    ),
    INFINITY_LOOP(
        "Infinity Sweep (∞)",
        "Continuous smooth figure-8 loop covering screen while avoiding bomb spawners"
    ),
    TOP_ARC_SAFE(
        "Upper Arc (Bomb Immune)",
        "Horizontal high-altitude sweeps strictly above bomb trajectory"
    )
}

data class SlicerConfig(
    val isRunning: Boolean = false,
    val speedLevel: Int = 5, // 1 to 10
    val pattern: SlicingPattern = SlicingPattern.SIX_POINT_DIAGRAM,
    val avoidBombs: Boolean = true,
    val touchRadius: Float = 24f,
    val multiFinger: Boolean = false,
    val vibrationFeedback: Boolean = true
) {
    // Calculates swipe duration based on speed level (1=100ms, 10=18ms)
    val strokeDurationMs: Long
        get() = (110 - (speedLevel * 9)).coerceIn(15, 120).toLong()

    // Calculates sleep interval between stroke dispatches (1=120ms, 10=5ms)
    val intervalDelayMs: Long
        get() = (130 - (speedLevel * 12)).coerceIn(5, 150).toLong()

    companion object {
        // Exact 6-point coordinates as indicated in the user image:
        // 1: Top Right, 2: Top Left, 3: Mid Left, 4: Mid Right, 5: Bottom Right, 6: Bottom Left
        val IMAGE_DIAGRAM_POINTS = listOf(
            RelativePoint(1, 0.88f, 0.22f), // Point 1 (Top-Right)
            RelativePoint(2, 0.15f, 0.25f), // Point 2 (Top-Left)
            RelativePoint(3, 0.15f, 0.52f), // Point 3 (Mid-Left)
            RelativePoint(4, 0.88f, 0.52f), // Point 4 (Mid-Right)
            RelativePoint(5, 0.88f, 0.80f), // Point 5 (Bottom-Right)
            RelativePoint(6, 0.15f, 0.75f)  // Point 6 (Bottom-Left)
        )
    }
}
