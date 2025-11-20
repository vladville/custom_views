package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import java.util.Collections.emptyList
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes,
) {

    //set animation value
    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private var rotationAngle = 0f

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5).toFloat()
    private var colors = emptyList<Int>()

    private var fillEffect: Int = 0

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
            fillEffect = getInteger(R.styleable.StatsView_fillEffect, fillEffect)
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = this@StatsView.lineWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val circlePaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        color = 0xFFF8F7F3.toInt()
        style = Paint.Style.STROKE
        strokeWidth = this@StatsView.lineWidth
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        canvas.drawCircle(center.x, center.y, radius, circlePaint)

        canvas.drawText(
            "%.2f%%".format(data.sum() / 100 * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )

        var startAngle = -90F
        var filled = 0F
        val progressAngel = progress * 360F
        when (fillEffect) {
            0 -> {
                data.forEachIndexed { index, datum ->
                    val angle = (datum / 100) * 360F
                    paint.color = colors.getOrElse(index) { generateRandomColor() }
                    canvas.drawArc(oval, startAngle, angle * progress, false, paint)
                    startAngle += angle

                }.also {
                    //add point for round
                    paint.color = colors.getOrNull(0) ?: generateRandomColor()
                    canvas.drawArc(oval, startAngle, -1F, false, paint)
                }
            }

            1 -> {
                data.forEachIndexed { index, datum ->
                    val angle = (datum / 100) * 360F
                    paint.color = colors.getOrNull(index) ?: generateRandomColor()
                    canvas.drawArc(oval, startAngle, progressAngel - filled, false, paint)
                    startAngle += angle
                    filled += angle
                    if (filled > progressAngel) return

                }.also {
                    //add point for round
                    paint.color = colors.getOrNull(0) ?: generateRandomColor()
                    canvas.drawArc(oval, startAngle, -1F, false, paint)
                }
            }
            2-> {
                data.forEachIndexed { index, datum ->
                    val angle = (datum / 100) * 360F
                    paint.color = colors.getOrElse(index) { generateRandomColor() }
                    canvas.drawArc(oval, startAngle + 360F * progress, angle * progress, false, paint) //with rotate
                    startAngle += angle

                }.also {
                    //add point for round
                    paint.color = colors.getOrNull(0) ?: generateRandomColor()
                    canvas.drawArc(oval, startAngle + 360 * progress, -1F, false, paint) //with rotate
                }
            }
        }
    }

    private fun update() {
        //clear before animation
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }

        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                rotationAngle = 360f * progress
                invalidate()
            }
            startDelay = 1500
            duration = 5000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun generateRandomColor(): Int = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}