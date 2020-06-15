package com.yibasan.opengl30demo.util

import android.content.res.Resources


object AssetsUtils {


    fun loadFromAssetsFile(
        res: Resources,
        fname: String?
    ): String? {
        val result = java.lang.StringBuilder()
        try {
            val `is` = res.assets.open(fname!!)
            var ch: Int
            val buffer = ByteArray(1024)
            while (-1 != `is`.read(buffer).also { ch = it }) {
                result.append(String(buffer, 0, ch))
            }
        } catch (e: java.lang.Exception) {
            return null
        }
        return result.toString().replace("\\r\\n".toRegex(), "\n")
    }


}