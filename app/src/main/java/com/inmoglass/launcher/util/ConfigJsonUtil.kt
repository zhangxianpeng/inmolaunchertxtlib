package com.inmoglass.launcher.util

import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.inmoglass.launcher.base.BaseApplication
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * 排列顺序操作工具类
 * @author Administrator
 * @date 2021-12-03
 */
class ConfigJsonUtil private constructor() {
    val filePath = BaseApplication.mContext.getExternalFilesDir(null)!!.absolutePath
    val fileName = "launcherConfig.json"
    companion object {
        @Volatile
        private var instance: ConfigJsonUtil? = null
            get() {
                if (field == null) {
                    synchronized(ConfigJsonUtil::class) {
                        if (field == null)
                            field = ConfigJsonUtil()
                    }
                }
                return field
            }

        @Synchronized
        fun get(): ConfigJsonUtil {
            return instance!!
        }
    }

    /**
     * 创建文件
     */
    fun createFile(): Boolean {
        var flag = false
        LogUtils.i("path = $filePath")
        val name = File("$filePath/$fileName")
        if(!name.exists()) {
            name.createNewFile()
            flag = true
        }
        return flag
    }

    /**
     * 写入配置数据到json文件
     */
    fun writeJSONArrayData2File(data: String) {
        val thisFile = File("$filePath/$fileName")
        try {
            if (!thisFile.parentFile.exists()) {
                thisFile.parentFile.mkdirs()
            }
            val fw = FileWriter("$filePath/$fileName")
            fw.write(data)
            fw.close()
        } catch (e: IOException) {
            LogUtils.e("writeJSONArrayData2File error = " + e.message)
            e.printStackTrace()
        }
    }

    /**
     * 从json文件读取数据
     */
    fun getJSONArrayDataFromFile() {

    }
}