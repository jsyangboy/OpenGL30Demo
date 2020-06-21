package com.yibasan.opengl30demo.util

import android.graphics.Bitmap
import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer

object BitmapUtils {

    private final var TAG = "BitmapUtils"

    fun createBitmap(width: Int, height: Int, byteBuffer: ByteBuffer?): Bitmap? {
        try {
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(byteBuffer)
            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return null
    }
}