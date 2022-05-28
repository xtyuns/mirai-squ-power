package com.xtyuns.wanxiao

import kotlinx.serialization.Serializable


/**
 * 当接口的返回值不存在 businessData 属性时 json 无法正常反序列化, 因为非空的泛型无法定义默认值
 */
@Serializable
class WanXiaoRespWithNullField<T>(var businessData: T? = null, var returncode: String, var returnmsg: String) {
}

@Serializable
class WanXiaoResp<T>(var businessData: T, var returncode: String, var returnmsg: String) {
}

@Serializable
class IdInfo(var id: String, var name: String) {
}

@Serializable
class PowerInfo(var description: String, var quantity: String, var quantityunit: String) {
}