package com.hamomel.vision.camerascreen.presintation.view

import android.graphics.Rect
import android.util.Size
import android.view.View

/**
 * @author Роман Зотов on 24.12.2021
 *
 * Image may have different size than view, so we have to scale and crop it to fit the view.
 * But we get bounding boxes of objects in coordinates of original image.
 * This helper class maps them to view's coordinates to draw.
 */
class ImageSizeMapper(
    private val view: View
) {
    private var imageSize = Size(0, 0)
    private var scaleFactor = 1f
    private var horizontalOffset = 0f
    private var verticalOffset = 0f

    fun updateWithImageSize(size: Size) {
        imageSize = size
        updateOffsetsAndScaleFactor()
    }

    fun onViewSizeChanged() {
        updateOffsetsAndScaleFactor()
    }

    private fun updateOffsetsAndScaleFactor() {
        if (imageSize.height == 0 || imageSize.width == 0) return

        scaleFactor = if (imageSize.height >= imageSize.width) {
            view.width.toFloat() / imageSize.width
        } else {
            view.height.toFloat() / imageSize.height
        }

        if (imageSize.width * scaleFactor < view.width) {
            horizontalOffset = (imageSize.width * scaleFactor - view.width) / 2
            verticalOffset = 0f
        } else {
            verticalOffset = (imageSize.height * scaleFactor - view.height) / 2
            horizontalOffset = 0f
        }
    }

    fun calculateCenterX(rect: Rect): Float =
        (rect.exactCenterX() * scaleFactor) - horizontalOffset

    fun calculateCenterY(rect: Rect): Float =
        (rect.exactCenterY() * scaleFactor) - verticalOffset
}