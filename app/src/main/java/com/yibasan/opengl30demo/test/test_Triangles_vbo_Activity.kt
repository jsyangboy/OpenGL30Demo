package com.yibasan.opengl30demo.test

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.R
import com.yibasan.opengl30demo.util.AssetsUtils
import com.yibasan.opengl30demo.util.ShardUtils
import kotlinx.android.synthetic.main.activity_test__triangles.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class test_Triangles_vbo_Activity : AppCompatActivity() {

    companion object {
        var TAG = "test_Triangles_vbo_Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test__triangles_vbo)
        title = "三角形+VBO"

        glSurfaceView.setEGLContextClientVersion(3)

        glSurfaceView.setRenderer(TrianglesRenderer(applicationContext))

    }


    private class TrianglesRenderer constructor(context: Context) : GLSurfaceView.Renderer {
        private var aColor = 0
        private var aPosition = 0
        private var mContext: Context = context

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

        var bufferColor: FloatBuffer? = null
        var bufferVertex: FloatBuffer? = null
        var vao: IntBuffer? = null

        private fun getVertexString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext.resources, "very/vertex.sh")
        }

        private fun getFragmentString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext.resources, "very/fragment.sh")
        }

        override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
            /**
             * 使用什么颜色做垫底
             */
            GLES30.glClearColor(1f, 1f, 1f, 1f)

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
             * 加载变量aColor的值跟内部的location = xx的值相同
             */
            aColor = GLES30.glGetAttribLocation(programIndex, "aColor")
            aPosition = GLES30.glGetAttribLocation(programIndex, "aPosition")

            Log.d(
                TAG,
                "vertexIndex=$vertexIndex,fragmenIndex=$fragmenIndex,programIndex=$programIndex,aColor=$aColor,aPosition=$aPosition"
            )

            /**
             * 颜色缓冲区
             */
            bufferVertex =
                ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            bufferVertex?.put(vertexPoints)
            bufferVertex?.position(0)

            /**
             * 颜色缓冲区
             */
            bufferColor =
                ByteBuffer.allocateDirect(color.size * 4).order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            bufferColor?.put(color)
            bufferColor?.position(0)

            /**
             * 使用程序（不能少）
             */
            GLES30.glUseProgram(programIndex)

            /**
             * 创建一个顶点数组VAO
             */
            vao = IntBuffer.allocate(2)
            GLES30.glGenVertexArrays(1, vao)
            GLES30.glBindVertexArray(vao!![0])

            /**
             * 第一个vbo填充顶点数据
             */
            var vbo: IntBuffer? = IntBuffer.allocate(2)
            GLES30.glGenBuffers(2, vbo)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo!![0])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                vertexPoints.size * 4,
                bufferVertex,
                GLES30.GL_STATIC_DRAW
            )

            GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_FLOAT, false, 12, 0)
            GLES30.glEnableVertexAttribArray(aPosition)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

            /**
             * 第二个vbo填充颜色缓冲区
             */
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo!![1])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                color.size * 4,
                bufferColor,
                GLES30.GL_STATIC_DRAW
            )
            GLES30.glVertexAttribPointer(aColor, 4, GLES30.GL_FLOAT, false, 16, 0)
            GLES30.glEnableVertexAttribArray(aColor)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

            /**
             * 解绑VAO
             */
            GLES30.glBindVertexArray(0)
        }


        override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)

        }

        override fun onDrawFrame(p0: GL10?) {

            /**
             * 这个函数的作用是清空颜色，并使用glClearColor设置的颜色填充
             */
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            /**
             * 这里比基本三角形单元简单多了，直接使用vao
             */
            GLES30.glBindVertexArray(vao!![0])

            /**
             * 绘制三角形
             */
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

            /**
             * 解绑
             */
            GLES30.glBindVertexArray(0)
        }
    }

}