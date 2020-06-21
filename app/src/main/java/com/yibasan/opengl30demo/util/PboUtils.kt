package com.yibasan.opengl30demo.util

import android.opengl.GLES30
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer


object PboUtils {

    private val TAG = " PboUtils"

    /**
     * 创建一个pbo
     */
    fun createPboBuffer(width: Int, height: Int): Int {
        if (width == 0 || height == 0) {
            Log.e(TAG, "width==$width , height ==$height")
            return GLES30.GL_NONE
        }
        val size = width * height * 4
        var pbo = IntArray(1)
        GLES30.glGenBuffers(1, pbo, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbo[0])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES30.GL_NONE)
        Log.e(TAG, "createPboBuffer pbo[0]=" + pbo[0])
        return pbo[0]
    }

    /**
     * 创建多个pbo
     */
    fun createPboBuffers(pboSiz: Int, width: Int, height: Int): IntArray {
        if (width == 0 || height == 0) {
            Log.e(TAG, "width=$width , height =$height")
            return IntArray(GLES30.GL_NONE)
        }
        Log.e(TAG, "width=$width , height =$height")
        val size = width * height * 4
        var pbos = IntArray(pboSiz)
        GLES30.glGenBuffers(pboSiz, pbos, 0)
        for (i in 0 until pboSiz) {
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[i])
            GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, size, null, GLES30.GL_STATIC_DRAW)
        }
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES30.GL_NONE)
        Log.e(TAG, "createPboBuffer pbos[0]=" + pbos[0] + ",pbos[1]=" + pbos[1])
        return pbos
    }

    /**
     * 直接从显示的缓存中pix
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun readPixels(
        pbos: IntArray, width: Int, height: Int, pos: IntArray
    ): ByteBuffer? {
        Log.d(
            TAG,
            "pos[0]=" + pos[0] + ",pos[1]=" + pos[1] + ",width=" + width + ",height=" + height
        )
        var byteBuffer = readPixels(pbos, width, height, pos[0], pos[1])
        //交换索引
        pos[0] = (pos[0] + 1) % 2
        pos[1] = (pos[1] + 1) % 2
        return byteBuffer
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun readPixelsFromFbo(
        pbos: IntArray, width: Int, height: Int, pos: IntArray, fbo: Int
    ): ByteBuffer? {
        Log.d(
            TAG,
            "pos[0]=" + pos[0] + ",pos[1]=" + pos[1] + ",fbo=" + fbo + ",width=" + width + ",height=" + height
        )
        var byteBuffer = readPixelsFromFbo(pbos, width, height, pos[0], pos[1], fbo)
        //交换索引
        pos[0] = (pos[0] + 1) % 2
        pos[1] = (pos[1] + 1) % 2
        return byteBuffer
    }

    /**
     * 直接从显示的缓存中pix
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun readPixels(
        pbos: IntArray, width: Int, height: Int, first: Int,
        next: Int
    ): ByteBuffer? {
        var startTime = System.currentTimeMillis()

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[first])
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[next])
        var secondTime = System.currentTimeMillis()
        var pixBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            width * height * 4,
            GLES30.GL_MAP_READ_BIT
        )

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        Log.e(
            TAG,
            "readTime = ${secondTime - startTime},mapTime =${System.currentTimeMillis() - secondTime}"
        )
        return if (pixBuffer == null) {
            null
        } else {
            pixBuffer as ByteBuffer
        }
    }

    /**
     * 从FBO帧缓存中读取pix
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun readPixelsFromFbo(
        pbos: IntArray,
        width: Int,
        height: Int,
        first: Int,
        next: Int,
        fbo: Int
    ): ByteBuffer? {
        //Log.e(TAG, "first=$first,next=$next")
        var startTime = System.currentTimeMillis()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[first])
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbos[next])

        var secondTime = System.currentTimeMillis()
        var pixBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            width * height * 4,
            GLES30.GL_MAP_READ_BIT
        )

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        Log.e(
            TAG,
            "readTime = ${secondTime - startTime},mapTime =${System.currentTimeMillis() - secondTime}"
        )
        return if (pixBuffer == null) {
            null
        } else {
            pixBuffer as ByteBuffer
        }
    }


}