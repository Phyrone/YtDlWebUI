package de.phyrone.ytdlwebui

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml.toToml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

private val cfgFile = File("Config.toml")
private fun loadConfig(): Config {
    var ret = Config {
        addSpec(Sel)
        addSpec(YTDLSel)
        addSpec(NetworkSel)
    }
    ret = ret.from.env()
    if (cfgFile.exists()) {
        ret = ret.from.toml.file(cfgFile)
    }
    GlobalScope.launch(Dispatchers.IO) {
        ret.toToml.toFile(cfgFile)
    }
    ret = ret.from.systemProperties()
    return ret
}

val config = loadConfig()

object Sel : ConfigSpec() {
    val sessionName by optional("YTDL-Session", "SessionName")
    val youtubeDLPath by optional("youtube-dl", "YoutubeDL.ExecPath")
}

object YTDLSel : ConfigSpec("YoutubeDL") {
    val defaultArgs by optional(arrayOf("no-color", "no-playlist", "no-continue", "restrict-filenames", "embed-subs"), "OPTIONS")
    val defaultProfileName by optional("Audio", "DefaultProfileName")
    val defaultProfileArgs by optional(arrayOf("extract-audio", "audio-format mp3", "add-metadata", "embed-thumbnail"), "DefaultProfileArgs")
    val profiles by optional(hashMapOf(
            "MaxQuality" to arrayOf("format bestvideo+bestaudio/best"),
            "1080p" to arrayOf("format bestvideo[height<=1080]+bestaudio/best[height<=1080]/best"),
            "720p" to arrayOf("format bestvideo[height<=720]+bestaudio/best[height<=720]/best"),
            "480p" to arrayOf("format bestvideo[height<=480]+bestaudio/best[height<=480]/best"),
            "MP4" to arrayOf("format bestvideo+bestaudio/best", "recode-video mp4", "embed-thumbnail")
    ), "Profiles")
}

object NetworkSel : ConfigSpec("Network") {
    val port by optional(8080, "Port")
}


