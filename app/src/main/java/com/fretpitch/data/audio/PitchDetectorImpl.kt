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
        private const val CONFIDENCE_THRESHOLD = 0.4f
        private const val MIN_AMPLITUDE = 0.015f
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

        val windowed = FloatArray(floatBuffer.size) { i ->
            val window = 0.5f * (1f - kotlin.math.cos(
                (2.0 * Math.PI * i / (floatBuffer.size - 1)).toDouble()
            )).toFloat()
            floatBuffer[i] * window
        }

        val minLag = (AudioCapture.SAMPLE_RATE / MAX_FREQUENCY).toInt().coerceAtLeast(1)
        val maxLag = (AudioCapture.SAMPLE_RATE / MIN_FREQUENCY).toInt().coerceAtMost(windowed.size - 1)

        var bestLag = -1
        var bestValue = 0f

        for (lag in minLag..maxLag) {
            var sum = 0f
            var normA = 0f
            var normB = 0f

            for (i in 0 until windowed.size - lag) {
                sum += windowed[i] * windowed[i + lag]
                normA += windowed[i] * windowed[i]
                normB += windowed[i + lag] * windowed[i + lag]
            }

            val denominator = sqrt(normA * normB)
            val normalized = if (denominator > 0f) sum / denominator else 0f

            if (normalized > bestValue && normalized > CONFIDENCE_THRESHOLD) {
                bestValue = normalized
                bestLag = lag
            }
        }

        if (bestLag <= 0) return null

        val refinedLag = refineLag(windowed, bestLag, minLag, maxLag)
        val frequency = AudioCapture.SAMPLE_RATE / refinedLag

        if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) return null

        return PitchResult(
            frequency = frequency,
            confidence = bestValue,
            amplitude = rms
        )
    }

    private fun refineLag(buffer: FloatArray, lag: Int, minLag: Int, maxLag: Int): Float {
        if (lag <= minLag || lag >= maxLag) return lag.toFloat()

        val prev = autocorrelation(buffer, lag - 1)
        val curr = autocorrelation(buffer, lag)
        val next = autocorrelation(buffer, lag + 1)

        val denom = 2f * (2f * curr - prev - next)
        return if (denom > 0f) {
            lag - (next - prev) / denom
        } else {
            lag.toFloat()
        }
    }

    private fun autocorrelation(buffer: FloatArray, lag: Int): Float {
        var sum = 0f
        for (i in 0 until buffer.size - lag) {
            sum += buffer[i] * buffer[i + lag]
        }
        return sum
    }
}
