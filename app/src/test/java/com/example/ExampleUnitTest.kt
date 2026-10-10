package com.example

import com.example.data.model.BpClassification
import com.example.data.model.BpReading
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testStandardNormalReading() {
        val feedback = BpReading.evaluate(120, 80)
        assertEquals(BpClassification.NORMAL, feedback.classification)
        assertEquals(0, feedback.systolicDiff)
        assertEquals(0, feedback.diastolicDiff)
    }

    @Test
    fun testWithinTenTolerance() {
        // Within 120 +/- 10 and 80 +/- 10
        val feedbackMin = BpReading.evaluate(110, 70)
        assertEquals(BpClassification.NORMAL, feedbackMin.classification)

        val feedbackMax = BpReading.evaluate(130, 90)
        assertEquals(BpClassification.NORMAL, feedbackMax.classification)
    }

    @Test
    fun testHighBloodPressure() {
        // More than +10 above 120/80
        val feedbackSysHigh = BpReading.evaluate(135, 80)
        assertEquals(BpClassification.HIGH, feedbackSysHigh.classification)

        val feedbackDiaHigh = BpReading.evaluate(120, 95)
        assertEquals(BpClassification.HIGH, feedbackDiaHigh.classification)
    }

    @Test
    fun testLowBloodPressure() {
        // More than -10 below 120/80
        val feedbackSysLow = BpReading.evaluate(105, 78)
        assertEquals(BpClassification.LOW, feedbackSysLow.classification)

        val feedbackDiaLow = BpReading.evaluate(118, 65)
        assertEquals(BpClassification.LOW, feedbackDiaLow.classification)
    }
}
