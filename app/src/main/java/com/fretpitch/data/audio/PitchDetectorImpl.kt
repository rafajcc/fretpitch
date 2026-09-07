package com.fretpitch.data.audio

import com.fretpitch.data.mapper.FrequencyMapper
import com.fretpitch.domain.repository.PitchDetector
import com.fretpitch.domain.repository.PitchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class PitchDetectorImpl @Inject constructor(
    private val audioCapture: AudioCapture,
    private val frequencyMapper: FrequencyMapper
) : PitchDetector {

    private val _pitchResults = MutableSharedFlow<PitchResult>(extraBufferCapacity = 64)
    private var detectionJob: Job? = null

    companion object {
        private const val MIN_FREQUENCY = 80f
        private const val MAX_FREQUENCY = 1100f
        private const val YIN_THRESHOLD = 0.15f
        private const val MIN_AMPLITUDE = 0.008f
    }

    override fun start() {
        if (detectionJob?.isActive == true) return

        audioCapture.start()

        detectionJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val buffer = audioCapture.readBuffer()
                if (buffer != null && buffer.isNotEmpty()) {
                    val result = detectPitch(buffer)
                    if (result != null) {
                        _pitchResults.emit(result)
                    }
                }
                delay(10)
            }
        }
    }

    override fun stop() {
        detectionJob?.cancel()
        detectionJob = null
        audioCapture.stop()
    }

    override fun pitchResults(): Flow<PitchResult> = _pitchResults

    private fun detectPitch(buffer: ShortArray): PitchResult? {
        if (buffer.size < 2) return null

        val floatBuffer = FloatArray(buffer.size) { buffer[it] / 32768f }

        var sumSquares = 0f
        for (sample in floatBuffer) {
            sumSquares += sample * sample
        }
        val rms = sqrt(sumSquares / floatBuffer.size)
        if (rms < MIN_AMPLITUDE) return null

        // YIN Algorithm
        val minLag = (AudioCapture.SAMPLE_RATE / MAX_FREQUENCY).toInt().coerceAtLeast(1)
        val maxLag = (AudioCapture.SAMPLE_RATE / MIN_FREQUENCY).toInt().coerceAtMost(floatBuffer.size / 2)

        // Step 1: Difference Function
        val yinBuffer = FloatArray(maxLag)
        for (tau in 1 until maxLag) {
            for (i in 0 until floatBuffer.size - tau) {
                val delta = floatBuffer[i] - floatBuffer[i + tau]
                yinBuffer[tau] += delta * delta
            }
        }

        // Step 2: Cumulative Mean Normalized Difference Function
        yinBuffer[0] = 1f
        var runningSum = 0f
        for (tau in 1 until maxLag) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / runningSum
        }

        // Step 3: Absolute Threshold
        var tau = -1
        for (t in minLag until maxLag) {
            if (yinBuffer[t] < YIN_THRESHOLD) {
                tau = t
                // Find the first local minimum below the threshold
                while (tau + 1 < maxLag && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                break
            }
        }

        // If no lag was found below threshold, use the global minimum
        if (tau == -1) {
            var minVal = 1f
            for (t in minLag until maxLag) {
                if (yinBuffer[t] < minVal) {
                    minVal = yinBuffer[t]
                    tau = t
                }
            }
        }

        if (tau == -1 || yinBuffer[tau] >= 0.5f) return null

        // Step 4: Parabolic Interpolation
        val refinedTau = if (tau > 0 && tau < maxLag - 1) {
            val s0 = yinBuffer[tau - 1]
            val s1 = yinBuffer[tau]
            val s2 = yinBuffer[tau + 1]
            tau + (s2 - s0) / (2f * (2f * s1 - s2 - s0))
        } else {
            tau.toFloat()
        }

        val frequency = AudioCapture.SAMPLE_RATE / refinedTau

        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) return null

        return PitchResult(
            frequency = frequency,
            confidence = yinBuffer[tau], // In YIN, lower is better confidence
            amplitude = rms
        )
    }
}
