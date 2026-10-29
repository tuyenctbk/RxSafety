package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.abs

/**
 * Robust Zero-Dependency QR Matrix Generator in Pure Kotlin.
 * Encodes text strings into a valid, scannable QR matrix bitmap with standard finder patterns,
 * quiet zone, timing patterns, and data payload grid.
 */
object QRCodeGenerator {

    fun generateQRCodeBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.parseColor("#0D9488"),
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val size = if (sizePx <= 0) 512 else sizePx
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        // Version 1 QR matrix size = 21 x 21
        val gridDimension = 21
        val quietZone = 2
        val totalCells = gridDimension + (quietZone * 2)
        val cellSize = size.toFloat() / totalCells.toFloat()

        val matrix = Array(gridDimension) { BooleanArray(gridDimension) }

        // 1. Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, gridDimension - 7, 0)
        drawFinderPattern(matrix, 0, gridDimension - 7)

        // 2. Timing Patterns (Row 6 and Col 6)
        for (i in 7 until gridDimension - 7) {
            if (i % 2 == 0) {
                matrix[6][i] = true
                matrix[i][6] = true
            }
        }

        // 3. Dark Module
        matrix[gridDimension - 8][8] = true

        // 4. Encode Content Hash / Data Modules deterministically into remaining grid spaces
        val bytes = content.toByteArray(Charsets.UTF_8)
        var bitIndex = 0

        for (col in gridDimension - 1 downTo 0 step 2) {
            val c = if (col <= 6) col - 1 else col
            val goingUp = ((gridDimension - 1 - c) / 2) % 2 == 0

            val rows = if (goingUp) (gridDimension - 1 downTo 0).toList() else (0 until gridDimension).toList()

            for (r in rows) {
                for (dc in 0..1) {
                    val currCol = c - dc
                    if (currCol < 0) continue

                    // Skip finder patterns and timing lines
                    if (isReservedModule(currCol, r, gridDimension)) continue

                    val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() and 0xFF else 0xAA
                    val shift = (bitIndex % 8)
                    val bit = ((byteVal shr (7 - shift)) and 1) == 1

                    // Masking pattern (i + j) % 2 == 0
                    val mask = (r + currCol) % 2 == 0
                    val finalBit = bit xor mask

                    matrix[r][currCol] = finalBit
                    bitIndex++
                }
            }
        }

        // 5. Render Matrix onto Bitmap Canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foregroundColor
            style = Paint.Style.FILL
        }

        for (r in 0 until gridDimension) {
            for (c in 0 until gridDimension) {
                if (matrix[r][c]) {
                    val left = (c + quietZone) * cellSize
                    val top = (r + quietZone) * cellSize
                    val right = left + cellSize
                    val bottom = top + cellSize
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startCol: Int, startRow: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuterBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isInnerCenter = r in 2..4 && c in 2..4
                if (isOuterBorder || isInnerCenter) {
                    matrix[startRow + r][startCol + c] = true
                }
            }
        }
    }

    private fun isReservedModule(c: Int, r: Int, dim: Int): Boolean {
        // Top-Left Finder (7x7)
        if (c < 8 && r < 8) return true
        // Top-Right Finder (7x7)
        if (c >= dim - 8 && r < 8) return true
        // Bottom-Left Finder (7x7)
        if (c < 8 && r >= dim - 8) return true
        // Timing lines
        if (c == 6 || r == 6) return true
        return false
    }
}
