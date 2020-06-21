package com.yibasan.opengl30demo.test

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.Bundle
import android.os.Environment
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.R
import com.yibasan.opengl30demo.util.*
import kotlinx.android.synthetic.main.activity_test_texture_video_save.*
import java.io.File
import java.nio.*

class test_Texture_vbo_zhengjiao_fbo_rbo_video_save_Activity : AppCompatActivity() {
    var textureRenderer: TextureRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_texture_video_save)
        title = "fbo+rbo+视频保存"
        //glSurfaceView14.setEGLContextClientVersion(3)

        textureRenderer = TextureRenderer("application")
        textureRenderer?.mContext = applicationContext
        //glSurfaceView14.setRenderer(textureRenderer)
        textureRenderer?.init(544, 960)

        btn_start.setOnClickListener {
            textureRenderer?.make()
        }

    }

    override fun onResume() {
        super.onResume()
        //glSurfaceView14.onResume()
    }

    override fun onPause() {
        super.onPause()
        //glSurfaceView14.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        textureRenderer?.destroy3()
    }

    class TextureRenderer(name: String):HandlerThread(name) {

        var mContext: Context? = null

        private var TAG = "TextureRenderer"

        private var vertexBuffer: FloatBuffer? = null

        private var mTexVertexBuffer: FloatBuffer? = null

        private var mVertexIndexBuffer: ShortBuffer? = null

        private var mProgram = 0

        private var textureId = 0

        private var aTextureCoord = 0
        private var aPosition = 0
        private var u_Matrix = 0
        private var width: Int = 0
        private var height: Int = 0

        //路径静态常量
        private val BASE_PATH =
            Environment.getExternalStorageDirectory().path
        private val VIME_SAVE_ROOT_PATH = "$BASE_PATH/183/LizhiFM/ViMe"
        private val VIME_AUDIO_PATH = "$VIME_SAVE_ROOT_PATH/audio/"
        private val VIME_VIDEO_PATH = "$VIME_SAVE_ROOT_PATH/video/"
        private val VIME_IMAGE_PATH = "$VIME_SAVE_ROOT_PATH/image/"
        private val VIME_IMAGE_PATH_TEST = "$VIME_SAVE_ROOT_PATH/test"

        var vao: IntBuffer? = null
        var fbo: Int = 0
        var fboTextureId = 0

        private val mMatrix = FloatArray(16)

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

        private var bitmapWidth: Int = 0
        private var bitmapHeight: Int = 0


        private var mEglCore: EglCore? = null
        private var videoEncoderCode: VideoEncoderCode? = null
        private var windowSurface: WindowSurface? = null

        private fun getVertexString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext?.resources!!, "texture2/vertex.sh")
        }

        private fun getFragmentString(): String? {
            return AssetsUtils.loadFromAssetsFile(mContext?.resources!!, "texture2/fragment.sh")
        }

        fun init(width: Int, height: Int) {
            Log.e(TAG, "init")
            onSurfaceCreated()
            onSurfaceChanged(width, height)
        }

        fun onSurfaceCreated() {
            Log.e(TAG, "onSurfaceCreated start")

            try {
                var file = File(VIME_VIDEO_PATH)
                if (file.mkdirs()) {
                    Log.e(TAG, "mkdirs faile")
                }
                mEglCore = EglCore(null, EglCore.FALG_TRY_GLES3)
                videoEncoderCode = VideoEncoderCode(
                    544,
                    960,
                    1000000,
                    File(VIME_VIDEO_PATH + System.currentTimeMillis() + ".mp4")
                )
                windowSurface =
                    WindowSurface(mEglCore!!, videoEncoderCode!!.getInputSurface()!!, false)
                windowSurface?.makeCurrent()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            //GLES30.glClearColor(1f, 1f, 1f, 1f)

            var vertexIndex = ShardUtils.loadShader(GLES30.GL_VERTEX_SHADER, getVertexString())

            var fragmenIndex = ShardUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, getFragmentString())
            var programIndex = ShardUtils.createProgram(vertexIndex, fragmenIndex)
            programIndex = ShardUtils.linkProgram(programIndex)

            aTextureCoord = GLES30.glGetAttribLocation(programIndex, "aTextureCoord")
            aPosition = GLES30.glGetAttribLocation(programIndex, "aPosition")
            u_Matrix = GLES30.glGetUniformLocation(programIndex, "u_Matrix")

            GLES30.glUseProgram(programIndex)

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

            val options = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeResource(
                mContext?.resources,
                R.drawable.girl,
                options
            )

            bitmapWidth = options.outWidth
            bitmapHeight = options.outHeight

            textureId = TextureUtils.createTexture(bitmap)

            vao = IntBuffer.allocate(1)
            GLES30.glGenVertexArrays(1, vao)
            GLES30.glBindVertexArray(vao!![0])

            var vbo: IntBuffer? = IntBuffer.allocate(2)
            GLES30.glGenBuffers(2, vbo)

            /**
             * 设置顶点的vbo（Vertex Buffer Object）
             */
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo!![0])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                POSITION_VERTEX.size * 4,
                vertexBuffer,
                GLES30.GL_STATIC_DRAW
            )
            GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_FLOAT, false, 12, 0)
            GLES30.glEnableVertexAttribArray(aPosition)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

            /**
             * 设置纹理顶点的vbo（Vertex Buffer Object）
             */
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo!![1])
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                TEX_VERTEX.size * 4,
                mTexVertexBuffer,
                GLES30.GL_STATIC_DRAW
            )
            GLES30.glVertexAttribPointer(aTextureCoord, 2, GLES30.GL_FLOAT, false, 8, 0)
            GLES30.glEnableVertexAttribArray(aTextureCoord)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

            GLES30.glBindVertexArray(0)

            /**
             * 创建一个指定大小的rgba的纹理
             */
            fboTextureId = TextureUtils.createTexture(bitmapWidth, bitmapHeight)
            /**
             * 创建FBO+RBO（Frame Buffer Object+Render Buffer Object）
             */
            fbo = FboUtils.createFboRbo(bitmapWidth, bitmapHeight, fboTextureId)
            Log.e(TAG, "fbo[0]=$fbo")


            Log.e(TAG, "onSurfaceCreated end")
        }

        var mTempMatrix = FloatArray(16)

        fun onSurfaceChanged(width: Int, height: Int) {
            Log.e(TAG, "onSurfaceChanged start")
            GLES30.glViewport(0, 0, width, height)
            this.width = width
            this.height = height
            Matrix.orthoM(
                mTempMatrix,
                0,
                -1f,
                1f,
                1f,//这里的bottom跟top要颠倒，要不得到的图像会翻转
                -1f,
                -1f,
                1f
            )

            /**
             * 为了让图片等比例缩放
             */
            val aspectRatio =
                if (bitmapWidth > bitmapHeight) bitmapWidth.toFloat() / bitmapHeight.toFloat() else bitmapHeight.toFloat() / bitmapWidth.toFloat()


            if (width > height) {
                //横屏
                Matrix.orthoM(
                    mMatrix,
                    0,
                    -aspectRatio,
                    aspectRatio,
                    -1f,
                    1f,
                    -1f,
                    1f
                )
            } else {
                //竖屏
                Matrix.orthoM(
                    mMatrix,
                    0,
                    -1f,
                    1f,
                    -aspectRatio,
                    aspectRatio,
                    -1f,
                    1f
                )
            }

            Log.e(TAG, "onSurfaceChanged end")

        }
        var frameIndex = 0;

        fun onDrawFrame() {
            Log.e(TAG, "onDrawFrame =")
            videoEncoderCode?.drainEncoder(false)
            /**
             * 第一步先绘制到FBO绑定的纹理（绘制就是重新走一遍绘制流程）
             * 注意1：glViewport的宽高跟屏幕的宽高不一样的，我们这里使用了bitmap的宽高最为绘制窗口的大小
             * 注意2：mTempMatrix 这里因为我们绘制的大小是bitmap的大小，不需要做缩放，全部铺满即可
             */
            GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES30.glUniformMatrix4fv(u_Matrix, 1, false, mTempMatrix, 0)//注意mTempMatrix是不需要做出来，fu
            GLES30.glBindVertexArray(vao!![0])
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
            GLES30.glViewport(0, 0, bitmapWidth, bitmapHeight)
            //激活纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            // 绘制回执4个三角形
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                VERTEX_INDEX.size,
                GLES20.GL_UNSIGNED_SHORT,
                mVertexIndexBuffer
            )
            GLES30.glBindVertexArray(GLES30.GL_NONE)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0) //这里解绑后下一次回去就会自动切换到屏幕
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)


            /**
             * 第二步将FBO绘制好的纹理fboTextureId绘制到屏幕（绘制就是重新走一遍绘制流程）
             */
            GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES30.glUniformMatrix4fv(u_Matrix, 1, false, mMatrix, 0)
            GLES30.glViewport(0, 0, width, height)
            GLES30.glBindVertexArray(vao!![0])
            //绑定纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboTextureId)
            // 绘制回执4个三角形
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                VERTEX_INDEX.size,
                GLES20.GL_UNSIGNED_SHORT,
                mVertexIndexBuffer
            )
            GLES30.glBindVertexArray(GLES30.GL_NONE)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
            val presentationTimeNsec: Long = computePresentationTimeNsec(frameIndex)
            windowSurface?.setPresentationTime(presentationTimeNsec)
            windowSurface?.swapBuffers()
        }

        //fps 30
        private fun computePresentationTimeNsec(frameIndex: Int): Long {
            val ONE_BILLION: Long = 1000000000
            return frameIndex * ONE_BILLION / 30
        }

        fun make(){

            Log.e(TAG, "run")
            while (true) {
                Log.e(TAG, "index =$frameIndex")
                onDrawFrame()
                frameIndex++

                if (frameIndex > 300) {
                    break
                }
            }
            Log.e(TAG, "drawFrame End")
        }

        fun destroy3() {
            videoEncoderCode?.drainEncoder(true)
        }

    }
}