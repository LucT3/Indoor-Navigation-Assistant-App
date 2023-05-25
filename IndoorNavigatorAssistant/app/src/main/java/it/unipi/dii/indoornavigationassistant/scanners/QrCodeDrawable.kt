package it.unipi.dii.indoornavigationassistant.scanners

import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * A Drawable that handles displaying a QR Code's data and a bounding box around the QR code.
 */
class QrCodeDrawable(private val qrCode: Barcode) : Drawable() {
    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.YELLOW
        strokeWidth = 10F
        alpha = 200
    }
    
    
    override fun draw(canvas: Canvas) {
        canvas.drawRect(qrCode.boundingBox!!, boundingRectPaint)
    }
    
    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
    }
    
    override fun setColorFilter(colorFiter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
    }
    
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
