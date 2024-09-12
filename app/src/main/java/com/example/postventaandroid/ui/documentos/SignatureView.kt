package com.example.postventaandroid.ui.documentos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


//Clase View para que el usuario que se registre pueda Firmar documento
//Logica del dibujo o trazado del "lapiz"
class SignatureView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val path = android.graphics.Path()
    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 5f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            else -> return false
        }

        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun getSignatureBitmap(): Bitmap {
        val scale = 4 // Esto creará un bitmap 4 veces más grande
        val scaledWidth = width * scale
        val scaledHeight = height * scale

        val signatureBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(signatureBitmap)
        canvas.scale(scale.toFloat(), scale.toFloat())
        draw(canvas)
        return signatureBitmap
    }

    //Función para verificar si el campo de la firma está vacío
    fun isSignatureEmpty(): Boolean {
        val emptyPath = android.graphics.Path()
        return path.isEmpty
    }


}