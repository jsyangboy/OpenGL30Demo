package com.yibasan.opengl30demo.util

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface

class EglCore(var sharedContext: EGLContext?, flags: Int) {


    companion object {
        private val TAG = "EglCore"

        /**
         *
         */
        private val FLAG_RECODABLE = 0x01

        val FALG_TRY_GLES3 = 0x02

        private val EGL_RECORDABLE_ANDROID = 0x3142
    }

    private var mGlVersion: Int = 0


    private var mEGLDisplay: EGLDisplay? = EGL14.EGL_NO_DISPLAY
    private var mEGLContext: EGLContext? = EGL14.EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null

    init {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGL already set up")
        }

        if (sharedContext == null) {
            sharedContext = EGL14.EGL_NO_CONTEXT
        }

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL 14 display")
        }

        var version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }

        /**
         *  尝试获取GLES3 content
         */
        if ((flags and FALG_TRY_GLES3) != 0) {
            var config: EGLConfig? = getConfig(flags, 3)
            if (config != null) {
                val attrib3_list = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
                )
                val context = EGL14.eglCreateContext(
                    mEGLDisplay, config, sharedContext,
                    attrib3_list, 0
                )

                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig = config
                    mEGLContext = context
                    mGlVersion = 3
                }
            }
        }

        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            var config: EGLConfig? =
                getConfig(flags, 2) ?: throw RuntimeException("Unable to find a suitable EGLConfig")
            val attrib2_list = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )

            var context =
                EGL14.eglCreateContext(
                    mEGLDisplay, config, sharedContext,
                    attrib2_list, 0
                )
            checkEglError("eglCreateContext")

            mEGLConfig = config
            mEGLContext = context
            mGlVersion = 2
        }

        var values = IntArray(1)
        EGL14.eglQueryContext(
            mEGLDisplay, mEGLContext,
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            values, 0
        )
        Log.d(TAG, "EGLContext created, client version " + values[0])
    }


    fun release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )

            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }

        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLConfig = null
    }


    @Throws(Throwable::class)
    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        try {
            if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
                Log.w(
                    TAG,
                    "WARNING: EglCore was not explicitly released -- state may be leaked"
                )
                release()
            }
        } finally {

        }
    }


    fun releaseSurface(eglSurface: EGLSurface?) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }

    fun createWindowSurface(surface: Any?): EGLSurface? {
        if ((surface !is Surface) and (surface !is SurfaceTexture)) {
            throw RuntimeException("invalid surface:$surface")
        }

        var surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        var eglSurface = EGL14.eglCreateWindowSurface(
            mEGLDisplay,
            mEGLConfig, surface, surfaceAttribs, 0
        )

        checkEglError("eglCreateWindowSurface")
        if (eglSurface == null) {
            throw RuntimeException("surface was null")
        }
        return eglSurface
    }

    /**
     * 创建一个离屏的surface
     */
    fun createOffscrrenSurface(width: Int, height: Int): EGLSurface? {
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        val eglSurface = EGL14.eglCreatePbufferSurface(
            mEGLDisplay, mEGLConfig,
            surfaceAttribs, 0
        )
        checkEglError("eglCreatePbufferSurface")
        if (eglSurface == null) {
            throw java.lang.RuntimeException("surface was null")
        }
        return eglSurface
    }


    fun makeCurrent(eglSurface: EGLSurface?) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "NOTE: makeCurrent w/o display")
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }


    fun makeCurrent(
        drawSurface: EGLSurface?,
        readSurface: EGLSurface?
    ) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            // called makeCurrent() before create?
            Log.d(TAG, "NOTE: makeCurrent w/o display")
        }
        if (!EGL14.eglMakeCurrent(
                mEGLDisplay,
                drawSurface,
                readSurface, mEGLContext
            )
        ) {
            throw java.lang.RuntimeException("eglMakeCurrent(draw,read) failed")
        }
    }


    fun makeNothingCurrent() {
        if (!EGL14.eglMakeCurrent(
                mEGLDisplay, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        ) {
            throw java.lang.RuntimeException("eglMakeCurrent failed")
        }
    }

    fun swapBuffers(eglSurface: EGLSurface?): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }


    /**
     * 发送演示时间戳到EGL。时间以纳秒表示。
     */
    fun setPresentationTime(eglSurface: EGLSurface?, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    /**
     * Returns true if our context and the specified surface are current.
     */
    fun isCurrent(eglSurface: EGLSurface?): Boolean {
        if (eglSurface != null) {
            mEGLContext?.let {
                return mEGLContext!!.equals(EGL14.eglGetCurrentContext()) && eglSurface.equals(
                    EGL14.eglGetCurrentSurface(
                        EGL14.EGL_DRAW
                    )
                )
            }
        }
        return false
    }

    /**
     * Performs a simple surface query.
     */
    fun querySurface(eglSurface: EGLSurface?, what: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0)
        return value[0]
    }

    /**
     * Queries a string value.
     */
    fun queryString(what: Int): String? {
        return EGL14.eglQueryString(mEGLDisplay, what)
    }

    /**
     * Returns the GLES version this context is configured for (currently 2 or 3).
     */
    fun getGlVersion(): Int {
        return mGlVersion
    }


    /**
     * Writes the current display, context, and surface to the log.
     */
    fun logCurrent(msg: String) {
        val display: EGLDisplay
        val context: EGLContext
        val surface: EGLSurface
        display = EGL14.eglGetCurrentDisplay()
        context = EGL14.eglGetCurrentContext()
        surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
        Log.i(
            TAG, "Current EGL (" + msg + "): display=" + display + ", context=" + context +
                    ", surface=" + surface
        )
    }

    private fun getConfig(flags: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,  //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,  // placeholder for recordable [@-3]
            EGL14.EGL_NONE
        )

        if ((flags and FLAG_RECODABLE) != 0) {
            attribList[attribList.size - 3] = EGL_RECORDABLE_ANDROID
            attribList[attribList.size - 2] = 1
        }

        val configs =
            arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)

        if (!EGL14.eglChooseConfig(
                mEGLDisplay,
                attribList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            Log.w(TAG, "unable to find RGB88888 /$version EGLConfig")
            return null
        }
        return configs[0]
    }

    private fun checkEglError(msg: String?) {
        var error: Int = 0
        error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            throw RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
        }
    }


}