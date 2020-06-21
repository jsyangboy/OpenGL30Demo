package com.yibasan.opengl30demo.util

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface

class WindowSurface(eglCore: EglCore, surface: Surface, releaseSurface: Boolean) {


    /**
     * Associates an EGL surface with the SurfaceTexture.
     */
    fun WindowSurface(eglCore: EglCore, surfaceTexture: SurfaceTexture?) {
        mEglCore = eglCore
        createWindowSurface(surfaceTexture)
    }

    fun WindowSurface(eglCore: EglCore, surfaceTexture: Surface?) {
        mEglCore = eglCore
        createWindowSurface(surfaceTexture!!)
    }

    companion object {
        val TAG = "WindowSurface"
    }

    private var mSurface: Surface? = null
    private var mReleaseSurface: Boolean = false
    private var mEglCore: EglCore? = null

    private var mEGLSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    init {
        mEglCore = eglCore
        mSurface = surface
        mReleaseSurface = releaseSurface
        createWindowSurface(surface)
    }

    private fun createWindowSurface(surface: Any?) {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw IllegalStateException("surface already create")
        }
        try {
            mEGLSurface = mEglCore?.createWindowSurface(surface)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    fun releaseEglSurface() {
        mEglCore?.releaseSurface(mEGLSurface)
        mEGLSurface = EGL14.EGL_NO_SURFACE
    }

    fun makeCurrent() {
        mEglCore?.makeCurrent(mEGLSurface)
    }

    fun swapBuffers(): Boolean {
        var result = mEglCore?.swapBuffers(mEGLSurface)
        if (!result!!) {
            Log.e(TAG, "WARNING: swapBuffers() failed")
        }
        return result
    }

    fun release() {
        releaseEglSurface()
        if (mSurface != null) {
            if (mReleaseSurface) {
                mSurface?.release()
            }
            mSurface = null
        }
    }

    fun recreate(newEglCore: EglCore) {
        if (mSurface == null) {
            throw RuntimeException("not yet implemented for surfaceTexture")
        }
        mEglCore = newEglCore
        createWindowSurface(mSurface)
    }

}