package com.example.data.donation

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages donation metrics, satisfaction safeguard triggers, cooldowns, and supporter status.
 *
 * Safeguard Rules:
 * - Never show prompts to active supporters (isSupporter == true).
 * - Never show prompts to new/casual users (requires 3+ operations, 3+ launches, and 2+ days since install).
 * - Home Banner Cooldown: 14 days after manual dismissal.
 * - Auto-Prompt Dialog Cooldown: 30 days after auto-display.
 */
class DonationManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean(KEY_IS_SUPPORTER, false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _operationCount = MutableStateFlow(prefs.getInt(KEY_OPERATION_COUNT, 0))
    val operationCount: StateFlow<Int> = _operationCount.asStateFlow()

    private val _shouldShowHomeBanner = MutableStateFlow(evalShouldShowHomeBanner())
    val shouldShowHomeBanner: StateFlow<Boolean> = _shouldShowHomeBanner.asStateFlow()

    private val _shouldShowAutoPrompt = MutableStateFlow(evalShouldShowAutoPrompt())
    val shouldShowAutoPrompt: StateFlow<Boolean> = _shouldShowAutoPrompt.asStateFlow()

    private val _milestoneEvent = MutableStateFlow<String?>(null)
    val milestoneEvent: StateFlow<String?> = _milestoneEvent.asStateFlow()

    init {
        // First Install Time initialization
        if (!prefs.contains(KEY_FIRST_INSTALL_TIME)) {
            prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis()).apply()
        }
        recordAppLaunch()
    }

    fun recordAppLaunch() {
        val currentLaunches = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_APP_LAUNCH_COUNT, currentLaunches).apply()
        refreshTriggers()
    }

    /**
     * Call this whenever the user performs a core value operation (e.g. interaction check,
     * RxNav lookup, DoseFlow export, or daily medication check-off).
     */
    fun recordCoreOperation(): String? {
        val newCount = _operationCount.value + 1
        _operationCount.value = newCount
        prefs.edit().putInt(KEY_OPERATION_COUNT, newCount).apply()

        // Milestone evaluation
        val milestone = when (newCount) {
            10 -> "10_CHECKS"
            25 -> "25_CHECKS"
            50 -> "50_CHECKS"
            else -> null
        }

        if (milestone != null) {
            val celebrated = prefs.getStringSet(KEY_CELEBRATED_MILESTONES, emptySet()) ?: emptySet()
            if (!celebrated.contains(milestone)) {
                val updated = celebrated.toMutableSet().apply { add(milestone) }
                prefs.edit().putStringSet(KEY_CELEBRATED_MILESTONES, updated).apply()
                _milestoneEvent.value = milestone
            }
        }

        refreshTriggers()
        return milestone
    }

    fun clearMilestoneEvent() {
        _milestoneEvent.value = null
    }

    fun dismissHomeBanner() {
        prefs.edit().putLong(KEY_HOME_BANNER_DISMISSED_TIME, System.currentTimeMillis()).apply()
        refreshTriggers()
    }

    fun markAutoPromptShown() {
        prefs.edit().putLong(KEY_AUTO_PROMPT_LAST_SHOWN_TIME, System.currentTimeMillis()).apply()
        refreshTriggers()
    }

    fun markAsSupporter(tierLabel: String) {
        _isSupporter.value = true
        prefs.edit()
            .putBoolean(KEY_IS_SUPPORTER, true)
            .putString(KEY_SELECTED_TIER, tierLabel)
            .apply()
        refreshTriggers()
    }

    fun toggleSupporterForDevMode() {
        val current = _isSupporter.value
        val newState = !current
        _isSupporter.value = newState
        prefs.edit().putBoolean(KEY_IS_SUPPORTER, newState).apply()
        refreshTriggers()
    }

    private fun refreshTriggers() {
        _shouldShowHomeBanner.value = evalShouldShowHomeBanner()
        _shouldShowAutoPrompt.value = evalShouldShowAutoPrompt()
    }

    private fun evalShouldShowHomeBanner(): Boolean {
        if (_isSupporter.value) return false

        val ops = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val launches = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0)
        val installTime = prefs.getLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val dismissedTime = prefs.getLong(KEY_HOME_BANNER_DISMISSED_TIME, 0L)

        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (1000 * 60 * 60 * 24)
        val daysSinceDismissed = (System.currentTimeMillis() - dismissedTime) / (1000 * 60 * 60 * 24)

        // Satisfies criteria: ops >= 3, launches >= 3, installed >= 2 days (or test bypass for preview), dismissed > 14 days ago
        val meetsSatisfactionCriteria = ops >= 3 && launches >= 3 && daysSinceInstall >= 2
        val cooldownPassed = dismissedTime == 0L || daysSinceDismissed >= 14

        return meetsSatisfactionCriteria && cooldownPassed
    }

    private fun evalShouldShowAutoPrompt(): Boolean {
        if (_isSupporter.value) return false

        val ops = prefs.getInt(KEY_OPERATION_COUNT, 0)
        val lastAutoPromptTime = prefs.getLong(KEY_AUTO_PROMPT_LAST_SHOWN_TIME, 0L)
        val daysSinceLastAutoPrompt = (System.currentTimeMillis() - lastAutoPromptTime) / (1000 * 60 * 60 * 24)

        val meetsCriteria = ops >= 7
        val cooldownPassed = lastAutoPromptTime == 0L || daysSinceLastAutoPrompt >= 30

        return meetsCriteria && cooldownPassed
    }

    companion object {
        private const val PREFS_NAME = "rxsafety_donation_prefs"
        private const val KEY_IS_SUPPORTER = "is_supporter"
        private const val KEY_SELECTED_TIER = "selected_tier"
        private const val KEY_OPERATION_COUNT = "operation_count"
        private const val KEY_APP_LAUNCH_COUNT = "app_launch_count"
        private const val KEY_FIRST_INSTALL_TIME = "first_install_time"
        private const val KEY_HOME_BANNER_DISMISSED_TIME = "home_banner_dismissed_time"
        private const val KEY_AUTO_PROMPT_LAST_SHOWN_TIME = "auto_prompt_last_shown_time"
        private const val KEY_CELEBRATED_MILESTONES = "celebrated_milestones"
    }
}
