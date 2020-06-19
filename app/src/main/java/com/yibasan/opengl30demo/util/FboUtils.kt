package com.yibasan.opengl30demo.util

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import java.nio.IntBuffer

object FboUtils {

    private var TAG = "FboUtils"

    /**
     * 创建一个FBO（Frame Buffer Object）
     */
    fun createFbo(width: Int, height: Int, textureId: Int): Int {
        var fbo = IntBuffer.allocate(1)
        GLES30.glGenFramebuffers(1, fbo)
        var status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "create FBO error")
            return 0
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)

        /**
         * 使用纹理填充帧缓冲区
         */
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            textureId,
            0
        )

        /**
         * 解绑
         */
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
        return fbo[0]
    }


    /**
     * 这里创建一个带有渲染缓冲区的FBO
     * FBO(Frame Buffer Object)
     * RBO(Render Buffer Object)
     */
    fun createFboRbo(width: Int, height: Int, textureId: Int): Int {
        var fbo = IntBuffer.allocate(1)
        GLES30.glGenFramebuffers(1, fbo)
        var status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "create FBO error")
            return 0
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])

        var rbo = IntBuffer.allocate(1)
        GLES30.glGenRenderbuffers(1, rbo)
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, rbo[0])

        /**
         * 为我们的RenderBuffer申请存储空间
         */
        GLES30.glRenderbufferStorage(
            GLES30.GL_RENDERBUFFER,
            GLES30.GL_DEPTH_COMPONENT16,
            width,
            height
        )

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        /**
         * 使用纹理填充帧缓冲区
         */
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            textureId,
            0
        )

        /**
         * 将renderBuffer挂载到frameBuffer的depth attachment 上。就上面申请了OffScreenId和FrameBuffer相关联
         */
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER,
            rbo!![0]
        )

        /**
         * 解绑
         */
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, GLES30.GL_NONE)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)

        return fbo[0]
    }


}