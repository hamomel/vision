package com.hamomel.vision.camerascreen.presintation.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.objects.DetectedObject
import com.hamomel.vision.R
import kotlin.math.abs

/**
 * @author Роман Зотов on 24.12.2021
 */
private const val CENTER_SPOT_SCALE_FACTOR = 1.3f

class DetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var objects = listOf<DetectedObject>()
       set(value) {
           field = value
           invalidate()
       }

    private val sizeMapper = ImageSizeMapper(this)

    private val spotRadius = context.resources.getDimension(R.dimen.overlay_view_object_spot_radius)

    private val spotTouchAreaRadius =
        context.resources.getDimension(R.dimen.overlay_view_spot_touch_area_radius)

    private val spotPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL_AND_STROKE
    }
    private val centerAreaSize =
        context.resources.getDimension(R.dimen.overlay_view_center_area_size)

    private val centerAreaCornerRadius =
        context.resources.getDimension(R.dimen.overlay_view_center_area_corner_radius)

    private val centerAreaPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.overlay_view_center_area_stroke_width)
    }
    private val centerAreaRect = RectF()

    private var onSpotTouchListener: (DetectedObject) -> Unit = {}

    fun setOnSpotTouchListener(listener: (DetectedObject) -> Unit) {
        onSpotTouchListener = listener
    }

    fun updateImageSize(size: Size) {
        sizeMapper.updateWithImageSize(size)
    }

    fun getMostCenteredObject(): DetectedObject? {
        val viewCenterX = width / 2
        val viewCenterY = height / 2
        var leastDistanceX = Float.MAX_VALUE
        var leastDistanceY = Float.MAX_VALUE
        var mostCentered = objects.firstOrNull()

        objects.forEach { obj ->
            val centerX = sizeMapper.calculateCenterX(obj.boundingBox)
            val centerY = sizeMapper.calculateCenterY(obj.boundingBox)
            if (abs(viewCenterX - centerX) < leastDistanceX &&
                abs(viewCenterY - centerY) < leastDistanceY
            ) {
                leastDistanceX = abs(viewCenterX - centerX)
                leastDistanceY = abs(viewCenterY - centerY)
                mostCentered = obj
            }
        }

        return mostCentered
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            for (obj  in objects) {
                val centerX = sizeMapper.calculateCenterX(obj.boundingBox)
                val centerY = sizeMapper.calculateCenterY(obj.boundingBox)
                if (abs(centerX - event.x) < spotTouchAreaRadius &&
                        abs(centerY - event.y) < spotTouchAreaRadius) {
                    onSpotTouchListener(obj)
                    break
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        sizeMapper.onViewSizeChanged()

        centerAreaRect.left = w / 2 - centerAreaSize / 2
        centerAreaRect.right = centerAreaRect.left + centerAreaSize
        centerAreaRect.top = h / 2 - centerAreaSize / 2
        centerAreaRect.bottom = centerAreaRect.top + centerAreaSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val mostCentered = getMostCenteredObject()

        objects.forEach { obj ->
            val centerX = sizeMapper.calculateCenterX(obj.boundingBox)
            val centerY = sizeMapper.calculateCenterY(obj.boundingBox)
            val isMostCentered = obj.boundingBox == mostCentered?.boundingBox

            val radius = if (isMostCentered && centerAreaRect.contains(centerX, centerY)) {
                spotRadius * CENTER_SPOT_SCALE_FACTOR
            } else {
                spotRadius
            }
            canvas.drawCircle(centerX, centerY, radius, spotPaint)
        }

        canvas.drawRoundRect(
            centerAreaRect,
            centerAreaCornerRadius,
            centerAreaCornerRadius,
            centerAreaPaint
        )
    }
}