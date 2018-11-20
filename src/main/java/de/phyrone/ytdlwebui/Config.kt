package de.phyrone.ytdlwebui

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml.toToml
import io.ktor.network.util.ioCoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

private val cfgFile = File("Config.toml")
private fun loadConfig(): Config {
    var ret = Config() {
        addSpec(Sel)
        addSpec(NetworkSel)
        addSpec(AuthSel)
        addSpec(DatabaseSel)
    }
    if (cfgFile.exists()) {
        ret = ret.from.toml.file(cfgFile)
    }
    GlobalScope.launch(ioCoroutineDispatcher) {
        ret.toToml.toFile(cfgFile)
    }
    return ret
}

val config = loadConfig()

object Sel : ConfigSpec() {
    val sessionName by optional("YTDL-Session", "SessionName")
    val youtubeDLPath by optional("youtube-dl", "YoutubeDL.ExecPath")
}

object NetworkSel : ConfigSpec("Network") {
    val port by optional(8080, "Port")
}

object AuthSel : ConfigSpec("AuthSel") {
    val enabled by optional(false, "Enabled")
}

object DatabaseSel : ConfigSpec("Database") {
    val type by optional(DatabaseType.H2, "Type")
    val url by optional("", "Url")
    val host by optional("localhost", "Host")
    val port by optional(3306, "Port")
    val databaseName by optional("YoutubeDLWebUI", "DatabaseName")
    val dbPath by optional("", "Path")
    val authEnabled by optional(false, "Auth.Enabled")
    val username by optional("", "Auth.Username")
    val password by optional("", "Auth.Password")

}

