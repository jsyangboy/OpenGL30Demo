package com.yibasan.opengl30demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yibasan.opengl30demo.test.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //读写权限
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 2)
            }
        }


        var intentTriangles = Intent(this, test_Triangles_Activity::class.java)
        test_triangles.setOnClickListener {
            startActivity(intentTriangles)
        }

        var intentTrianglesVBO = Intent(this, test_Triangles_vbo_Activity::class.java)
        test_triangles_vbo.setOnClickListener {
            startActivity(intentTrianglesVBO)
        }

        var intentTexture = Intent(this, test_Texture_Activity::class.java)
        test_texture.setOnClickListener {
            startActivity(intentTexture)
        }

        var intentTextureVBO = Intent(this, test_Texture_vbo_Activity::class.java)
        test_texture_vbo.setOnClickListener {
            startActivity(intentTextureVBO)
        }

        var intentTextureVBOZhengjiao =
            Intent(this, test_Texture_vbo_zhengjiao_Activity::class.java)
        test_texture_vbo_zhengjiao.setOnClickListener {
            startActivity(intentTextureVBOZhengjiao)
        }

        var intentTextureVBOZhengjiaoFBo =
            Intent(this, test_Texture_vbo_zhengjiao_fbo_Activity::class.java)
        test_texture_vbo_zhengjiao_fbo.setOnClickListener {
            startActivity(intentTextureVBOZhengjiaoFBo)
        }

        var intentTextureVBOZhengjiaoFboRbo =
            Intent(this, test_Texture_vbo_zhengjiao_fbo_rbo_Activity::class.java)
        test_texture_vbo_zhengjiao_fbo_rbo.setOnClickListener {
            startActivity(intentTextureVBOZhengjiaoFboRbo)
        }

        var intentTextureVBOZhengjiaoFboRboPbo =
            Intent(this, test_Texture_vbo_zhengjiao_fbo_rbo_pbo_Activity::class.java)
        test_texture_vbo_zhengjiao_fbo_rbo_pbo.setOnClickListener {
            startActivity(intentTextureVBOZhengjiaoFboRboPbo)
        }

        var intentTextureVBOZhengjiaoFboRboVideoSave =
            Intent(this, test_Texture_vbo_zhengjiao_fbo_rbo_video_save_Activity::class.java)
        test_texture_vbo_zhengjiao_fbo_rbo_video_save.setOnClickListener {
            startActivity(intentTextureVBOZhengjiaoFboRboVideoSave)
        }
    }


}