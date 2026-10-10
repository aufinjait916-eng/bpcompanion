package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.BillingManager
import com.example.data.local.AppDatabase
import com.example.data.model.BpReading
import com.example.data.model.BpReminder
import com.example.data.repository.BpRepository
import com.example.notification.BpNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class PeriodFilter(val label: String, val days: Long) {
    LAST_7_DAYS("7 Days", 7),
    LAST_30_DAYS("30 Days", 30),
    LAST_90_DAYS("90 Days", 90),
    ALL("All Time", 3650)
}

enum class AppThemeMode {
    LIGHT,
    DARK
}

class BpViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("calmbp_prefs", Context.MODE_PRIVATE)
    private val repository: BpRepository

    // Selected Tab: 0 = Measure & Log, 1 = Records & Trends, 2 = Notify
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Explicit Light/Dark Mode (persisted)
    private val _themeMode = MutableStateFlow(
        if (prefs.getBoolean("is_dark_mode", false)) AppThemeMode.DARK else AppThemeMode.LIGHT
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Period filter for Trends tab
    private val _selectedPeriod = MutableStateFlow(PeriodFilter.LAST_30_DAYS)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    // Last recorded reading for instant highlight
    private val _lastSavedReading = MutableStateFlow<BpReading?>(null)
    val lastSavedReading: StateFlow<BpReading?> = _lastSavedReading.asStateFlow()

    // In-App Paid Feature: Pro Unlocked state
    private val _isProUnlocked = MutableStateFlow(
        prefs.getBoolean("is_pro_unlocked", false)
    )
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    // Scheduled Reminders list
    private val _reminders = MutableStateFlow<List<BpReminder>>(loadRemindersFromPrefs())
    val reminders: StateFlow<List<BpReminder>> = _reminders.asStateFlow()

    // Official Google Play Billing integration
    private val billingManager: BillingManager = BillingManager(application) {
        unlockPro()
    }
    val formattedProPrice: StateFlow<String> = billingManager.formattedPrice
    val billingStatusMessage: StateFlow<String?> = billingManager.billingStatusMessage

    init {
        val database = AppDatabase.getInstance(application)
        repository = BpRepository(database.bpDao())

        BpNotificationHelper.createNotificationChannel(application)

        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    val allReadingsDesc: StateFlow<List<BpReading>> = repository.allReadingsDesc
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered readings based on the selected period
    val filteredReadings: StateFlow<List<BpReading>> = combine(
        repository.allReadingsAsc,
        _selectedPeriod
    ) { readings, period ->
        if (period == PeriodFilter.ALL) {
            readings
        } else {
            val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(period.days)
            readings.filter { it.timestamp >= cutoff }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleDarkMode() {
        val nextMode = if (_themeMode.value == AppThemeMode.LIGHT) AppThemeMode.DARK else AppThemeMode.LIGHT
        _themeMode.value = nextMode
        prefs.edit().putBoolean("is_dark_mode", nextMode == AppThemeMode.DARK).apply()
    }

    fun setPeriodFilter(period: PeriodFilter) {
        _selectedPeriod.value = period
    }

    fun saveReading(reading: BpReading) {
        viewModelScope.launch {
            repository.insertReading(reading)
            _lastSavedReading.value = reading
        }
    }

    fun deleteReading(reading: BpReading) {
        viewModelScope.launch {
            repository.deleteReading(reading)
            if (_lastSavedReading.value?.id == reading.id) {
                _lastSavedReading.value = null
            }
        }
    }

    fun addSampleWeekData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMillis = TimeUnit.DAYS.toMillis(1)
            val demo = listOf(
                BpReading(systolic = 118, diastolic = 77, pulse = 68, timestamp = now - (dayMillis * 5), arm = "Left", notes = "Morning after deep breathing"),
                BpReading(systolic = 125, diastolic = 83, pulse = 72, timestamp = now - (dayMillis * 4), arm = "Left", notes = "Evening check"),
                BpReading(systolic = 133, diastolic = 88, pulse = 80, timestamp = now - (dayMillis * 3), arm = "Right", notes = "Post commute"),
                BpReading(systolic = 121, diastolic = 79, pulse = 70, timestamp = now - (dayMillis * 2), arm = "Left", notes = "Standard target reading"),
                BpReading(systolic = 116, diastolic = 75, pulse = 66, timestamp = now - (dayMillis * 1), arm = "Left", notes = "Quiet evening reading")
            )
            demo.forEach { repository.insertReading(it) }
        }
    }

    // --- Google Play Billing & In-App Paid Feature ---

    fun launchGooglePlayPurchase(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restoreGooglePlayPurchases() {
        billingManager.restorePurchases()
    }

    fun clearBillingStatusMessage() {
        billingManager.clearStatusMessage()
    }

    fun unlockPro() {
        _isProUnlocked.value = true
        prefs.edit().putBoolean("is_pro_unlocked", true).apply()
        // If no reminders configured, seed two recommended slots
        if (_reminders.value.isEmpty()) {
            val morning = BpReminder(label = "Morning Baseline Check", hour = 8, minute = 0)
            val evening = BpReminder(label = "Evening Rest Check", hour = 20, minute = 0)
            addReminder(morning)
            addReminder(evening)
        }
    }

    fun lockPro() {
        _isProUnlocked.value = false
        prefs.edit().putBoolean("is_pro_unlocked", false).apply()
    }

    fun addReminder(reminder: BpReminder) {
        val updated = _reminders.value + reminder
        _reminders.value = updated
        saveRemindersToPrefs(updated)
        BpNotificationHelper.scheduleReminder(getApplication(), reminder)
    }

    fun toggleReminder(reminderId: String, isEnabled: Boolean) {
        val updated = _reminders.value.map {
            if (it.id == reminderId) it.copy(isEnabled = isEnabled) else it
        }
        _reminders.value = updated
        saveRemindersToPrefs(updated)

        val target = updated.find { it.id == reminderId }
        if (target != null) {
            if (isEnabled) {
                BpNotificationHelper.scheduleReminder(getApplication(), target)
            } else {
                BpNotificationHelper.cancelReminder(getApplication(), reminderId)
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        BpNotificationHelper.cancelReminder(getApplication(), reminderId)
        val updated = _reminders.value.filter { it.id != reminderId }
        _reminders.value = updated
        saveRemindersToPrefs(updated)
    }

    private fun loadRemindersFromPrefs(): List<BpReminder> {
        val raw = prefs.getString("saved_reminders_json", null) ?: return listOf(
            BpReminder(id = "default_morning", label = "Morning Baseline Check", hour = 8, minute = 0, isEnabled = true),
            BpReminder(id = "default_evening", label = "Evening Rest Check", hour = 20, minute = 0, isEnabled = true)
        )
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<BpReminder>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BpReminder(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        label = obj.optString("label", "Blood Pressure Check"),
                        hour = obj.optInt("hour", 8),
                        minute = obj.optInt("minute", 0),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        repeatDaily = obj.optBoolean("repeatDaily", true),
                        specificDate = if (obj.has("specificDate")) obj.optLong("specificDate") else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveRemindersToPrefs(list: List<BpReminder>) {
        val array = JSONArray()
        list.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("label", r.label)
                put("hour", r.hour)
                put("minute", r.minute)
                put("isEnabled", r.isEnabled)
                put("repeatDaily", r.repeatDaily)
                if (r.specificDate != null) {
                    put("specificDate", r.specificDate)
                }
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_reminders_json", array.toString()).apply()
    }
}
