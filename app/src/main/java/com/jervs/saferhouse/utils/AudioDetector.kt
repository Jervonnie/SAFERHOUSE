package com.jervs.saferhouse.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.*

class AudioDetector(
    private val context: Context,
    private val onKeywordDetected: () -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val keywords = listOf("help", "saklolo", "tulong", "emergency", "rescue")
    private var isListening = false

    fun startListening() {
        if (isListening) return
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("AudioDetector", "Ready for speech")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    Log.e("AudioDetector", "Error: $error")
                    // Restart listening on error (e.g., timeout)
                    if (isListening) {
                        isListening = false
                        startListening()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.forEach { result ->
                        Log.d("AudioDetector", "Heard: $result")
                        if (keywords.any { result.lowercase(Locale.ROOT).contains(it) }) {
                            Log.d("AudioDetector", "KEYWORD DETECTED!")
                            onKeywordDetected()
                        }
                    }
                    // Continue listening
                    if (isListening) {
                        isListening = false
                        startListening()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Add "fil-PH" if needed
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
