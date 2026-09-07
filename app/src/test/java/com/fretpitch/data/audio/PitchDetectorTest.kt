package com.fretpitch.data.audio

import com.fretpitch.data.mapper.FrequencyMapper
import com.fretpitch.domain.repository.PitchResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class PitchDetectorTest {

    private lateinit var pitchDetector: PitchDetectorImpl
    private val sampleRate = AudioCapture.SAMPLE_RATE

    @Before
    fun setup() {
        // Mocking AudioCapture would be cleaner, but for this precision test 
        // we want to test the detectPitch logic directly or via a controlled flow.
        // We'll use a simple manual instance for the algorithm test.
        pitchDetector = PitchDetectorImpl(
            audioCapture = org.mockito.Mockito.mock(AudioCapture::class.java),
            frequencyMapper = FrequencyMapper()
        )
    }

    @Test
    fun `detectPitch identifies 440Hz sine wave correctly`() {
        val frequency = 440f
        val buffer = generateSineWave(frequency, AudioCapture.BUFFER_SIZE_SAMPLES)
        
        val method = pitchDetector.javaClass.getDeclaredMethod("detectPitch", ShortArray::class.java)
        method.isAccessible = true
        val result = method.invoke(pitchDetector, buffer) as PitchResult?

        assertNotNull("Pitch result should not be null", result)
        assertEquals("Frequency should be 440Hz", frequency, result!!.frequency, 1.0f)
        assertTrue("Confidence should be high (low YIN value)", result.confidence < 0.1f)
    }

    @Test
    fun `detectPitch identifies low E string frequency (82,41Hz)`() {
        val frequency = 82.41f
        val buffer = generateSineWave(frequency, AudioCapture.BUFFER_SIZE_SAMPLES)
        
        val method = pitchDetector.javaClass.getDeclaredMethod("detectPitch", ShortArray::class.java)
        method.isAccessible = true
        val result = method.invoke(pitchDetector, buffer) as PitchResult?

        assertNotNull("Pitch result should not be null", result)
        assertEquals("Frequency should be ~82.4Hz", frequency, result!!.frequency, 1.0f)
    }

    private fun generateSineWave(freq: Float, size: Int): ShortArray {
        val buffer = ShortArray(size)
        for (i in 0 until size) {
            val angle = 2.0 * Math.PI * i * freq / sampleRate
            buffer[i] = (sin(angle) * 32767).toInt().toShort()
        }
        return buffer
    }
}
