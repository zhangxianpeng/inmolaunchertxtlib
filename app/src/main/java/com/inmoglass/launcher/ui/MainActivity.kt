package com.inmoglass.launcher.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.os.*
import android.view.KeyEvent
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.core.widget.toast
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.inmo.network.AndroidNetworking
import com.inmo.network.error.ANError
import com.inmo.network.interfaces.JSONObjectRequestListener
import com.inmoglass.coverflowlib.CoverFlowView
import com.inmoglass.launcher.R
import com.inmoglass.launcher.adapter.CoverFlowAdapter
import com.inmoglass.launcher.base.BaseActivity
import com.inmoglass.launcher.base.BaseApplication
import com.inmoglass.launcher.bean.Channel
import com.inmoglass.launcher.service.SocketService
import com.inmoglass.launcher.util.APPUtil
import com.inmoglass.launcher.util.WeatherResUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_status_bar.*
import org.json.JSONObject

/**
 * 桌面
 * @author Administrator
 * @date 2021-12-01
 */
class MainActivity : BaseActivity() {
    private val TAG = "MainActivity"
    private var channelBeanList: MutableList<Channel>? = null
    private val selfStudyApps = arrayOf(
        "com.inmolens.inmomemo",
        "com.yulong.coolcamera",
        "com.yulong.coolgallery",
        "com.inmo.settings",
        "com.tentencent.qqlive"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        // 定位天气
        val strings = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (!PermissionUtils.isGranted(*strings)) {
            PermissionUtils
                .permission(*strings)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        startLocation()
                    }

                    override fun onDenied() {
                        toast("请授予权限，才能定位")
                    }
                }).request()
        } else {
            startLocation()
        }

        // 蓝牙服务
        startSocketService()
        // 获取设备已安装应用信息，写入json文件
        // 使用本地json文件的方案的原因： 后续版本会在设置中修改launcher显示应用的排列顺序，顺序更新后重新update json即可
        // 三方APP
        // 正式代码
        val allAppList = APPUtil.getPackageInfos(this, 0)
//        initCoverFlowData(allAppList)

        // 写死用来测试
        initCoverFlowTestData()
//        initCoverFlow2TestData()
    }

    var batteryReceiver: BatteryReceiver? = null
    override fun onResume() {
        super.onResume()
        batteryReceiver = BatteryReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, intentFilter)
    }

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val level = p1!!.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val isCharging = p1.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) !== 0
            LogUtils.i(TAG, "currentBattery$level")
            sbb_battery.setBattaryPercent(level)
            if (level in 0..5) {
                sbb_battery.setFrontColor(getColor(R.color.color_battery_red))
            } else if (level in 6..15) {
                sbb_battery.setFrontColor(getColor(R.color.color_battery_orange))
            } else if (level in 16..100) {
                sbb_battery.setFrontColor(getColor(R.color.color_battery_white))
            }
            iv_isCharging.visibility = if (isCharging) VISIBLE else INVISIBLE
        }
    }

    override fun onDestroy() {
        LogUtils.e(TAG, "onDestroy")
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    private fun initCoverFlowData(appList: List<PackageInfo>) {
        var resultApps = ArrayList<PackageInfo>()
        for (i in 0..appList.size) {
            for (j in 0..selfStudyApps.size) {
                if (appList[i].packageName.equals(selfStudyApps[j])) {
                    continue
                } else {
                    resultApps.add(appList[i])
                }
            }
        }
    }


//        val pm = packageManager
//        channelBeanList = ArrayList()
//        val memoChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_memo), getDrawable(R.mipmap.ic_launcher)!!, "com.inmolens.inmomemo")
//        channelBeanList!!.add(memoChannel)
//        val cameraChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_camera), getDrawable(R.mipmap.ic_launcher)!!, "com.yulong.coolcamera")
//        channelBeanList!!.add(cameraChannel)
//        val phoneChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_phone), getDrawable(R.mipmap.ic_launcher)!!, "com.tencent.mm")
//        channelBeanList!!.add(phoneChannel)
//        for (item in thirdPartyAppList) {
//            val itemChannel = Channel(R.mipmap.ic_launcher, item.applicationInfo.loadLabel(pm).toString(), item.applicationInfo.loadIcon(pm), item.packageName)
//            channelBeanList!!.add(itemChannel)
//        }
//        val coverFlowAdapter = CoverFlowAdapter(this, channelBeanList)
//        coverflow.setAdapter(coverFlowAdapter)

    // 给coverFlowView的TOPView 添加点击事件监听

    // 给coverFlowView的TOPView 添加点击事件监听
//        coverflow.setOnTopViewClickListener(mOnTopViewClickListener)

    private fun initCoverFlowTestData() {
        channelBeanList = ArrayList()
        val wechatChannel = Channel(R.drawable.img_home_beiwanglu, getString(R.string.string_home_beiwanglu), R.drawable.icon_home_beiwanglu, selfStudyApps[0])
        channelBeanList!!.add(wechatChannel)
        val tencentChannel = Channel(R.drawable.img_home_camera, getString(R.string.string_home_camera), R.drawable.icon_home_camera, selfStudyApps[1])
        channelBeanList!!.add(tencentChannel)
        val qqMusicChannel = Channel(R.drawable.img_home_meitiwenjian, getString(R.string.string_home_media), R.drawable.icon_home_meiti, selfStudyApps[2])
        channelBeanList!!.add(qqMusicChannel)
        val kugouChannel = Channel(R.drawable.img_home_setting, getString(R.string.string_home_setting), R.drawable.icon_home_setting, selfStudyApps[3])
        channelBeanList!!.add(kugouChannel)
        val aMapChannel = Channel(R.drawable.img_home_tengxun, getString(R.string.string_home_tencent), R.drawable.icon_home_tengxun, selfStudyApps[4])
        channelBeanList!!.add(aMapChannel)
        val coverFlowAdapter = CoverFlowAdapter(this, channelBeanList)
        coverflow.adapter = coverFlowAdapter
        coverflow.onTopViewClickListener = mOnTopViewClickListener
    }

    private val mOnTopViewClickListener: CoverFlowView.OnTopViewClickListener = CoverFlowView.OnTopViewClickListener { position, itemView ->
        val channelBean: Channel = channelBeanList!![position]
        APPUtil.getInstance().openApplication(channelBean.packageName)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
        LogUtils.i(TAG, "KEYcODE = " + keyCode)
    }

    private fun setConfigJsonFile(thirdPartyAppList: List<PackageInfo>) {
//        val pm = packageManager
//        val jsonObject = JSONObject()
//        val jsonArray = JSONArray()
//        val memoChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_memo), getDrawable(R.mipmap.ic_launcher)!!, "com.inmolens.inmomemo")
//        jsonArray.put(memoChannel)
//        val cameraChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_camera), getDrawable(R.mipmap.ic_launcher)!!, "com.yulong.coolcamera")
//        jsonArray.put(cameraChannel)
//        val phoneChannel = Channel(R.mipmap.ic_launcher, getString(R.string.string_home_phone), getDrawable(R.mipmap.ic_launcher)!!, "com.tencent.mm")
//        jsonArray.put(phoneChannel)
//        for (item in thirdPartyAppList) {
//            var itemChannel = Channel(R.mipmap.ic_launcher, item.applicationInfo.loadLabel(pm).toString(), item.applicationInfo.loadIcon(pm), item.packageName)
//            jsonArray.put(itemChannel)
//        }
//        jsonObject.put("launcherConfig", jsonArray)
//        var configData = jsonObject.toString()
//        LogUtils.i(TAG, "all APPS = $configData")
//        if (ConfigJsonUtil.get().createFile()) {
//            ConfigJsonUtil.get().writeJSONArrayData2File(configData)
//        }
    }

    private fun initViews() {
    }

    /**
     * 开启需要的服务
     */
    private fun startSocketService() {
        val intent = Intent(this, SocketService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    var mLocationClient: LocationClient? = null
    var myListener = MyLocationListener()
    private fun startLocation() {
        mLocationClient = LocationClient(applicationContext)
        // 声明LocationClient类
        mLocationClient!!.registerLocationListener(myListener)
        val option = LocationClientOption()
        option.setIsNeedAddress(true)
        option.setNeedNewVersionRgc(true)
        mLocationClient!!.locOption = option
        mLocationClient!!.start()
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            val latitude: Double = location!!.latitude
            val longitude: Double = location.longitude
            LogUtils.i("map = $latitude")
            getCurrentWeather(latitude, longitude)
        }
    }

    private fun getCurrentWeather(lat: Double, lng: Double) {
        val baseUrl = "https://devapi.qweather.com/v7/weather/now"
        AndroidNetworking.get(baseUrl)
            .addQueryParameter("key", BaseApplication.WEATHER_KEY)
            .addQueryParameter("location", "$lng,$lat")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    LogUtils.i(TAG, "onResponse = $response")
                    val code = response.get("code")
                    if (!code.equals("200")) {
                        return
                    }
                    val message = Message()
                    message.what = 0
                    message.obj = response
                    myHandler.sendMessage(message);
                }

                override fun onError(error: ANError) {
                    LogUtils.e(TAG, "onError = " + error.message)
                }
            })
    }

    private val myHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var action = msg.what
            when (action) {
                0 -> { // 刷天气
                    var obj = msg.obj as JSONObject
                    var nowObj = obj.get("now") as JSONObject
                    var temp = nowObj.get("temp") as String
                    var icon = nowObj.get("icon") as String
                    var text = nowObj.get("text") as String
                    tv_temperature.text = "$temp℃"
                    tv_weather.text = text
                    iv_temperature.setImageResource(WeatherResUtil.getEqualRes(icon))
                }
            }
        }
    }
}