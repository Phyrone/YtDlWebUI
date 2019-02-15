package de.phyrone.ytdlwebui

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import com.sapher.youtubedl.YoutubeDL
import com.sapher.youtubedl.YoutubeDLRequest
import io.ktor.http.cio.websocket.send
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.tika.Tika
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val hasher = Hashing.sha256()
val base64 = BaseEncoding.base64()
val requests = HashMap<String, DownloadRequest>()
val logUpdateExecutor = Executors.newCachedThreadPool()
val defaultArgs = config[YTDLSel.defaultArgs]
val scheudler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors().coerceAtLeast(1))
val ytDlPath = config[Sel.youtubeDLPath]
val profiles = config[YTDLSel.profiles]
val defaultProfile = config[YTDLSel.defaultProfileArgs]
val defaultProfileName = config[YTDLSel.defaultProfileName]
val tika = Tika()


object DownloadBackend {
    init {
        YoutubeDL.setExecutablePath(config[Sel.youtubeDLPath])
    }

    fun getMimeType(file: File): String {
        return tika.detect(file)
    }

    fun startDowload(req: RequestDownloadJson, defaultWebSocketServerSession: DefaultWebSocketServerSession) {
        scope.launch {
            val downloadReq = DownloadRequest(req.url, req.profile, defaultWebSocketServerSession)
            requests[downloadReq.id] = downloadReq
            downloadReq.run()
        }

    }

    fun getFile(id: String): File? {
        return requests[id]?.file
    }
}

val tempParentFile = File("temp/").also {
    if (it.exists()) it.deleteRecursively()
    it.mkdirs()
    Runtime.getRuntime().addShutdownHook(Thread {
        it.deleteRecursively()
    })
}

class DownloadRequest(val url: String, val profileName: String, val defaultWebSocketServerSession: DefaultWebSocketServerSession) : Runnable {
    fun genID() = base64.encode(hasher.newHasher().putString(url, Charsets.UTF_8).putString(profileName, Charsets.UTF_8).hash().asBytes()).replace("/", "\\/").replace("\\", "\\\\")

    var lastUse = System.currentTimeMillis()
    val id = genID()
    var finished = false
    val time = System.currentTimeMillis()
    private val tempFile = File(tempParentFile.path, id).also {
        it.mkdirs()
    }


    var file: File? = null
    override fun run() {
        runBlocking {
            scheudler.schedule({ delete() }, 2, TimeUnit.HOURS)
            scope.launch {
                try {
                    defaultWebSocketServerSession.send(AddVideoJson(id, url, getTitle()).toString())
                    logger.info("Start Download: $id")
                    val profArgs = profiles[profileName] ?: defaultProfile
                    val cmdArray = arrayOf(
                            *defaultArgs,
                            *profArgs
                    )
                    val cmd = cmdArray.joinToString(" ")

                    val req = YoutubeDLRequest(url, tempFile.path)
                    cmdArray.forEach {
                        if (it.contains(" ")) {
                            val spl = it.split(" ")
                            req.setOption(spl[0], spl.subList(1, spl.size).joinToString(" "))

                        } else {
                            req.setOption(it)
                        }
                    }
                    defaultWebSocketServerSession.send(UpdateDownloadJson(id, 0F).toString())
                    val res = YoutubeDL.execute(req) { fl, _ ->
                        runBlocking {
                            defaultWebSocketServerSession.send(UpdateDownloadJson(id, fl).toString())
                        }
                    }
                    defaultWebSocketServerSession.send(UpdateDownloadJson(id, 100F).toString())
                    println("Download Finish: $id")
                    if (res.exitCode == 0) {
                        file = tempFile.listFiles().firstOrNull()
                    }
                    if (file != null) {
                        defaultWebSocketServerSession.send(DownloadResultJson(id, DownloadResultJson.Status.FINISH).toString())
                    } else {
                        defaultWebSocketServerSession.send(DownloadResultJson(id, DownloadResultJson.Status.FAILED).toString())
                    }
                } catch (e: Exception) {
                    defaultWebSocketServerSession.send(DownloadResultJson(id, DownloadResultJson.Status.FAILED).toString())
                    e.printStackTrace()
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

    fun delete() {
        tempFile.deleteRecursively()
        requests.remove(id)
    }
}