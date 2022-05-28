package com.xtyuns.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PowerPluginConfig : AutoSavePluginConfig("config") {
    @ValueDescription("启用中的群列表")
    val acceptGroupList by value<MutableMap<Long, Boolean>>()

    @ValueDescription("触发查询消息内容前缀")
    val queryPrefix by value("查电费")

    @ValueDescription("管理员 QQ")
    val admin by value(948038320L)

    @ValueDescription("开启当前群查询功能消息内容")
    val openGroupMsg by value("/open")

    @ValueDescription("关闭当前群查询功能消息内容")
    val closGroupMsg by value("/close")
}