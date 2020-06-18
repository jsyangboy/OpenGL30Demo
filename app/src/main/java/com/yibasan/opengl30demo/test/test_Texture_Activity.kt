package com.yibasan.opengl30demo.test

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.R
import com.yibasan.opengl30demo.util.AssetsUtils
import com.yibasan.opengl30demo.util.ShardUtils
import com.yibasan.opengl30demo.util.TextureUtils
import kotlinx.android.synthetic.main.activity_test_texture.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class test_Texture_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_texture)
        title = "简单纹理"
        glSurfaceView.setEGLContextClientVersion(3)

        glSurfaceView.setRenderer(TextureRenderer(application))
    }

    private class TextureRenderer(context: Context) : GLSurfaceView.Renderer {
        private var mContext = context

        private var TAG = "TextureRenderer"

        private var vertexBuffer: FloatBuffer? = null

        private var mTexVertexBuffer: FloatBuffer? = null

        private var mVertexIndexBuffer: ShortBuffer? = null

        private var mProgram = 0

        private var textureId = 0

        private var aTextureCoord = 0
        private var aPosition = 0

        /**
         * 顶点坐标
         * (x,y,z)
         */
        private val POSITION_VERTEX = floatArrayOf(
            0f, 0f, 0f,  //顶点坐标V0
            1f, 1f, 0f,  //顶点坐标V1
            -1f, 1f, 0f,  //顶点坐标V2
            -1f, -1f, 0f,  //顶点坐标V3
            1f, -1f, 0f //顶点坐标V4
        )

        /**
         * 纹理坐标
         * (s,t)
         */
        private val TEX_VERTEX = floatArrayOf(
            0.5f, 0.5f,  //纹理坐标V0
            1f, 0f,  //纹理坐标V1
            0f, 0f,  //纹理坐标V2
            0f, 1.0f,  //纹理坐标V3
            1f, 1.0f //纹理坐标V4
        )

        /**
         * 索引
         */
        private val VERTEX_INDEX = shortArrayOf(
            0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
            0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
            0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
            0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
        )

        private fun getVertexString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext.resources, "texture/vertex.sh")
        }

        private fun getFragmentString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext.resources, "texture/fragment.sh")
        }

        override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

            GLES30.glClearColor(1f, 1f, 1f, 1f)

            var vertexIndex = ShardUtils.loadShader(GLES30.GL_VERTEX_SHADER, getVertexString())

            var fragmenIndex = ShardUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, getFragmentString())
            var programIndex = ShardUtils.createProgram(vertexIndex, fragmenIndex)
            programIndex = ShardUtils.linkProgram(programIndex)

            aTextureCoord = GLES30.glGetAttribLocation(programIndex, "aTextureCoord")
            aPosition = GLES30.glGetAttribLocation(programIndex, "aPosition")

            //分配内存空间,每个浮点型占4字节空间
            vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            //传入指定的坐标数据
            vertexBuffer?.put(POSITION_VERTEX)
            vertexBuffer?.position(0)

            mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX)
            mTexVertexBuffer?.position(0)

            mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX)
            mVertexIndexBuffer?.position(0)

            textureId = TextureUtils.createTexture(
                BitmapFactory.decodeResource(
                    mContext.resources,
                    R.drawable.girl
                )
            )

            GLES30.glUseProgram(programIndex)
        }

        override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
        }


        override fun onDrawFrame(p0: GL10?) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            /**
             * 给aPosition设置顶点数组数据
             */
            GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_FLOAT, false, 12, vertexBuffer)
            GLES30.glEnableVertexAttribArray(aPosition)

            /**
             * 给aTextureCoord设置顶点数组数据
             */
            GLES30.glVertexAttribPointer(
                aTextureCoord,
                2,
                GLES30.GL_FLOAT,
                false,
                8,
                mTexVertexBuffer
            )
            GLES30.glEnableVertexAttribArray(aTextureCoord)

            //激活纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            //绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)

            // 绘制回执4个三角形
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                VERTEX_INDEX.size,
                GLES20.GL_UNSIGNED_SHORT,
                mVertexIndexBuffer
            )

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0)
            /**
             * 每次绘制完毕后要禁用
             */
            GLES30.glDisableVertexAttribArray(aPosition)
            GLES30.glDisableVertexAttribArray(aTextureCoord)
        }
    }
}