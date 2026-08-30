package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.model.RelativePoint
import com.example.model.SlicerConfig
import com.example.model.SlicingPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutoSlicerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var slicingJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceActive.value = true
        activeInstance = this
        Log.d("AutoSlicer", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event interception needed, service handles gesture dispatching
    }

    override fun onInterrupt() {
        stopSlicing()
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSlicing()
        _isServiceActive.value = false
        if (activeInstance == this) {
            activeInstance = null
        }
        serviceScope.cancel()
    }

    fun startSlicing() {
        if (slicingJob?.isActive == true) return
        _slicerConfig.value = _slicerConfig.value.copy(isRunning = true)

        slicingJob = serviceScope.launch {
            var stepIndex = 0
            while (isActive && _slicerConfig.value.isRunning) {
                val config = _slicerConfig.value
                val metrics = resources.displayMetrics
                val screenWidth = metrics.widthPixels.toFloat()
                val screenHeight = metrics.heightPixels.toFloat()

                when (config.pattern) {
                    SlicingPattern.SIX_POINT_DIAGRAM -> {
                        val points = SlicerConfig.IMAGE_DIAGRAM_POINTS
                        val fromPoint = points[stepIndex % points.size]
                        val nextIndex = (stepIndex + 1) % points.size
                        val toPoint = points[nextIndex]

                        _currentSliceStep.value = fromPoint.id
                        dispatchSingleSwipe(
                            fromX = fromPoint.relativeX * screenWidth,
                            fromY = fromPoint.relativeY * screenHeight,
                            toX = toPoint.relativeX * screenWidth,
                            toY = toPoint.relativeY * screenHeight,
                            durationMs = config.strokeDurationMs
                        )
                        stepIndex++
                    }
                    SlicingPattern.CROSS_CUT -> {
                        // Fast X slices across upper & middle zones
                        val isDiagonal1 = (stepIndex % 2 == 0)
                        val fromX = if (isDiagonal1) screenWidth * 0.15f else screenWidth * 0.85f
                        val fromY = screenHeight * 0.25f
                        val toX = if (isDiagonal1) screenWidth * 0.85f else screenWidth * 0.15f
                        val toY = screenHeight * 0.65f

                        _currentSliceStep.value = (stepIndex % 4) + 1
                        dispatchSingleSwipe(fromX, fromY, toX, toY, config.strokeDurationMs)
                        stepIndex++
                    }
                    SlicingPattern.INFINITY_LOOP -> {
                        val phase = (stepIndex % 8) / 8f
                        val angle = phase * 2 * Math.PI
                        val centerX = screenWidth * 0.5f
                        val centerY = screenHeight * 0.45f
                        val radiusX = screenWidth * 0.35f
                        val radiusY = screenHeight * 0.20f

                        val fromX = (centerX + radiusX * Math.sin(angle)).toFloat()
                        val fromY = (centerY + radiusY * Math.sin(2 * angle) / 2f).toFloat()

                        val nextAngle = ((stepIndex + 1) % 8 / 8f) * 2 * Math.PI
                        val toX = (centerX + radiusX * Math.sin(nextAngle)).toFloat()
                        val toY = (centerY + radiusY * Math.sin(2 * nextAngle) / 2f).toFloat()

                        _currentSliceStep.value = (stepIndex % 6) + 1
                        dispatchSingleSwipe(fromX, fromY, toX, toY, config.strokeDurationMs)
                        stepIndex++
                    }
                    SlicingPattern.TOP_ARC_SAFE -> {
                        // Bomb safe horizontal sweeps at top/mid screen
                        val isLeftToRight = (stepIndex % 2 == 0)
                        val yPos = if (stepIndex % 4 < 2) screenHeight * 0.28f else screenHeight * 0.48f
                        val fromX = if (isLeftToRight) screenWidth * 0.12f else screenWidth * 0.88f
                        val toX = if (isLeftToRight) screenWidth * 0.88f else screenWidth * 0.12f

                        _currentSliceStep.value = (stepIndex % 4) + 1
                        dispatchSingleSwipe(fromX, yPos, toX, yPos, config.strokeDurationMs)
                        stepIndex++
                    }
                }

                delay(config.intervalDelayMs)
            }
        }
    }

    private fun dispatchSingleSwipe(
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        durationMs: Long
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        try {
            val path = Path().apply {
                moveTo(fromX, fromY)
                // Slight natural curve for slicing accuracy
                val midX = (fromX + toX) / 2f
                val midY = (fromY + toY) / 2f + 10f
                quadTo(midX, midY, toX, toY)
            }

            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(10))
            val gestureBuilder = GestureDescription.Builder().addStroke(stroke)

            // Optional multi-finger cut
            if (_slicerConfig.value.multiFinger) {
                val secondPath = Path().apply {
                    moveTo(fromX, fromY + 40f)
                    lineTo(toX, toY + 40f)
                }
                val secondStroke = GestureDescription.StrokeDescription(secondPath, 0, durationMs.coerceAtLeast(10))
                gestureBuilder.addStroke(secondStroke)
            }

            dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                }
            }, null)
        } catch (e: Exception) {
            Log.e("AutoSlicer", "Error dispatching gesture", e)
        }
    }

    fun stopSlicing() {
        slicingJob?.cancel()
        slicingJob = null
        _slicerConfig.value = _slicerConfig.value.copy(isRunning = false)
        _currentSliceStep.value = 0
    }

    fun updateConfig(config: SlicerConfig) {
        val wasRunning = _slicerConfig.value.isRunning
        _slicerConfig.value = config
        if (wasRunning && !config.isRunning) {
            stopSlicing()
        } else if (!wasRunning && config.isRunning) {
            startSlicing()
        }
    }

    companion object {
        var activeInstance: AutoSlicerAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _slicerConfig = MutableStateFlow(SlicerConfig())
        val slicerConfig: StateFlow<SlicerConfig> = _slicerConfig.asStateFlow()

        private val _currentSliceStep = MutableStateFlow(0)
        val currentSliceStep: StateFlow<Int> = _currentSliceStep.asStateFlow()

        fun toggleSlicing() {
            val current = _slicerConfig.value
            val nextState = !current.isRunning
            _slicerConfig.value = current.copy(isRunning = nextState)
            if (nextState) {
                activeInstance?.startSlicing()
            } else {
                activeInstance?.stopSlicing()
            }
        }

        fun setSpeedLevel(level: Int) {
            _slicerConfig.value = _slicerConfig.value.copy(speedLevel = level.coerceIn(1, 10))
        }

        fun setPattern(pattern: SlicingPattern) {
            _slicerConfig.value = _slicerConfig.value.copy(pattern = pattern)
        }

        fun setBombAvoidance(avoid: Boolean) {
            _slicerConfig.value = _slicerConfig.value.copy(avoidBombs = avoid)
        }
    }
}
