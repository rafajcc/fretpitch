package com.fretpitch.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    }

    suspend fun playCorrect() = withContext(Dispatchers.IO) {
        playTone(880f, 80)
        delay(50)
        playTone(1100f, 80)
    }

    suspend fun playIncorrect() = withContext(Dispatchers.IO) {
        playTone(440f, 100)
        delay(60)
        playTone(330f, 150)
    }

    private fun playTone(frequency: Float, durationMs: Int) {
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val sample = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val value = (sin(2.0 * PI * frequency * t) * 0.5 * 32767).toInt()
            sample[i] = value.coerceIn(-32768, 32767).toShort()
        }

        val bufferSize = sample.size * 2
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

        track.write(sample, 0, sample.size)
        track.play()

        Thread.sleep(durationMs.toLong())

        track.stop()
        track.release()
    }
}
