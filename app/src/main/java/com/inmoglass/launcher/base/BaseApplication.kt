package com.inmoglass.launcher.base

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.inmo.network.AndroidNetworking
import com.qweather.sdk.view.HeConfig
import com.tencent.mmkv.MMKV

class BaseApplication : Application() {

    /**
    //     * 和风天气SDK key
    //     */
//    val WEATHER_KEY: String = "b9754523666e4abeabef84808c64ed2b"

    /**
     * 和风天气SDK 产品ID
     */
    val WEATHER_PRODUCT_ID: String = "HE1701090954491910"

    override fun onCreate() {
        super.onCreate()
        mContext = this@BaseApplication
        MMKV.initialize(this)
        initWaetherSdk()
//        AndroidNetworking.enableLogging()
        LogUtils.getConfig().isLogSwitch = false
    }

    companion object {
        val WEATHER_KEY = "b9754523666e4abeabef84808c64ed2b"
        lateinit var mContext: Context
    }

    private fun initWaetherSdk() {
        HeConfig.init(WEATHER_PRODUCT_ID, WEATHER_KEY)
        // 切换至开发版服务
        HeConfig.switchToDevService()
    }
}