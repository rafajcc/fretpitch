package com.fretpitch.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class TonePlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val ATTACK_MS = 8
        private const val DECAY_MS = 15
        private const val RELEASE_MS = 30
    }

    suspend fun playCorrect() = withContext(Dispatchers.IO) {
        playPleasantChime()
    }

    suspend fun playIncorrect() = withContext(Dispatchers.IO) {
        playMutedBlip()
    }

    suspend fun playStringTuned() = withContext(Dispatchers.IO) {
        playTunedConfirmation()
    }

    private fun playTunedConfirmation() {
        val durationMs = 250
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val sample = FloatArray(numSamples)

        val freqs = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.5f)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = computeEnvelope(i, numSamples, 5, 20, 60)

            var value = 0f
            for ((idx, freq) in freqs.withIndex()) {
                val delay = idx * 0.03f
                val tDelayed = t - delay
                if (tDelayed > 0f) {
                    val partialEnvelope = computeEnvelope(
                        (tDelayed * SAMPLE_RATE).toInt(),
                        numSamples, 5, 20, 60
                    )
                    value += sin(2.0 * PI * freq * tDelayed).toFloat() * (0.25f - idx * 0.04f) * partialEnvelope
                }
            }

            val shimmer = 0.85f + 0.15f * sin(2.0 * PI * 12f * t).toFloat()
            sample[i] = (value * envelope * shimmer * 0.5f).coerceIn(-1f, 1f)
        }

        playSamples(sample)
    }

    private fun playPleasantChime() {
        val durationMs = 180
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val sample = FloatArray(numSamples)

        val freq1 = 523.25f
        val freq2 = 659.25f
        val freq3 = 783.99f

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = computeEnvelope(i, numSamples, ATTACK_MS, DECAY_MS, RELEASE_MS)

            val s1 = sin(2.0 * PI * freq1 * t).toFloat() * 0.35f
            val s2 = sin(2.0 * PI * freq2 * t).toFloat() * 0.25f
            val s3 = sin(2.0 * PI * freq3 * t).toFloat() * 0.15f
            val fundamental = sin(2.0 * PI * freq1 * 0.5f * t).toFloat() * 0.1f

            val warmFilter = 0.7f + 0.3f * sin(2.0 * PI * 8f * t).toFloat()

            sample[i] = (s1 + s2 + s3 + fundamental) * envelope * warmFilter * 0.6f
        }

        playSamples(sample)
    }

    private fun playMutedBlip() {
        val durationMs = 120
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val sample = FloatArray(numSamples)

        val freq1 = 220f
        val freq2 = 165f

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = computeEnvelope(i, numSamples, 3, 8, 20)

            val s1 = sin(2.0 * PI * freq1 * t).toFloat() * 0.25f
            val s2 = sin(2.0 * PI * freq2 * t).toFloat() * 0.15f
            val noise = (Math.random().toFloat() - 0.5f) * 0.05f

            val dampening = 1.0f - (i.toFloat() / numSamples) * 0.7f

            sample[i] = (s1 + s2 + noise) * envelope * dampening * 0.4f
        }

        playSamples(sample)
    }

    private fun computeEnvelope(sampleIndex: Int, totalSamples: Int, attackMs: Int, decayMs: Int, releaseMs: Int): Float {
        val attackSamples = SAMPLE_RATE * attackMs / 1000
        val decaySamples = SAMPLE_RATE * decayMs / 1000
        val releaseSamples = SAMPLE_RATE * releaseMs / 1000
        val sustainLevel = 0.65f

        return when {
            sampleIndex < attackSamples -> {
                sampleIndex.toFloat() / attackSamples
            }
            sampleIndex < attackSamples + decaySamples -> {
                val decayProgress = (sampleIndex - attackSamples).toFloat() / decaySamples
                1.0f - (1.0f - sustainLevel) * decayProgress
            }
            sampleIndex > totalSamples - releaseSamples -> {
                val releaseProgress = (totalSamples - sampleIndex).toFloat() / releaseSamples
                sustainLevel * releaseProgress
            }
            else -> sustainLevel
        }
    }

    private fun playSamples(samples: FloatArray) {
        val pcm = ShortArray(samples.size) { i ->
            (samples[i] * 32767f).toInt().coerceIn(-32768, 32767).toShort()
        }

        val bufferSize = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()

        Thread.sleep((samples.size.toLong() * 1000) / SAMPLE_RATE)

        track.stop()
        track.release()
    }
}
