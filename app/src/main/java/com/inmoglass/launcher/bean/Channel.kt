package com.inmoglass.launcher.bean

/**
 * launcher数据实体
 * @author Administrator
 * @date 2021-12-02
 */
data class Channel(
    // 应用快照
    var appImg: Int,
    // 应用名称
    var appName: String,
    // 应用图标
    var appIcon: Int,
    // 包名
    var packageName: String
)