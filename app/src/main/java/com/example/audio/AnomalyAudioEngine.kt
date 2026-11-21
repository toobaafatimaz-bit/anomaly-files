package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.EquipmentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class AnomalyAudioEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile
    var isEnabled: Boolean = true

    fun playFootstep(running: Boolean) {
        if (!isEnabled) return
        val freq = if (running) 115.0 else 85.0
        val dur = if (running) 0.055 else 0.07
        playTone(freq = freq, durationSec = dur, volume = 0.25f, noiseMix = 0.35f)
    }

    fun playDoorToggle() {
        if (!isEnabled) return
        playSweep(startFreq = 160.0, endFreq = 95.0, durationSec = 0.22, volume = 0.3f)
    }

    fun playEquipmentPulse(equipment: EquipmentType) {
        if (!isEnabled) return
        when (equipment) {
            EquipmentType.REALITY_SCANNER -> playSweep(520.0, 960.0, 0.24, 0.28f)
            EquipmentType.THERMAL_VISION -> playTone(220.0, 0.12, 0.2f, 0.1f)
            EquipmentType.AUDIO_ANALYZER -> playSweep(300.0, 440.0, 0.18, 0.2f)
            EquipmentType.MEMORY_RECORDER -> playSweep(680.0, 340.0, 0.35, 0.25f)
            EquipmentType.SPECTRAL_LENS -> playTone(880.0, 0.16, 0.22f, 0.05f)
            EquipmentType.EVIDENCE_CAMERA -> playTone(1200.0, 0.045, 0.32f, 0.65f)
            EquipmentType.SIGNAL_DETECTOR -> playTone(1450.0, 0.04, 0.25f, 0.15f)
        }
    }

    fun playEmfTick(strength: Float) {
        if (!isEnabled || strength < 0.2f) return
        playTone(freq = 900.0 + strength * 700.0, durationSec = 0.025, volume = (strength * 0.22f).coerceIn(0.05f, 0.3f), noiseMix = 0.1f)
    }

    fun playEvidenceCollected() {
        if (!isEnabled) return
        scope.launch {
            playPcmBuffer(generateTonePcm(523.25, 0.09, 0.25f, 0f) + generateTonePcm(783.99, 0.14, 0.25f, 0f))
        }
    }

    fun playAnomalyAlert() {
        if (!isEnabled) return
        playSweep(startFreq = 190.0, endFreq = 65.0, durationSec = 0.45, volume = 0.36f, noiseMix = 0.25f)
    }

    private fun playTone(freq: Double, durationSec: Double, volume: Float, noiseMix: Float = 0f) {
        scope.launch {
            playPcmBuffer(generateTonePcm(freq, durationSec, volume, noiseMix))
        }
    }

    private fun playSweep(
        startFreq: Double,
        endFreq: Double,
        durationSec: Double,
        volume: Float,
        noiseMix: Float = 0f
    ) {
        scope.launch {
            val sampleRate = 22050
            val count = (sampleRate * durationSec).toInt().coerceAtLeast(1)
            val samples = ShortArray(count)
            var phase = 0.0
            for (i in 0 until count) {
                val t = i.toDouble() / count
                val freq = startFreq + (endFreq - startFreq) * t
                phase += 2.0 * PI * freq / sampleRate
                val env = exp(-t * 3.0)
                val tone = sin(phase)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * noiseMix
                val mixed = ((tone * (1.0 - noiseMix) + noise) * env * volume * Short.MAX_VALUE)
                samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(samples)
        }
    }

    private fun generateTonePcm(freq: Double, durationSec: Double, volume: Float, noiseMix: Float): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * durationSec).toInt().coerceAtLeast(1)
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val env = exp(-t * 4.5)
            val tone = sin(2.0 * PI * freq * i / sampleRate)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * noiseMix
            val mixed = ((tone * (1.0 - noiseMix) + noise) * env * volume * Short.MAX_VALUE)
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    private fun playPcmBuffer(samples: ShortArray) {
        runCatching {
            val sampleRate = 22050
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val byteSize = maxOf(minBuf, samples.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep((samples.size * 1000L / sampleRate) + 40L)
            track.stop()
            track.release()
        }
    }
}
