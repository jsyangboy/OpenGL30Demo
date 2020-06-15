package com.yibasan.opengl30demo

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.util.AssetsUtils
import kotlinx.android.synthetic.main.activity_main.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private val TAG = "ShaderUtils"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)

        glSurfaceView.setRenderer(this)

    }

    private fun getVertString(): String? {
        return AssetsUtils.loadFromAssetsFile(applicationContext.resources, "very/vertex.sh")
    }

    private fun getFragmentString(): String? {
        return AssetsUtils.loadFromAssetsFile(applicationContext.resources, "very/vertex.sh")
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(1f, 0f, 0f, 0f)

        /**
         * 创建和设置,编译顶点作色器
         */
        var vertexIndex = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vertexIndex, getVertString())
        GLES30.glCompileShader(vertexIndex)
        var compiler = IntArray(1)
        GLES30.glGetShaderiv(vertexIndex, GLES30.GL_COMPILE_STATUS, compiler, 0)
        if (compiler[0] == 0) {
            Log.e("TAG", "Could not compile shader:${GLES30.GL_VERTEX_SHADER}")
            Log.e("TAG", "GLES20 Error:" + GLES20.glGetShaderInfoLog(GLES30.GL_VERTEX_SHADER))
            GLES20.glDeleteShader(vertexIndex)
            vertexIndex = 0
        }

        /**
         * 创建和设置，片段作色器
         */
        var fragmenIndex = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fragmenIndex, getFragmentString())
        GLES30.glCompileShader(fragmenIndex)


    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_ALPHA)
    }

}