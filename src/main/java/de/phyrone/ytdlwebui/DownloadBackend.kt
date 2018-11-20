package de.phyrone.ytdlwebui

import com.fasterxml.jackson.annotation.JsonProperty
import com.sapher.youtubedl.DownloadProgressCallback
import com.sapher.youtubedl.YoutubeDL
import com.sapher.youtubedl.YoutubeDLRequest
import io.ktor.http.cio.websocket.send
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

val randomDownloadID = RandomString(128)
val requests = HashMap<String, DownloadRequest>()
val logUpdateExecutor = Executors.newCachedThreadPool()
val defaultArgs = arrayOf("no-color", "no-playlist", "no-continue", "restrict-filenames", "newline", "add-metadata", "embed-thumbnail", "embed-subs")
val ytDlPath = config[Sel.youtubeDLPath]
fun genID(): String {
    var ret = randomDownloadID.nextString()
    while (requests.containsKey(ret)) {
        ret = randomDownloadID.nextString()
    }
    return ret
}

object DownloadBackend {
    fun startDowload(req: RequestDownloadJson, defaultWebSocketServerSession: DefaultWebSocketServerSession) {
        val downloadReq = DownloadRequest(req.url, req.quality, defaultWebSocketServerSession)
        requests[downloadReq.id] = downloadReq
        scope.launch {
            downloadReq.run()
        }

    }

    fun getFile(id: String): File? {
        return requests[id]?.file
    }
}

val tempParentFile = File("temp/").also {
    it.mkdirs()
    Runtime.getRuntime().addShutdownHook(Thread {
        it.deleteRecursively()
    })
}

class DownloadRequest(val url: String, val quality: Quality, val defaultWebSocketServerSession: DefaultWebSocketServerSession) : Runnable {
    val id = genID()
    var finished = false
    val time = System.currentTimeMillis()
    private val tempFile = File(tempParentFile.path, id).also {
        it.mkdirs()
    }
    var latstState = CurrenState.STARTED
    var proc: Process? = null
    var file: File? = null
    override fun run() {
        runBlocking {
            scope.launch {
                defaultWebSocketServerSession.send(AddVideoJson(id, url, getTitle()).toString())
                val format = quality.format + "/best[ext=mp4]/best"
                logger.info("Start Download: $id")
                val encArgs = if (quality.extract) {
                    arrayOf("audio-format mp3", "extract-audio")
                } else {
                    arrayOf("recode-video mp4")
                }
                val cmdArray = arrayOf(
                        *defaultArgs,
                        *encArgs
                )
                val cmd = cmdArray.joinToString(" ")

                val req = YoutubeDLRequest(url, tempFile.path)
                req.setOption("format", format)
                cmdArray.forEach {
                    if (it.contains(" ")) {
                        val spl = it.split(" ")
                        req.setOption(spl[0], spl[1])
                    } else {
                        req.setOption(it)
                    }
                }
                defaultWebSocketServerSession.send(UpdateDownloadJson(id, 20).toString())
                val res = YoutubeDL.execute(req) { fl, _ ->
                    println("Proc: $fl")
                    runBlocking {
                        defaultWebSocketServerSession.send(UpdateDownloadJson(id, fl.toInt()).toString())
                    }
                }
                defaultWebSocketServerSession.send(UpdateDownloadJson(id, 100).toString())
                println("Download Finish: $id")
                if (res.exitCode == 0) {
                    file = tempFile.listFiles().firstOrNull()
                }
                if (file != null) {
                    defaultWebSocketServerSession.send(UpdateStatusJson(id, UpdateStatusJson.Status.FINISH).toString())
                } else {
                    defaultWebSocketServerSession.send(UpdateStatusJson(id, UpdateStatusJson.Status.FAILED).toString())
                }
            }
        }
    }

    fun getTitle(): String {
        return YoutubeDL.getVideoInfo(url).fulltitle ?: "Video-$id"
    }

    fun onFinish() {
        finished = true
    }

    fun cancel() {
        proc?.destroy()
        tempFile.deleteRecursively()
    }
}

enum class CurrenState(val percent: Int) {
    STARTED(10), UNKNOWN(0), DOWNLOAD(50), EXTRACE(74), FINISHING(90)
}

enum class Quality(val extract: Boolean = false, vararg val formats: String) {

    MAX("bestvideo+bestaudio"), FULLHD("bestvideo[height<=1080]+bestaudio", "bestvideo[height<=1080]"), HD("bestvideo[height<=720]+bestaudio", "bestvideo[height<=720]"), SD("bestvideo[height<=360]+bestaudio", "bestvideo[height<=360]"), AUDIO(true, "bestaudio");

    val format = formats.joinToString(",")

    constructor(vararg format: String) : this(false, *format)
}

fun main(args: Array<String>) {
    val procB = ProcessBuilder("youtube-dl", "https://youtu.be/DScuWec_fSE")
    val p = procB.start()
    Thread {
        val scanner = Scanner(p.inputStream)
        while (scanner.hasNextLine()) {
            println("-> " + scanner.nextLine())
        }
    }.start()
    Thread {
        val scanner = Scanner(p.errorStream)
        while (scanner.hasNextLine()) {
            println("E> " + scanner.nextLine())
        }
    }.start()
    p.onExit().handle { process, throwable ->
        println("OnExit")
        println("Code : " + process.waitFor())
    }
    p.waitFor()
}

class VideoInfo {
    @JsonProperty("fulltitle")
    var title: String? = null
}