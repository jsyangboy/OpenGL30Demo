package com.yibasan.opengl30demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yibasan.opengl30demo.test.test_Texture_Activity
import com.yibasan.opengl30demo.test.test_Texture_vbo_Activity
import com.yibasan.opengl30demo.test.test_Triangles_Activity
import com.yibasan.opengl30demo.test.test_Triangles_vbo_Activity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }


}