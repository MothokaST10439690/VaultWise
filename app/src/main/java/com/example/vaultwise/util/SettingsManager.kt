package com.example.vaultwise.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SettingsManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "VaultWiseSettings"

        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"

        // Profile photos are saved per user, not shared between users
        private const val KEY_PROFILE_PHOTO_URI_PREFIX = "profile_photo_uri_user_"
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        applyTheme(enabled)
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun applyTheme(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun setLanguage(langCode: String) {
        prefs.edit()
            .putString(KEY_LANGUAGE, langCode)
            .apply()

        applyLanguage(langCode)
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun applyStoredLanguage() {
        applyLanguage(getLanguage())
    }

    fun setProfilePhotoUri(userId: Int, uri: String) {
        if (userId <= 0) return

        prefs.edit()
            .putString(profilePhotoKey(userId), uri)
            .apply()
    }

    fun getProfilePhotoUri(userId: Int): String? {
        if (userId <= 0) return null

        return prefs.getString(profilePhotoKey(userId), null)
    }

    fun clearProfilePhotoUri(userId: Int) {
        if (userId <= 0) return

        prefs.edit()
            .remove(profilePhotoKey(userId))
            .apply()
    }

    private fun profilePhotoKey(userId: Int): String {
        return "$KEY_PROFILE_PHOTO_URI_PREFIX$userId"
    }

    private fun applyLanguage(langCode: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(langCode)
        )
    }
}