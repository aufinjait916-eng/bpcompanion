package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.BpClassification
import com.example.data.model.BpReading
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun generateAndSharePdf(context: Context, readings: List<BpReading>) {
        if (readings.isEmpty()) return

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions in pt
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Primary Theme Colors
        val tealColor = Color.rgb(27, 106, 86)
        val mintColor = Color.rgb(56, 142, 60)
        val coralColor = Color.rgb(216, 67, 21)
        val blueColor = Color.rgb(21, 101, 192)
        val grayText = Color.rgb(100, 110, 105)
        val lightBg = Color.rgb(240, 246, 243)
        val darkText = Color.rgb(30, 40, 35)

        var y = 50f

        // Document Header
        paint.color = tealColor
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BP Companion", 40f, y, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = grayText
        val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Blood Pressure Log & Analytics Report • Generated $dateStr", 40f, y + 18f, paint)

        y += 42f

        // Summary Statistics Card
        val avgSys = (readings.map { it.systolic }.average()).toInt()
        val avgDia = (readings.map { it.diastolic }.average()).toInt()
        val inRangeCount = readings.count { it.getFeedback().classification == BpClassification.NORMAL }
        val inRangePercent = (inRangeCount * 100) / readings.size
        val pulses = readings.mapNotNull { it.pulse }
        val avgPulse = if (pulses.isNotEmpty()) pulses.average().toInt() else 0

        paint.color = lightBg
        canvas.drawRoundRect(40f, y, 555f, y + 54f, 8f, 8f, paint)

        paint.color = tealColor
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL READINGS", 55f, y + 20f, paint)
        canvas.drawText("AVERAGE BP", 175f, y + 20f, paint)
        canvas.drawText("WITHIN ±10 TARGET", 305f, y + 20f, paint)
        canvas.drawText("AVG PULSE", 450f, y + 20f, paint)

        paint.color = darkText
        paint.textSize = 14f
        canvas.drawText("${readings.size} records", 55f, y + 40f, paint)
        canvas.drawText("$avgSys / $avgDia mmHg", 175f, y + 40f, paint)
        canvas.drawText("$inRangePercent% ($inRangeCount)", 305f, y + 40f, paint)
        canvas.drawText(if (avgPulse > 0) "$avgPulse bpm" else "--", 450f, y + 40f, paint)

        y += 75f

        // Table Header
        paint.color = tealColor
        canvas.drawRect(40f, y, 555f, y + 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE & TIME", 48f, y + 16f, paint)
        canvas.drawText("SYS/DIA", 180f, y + 16f, paint)
        canvas.drawText("PULSE", 255f, y + 16f, paint)
        canvas.drawText("ARM", 310f, y + 16f, paint)
        canvas.drawText("EVALUATION", 360f, y + 16f, paint)
        canvas.drawText("NOTES", 470f, y + 16f, paint)

        y += 24f

        val sorted = readings.sortedByDescending { it.timestamp }
        val dateFormat = SimpleDateFormat("MM/dd/yy h:mm a", Locale.getDefault())

        sorted.take(30).forEachIndexed { index, r ->
            paint.color = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 249)
            canvas.drawRect(40f, y, 555f, y + 20f, paint)

            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = darkText
            canvas.drawText(dateFormat.format(Date(r.timestamp)), 48f, y + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${r.systolic}/${r.diastolic}", 180f, y + 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(if (r.pulse != null) "${r.pulse} bpm" else "--", 255f, y + 14f, paint)
            canvas.drawText(r.arm, 310f, y + 14f, paint)

            val feedback = r.getFeedback()
            paint.color = when (feedback.classification) {
                BpClassification.NORMAL -> mintColor
                BpClassification.HIGH -> coralColor
                BpClassification.LOW -> blueColor
                BpClassification.MIXED -> coralColor
            }
            val statusLabel = when (feedback.classification) {
                BpClassification.NORMAL -> "Normal (±10)"
                BpClassification.HIGH -> "High (+10)"
                BpClassification.LOW -> "Low (-10)"
                BpClassification.MIXED -> "Mixed"
            }
            canvas.drawText(statusLabel, 360f, y + 14f, paint)

            paint.color = grayText
            val noteSnippet = if (r.notes.length > 18) r.notes.take(16) + ".." else r.notes
            canvas.drawText(if (noteSnippet.isNotBlank()) noteSnippet else "--", 470f, y + 14f, paint)

            y += 20f
        }

        // Footer Notice
        paint.color = grayText
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Target Standard: 120/80 mmHg (Normal Corridor: 110-130 / 70-90 mmHg). Consult your physician for medical diagnosis.", 40f, 805f, paint)
        canvas.drawText("BP Companion App", 495f, 805f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache
        val file = File(context.cacheDir, "BP_Companion_Report.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        // Share / View via Android FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BP Companion - Blood Pressure PDF Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Download or Share PDF Report"))
    }

    fun exportToGoogleDriveSheets(context: Context, readings: List<BpReading>) {
        if (readings.isEmpty()) return

        val sorted = readings.sortedByDescending { it.timestamp }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val csvBuilder = StringBuilder()
        // CSV Header for Google Sheets
        csvBuilder.append("Timestamp,Date & Time,Systolic (mmHg),Diastolic (mmHg),Pulse (BPM),Measurement Arm,Status,Diff from 120,Diff from 80,Notes\n")

        sorted.forEach { r ->
            val feedback = r.getFeedback()
            val formattedDate = dateFormat.format(Date(r.timestamp))
            val cleanNotes = r.notes.replace("\"", "\"\"")
            val status = when (feedback.classification) {
                BpClassification.NORMAL -> "Within Standard (120/80 ± 10)"
                BpClassification.HIGH -> "High (+10 mmHg above standard)"
                BpClassification.LOW -> "Low (-10 mmHg below standard)"
                BpClassification.MIXED -> "Mixed divergence"
            }
            csvBuilder.append("\"${r.timestamp}\",")
            csvBuilder.append("\"$formattedDate\",")
            csvBuilder.append("${r.systolic},")
            csvBuilder.append("${r.diastolic},")
            csvBuilder.append("${r.pulse ?: ""},")
            csvBuilder.append("\"${r.arm}\",")
            csvBuilder.append("\"$status\",")
            csvBuilder.append("${feedback.systolicDiff},")
            csvBuilder.append("${feedback.diastolicDiff},")
            csvBuilder.append("\"$cleanNotes\"\n")
        }

        val file = File(context.cacheDir, "BP_Companion_Readings.csv")
        file.writeText(csvBuilder.toString())

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BP Companion Readings - Google Sheets")
            putExtra(
                Intent.EXTRA_TEXT,
                "Here are your blood pressure records from BP Companion. Select Google Drive to upload directly into Google Sheets."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Upload to Google Drive / Open as Sheets"))
    }
}
