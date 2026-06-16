package com.example.vaultwise.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.vaultwise.data.dao.CategorySum
import com.example.vaultwise.util.CategoryUtils
import kotlin.math.min

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Int = 0
    private val slices = mutableListOf<Slice>()
    private var totalAmount = 0.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()

    data class Slice(
        val category: String,
        val amount: Double,
        val color: Int
    )

    fun setData(breakdown: List<*>?) {
        slices.clear()

        if (breakdown.isNullOrEmpty()) {
            invalidate()
            return
        }

        // Safe casting - works with your current model
        @Suppress("UNCHECKED_CAST")
        val list = breakdown as? List<Any> ?: return

        totalAmount = list.sumOf { item ->
            when (item) {
                is CategorySum -> item.total
                is Map<*, *> -> (item["total"] as? Double) ?: 0.0
                else -> {
                    try {
                        val field = item.javaClass.getDeclaredField("total")
                        field.isAccessible = true
                        (field.get(item) as? Double) ?: 0.0
                    } catch (e: Exception) {
                        0.0
                    }
                }
            }
        }

        if (totalAmount <= 0) {
            invalidate()
            return
        }

        list.forEach { item ->
            val category = when (item) {
                is CategorySum -> item.category
                is Map<*, *> -> item["category"] as? String
                else -> {
                    try {
                        val field = item.javaClass.getDeclaredField("category")
                        field.isAccessible = true
                        field.get(item) as? String
                    } catch (e: Exception) {
                        null
                    }
                }
            } ?: "Other"

            val amount = when (item) {
                is CategorySum -> item.total
                is Map<*, *> -> item["total"] as? Double
                else -> {
                    try {
                        val field = item.javaClass.getDeclaredField("total")
                        field.isAccessible = true
                        field.get(item) as? Double
                    } catch (e: Exception) {
                        null
                    }
                }
            } ?: 0.0

            val color = CategoryUtils.getCategoryColor(context, category)
            slices.add(Slice(category, amount, color))
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        if (size <= 0) return

        val strokeWidth = size * 0.22f
        val radius = (size - strokeWidth) / 2f

        paint.strokeWidth = strokeWidth

        rect.set(
            width/2f - radius,
            height/2f - radius,
            width/2f + radius,
            height/2f + radius
        )

        // Draw background track
        paint.color = 0xFFF1F5F9.toInt() // light grey/border color
        canvas.drawArc(rect, 0f, 360f, false, paint)

        if (slices.isEmpty() || totalAmount <= 0) return

        var startAngle = -90f

        slices.forEach { slice ->
            val sweepAngle = ((slice.amount / totalAmount) * 360f)
                .toFloat()
                .coerceAtLeast(3f)   // minimum visible slice

            paint.color = slice.color
            canvas.drawArc(rect, startAngle, sweepAngle, false, paint)

            startAngle += sweepAngle
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 140
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec)
        )
    }
}