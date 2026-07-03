package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.Attendance
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    /**
     * Escapes a string value for CSV format.
     */
    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Generates a CSV string representation of a list of attendance records.
     */
    fun generateCsv(records: List<Attendance>): String {
        val sb = StringBuilder()
        // Headers
        sb.append("ID,Date,Day,Time,Student Name,Status,Recorded By,Teacher Notes\n")
        for (record in records) {
            sb.append(record.id).append(",")
            sb.append(escapeCsv(record.date)).append(",")
            sb.append(escapeCsv(record.day)).append(",")
            sb.append(escapeCsv(record.time)).append(",")
            sb.append(escapeCsv(record.studentName)).append(",")
            sb.append(escapeCsv(record.status)).append(",")
            sb.append(escapeCsv(record.recordedBy)).append(",")
            sb.append(escapeCsv(record.teacherNotes)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Truncates text so it does not overflow column widths in the PDF.
     */
    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isNotEmpty()) "$truncated..." else "..."
    }

    /**
     * Generates a beautifully styled PDF containing attendance records and writes it to the output stream.
     * Supports pagination, column styling, summary metrics, and status color coding.
     */
    fun generatePdf(context: Context, records: List<Attendance>, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()

        // Page dimensions
        val pageWidth = 595 // A4 width
        val pageHeight = 842 // A4 height

        // Calculate statistics
        val totalCount = records.size
        val presentCount = records.count { it.status.lowercase() == "present" }
        val absentCount = records.count { it.status.lowercase() == "absent" }
        val leaveCount = records.count { it.status.lowercase() == "leave" }

        // Define paints
        val titlePaint = Paint().apply {
            color = 0xFF004D40.toInt() // Dark Emerald Green
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val metaPaint = Paint().apply {
            color = 0xFF555555.toInt()
            textSize = 9f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = 0xFFE0F2F1.toInt() // Soft teal background
            style = Paint.Style.FILL
        }

        val headerTextPaint = Paint().apply {
            color = 0xFF004D40.toInt()
            textSize = 10f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val contentTextPaint = Paint().apply {
            color = 0xFF212121.toInt()
            textSize = 9f
            isAntiAlias = true
        }

        val statusPresentPaint = Paint().apply {
            color = 0xFF2E7D32.toInt() // Success Green
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val statusAbsentPaint = Paint().apply {
            color = 0xFFC62828.toInt() // Error Red
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val statusLeavePaint = Paint().apply {
            color = 0xFFEF6C00.toInt() // Warning Amber
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = 0xFFE0E0E0.toInt()
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Define helper function to draw page and headers
        fun drawPageDecorations(canvas: Canvas, isFirstPage: Boolean, tableStartY: Float) {
            if (isFirstPage) {
                // Header Banner Title
                canvas.drawText("STUDENT ATTENDANCE REPORT", 36f, 50f, titlePaint)
                canvas.drawLine(36f, 60f, 559f, 60f, linePaint)

                // Date & Time Generated
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateStr = sdf.format(Date())
                canvas.drawText("Generated At: $dateStr", 36f, 75f, metaPaint)

                // Analytics Summary Banner
                val summaryStr = "Total Records: $totalCount   |   Present: $presentCount   |   Absent: $absentCount   |   Leave: $leaveCount"
                canvas.drawText(summaryStr, 36f, 90f, metaPaint)
                canvas.drawLine(36f, 100f, 559f, 100f, linePaint)
            } else {
                // Subsequent Pages Header
                canvas.drawText("STUDENT ATTENDANCE REPORT", 36f, 35f, metaPaint)
                canvas.drawLine(36f, 42f, 559f, 42f, linePaint)
            }

            // Draw Table Header
            canvas.drawRect(36f, tableStartY, 559f, tableStartY + 20f, headerBgPaint)
            canvas.drawText("Date", 42f, tableStartY + 14f, headerTextPaint)
            canvas.drawText("Student Name", 115f, tableStartY + 14f, headerTextPaint)
            canvas.drawText("Status", 280f, tableStartY + 14f, headerTextPaint)
            canvas.drawText("Remarks / Notes", 350f, tableStartY + 14f, headerTextPaint)
            canvas.drawLine(36f, tableStartY + 20f, 559f, tableStartY + 20f, linePaint)
        }

        // Setup starting state
        var currentY = 115f
        drawPageDecorations(canvas, isFirstPage = true, tableStartY = currentY)
        currentY += 20f // Move below the table header

        // Loop and draw each record
        for (record in records) {
            // Check page height limit (A4 height is 842; leave space for margin and footer)
            if (currentY + 20f > 800f) {
                // Draw footer with page number
                canvas.drawText("Page $pageNum", 510f, 822f, metaPaint)
                pdfDocument.finishPage(page)

                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                currentY = 55f
                drawPageDecorations(canvas, isFirstPage = false, tableStartY = currentY)
                currentY += 20f
            }

            // Map and truncate strings to prevent overlapping adjacent columns
            val dateStr = truncateText(record.date, contentTextPaint, 70f)
            val nameStr = truncateText(record.studentName, contentTextPaint, 155f)
            val remarksStr = truncateText(record.teacherNotes.ifEmpty { "-" }, contentTextPaint, 200f)

            val statusPaint = when (record.status.lowercase()) {
                "present" -> statusPresentPaint
                "absent" -> statusAbsentPaint
                else -> statusLeavePaint
            }

            // Draw cells
            canvas.drawText(dateStr, 42f, currentY + 14f, contentTextPaint)
            canvas.drawText(nameStr, 115f, currentY + 14f, contentTextPaint)
            canvas.drawText(record.status, 280f, currentY + 14f, statusPaint)
            canvas.drawText(remarksStr, 350f, currentY + 14f, contentTextPaint)

            // Draw divider line under row
            canvas.drawLine(36f, currentY + 20f, 559f, currentY + 20f, linePaint)
            currentY += 20f
        }

        // Draw final page footer
        canvas.drawText("Page $pageNum", 510f, 822f, metaPaint)
        pdfDocument.finishPage(page)

        // Write the PDF document to output stream
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
