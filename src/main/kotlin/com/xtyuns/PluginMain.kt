package com.xtyuns

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object PluginMain : KotlinPlugin(JvmPluginDescription.loadFromResource("mirai.yml")) {
    override fun onEnable() {
        super.onEnable()
    }
}