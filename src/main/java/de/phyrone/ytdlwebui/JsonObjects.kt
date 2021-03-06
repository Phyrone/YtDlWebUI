package de.phyrone.ytdlwebui

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper

val mapper = ObjectMapper()

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class RestItem(val id: Int) {
    constructor(type: PacketType) : this(type.id)

    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }

    companion object {
        fun <T : RestItem> fromJson(json: String, type: Class<T>): T {
            return mapper.readValue(json, type)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
open class IdRestItem : RestItem(0)

@JsonIgnoreProperties(ignoreUnknown = true)
class RequestDownloadJson : RestItem(PacketType.DOWNLOADREQUEST) {
    lateinit var url: String
    var profile = defaultProfileName
}

class AddVideoJson(val videoID: String, val url: String, val title: String) : RestItem(PacketType.ADDVIDEO)

class UpdateDownloadJson(val videoID: String, val percent: Float) : RestItem(PacketType.UPDATEVIDEO)

class DownloadResultJson(val videoID: String, val state: Status) : RestItem(PacketType.SETSTATUS) {
    enum class Status {
        FAILED, FINISH
    }
}

enum class PacketType(val id: Int) {
    DOWNLOADREQUEST(31), UPDATEVIDEO(32), ADDVIDEO(30), SETSTATUS(33)
}

class DownloadInfoJson(val name: String, val type: String) {
    override fun toString(): String {
        return mapper.writeValueAsString(this)
    }
}