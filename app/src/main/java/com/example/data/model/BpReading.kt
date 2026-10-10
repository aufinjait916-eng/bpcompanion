package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BpClassification {
    NORMAL, // Both within +/- 10 mmHg of 120/80 (Sys: 110-130, Dia: 70-90)
    HIGH,   // Either Sys > 130 or Dia > 90
    LOW,    // Either Sys < 110 or Dia < 70 (and none high)
    MIXED   // One high and one low
}

data class BpFeedback(
    val classification: BpClassification,
    val title: String,
    val summary: String,
    val systolicDiff: Int, // systolic - 120
    val diastolicDiff: Int, // diastolic - 80
    val systolicStatus: String,
    val diastolicStatus: String,
    val advice: String
)

@Entity(tableName = "bp_readings")
data class BpReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val arm: String = "Left", // "Left" or "Right"
    val notes: String = ""
) {
    companion object {
        const val STANDARD_SYSTOLIC = 120
        const val STANDARD_DIASTOLIC = 80
        const val TOLERANCE = 10

        const val SYS_MIN_NORMAL = STANDARD_SYSTOLIC - TOLERANCE // 110
        const val SYS_MAX_NORMAL = STANDARD_SYSTOLIC + TOLERANCE // 130
        const val DIA_MIN_NORMAL = STANDARD_DIASTOLIC - TOLERANCE // 70
        const val DIA_MAX_NORMAL = STANDARD_DIASTOLIC + TOLERANCE // 90

        fun evaluate(systolic: Int, diastolic: Int): BpFeedback {
            val sysDiff = systolic - STANDARD_SYSTOLIC
            val diaDiff = diastolic - STANDARD_DIASTOLIC

            val sysIsHigh = systolic > SYS_MAX_NORMAL
            val sysIsLow = systolic < SYS_MIN_NORMAL
            val sysIsNormal = systolic in SYS_MIN_NORMAL..SYS_MAX_NORMAL

            val diaIsHigh = diastolic > DIA_MAX_NORMAL
            val diaIsLow = diastolic < DIA_MIN_NORMAL
            val diaIsNormal = diastolic in DIA_MIN_NORMAL..DIA_MAX_NORMAL

            val sysStatusText = when {
                sysIsHigh -> "High (+${sysDiff} mmHg above standard 120)"
                sysIsLow -> "Low (${sysDiff} mmHg below standard 120)"
                else -> "Within standard 120 (±10 mmHg)"
            }

            val diaStatusText = when {
                diaIsHigh -> "High (+${diaDiff} mmHg above standard 80)"
                diaIsLow -> "Low (${diaDiff} mmHg below standard 80)"
                else -> "Within standard 80 (±10 mmHg)"
            }

            val classification = when {
                (sysIsHigh && diaIsLow) || (sysIsLow && diaIsHigh) -> BpClassification.MIXED
                sysIsHigh || diaIsHigh -> BpClassification.HIGH
                sysIsLow || diaIsLow -> BpClassification.LOW
                else -> BpClassification.NORMAL
            }

            val title = when (classification) {
                BpClassification.NORMAL -> "Within Standard (120/80 ± 10)"
                BpClassification.HIGH -> "Higher than Standard (+10 mmHg)"
                BpClassification.LOW -> "Lower than Standard (-10 mmHg)"
                BpClassification.MIXED -> "Mixed Reading (Above & Below)"
            }

            val summary = when (classification) {
                BpClassification.NORMAL ->
                    "Great! Your reading of $systolic/$diastolic mmHg is within the standard healthy margin (Systolic 110–130, Diastolic 70–90)."
                BpClassification.HIGH ->
                    "Your reading of $systolic/$diastolic mmHg exceeds standard baseline (120/80) by more than 10 mmHg. Continue tracking and rest calmly."
                BpClassification.LOW ->
                    "Your reading of $systolic/$diastolic mmHg is more than 10 mmHg below standard baseline (120/80). Ensure proper hydration and rest."
                BpClassification.MIXED ->
                    "Your systolic and diastolic values diverge from the standard 120/80 range in opposite directions."
            }

            val advice = when (classification) {
                BpClassification.NORMAL ->
                    "Keep up your healthy daily habits, regular exercise, and balanced diet."
                BpClassification.HIGH ->
                    "Avoid caffeine or stress before measuring. If consistently above 140/90, consult your healthcare provider."
                BpClassification.LOW ->
                    "If you feel lightheaded, sit down and sip water. Contact your doctor if low readings persist with symptoms."
                BpClassification.MIXED ->
                    "Verify cuff placement at heart level and take a repeat measurement after 2 minutes of quiet rest."
            }

            return BpFeedback(
                classification = classification,
                title = title,
                summary = summary,
                systolicDiff = sysDiff,
                diastolicDiff = diaDiff,
                systolicStatus = sysStatusText,
                diastolicStatus = diaStatusText,
                advice = advice
            )
        }
    }

    fun getFeedback(): BpFeedback = evaluate(systolic, diastolic)
}
