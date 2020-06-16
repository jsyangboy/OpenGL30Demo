package com.yibasan.opengl30demo.test

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.yibasan.opengl30demo.R
import com.yibasan.opengl30demo.util.AssetsUtils
import com.yibasan.opengl30demo.util.ShardUtils
import kotlinx.android.synthetic.main.activity_test__triangles.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class test_Triangles_vbo_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test__triangles_vbo)
        title = "三角形+VBO"

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
                test_Triangles_Activity.TAG,
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
             * 将坐标数组值传递给aPosition，并启用生效（不能少）
             * size:表示顶点的数量
             * stride:表示每个顶点之间数据的偏移量，比如每个顶点有x,y,x 每个数值4字节 那下个顶点的偏移量就是3*4，这里填写0也没有问题
             */
            GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_FLOAT, false, 12, bufferVertex)
            GLES30.glEnableVertexAttribArray(aPosition)

            /**
             * 将颜色数组值传递给aColor，并启用生效（不能少）
             * size:表示每个顶点颜色的值有多少长度表示rgba就是4，rgb就是3
             * stride:表示每个顶点之间数据的偏移量，比如一个顶点argb，每个数值有4个字节，那下个颜色的偏移量未4*4,这里填写0也没有问题
             */
            GLES30.glVertexAttribPointer(aColor, 4, GLES30.GL_FLOAT, false, 16, bufferColor)
            GLES30.glEnableVertexAttribArray(aColor)

            /**
             * 绘制三角形
             */
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

            /**
             * 每次绘制完毕后要禁用
             */
            GLES30.glDisableVertexAttribArray(aPosition)
            GLES30.glDisableVertexAttribArray(aColor)
        }
    }

}