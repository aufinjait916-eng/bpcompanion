package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BpReminder(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Blood Pressure Check",
    val hour: Int = 8,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val repeatDaily: Boolean = true,
    // Optional specific date (epoch millis) if user wants a reminder on a mentioned date and time
    val specificDate: Long? = null,
    val daysOfWeek: List<Int> = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    )
) {
    fun getFormattedTime(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(calendar.time)
    }

    fun getFormattedSchedule(): String {
        if (specificDate != null) {
            val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            return "Specific date: ${dateFormat.format(Date(specificDate))}"
        }
        if (repeatDaily || daysOfWeek.size == 7) {
            return "Every day"
        }
        val dayNames = daysOfWeek.map {
            when (it) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> ""
            }
        }.filter { it.isNotEmpty() }
        return dayNames.joinToString(", ")
    }
}
