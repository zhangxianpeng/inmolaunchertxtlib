package com.inmoglass.launcher.base

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.AdaptScreenUtils
import com.blankj.utilcode.util.ScreenUtils

/**
 * 基于640宽度的基类
 * @author Administrator
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
    }

    override fun getResources(): Resources {
        return AdaptScreenUtils.adaptWidth(super.getResources(), 640)
    }

    private fun setFullScreen() {
        ScreenUtils.setFullScreen(this)
    }
}