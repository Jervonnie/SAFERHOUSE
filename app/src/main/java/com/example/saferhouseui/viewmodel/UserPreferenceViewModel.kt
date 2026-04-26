package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel

class UserPreferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("saferhouse_prefs", Context.MODE_PRIVATE)

    var language by mutableStateOf(prefs.getString("app_language", "en") ?: "en")
        private set

    var fontSize by mutableStateOf(prefs.getString("app_font_size", "Medium") ?: "Medium")
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        // Apply saved language on startup
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun setAppLanguage(newLanguage: String) {
        language = newLanguage
        prefs.edit { putString("app_language", newLanguage) }

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLanguage)
        AppCompatDelegate.setApplicationLocales(appLocale)
        
        triggerLoading() // Visual feedback for language change
    }

    fun setAppFontSize(newSize: String) {
        fontSize = newSize
        prefs.edit { putString("app_font_size", newSize) }
        triggerLoading()
    }

    fun triggerLoading() {
        isLoading = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isLoading = false
        }, 800)
    }
}
