package com.example.saferhouseui.service

import android.content.Context
import android.media.AudioRecord
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import org.json.JSONObject
import java.io.IOException

/**
 * AudioAnalyzer handles dual-layer distress detection:
 * 1. YAMNet (TFLite) identifies broad acoustic distress (screaming, shouting).
 * 2. Vosk (Offline STT) verifies specific keywords (Saklolo, Help, Tulong).
 */
class AudioAnalyzer(
    private val context: Context,
    private val onDistressDetected: (String) -> Unit
) : RecognitionListener {

    companion object {
        private const val TAG = "AudioAnalyzer"
        private const val YAMNET_MODEL = "yamnet.tflite"
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private val DISTRESS_LABELS = listOf("Screaming", "Shouting", "Crying", "Yell", "Speech", "Human voice")
        private const val VOSK_MODEL_NAME = "model" // Folder name in assets
    }

    private var audioClassifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    
    private var voskModel: Model? = null
    private var speechService: SpeechService? = null
    
    private val analyzerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        initYamnet()
        initVosk()
    }

    private fun initYamnet() {
        try {
            val assets = context.assets.list("") ?: emptyArray()
            if (!assets.contains(YAMNET_MODEL)) {
                Log.e(TAG, "YAMNet model file ($YAMNET_MODEL) not found in assets. Sound detection disabled.")
                return
            }

            audioClassifier = AudioClassifier.createFromFileAndOptions(
                context, 
                YAMNET_MODEL,
                AudioClassifier.AudioClassifierOptions.builder().build()
            )
            Log.d(TAG, "YAMNet initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing YAMNet: ${e.message}")
        }
    }

    private fun initVosk() {
        // StorageService.unpack extracts the model from assets to internal storage if needed
        StorageService.unpack(context, VOSK_MODEL_NAME, VOSK_MODEL_NAME,
            { model: Model ->
                voskModel = model
                Log.d(TAG, "Vosk model loaded successfully")
            },
            { exception: IOException ->
                Log.e(TAG, "Failed to load Vosk model: ${exception.message}. Keyword detection disabled.")
            }
        )
    }

    fun startMonitoring() {
        val classifier = audioClassifier ?: return
        try {
            audioRecord = classifier.createAudioRecord()
            audioRecord?.startRecording()

            analyzerScope.launch {
                while (isActive) {
                    // Only run YAMNet if Vosk isn't currently verifying to avoid Mic conflict
                    if (speechService == null) {
                        try {
                            val audioTensor = classifier.createInputTensorAudio()
                            audioTensor.load(audioRecord)
                            val results = classifier.classify(audioTensor)
                            
                            val topResult = results.flatMap { it.categories }
                                .filter { it.score > CONFIDENCE_THRESHOLD }
                                .find { category -> DISTRESS_LABELS.any { it.equals(category.label, ignoreCase = true) } }

                            if (topResult != null) {
                                Log.d(TAG, "Broad distress detected: ${topResult.label} (${topResult.score})")
                                startVoskVerification(topResult.label)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "YAMNet Classification error: ${e.message}")
                        }
                    }
                    delay(500)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio monitoring: ${e.message}")
        }
    }

    private fun startVoskVerification(soundType: String) {
        val model = voskModel ?: run {
            Log.w(TAG, "Vosk model not available, triggering emergency on sound alone.")
            onDistressDetected("SOUND: $soundType")
            return
        }

        if (speechService != null) return

        try {
            // Stop YAMNet's AudioRecord to release the microphone for Vosk
            audioRecord?.stop()
            
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(this)
            Log.d(TAG, "Vosk verification activated for sound: $soundType")

            // Timeout after 10 seconds if no keyword is detected
            analyzerScope.launch {
                delay(10000)
                if (speechService != null) {
                    Log.d(TAG, "Vosk verification timed out. Resuming YAMNet.")
                    stopVosk()
                    audioRecord?.startRecording()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Vosk: ${e.message}")
            // Fallback to triggering on sound if Vosk fails
            onDistressDetected("SOUND: $soundType (Vosk Error)")
            audioRecord?.startRecording()
        }
    }

    private fun stopVosk() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
    }

    fun stopMonitoring() {
        analyzerScope.cancel()
        stopVosk()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord: ${e.message}")
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        // We can check partial results for even faster triggering if needed
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            try {
                val json = JSONObject(it)
                val text = json.optString("text", "").lowercase()
                Log.d(TAG, "Vosk recognized: $text")
                
                if (text.contains("help") || text.contains("saklolo") || text.contains("tulong")) {
                    Log.d(TAG, "Emergency keyword confirmed!")
                    onDistressDetected("KEYWORD: $text")
                    stopVosk()
                    // YAMNet will be restarted by the scope if needed, but 
                    // emergency state usually takes over the UI.
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error: ${e.message}")
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {
        onResult(hypothesis)
    }

    override fun onError(exception: Exception?) {
        Log.e(TAG, "Vosk Error: ${exception?.message}")
        stopVosk()
        audioRecord?.startRecording()
    }

    override fun onTimeout() {
        Log.d(TAG, "Vosk Timeout")
        stopVosk()
        audioRecord?.startRecording()
    }
}
