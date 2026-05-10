package com.example.saferhouseui.service

import android.content.Context
import android.media.AudioRecord
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.vosk.android.RecognitionListener
import org.json.JSONObject

class AudioAnalyzer(
    private val context: Context,
    private val onDistressDetected: (String) -> Unit
) : RecognitionListener {

    companion object {
        private const val TAG = "AudioAnalyzer"
        private const val YAMNET_MODEL = "yamnet.tflite"
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private val DISTRESS_LABELS = listOf("Screaming", "Shouting", "Crying", "Yell")
    }

    private var audioClassifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    
    private val analyzerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        initYamnet()
    }

    private fun initYamnet() {
        try {
            val assets = context.assets.list("") ?: emptyArray()
            val modelExists = assets.contains(YAMNET_MODEL)
            if (!modelExists) {
                Log.e(TAG, "YAMNet model file ($YAMNET_MODEL) not found in assets. Audio detection disabled.")
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

    fun startMonitoring() {
        val classifier = audioClassifier ?: return
        try {
            audioRecord = classifier.createAudioRecord()
            audioRecord?.startRecording()

            analyzerScope.launch {
                while (isActive) {
                    try {
                        val audioTensor = classifier.createInputTensorAudio()
                        audioTensor.load(audioRecord)
                        val results = classifier.classify(audioTensor)
                        
                        val topResult = results.flatMap { it.categories }
                            .filter { it.score > CONFIDENCE_THRESHOLD }
                            .find { category -> DISTRESS_LABELS.any { it.equals(category.label, ignoreCase = true) } }

                        if (topResult != null) {
                            Log.d(TAG, "Distress sound detected: ${topResult.label} (${topResult.score})")
                            onDistressDetected("SOUND: ${topResult.label}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Classification error: ${e.message}")
                    }
                    delay(500)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio monitoring: ${e.message}")
        }
    }

    fun stopMonitoring() {
        analyzerScope.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord: ${e.message}")
        }
    }

    override fun onPartialResult(hypothesis: String?) {}
    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            try {
                val json = JSONObject(it)
                val text = json.optString("text", "").lowercase()
                if (text.contains("help") || text.contains("saklolo") || text.contains("tulong")) {
                    onDistressDetected("KEYWORD: $text")
                }
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error: ${e.message}")
            }
        }
    }
    override fun onFinalResult(hypothesis: String?) {}
    override fun onError(exception: Exception?) {
        Log.e(TAG, "Vosk Error: ${exception?.message}")
    }
    override fun onTimeout() {}
}
