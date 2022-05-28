package com.xtyuns

import com.xtyuns.data.PowerPluginConfig
import com.xtyuns.wanxiao.WanXiaoPowerUtils
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.time.LocalDateTime

object PluginMain : KotlinPlugin(JvmPluginDescription.loadFromResource("mirai.yml")) {
    override fun onEnable() {
        PowerPluginConfig.reload()

        GlobalEventChannel.parentScope(this)
            .subscribeAlways<GroupMessageEvent> {

                // null 表示不处理该事件, Unit 表示处理了该事件
                // 当 processGroupSwitch 处理群消息事件后, processPowerQuery 不再执行
                processGroupSwitch(it)?:
                processPowerQuery(this)
            }
    }

    private suspend fun processGroupSwitch(event: GroupMessageEvent): Unit? {
        if (event.sender.id != PowerPluginConfig.admin) {
            return null
        }

        when (event.message.contentToString()) {
            PowerPluginConfig.openGroupMsg -> {
                val sendMsg =
                    if (PowerPluginConfig.acceptGroupList[event.group.id] == true)
                        "机器人在本群已经处于开启状态了!"
                    else
                        "成功在本群中启用机器人了"

                PowerPluginConfig.acceptGroupList[event.group.id] = true
                // prevent cast to kotlin.Unit to return
                val ignore = event.group.sendMessage(sendMsg)
            }
            PowerPluginConfig.closGroupMsg -> {
                val sendMsg =
                    if (PowerPluginConfig.acceptGroupList[event.group.id] == null)
                        "机器人在本群还未被启用过呢!"
                    else if (PowerPluginConfig.acceptGroupList[event.group.id] == false)
                        "机器人当前已经处于禁用状态了!"
                    else
                        "成功在本群中禁用机器人了"

                PowerPluginConfig.acceptGroupList[event.group.id] = false

                // prevent cast to kotlin.Unit to return
                val ignore = event.group.sendMessage(sendMsg)
            }
            else -> {
                return null
            }
        }

        return Unit
    }

    private suspend fun processPowerQuery(event: GroupMessageEvent): Unit? {
        val groupSwitchStatus = PowerPluginConfig.acceptGroupList[event.group.id] ?: false
        if (!groupSwitchStatus) {
            logger.debug("skip process in group: ${event.group.id}")
            return null
        }

        val powerQueryMatcher = Regex("^${PowerPluginConfig.queryPrefix}\\s*(\\d+)[#＃](\\d+)\\s*$")
        powerQueryMatcher.matchEntire(event.message.contentToString())?.let {
            // 匹配到查询消息
            it.groupValues.run {
                event.group.sendMessage("剩余电量: " + WanXiaoPowerUtils.queryPower(this[1].toInt(), this[2].toInt()))
            }
        }

        return Unit
    }
}