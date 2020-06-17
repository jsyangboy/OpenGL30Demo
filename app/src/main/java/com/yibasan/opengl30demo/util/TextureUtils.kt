package com.yibasan.opengl30demo.util

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils

object TextureUtils {

    /**
     * 创建纹理
     */
    fun createTexture(bitmap: Bitmap?): Int {
        if (bitmap == null) {
            return 0
        }

        var textureId = IntArray(1)
        GLES30.glGenTextures(1, textureId, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0])
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return textureId[0]
    }
}