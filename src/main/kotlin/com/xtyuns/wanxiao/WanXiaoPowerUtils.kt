package com.xtyuns.wanxiao

import com.xtyuns.http.Client
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TreeMap

object WanXiaoPowerUtils {
    private suspend inline fun <reified T> post(urlString: String, data: JsonObject, client: HttpClient = Client.INSTANCE): WanXiaoResp<T> {
        val resp = client.post(urlString) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.body<WanXiaoRespWithNullField<T>>()

        // bad respond
        if ("SUCCESS" != resp.returncode) {
            resp.apply {
                throw RuntimeException("[${returncode}]: ${returnmsg}")
            }
        }

        // make sure businessData is not null
        if (null == resp.businessData) {
            throw RuntimeException("接口返回业务数据为空!")
        }

        // convert WanXiaoRespWithNullField to WanXiaoResp
        return Client.JSON.decodeFromString(Client.JSON.encodeToString(resp))
    }

    /**
     * buildNumber -> areaId
     */
    private suspend fun getAreaInfoMap(): Map<String, String> {
        val urlString = "${Constants.BASE_URL}/paygateway/smallpaygateway/trade"
        val data = buildJsonObject {
            put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            put("method", "samllProgramGetRoom")
            put("sourceId", 2)
            put("bizcontent", buildJsonObject {
                put("schoolno", "1402")
                put("optype", "1")
                put("payproid", 953)
                put("areaid", "0")
                put("buildid", "0")
                put("unitid", "0")
                put("levelid", "0")
                put("businesstype", "2")
            }.toString().replace('"', '\"'))
        }

        // send request
        val areaInfoResp = post<List<IdInfo>>(urlString, data)

        // key: buildNumber, value: areaId
        val areaInfoMap = TreeMap<String, String> { o1, o2 -> o1.toInt() - o2.toInt() }

        // get areaId of buildNumber info
        areaInfoResp.businessData.forEach { areaInfo ->
            // "1-6,10-13,3号楼" -> [1-6, 1, 6, ], [10-13, 10, 13, ], [3, , , 3]
            val buildNumberIterator = Regex("(\\d+)-(\\d+)|(\\d+)").findAll(areaInfo.name).iterator()
            buildNumberIterator.forEach { matchedBuildNumber ->
                val matchedTextArray = matchedBuildNumber.groupValues
                if (matchedTextArray[1].isEmpty()) {
                    // [3, , , 3]
                    areaInfoMap[matchedTextArray[3]] = areaInfo.id
                } else {
                    // [1-6, 1, 6, ]
                    for (i in matchedTextArray[1].toInt()..matchedTextArray[2].toInt()) {
                        areaInfoMap[i.toString()] = areaInfo.id
                    }
                }
            }
        }

        return areaInfoMap
    }

    private suspend fun getBuildInfoMap(areaId: String): Map<String, String> {
        val urlString = "${Constants.BASE_URL}/paygateway/smallpaygateway/trade"
        val data = buildJsonObject {
            put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            put("method", "samllProgramGetRoom")
            put("sourceId", 2)
            put("bizcontent", buildJsonObject {
                put("schoolno", "1402")
                put("optype", "2")
                put("payproid", 953)
                put("areaid", areaId)
                put("buildid", "0")
                put("unitid", "0")
                put("levelid", "0")
                put("businesstype", "2")
            }.toString().replace('"', '\"'))
        }

        // send request
        val buildInfoResp = post<List<IdInfo>>(urlString, data)

        // key: buildNumber, value: areaId
        val buildInfoMap = TreeMap<String, String> { o1, o2 -> o1.toInt() - o2.toInt() }

        buildInfoResp.businessData.forEach { buildInfo ->
            // "#10公寓", "#50专家楼"
            // "11号楼"
            val buildNumberIterator = Regex("#(\\d+)[\\s\\S]|(\\d+)号楼").findAll(buildInfo.name).iterator()
            buildNumberIterator.forEach { buildNumberMatched ->
                val matchedTextArray = buildNumberMatched.groupValues
                val matchedBuildNumber = matchedTextArray[1].ifEmpty { matchedTextArray[2] }
                buildInfoMap[matchedBuildNumber] = buildInfo.id
            }
        }

        return buildInfoMap
    }

    private suspend fun getFloorInfoMap(areaId: String, buildId: String): Map<String, String> {
        val urlString = "${Constants.BASE_URL}/paygateway/smallpaygateway/trade"
        val data = buildJsonObject {
            put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            put("method", "samllProgramGetRoom")
            put("sourceId", 2)
            put("bizcontent", buildJsonObject {
                put("schoolno", "1402")
                put("optype", "3")
                put("payproid", 953)
                put("areaid", areaId)
                put("buildid", buildId)
                put("unitid", "0")
                put("levelid", "0")
                put("businesstype", "2")
            }.toString().replace('"', '\"'))
        }

        // send request
        val floorInfoResp = post<List<IdInfo>>(urlString, data)

        // key: buildNumber, value: areaId
        val floorInfoMap = TreeMap<String, String> { o1, o2 -> o1.toInt() - o2.toInt() }

        floorInfoResp.businessData.forEach { floorInfo ->
            // "1", "1层", "10层"
            val floorNumberIterator = Regex("(\\d+)层?").findAll(floorInfo.name).iterator()
            floorNumberIterator.forEach { floorNumberMatched ->
                floorInfoMap[floorNumberMatched.groupValues[1]] = floorInfo.id
            }
        }

        return floorInfoMap
    }

    private suspend fun getRoomInfoMap(
        areaId: String,
        buildId: String,
        floorId: String,
        buildNumber: Int,
        roomNumber: Int
    ): Map<String, String> {
        val urlString = "${Constants.BASE_URL}/paygateway/smallpaygateway/trade"
        val data = buildJsonObject {
            put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            put("method", "samllProgramGetRoom")
            put("sourceId", 2)
            put("bizcontent", buildJsonObject {
                put("schoolno", "1402")
                put("optype", "4")
                put("payproid", 953)
                put("areaid", areaId)
                put("buildid", buildId)
                put("unitid", "0")
                put("levelid", floorId)
                put("businesstype", "2")
            }.toString().replace('"', '\"'))
        }

        // send request
        val roomInfoResp = post<List<IdInfo>>(urlString, data)

        // key: buildNumber, value: areaId
        val roomInfoMap = TreeMap<String, String> { o1, o2 -> o1.toInt() - o2.toInt() }

        roomInfoResp.businessData.forEach { roomInfo ->
            // 9号楼101宿舍 -> "101"
            val targetRoomName = "$roomNumber"

            // 32, 33 号楼的门牌号是 4 位, 其中序号占3位, 楼层占一位(共6层)
            // #32公寓101宿舍 -> "1001", #33公寓101宿舍 -> "1001"
            // 29号楼101宿舍 -> "101", 29号楼1001宿舍 -> "1001"
            // #9公寓101宿舍 -> "9101", #31公寓101宿舍 -> "31101"

            // 将以上规则统一处理为 ${floor}mn 的形式, 如 101(1001), 905, 1012
            val roomName = roomInfo.name.substring(roomInfo.name.length - targetRoomName.length)

            roomInfoMap[roomName] = roomInfo.id
        }

        return roomInfoMap
    }

    private suspend fun getRoomState(roomId: String): PowerInfo {
        val urlString = "${Constants.BASE_URL}/paygateway/smallpaygateway/trade"
        val data = buildJsonObject {
            put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            put("method", "samllProgramGetRoomState")
            put("sourceId", 2)
            put("bizcontent", buildJsonObject {
                put("payproid", 953)
                put("schoolcode", "1402")
                put("roomverify", roomId)
                put("businesstype", "2")
            }.toString().replace('"', '\"'))
        }

        // send request
        val roomStateResp = post<PowerInfo>(urlString, data)

        return roomStateResp.businessData
    }

    suspend fun queryPower(buildNumber: Int, roomNumber: Int): BigDecimal {
        val floorNumber = roomNumber.toString()
            .padStart(4, '0')
            .substring(0, 2)
            .toInt()

        var areaId = ""
        var buildId = ""
        var floorId = ""
        var roomId = ""

        getAreaInfoMap()[buildNumber.toString()]?.let {
            areaId = it
            getBuildInfoMap(areaId)[buildNumber.toString()]
        }?.let {
            buildId = it
            getFloorInfoMap(areaId, buildId)[floorNumber.toString()]
        }?.let {
            floorId = it
            getRoomInfoMap(areaId, buildId, floorId, buildNumber, roomNumber)["$roomNumber"]
        }?.let {
            roomId = it
            val roomState = getRoomState(roomId)
            return BigDecimal(roomState.quantity)
        } ?: throw IllegalArgumentException("获取房间信息状态失败, 请检查房间信息是否存在!")
    }
}