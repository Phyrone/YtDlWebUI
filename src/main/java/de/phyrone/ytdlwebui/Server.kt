package de.phyrone.ytdlwebui

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import picocli.CommandLine
import java.io.File
import java.security.SecureRandom
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


fun main(args: Array<String>) {
    CommandLine.run(ServerBootstrap, *args)
}

val logger = Logger.getLogger("YtDl-Server")
val executor = Executors.newCachedThreadPool()
val scope = CoroutineScope(executor.asCoroutineDispatcher())

@CommandLine.Command
object ServerBootstrap : Runnable {

    val sessionName = config[Sel.sessionName]
    val sessionFile = File("sessions")

    init {
        sessionFile.mkdirs()
        Runtime.getRuntime().addShutdownHook(Thread {
            sessionFile.deleteRecursively()
        })
    }

    val webserver = embeddedServer(
            factory = Netty,
            host = config[NetworkSel.host],
            port = config[NetworkSel.port]
    ) {
        install(WebSockets) {
            timeout = Duration.ofSeconds(30)
            pingPeriod = Duration.ofSeconds(3)
        }
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(ServerBootstrap::class.java, "/web/")
        }
        install(DefaultHeaders)
        install(Compression) {
            gzip {
                priority = 1.0
            }
            deflate {
                priority = 10.0
                minimumSize(1024) // condition
            }
        }
        routing {
            static("/assets") {
                resources("/web/assets")
            }


            route("/api") {
                webSocket("/ws") {
                    try {
                        while (true) {
                            val message = (incoming.receive() as Frame.Text).readText()
                            println("Ws-Message: $message")
                            scope.launch {
                                val idPacket = mapper.readValue(message, IdRestItem::class.java)

                                when (idPacket.id) {
                                    PacketType.DOWNLOADREQUEST.id -> {
                                        DownloadBackend.startDowload(mapper.readValue(message, RequestDownloadJson::class.java), this@webSocket)
                                    }
                                }
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        println("Ws-Closed")
                    }
                }
            }

            get("/") {
                val model = mapOf("title" to "Title",
                        "defaultprofilename" to defaultProfileName,
                        "profilesDropdown" to profilesHtml
                )
                call.respond(FreeMarkerContent("downloadpage.ftl", model))
            }
            get("/download") {
                val id = call.request.queryParameters["id"] ?: ""
                val file = DownloadBackend.getFile(id)
                if (file == null) {
                    call.respond(HttpStatusCode(404, "Not Found"), "Download not Found")
                } else {
                    call.respondFile(file)
                }
            }
            get("/download/info") {
                val id = call.request.queryParameters["id"] ?: ""
                val file = DownloadBackend.getFile(id)
                if (file == null) {
                    call.respond(HttpStatusCode(404, "Not Found"), "No Info Found")
                } else {

                    call.respond(DownloadInfoJson(
                            file.name,
                            DownloadBackend.getMimeType(file)
                    ).toString())
                }
            }

        }

    }

    override fun run() {
        webserver.start(true)

    }

    fun stop() {
        webserver.stop(20, 30, TimeUnit.SECONDS)
    }


}

val profilesHtml = profilesToDropdown()
fun profilesToDropdown(): String {
    val stringBuilder = StringBuilder()
    profiles.forEach {
        stringBuilder.append("<a class=\"dropdown-item\" onclick=\"onDownloadRequest('" + it.key + "')\">" + it.key + "</a>")
    }
    return stringBuilder.toString()
}


class RandomString @JvmOverloads constructor(length: Int = 21, random: Random = SecureRandom(), symbols: String = alphanum) {

    private val random: Random

    private val symbols: CharArray

    private val buf: CharArray

    /**
     * Generate a random string.
     */
    fun nextString(): String {
        for (idx in buf.indices)
            buf[idx] = symbols[random.nextInt(symbols.size)]
        return String(buf)
    }

    init {
        if (length < 1) throw IllegalArgumentException()
        if (symbols.length < 2) throw IllegalArgumentException()
        this.random = Objects.requireNonNull(random)
        this.symbols = symbols.toCharArray()
        this.buf = CharArray(length)
    }

    companion object {

        const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        const val lower = "abcdefghijklmnopqrstuvwxyz"

        const val digits = "0123456789"

        const val alphanum = upper + lower + digits
    }

}
