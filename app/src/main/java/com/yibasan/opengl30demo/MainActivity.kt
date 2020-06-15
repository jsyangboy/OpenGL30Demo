package com.yibasan.opengl30demo

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.util.AssetsUtils
import com.yibasan.opengl30demo.util.ShardUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private val TAG = "ShaderUtils"
    private var aColor = 0
    private var aPosition = 0

    private val color = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f
    )

    /**
     * 点的坐标
     */
    private val vertexPoints = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )


    var bufferColor:FloatBuffer? = null
    var bufferVertex:FloatBuffer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glSurfaceView.setEGLContextClientVersion(3)

        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)

        glSurfaceView.setRenderer(this)

    }

    private fun getVertexString(): String? {
        return AssetsUtils.loadFromAssetsFile(applicationContext.resources, "very/vertex.sh")
    }

    private fun getFragmentString(): String? {
        return AssetsUtils.loadFromAssetsFile(applicationContext.resources, "very/fragment.sh")
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(1f, 0f, 0f, 0f)

        /**
         * 创建和设置,编译顶点作色器
         */
        var vertexIndex = ShardUtils.loadShader(GLES30.GL_VERTEX_SHADER, getVertexString())

        /**
         * 创建和设置，片段作色器
         */
        var fragmenIndex = ShardUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, getFragmentString())

        /**
         * 创建程序
         */
        var programIndex = ShardUtils.createProgram(vertexIndex, fragmenIndex)

        /**
         * 链接程序
         */
        programIndex = ShardUtils.linkProgram(programIndex)

        /**
         * 加载变量
         */
        aColor = GLES30.glGetAttribLocation(programIndex, "aColor")
        aPosition = GLES30.glGetAttribLocation(programIndex, "aPosition")

        /**
         * 颜色缓冲区
         */
        bufferVertex = ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        bufferVertex?.put(vertexPoints)
        bufferVertex?.position(0)

        /**
         * 颜色缓冲区
         */
        bufferColor = ByteBuffer.allocateDirect(color.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        bufferColor?.put(color)
        bufferColor?.position(0)

    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glVertexAttribPointer(0,4,GLES30.GL_FLOAT,false,0,bufferColor)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1,3,GLES30.GL_FLOAT,false,0,bufferVertex)
        GLES30.glEnableVertexAttribArray(1)


        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

}