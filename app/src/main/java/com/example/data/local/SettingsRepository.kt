package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AutoSortMode
import com.example.data.model.SortOption
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "steel_vault_user_settings",
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val autoSortEnabled = prefs.getBoolean(KEY_AUTO_SORT_ENABLED, true)
        val sortOptionName = prefs.getString(KEY_DEFAULT_SORT_OPTION, SortOption.DATE_DESC.name)
            ?: SortOption.DATE_DESC.name
        val sortOption = runCatching { SortOption.valueOf(sortOptionName) }.getOrDefault(SortOption.DATE_DESC)

        val autoSortModeName = prefs.getString(KEY_AUTO_SORT_MODE, AutoSortMode.SMART_SUBTYPES.name)
            ?: AutoSortMode.SMART_SUBTYPES.name
        val autoSortMode = runCatching { AutoSortMode.valueOf(autoSortModeName) }.getOrDefault(AutoSortMode.SMART_SUBTYPES)

        val autoSortOnScan = prefs.getBoolean(KEY_AUTO_SORT_ON_SCAN, true)
        val autoSuggestRenames = prefs.getBoolean(KEY_AUTO_SUGGEST_RENAMES, true)
        val autoHashSha256 = prefs.getBoolean(KEY_AUTO_HASH_SHA256, true)
        val enableGemini = prefs.getBoolean(KEY_ENABLE_GEMINI, true)
        val defaultCasing = prefs.getString(KEY_DEFAULT_CASING, "TITLE_CASE") ?: "TITLE_CASE"
        val defaultSpaceReplacement = prefs.getString(KEY_DEFAULT_SPACE_REPLACEMENT, "_") ?: "_"
        val groupDuplicates = prefs.getBoolean(KEY_GROUP_DUPLICATES_BY_HASH, true)

        return UserSettings(
            autoSortEnabled = autoSortEnabled,
            defaultSortOption = sortOption,
            autoSortDestinationMode = autoSortMode,
            autoSortOnScan = autoSortOnScan,
            autoSuggestRenamesOnScan = autoSuggestRenames,
            autoHashSha256OnScan = autoHashSha256,
            enableGeminiOnlineEnrichment = enableGemini,
            defaultCasing = defaultCasing,
            defaultSpaceReplacement = defaultSpaceReplacement,
            groupDuplicatesByHash = groupDuplicates
        )
    }

    fun updateAutoSortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SORT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(autoSortEnabled = enabled)
    }

    fun updateDefaultSortOption(option: SortOption) {
        prefs.edit().putString(KEY_DEFAULT_SORT_OPTION, option.name).apply()
        _settings.value = _settings.value.copy(defaultSortOption = option)
    }

    fun updateAutoSortDestinationMode(mode: AutoSortMode) {
        prefs.edit().putString(KEY_AUTO_SORT_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(autoSortDestinationMode = mode)
    }

    fun updateAutoSortOnScan(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SORT_ON_SCAN, enabled).apply()
        _settings.value = _settings.value.copy(autoSortOnScan = enabled)
    }

    fun updateAutoSuggestRenamesOnScan(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SUGGEST_RENAMES, enabled).apply()
        _settings.value = _settings.value.copy(autoSuggestRenamesOnScan = enabled)
    }

    fun updateAutoHashSha256OnScan(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_HASH_SHA256, enabled).apply()
        _settings.value = _settings.value.copy(autoHashSha256OnScan = enabled)
    }

    fun updateGeminiOnlineEnrichment(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLE_GEMINI, enabled).apply()
        _settings.value = _settings.value.copy(enableGeminiOnlineEnrichment = enabled)
    }

    fun updateDefaultCasing(casing: String) {
        prefs.edit().putString(KEY_DEFAULT_CASING, casing).apply()
        _settings.value = _settings.value.copy(defaultCasing = casing)
    }

    fun updateDefaultSpaceReplacement(replacement: String) {
        prefs.edit().putString(KEY_DEFAULT_SPACE_REPLACEMENT, replacement).apply()
        _settings.value = _settings.value.copy(defaultSpaceReplacement = replacement)
    }

    fun updateGroupDuplicatesByHash(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GROUP_DUPLICATES_BY_HASH, enabled).apply()
        _settings.value = _settings.value.copy(groupDuplicatesByHash = enabled)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = UserSettings()
    }

    companion object {
        private const val KEY_AUTO_SORT_ENABLED = "key_auto_sort_enabled"
        private const val KEY_DEFAULT_SORT_OPTION = "key_default_sort_option"
        private const val KEY_AUTO_SORT_MODE = "key_auto_sort_mode"
        private const val KEY_AUTO_SORT_ON_SCAN = "key_auto_sort_on_scan"
        private const val KEY_AUTO_SUGGEST_RENAMES = "key_auto_suggest_renames"
        private const val KEY_AUTO_HASH_SHA256 = "key_auto_hash_sha256"
        private const val KEY_ENABLE_GEMINI = "key_enable_gemini"
        private const val KEY_DEFAULT_CASING = "key_default_casing"
        private const val KEY_DEFAULT_SPACE_REPLACEMENT = "key_default_space_replacement"
        private const val KEY_GROUP_DUPLICATES_BY_HASH = "key_group_duplicates_by_hash"
    }
}
